package controllers.networking.streaming.music.tcp;

import controllers.io.cache.file.FileCacheService;
import controllers.io.cache.file.StaticFileCacheService;
import controllers.networking.streaming.music.MusicStreamingService;
import controllers.networking.streaming.music.ServiceStatus;
import controllers.networking.streaming.music.callback.OnMusicStreamingStatusChanged;
import controllers.networking.streaming.music.callback.OnPlay;
import controllers.networking.streaming.music.callback.OnStop;
import models.clients.Server;
import models.networking.dtos.PlayCommand;
import models.networking.dtos.StopCommand;
import utils.exceptions.NotImplementedException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 01.03.2016.
 * The old TCP Music Streaming Controller using the old Socket implementation (as opposed to NIO).
 */
public class TCPMusicStreamingController implements MusicStreamingService {

    //region Constants

    /**
     * Socket timeout in milliseconds.
     */
    private static final int SOCKET_TIMEOUT = 1000;

    /**
     * Size of the receiving buffer of the socket, before
     * the data has to be cached.
     */
    private static final int SOCKET_BUFFER_SIZE = 4096;
    //endregion Constants

    //region Thread Requirements
    private static final String LISTENING_THREAD_NAME = "TCPListeningThread";
    private Thread listeningThread;
    /**
     * Running state of the thread.
     * Is false if the thread should be cancelled.
     */
    private volatile boolean running;
    //endregion
    private Server currentServer;

    private ServiceStatus currentServiceStatus;
    private List<OnMusicStreamingStatusChanged> statusChangedListeners;
    private List<OnPlay> playCommandListeners;
    private List<OnStop> stopCommandListeners;

    private Socket socket;
    private ObjectInputStream currentOIS;

    /**
     * File cache. The songs have to be cached here when they
     * were received completely.
     */
    private final FileCacheService cache;

    /**
     * Default constructor
     */
    public TCPMusicStreamingController() throws IOException {
        this.initializeListeners();
        this.cache = new StaticFileCacheService();
        this.setCurrentServiceStatus(ServiceStatus.STOPPED);
    }

    private void initializeListeners() {
        this.statusChangedListeners = new ArrayList<>();
        this.playCommandListeners = new ArrayList<>();
        this.stopCommandListeners = new ArrayList<>();
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
            this.running = false;
            Logger.getLogger(this.getClass().getName())
                    .log(Level.SEVERE, "Starting Streaming Service", exception);
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
            this.running = false;
            if (this.listeningThread != null && !this.listeningThread.isAlive())
                this.listeningThread.join(SOCKET_TIMEOUT + 1000);
        } catch (InterruptedException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
                    "Error joining TCP Listening Thread", e);
        } finally {
            if (this.listeningThread == null || !this.listeningThread.isAlive())
                this.setCurrentServiceStatus(ServiceStatus.STOPPED);

            // Close the socket.
            if(this.getSocket() != null) {
                try {
                    if(!this.getSocket().isClosed())
                        this.getSocket().close();
                } catch (IOException e) {
                    Logger.getLogger(this.getClass().getName())
                            .log(Level.SEVERE,
                                    "Error closing the open socket", e);
                }
            }
        }
    }

    /**
     * Listening method.
     * (Multithreaded!)
     */
    private void listen() {

        while (running && !this.getSocket().isClosed()) {

            try {
                Object receivedObject;
                receivedObject = this.currentOIS.readObject();

                // If it's a play command.
                if(receivedObject instanceof PlayCommand) {
                    PlayCommand command = (PlayCommand) receivedObject;
                    this.cache.writeData(command.data);
                    this.setCurrentServiceStatus(ServiceStatus.READY);
                    this.onPlayCommandReceived(command.songTitle, command.artist);
                }
                else if(receivedObject instanceof StopCommand) {
                    System.out.println("Received StopCommand");
                    this.onStopCommandReceived();
                }

            } catch (SocketTimeoutException ignore) {
            } catch(SocketException socketException) {
                try {
                    // Try to reconnect
                    this.setSocket(this.initSocket(this.currentServer.getServerAddress(),
                            this.currentServer.getServerListeningPort()));
                }
                catch(Exception e) {
                    Logger.getLogger(this.getClass().getName())
                            .log(Level.SEVERE, "Error in the TCPStreaming listener!", e);
                }
            }
            catch (IOException | ClassNotFoundException e) {
                Logger.getLogger(this.getClass().getName())
                        .log(Level.SEVERE, "Error in the TCPStreaming listener!", e);
            }
        }
    }

    /**
     * Call this method, when the streaming finished (the StreamMessage FINAL was received).
     * Example: "<stream>DATA</stream>"
     * Concurrency: Not thread-safe!
     *
     * @param data Data received.
     */
    private void streamFinished(byte[] data) {
        throw new NotImplementedException();
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
                this.socket.close();
            }

            this.socket = socket;

            if (this.socket != null) {
                if (!this.socket.isInputShutdown()) {
                    this.currentOIS =
                            new ObjectInputStream(socket.getInputStream());
                } else {
                    if (this.currentOIS != null) {
                        this.currentOIS.close();
                        this.currentOIS = null;
                    }
                }
            } else {
                if (this.currentOIS != null) {
                    this.currentOIS.close();
                    this.currentOIS = null;
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
        this.running = true;
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
    public void addServiceStatusChangedListener(OnMusicStreamingStatusChanged listener) {
        this.statusChangedListeners.add(listener);
    }

    @Override
    public void removeServiceStatusChangedListener(OnMusicStreamingStatusChanged listener) {
        this.statusChangedListeners.remove(listener);
    }

    @Override
    public void addOnPlayListener(OnPlay listener) {
        this.playCommandListeners.add(listener);
    }

    @Override
    public void removeOnPlayListener(OnPlay listener) {
        this.playCommandListeners.remove(listener);
    }

    @Override
    public void addOnStopListener(OnStop listener) {
        this.stopCommandListeners.add(listener);
    }

    @Override
    public void removeOnStopListener(OnStop listener) {
        this.stopCommandListeners.remove(listener);
    }

    /**
     * Returns the cache (FileCache) of the MusicStreamingService.
     *
     * @return FileCacheService.
     */
    @Override
    public FileCacheService getCache() {
        return this.cache;
    }

    //region Event Launchers
    private void onServiceStatusChanged() {
        for (OnMusicStreamingStatusChanged listener : this.statusChangedListeners) {
            listener.statusChanged(this.getCurrentServiceStatus());
        }
    }

    private void onPlayCommandReceived(String songTitle, String artist) {
        this.playCommandListeners.forEach(onPlay -> onPlay.play(songTitle, artist));
    }

    private void onStopCommandReceived() {
        this.stopCommandListeners.forEach(OnStop::stop);
    }
    //endregion Event Launchers
}