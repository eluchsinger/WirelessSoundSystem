package controllers.networking.streaming.music.tcp;

import controllers.io.cache.file.FileCacheService;
import controllers.io.cache.file.StaticFileCacheService;
import controllers.networking.streaming.music.MusicStreamingService;
import controllers.networking.streaming.music.ServiceStatus;
import controllers.networking.streaming.music.callback.*;
import models.clients.Server;
import models.networking.dtos.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * Created by Esteban Luchsinger on 01.03.2016.
 * The TCP Socket Implementation. Uses conventional sockets (Blocking mode).
 * </pre>
 */
public class TCPMusicStreamingController implements MusicStreamingService {

    //region Constants

    /**
     * Socket timeout in milliseconds.
     */
    private static final int SOCKET_TIMEOUT = 1000;

    //endregion Constants

    //region Thread Requirements
    /**
     * Name of the listening thread.
     */
    private static final String LISTENING_THREAD_NAME = "TCPListeningThread";

    /**
     * The thread listening for new connections.
     */
    private Thread listeningThread;
    /**
     * Running state of the thread.
     * Is false if the thread should be cancelled.
     */
    private volatile boolean isRunning;

    //endregion

    private final Logger logger;

    /**
     * Current status of the service.
     */
    private ServiceStatus currentServiceStatus;

    /**
     * The current server informations.
     * This object may change if a new server is found.
     */
    private Server currentServer;

    /**
     * The current socket bound to the server.
     */
    private Socket socket;

    /**
     * The current ObjectInputStream to the server.
     */
    private ObjectInputStream objectInputStream;

    /**
     * The current ObjectOutputStream to the server.
     */
    private ObjectOutputStream objectOutputStream;

    /**
     * File cache. The songs have to be cached here when they
     * were received completely.
     */
    private final FileCacheService cache;

    //region Listeners
    /**
     * List of listeners listening for currentServiceStatus changes.
     */
    private final List<OnMusicStreamingStatusChanged> statusChangedListeners;

    /**
     * List of listeners listening for playCommands received.
     */
    private final List<OnPlay> playCommandListeners;

    private final List<OnPause> pauseCommandListeners;

    /**
     * List of listeners listening for stopCommands received.
     */
    private final List<OnStop> stopCommandListeners;

    private final ArrayList<OnRename> renameCommandListeners;

    //endregion listeners

    /**
     * Default constructor
     */
    public TCPMusicStreamingController() throws IOException {
        this.logger = LoggerFactory.getLogger(this.getClass());

        // Initialize Listeners
        this.statusChangedListeners = new ArrayList<>();
        this.playCommandListeners = new ArrayList<>();
        this.pauseCommandListeners = new ArrayList<>();
        this.stopCommandListeners = new ArrayList<>();
        this.renameCommandListeners = new ArrayList<>();

        this.cache = new StaticFileCacheService();
        this.setCurrentServiceStatus(ServiceStatus.STOPPED);
    }

    /**
     * Starts the TCP Music Streaming Service
     */
    @Override
    public void start() {

        try {
            this.setSocket(this.initSocket(this.currentServer.getServerAddress(),
                    this.currentServer.getServerListeningPort()));
            this.initThread();
            this.setCurrentServiceStatus(ServiceStatus.WAITING);
        } catch (IOException exception) {
            this.isRunning = false;
            this.logger.error("Failed starting Music Streaming Service.", exception);

        }
    }

    /**
     * Stops the music streaming service.
     * Is a blocking call and waits for the separate threads to finish
     * (Timeout: SOCKET_TIMEOUT + 1000 ; in milliseconds)
     */
    @Override
    public void stop() {
        try {
            this.isRunning = false;
            if (this.listeningThread != null && !this.listeningThread.isAlive())
                this.listeningThread.join(SOCKET_TIMEOUT + 1000);
        } catch (InterruptedException e) {
            this.logger.error("Error joining TCP Listening Thread", e);
        } finally {
            if (this.listeningThread == null || !this.listeningThread.isAlive())
                this.setCurrentServiceStatus(ServiceStatus.STOPPED);

            // Close the socket.
            if(this.getSocket() != null) {
                try {
                    if(!this.getSocket().isClosed())
                        this.getSocket().close();
                } catch (IOException e) {
                    this.logger.error("Error closing the open socket", e);
                }
            }
        }
    }

    /**
     * Listening method.
     * (Multithreaded!)
     */
    private void listen() {

        while (this.isRunning && !this.getSocket().isClosed()) {

            try {
                Object receivedObject;
                receivedObject = this.objectInputStream.readObject();

                // If it's a cache song command.
                if(receivedObject instanceof CacheSongCommand) {
                    this.logger.info("Received CacheSongCommand");
                    CacheSongCommand command = (CacheSongCommand) receivedObject;
                    this.cache.writeData(command.data);
                    this.setCurrentServiceStatus(ServiceStatus.READY);
                }
                // A play command
                else if(receivedObject instanceof PlayCommand) {
                    this.logger.info("Received PlayCommand");
                    PlayCommand command = (PlayCommand) receivedObject;
                    this.onPlayCommandReceived(command.title, command.artist);
                }
                else if(receivedObject instanceof PauseCommand) {
                    this.logger.info("Received PauseCommand");
                    PauseCommand command = (PauseCommand) receivedObject;
                    this.onPauseCommandReceived();
                }
                // A stop command
                else if(receivedObject instanceof StopCommand) {
                    this.logger.info("Received StopCommand");
                    this.onStopCommandReceived();
                }
                // A rename command
                else if(receivedObject instanceof RenameCommand) {
                    this.logger.info("Received RenameCommand");

                    RenameCommand command = (RenameCommand) receivedObject;
                    this.onRenameCommandReceived(command.getName());
                }

            } catch (SocketTimeoutException ignore) {
            } catch(SocketException socketException) {
                try {
                    // Only try to reconnect, if the service is still up & running.
                    if(this.isRunning) {
                        // Try to reconnect
                        this.setSocket(this.initSocket(this.currentServer.getServerAddress(),
                                this.currentServer.getServerListeningPort()));
                    }
                }
                catch(Exception e) {
                    this.logger.error("Error in the TCPStreaming listener!", e);
                }
            }
            catch (IOException | ClassNotFoundException e) {
                this.logger.error("Error in the TCPStreaming listener!", e);
            }
        }
    }


    @Override
    public void setServer(Server server) {
        if (this.currentServer != server) {
            this.currentServer = server;
        }
    }

    /**
     * Synchronized getSocked method.
     *
     * @return Current socket.
     */
    private synchronized Socket getSocket() {
        return this.socket;
    }

    /**
     * Sets the current socket and the objectinputstream.
     * @param socket
     * @throws IOException
     */
    private synchronized void setSocket(Socket socket) throws IOException {

        // Change only if the socket really changed.
        // (By Reference)
        if(this.socket != socket) {
            // If the socket is not null, close it first!
            if(this.socket != null && !this.socket.isClosed()) {
                if(this.objectOutputStream != null) {
                    this.objectOutputStream.flush();
                    this.objectOutputStream.close();
                    this.objectOutputStream = null;
                }

                if(this.objectInputStream != null) {
                    this.objectInputStream.close();
                    this.objectInputStream = null;
                }
                this.socket.close();
            }

            this.socket = socket;

            if (this.socket != null) {
                // Handle ObjectOutputStream (do this before InputStream)
                if(!this.socket.isOutputShutdown()) {
                    this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

                    // Need to flush the OOS before opening the OIS. (By both sides)
                    // http://stackoverflow.com/a/7586021/2632991
                    this.objectOutputStream.flush();
                }

                // Handle ObjectInputStream
                if (!this.socket.isInputShutdown()) {
                    this.objectInputStream =
                            new ObjectInputStream(socket.getInputStream());
                }
            }
        }
    }

    /**
     * Initializes a thread.
     * If the thread is running, it is stopped and initialized again.
     */
    private void initThread() {

        if (this.listeningThread != null && this.listeningThread.isAlive()) {
            try {
                this.listeningThread.join(SOCKET_TIMEOUT + 1000);
            } catch (InterruptedException e) {
                if (this.listeningThread.isAlive())
                    this.listeningThread.interrupt();
            }
        }

        this.listeningThread = new Thread(this::listen, LISTENING_THREAD_NAME);
        this.listeningThread.setDaemon(true);
        this.isRunning = true;
        this.listeningThread.start();
    }

    /**
     * Initializes the socket.
     *
     * @param address Address
     * @param port    Port
     * @return Returns a new socket.
     * @throws IOException
     */
    private Socket initSocket(InetAddress address, int port) throws IOException {
        try {
            Socket socket = new Socket(address, Server.STREAMING_PORT);
            socket.setSoTimeout(SOCKET_TIMEOUT);
            socket.setTrafficClass(0x04);
            return socket;
        } catch (IOException exception) {
            throw new IOException("Error initializing socket with address "
                    + address.getHostAddress()
                    + " to port " + port, exception);
        }
    }

    public ServiceStatus getCurrentServiceStatus() {
        return currentServiceStatus;
    }

    private synchronized void setCurrentServiceStatus(ServiceStatus currentServiceStatus) {
        if (this.currentServiceStatus == null || !this.currentServiceStatus.equals(currentServiceStatus)) {
            this.currentServiceStatus = currentServiceStatus;

            this.onServiceStatusChanged();
        }
    }

    @Override
    public void addServiceStatusChangedListener(OnMusicStreamingStatusChanged listener) { this.statusChangedListeners.add(listener); }

    @Override
    public void removeServiceStatusChangedListener(OnMusicStreamingStatusChanged listener) { this.statusChangedListeners.remove(listener); }

    @Override
    public void addOnPlayListener(OnPlay listener) { this.playCommandListeners.add(listener); }

    @Override
    public void removeOnPlayListener(OnPlay listener) { this.playCommandListeners.remove(listener); }

    @Override
    public void addOnPauseListener(OnPause listener) { this.pauseCommandListeners.add(listener); }

    @Override
    public void removeOnPauseListener(OnPause listener) { this.pauseCommandListeners.remove(listener); }

    @Override
    public void addOnStopListener(OnStop listener) { this.stopCommandListeners.add(listener); }

    @Override
    public void removeOnStopListener(OnStop listener) { this.stopCommandListeners.remove(listener); }

    @Override
    public void addOnRenameListener(OnRename listener) { this.renameCommandListeners.add(listener);}

    @Override
    public void removeOnRenameListener(OnRename listener) { this.renameCommandListeners.remove(listener); }

    /**
     * Returns the cache (FileCache) of the MusicStreamingService.
     *
     * @return FileCacheService.
     */
    @Override
    public FileCacheService getCache() {
        return this.cache;
    }

    /**
     * Tells the server the name of this client.
     * @param name
     */
    public void sendName(String name) {
        try {
            this.objectOutputStream.writeObject(new RenameCommand(name));
        } catch(IOException ioException) {
            this.logger.error("Failed sending the current name to the server", ioException);
        }
    }

    //region Event Launchers

    /**
     * Call this method when the current ServiceStatus changes.
     * Fires the corresponding event to all the listeners.
     */
    private void onServiceStatusChanged() {
        for (OnMusicStreamingStatusChanged listener : this.statusChangedListeners) {
            listener.statusChanged(this.getCurrentServiceStatus());
        }
    }

    /**
     *
     * Fires the corresponding event to all the listeners.
     * @param songTitle The title of the received song. (May be null)
     * @param artist The artist of the received song. (May be null)
     */
    private void onPlayCommandReceived(String songTitle, String artist) {
        this.playCommandListeners.forEach(onPlay -> onPlay.play(songTitle, artist));
    }

    private void onPauseCommandReceived() {
        this.pauseCommandListeners.forEach(OnPause::pause);
    }

    /**
     *
     * Fires the corresponding event to all the listeners.
     */
    private void onStopCommandReceived() {
        this.stopCommandListeners.forEach(OnStop::stop);
    }

    /**
     * Fires the corresponding event to all the listeners.
     */
    private void onRenameCommandReceived(String name) { this.renameCommandListeners.forEach(l -> l.rename(name));}

    //endregion Event Launchers
}