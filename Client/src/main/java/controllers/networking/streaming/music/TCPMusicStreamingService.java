package controllers.networking.streaming.music;

import controllers.io.cache.file.FileCacheService;
import controllers.io.cache.file.StaticFileCacheService;
import controllers.networking.streaming.music.callback.OnMusicStreamingStatusChanged;
import models.clients.Server;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 01.03.2016.
 * The TCP streaming uses Base-64 encoding.
 */
public class TCPMusicStreamingService implements MusicStreamingService {

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

    private Socket socket;

    /**
     * File cache. The songs have to be cached here when they
     * were received completely.
     */
    private FileCacheService cache;

    /**
     * Default constructor
     */
    public TCPMusicStreamingService() throws IOException {
        this.statusChangedListeners = new ArrayList<>();
        this.cache = new StaticFileCacheService();
        this.setCurrentServiceStatus(ServiceStatus.STOPPED);
    }

    /**
     * Starts the TCP Music Streaming Service
     */
    @Override
    public void start() {

        try {
            this.socket = this.initSocket(this.currentServer.getServerAddress(),
                    this.currentServer.getServerListeningPort());
            this.initThread();
            this.setCurrentServiceStatus(ServiceStatus.WAITING);
        } catch (IOException exception) {
            this.running = false;
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Starting Streaming Service", exception);
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
        }
    }

    /**
     * Listening method.
     * (Multithreaded!)
     */
    private void listen() {

        while (running && !this.getSocket().isClosed() && this.getSocket().isConnected()) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                byte[] buf = new byte[SOCKET_BUFFER_SIZE];
                while (true) {
                    int n = this.getSocket().getInputStream().read(buf);
                    if (n < 0) break;
                    baos.write(buf, 0, n);

                    if(!this.getCurrentServiceStatus().equals(ServiceStatus.RECEIVING))
                        this.setCurrentServiceStatus(ServiceStatus.RECEIVING);
                }

                // Check for fragmentation with 0- bytes between buffer write();
                this.cache.writeData(baos.toByteArray());
                baos.reset();

                this.setCurrentServiceStatus(ServiceStatus.READY);
                // Finished stream.
                // Reconnect
//                this.setCurrentServiceStatus(ServiceStatus.WAITING);
                this.getSocket().close();
                this.initSocket(this.currentServer.getServerAddress(),
                        this.currentServer.getServerListeningPort());
            } catch (SocketTimeoutException ignore) {
            } catch (IOException e) {
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

    /**
     * Returns the cache (FileCache) of the MusicStreamingService.
     *
     * @return FileCacheService.
     */
    @Override
    public FileCacheService getCache() {
        return this.cache;
    }

    private void onServiceStatusChanged() {
        for (OnMusicStreamingStatusChanged listener : this.statusChangedListeners) {
            listener.statusChanged(this.getCurrentServiceStatus());
        }
    }
}