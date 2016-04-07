package controllers.networking.streaming.music.tcp;

import controllers.networking.streaming.music.tcp.callbacks.OnClientConnected;
import models.clients.Server;
import models.networking.clients.NetworkClient;
import models.networking.clients.SocketNetworkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.concurrent.ExecutorServiceUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Esteban Luchsinger on 26.03.2016.
 * The TCP Server uses a TCP Server Socket and listens for incoming connections.
 * This class is just here to accept incoming connections for clients.
 */
public class TCPSocketServer implements Closeable {
    private final Logger logger;

    /**
     * The maximum incoming connections waiting for acceptance.
     * (Java Default: 50)
     */
    private final static int MAXIMUM_BACKLOG = 50;

    /**
     * The server is dirty after starting for the first time.
     * The server cannot be restarted after closing.
     */
    private boolean dirtyServer;
    private volatile boolean isRunning;
    private final ExecutorService acceptingService;

    private final ServerSocket serverSocket;
    private final List<OnClientConnected> onClientConnectedListeners;

    /**
     * Default constructor.
     * Opens a server.
     * Start the listening with the #start() method.
     * @throws IOException Throws IOException if there was something wrong.
     */
    public TCPSocketServer() throws IOException {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.onClientConnectedListeners = new ArrayList<>();
        this.acceptingService = Executors.newSingleThreadExecutor();
        this.serverSocket = new ServerSocket(Server.STREAMING_PORT, MAXIMUM_BACKLOG);
    }

    /**
     * Starts the TCP Server.
     * Beginns accepting connections.
     */
    public void start() {

        if(!this.isRunning) {
            if (this.dirtyServer) {
                throw new RuntimeException("The server was already started and cannot be restarted");
            } else {
                // Everything ok. Start the service.
                this.dirtyServer = true;
                this.isRunning = true;
                this.acceptingService.submit(this::acceptConnections);
            }
        }
    }

    /**
     * Stops the TCP Server.
     * This method does the same as the close() method. It is just here to keep consistence between
     * start() and stop().
     * The server cannot be restarted after stop or close.
     * @throws IOException Throws an IO Exception if there are any problems.
     */
    public void stop() throws IOException {
        this.close();
    }

    /**
     * Multi-threaded method. Accepts connections, that's it.
     */
    private void acceptConnections() {
        while(this.isRunning) {
            try {
                Socket socket = this.serverSocket.accept();
                NetworkClient client = new SocketNetworkClient(socket);
                this.onClientConnected(client);
            } catch(SocketException socketException) {
                this.logger.info("Server Socket closed", socketException);
            } catch (IOException e) {
                this.logger.error("Error accepting connections", e);
            }
        }
    }

    /**
     * Call this method when a client connected.
     * @param client connected client.
     */
    private void onClientConnected(NetworkClient client) {
        synchronized (this.onClientConnectedListeners) {
            this.logger.info("Network client connected: " + client);
            this.onClientConnectedListeners.forEach(listener -> listener.onClientConnected(client));
        }
    }

    /**
     * Adds a listener for the onClientConnected event.
     * @param listener Listener to add.
     */
    public void addOnClientConnectedListener(OnClientConnected listener) {
        this.onClientConnectedListeners.add(listener);
    }

    /**
     * Removes the listener from the list of listeners.
     * @param listener listener to remove.
     * @return Return true, if the listener could be removed.
     */
    public boolean removeOnClientConnectedListener(OnClientConnected listener) {
        return this.onClientConnectedListeners.remove(listener);
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     * <p>
     * <p> As noted in {@link AutoCloseable#close()}, cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * <em>mark</em> the {@code Closeable} as closed, prior to throwing
     * the {@code IOException}.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        this.isRunning = false;
        ExecutorServiceUtils.stopExecutorService(this.acceptingService);

        if(this.serverSocket != null && !this.serverSocket.isClosed())
            this.serverSocket.close();
    }
}
