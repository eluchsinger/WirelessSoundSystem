package controllers.networking.discovery;

import controllers.networking.discovery.callback.OnServerConnected;
import controllers.networking.discovery.callback.OnServerDisconnected;
import models.clients.Server;
import utils.concurrent.ExecutorServiceUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 08.12.2015.
 * This is the ClientDiscoveryService of the Client.
 */
public class ClientDiscoveryService implements Closeable {

    private DatagramSocket scanningSocket;
    private DatagramSocket responseSocket;

    private List<OnServerConnected> onServerConnectedList;
    private List<OnServerDisconnected> onServerDisconnectedList;

    /**
     * Currently connected server.
     */
    private Server currentServer;

    /**
     * This is the port used for discovery.
     * Discovery packets will be incoming on this port
     */
    private static final int SCANNING_PORT = 6583;

    /**
     * This is the message sent in the discovery protocol.
     */
    private static final String DISCOVERY_MESSAGE = "WSSServer";

    /**
     * This is the reading buffer. In best case it would be the size of the receiving message.
     */
    private static final int SCANNING_BUFFER_SIZE = DISCOVERY_MESSAGE.length() + 1;

    /**
     * TIMEOUT (in milliseconds) for the reading socket.
     */
    private static final int SCANNING_TIMEOUT = 2000;

    /**
     * The scanning executor is doing the discovery scans asynchronically.
     */
    private final ExecutorService scanningExecutor;
    private final Logger logger;
    private volatile boolean isWorking = false;

    public ClientDiscoveryService() {
        this.logger = Logger.getLogger(this.getClass().getName());
        this.onServerConnectedList = new ArrayList<>();
        this.onServerDisconnectedList = new ArrayList<>();
        this.scanningExecutor = Executors.newSingleThreadExecutor();
        this.currentServer = null;
    }

    /**
     * Starts the service.
     */
    public void start() {

        if(!this.isWorking) {
            this.isWorking = true;
            this.scanningExecutor.submit(this::scan);
        }
    }

    /**
     * Stops the discovery service.
     * If stop is called when the service is already stopped, does nothing.
     * Redundant call with stop().
     * The DiscoveryService MUST be stopped to free blocked resources!
     */
    public void stop() {

        this.isWorking = false;

        if(this.scanningSocket != null) {
            this.scanningSocket.close();
            this.scanningSocket = null;
        }

        if(this.responseSocket != null) {
            this.responseSocket.close();
            this.responseSocket = null;
        }

        // Thread should be stopped anyways because close() throws a SocketException in the running thread.
        ExecutorServiceUtils.stopExecutorService(this.scanningExecutor);

        this.logger.log(Level.INFO, "Stopped Discovery service.");
    }

    /**
     * Scan method. Looks for incoming messages from Servers.
     */
    private void scan() {
        System.out.println("Discovering servers... (Port: " + ClientDiscoveryService.SCANNING_PORT + ")");

        try {
            // Init Scanning Socket
            if (this.scanningSocket == null) {
                this.scanningSocket = new DatagramSocket(ClientDiscoveryService.SCANNING_PORT);
//                this.scanningSocket.setSoTimeout(ClientDiscoveryService.SCANNING_TIMEOUT);
            }

            // Init response socket
            if (this.responseSocket == null) {
                this.responseSocket = new DatagramSocket();
            }

            while (isWorking) {
                byte[] receivingBuffer = new byte[ClientDiscoveryService.SCANNING_BUFFER_SIZE];
                DatagramPacket receivedPacket = new DatagramPacket(receivingBuffer, receivingBuffer.length);

                try {
                    // If the message is bigger than the READING_BUFFER_SIZE, it gets truncated (the last part is lost!)
                    this.scanningSocket.receive(receivedPacket);
                    String message = new String(receivedPacket.getData());
                    message = message.trim(); // Trim stuff, because the buffer was too big.

                    // Handle the message, if it is a SERVER_FOUND_MESSAGE.
                    if (message.equals(ClientDiscoveryService.DISCOVERY_MESSAGE)) {
                        if(!this.isWorking)
                            break;
                        this.foundServer(receivedPacket.getAddress(), Server.DISCOVERY_PORT);
                    }
                    // Handle unknown messages
                    else {
                        // Log Unknown message.
                        String log = "Received Datagram (IP = " + receivedPacket.getAddress().getHostAddress()
                                + ").\nContent: " + message;

                        this.logger.log(Level.INFO, log);
                    }
                }
                // This catch is called if the socket was timed out. It's normal.
                catch (SocketTimeoutException e) {
                    // Uncomment this if you want to log the timeout exception.
                    //Logger.getLogger(ClientDiscoveryService.class.getName()).log(Level.INFO, "[WARNING]: Reading Socket timed out. Reinitializing reading...");
                }
            }
        } catch(SocketException socketException) {
            if(socketException.getMessage().equals("socket closed")) {
                this.logger.log(Level.INFO, "Socket closed in DiscoveryService");
            }
            else {
                this.logger.log(Level.SEVERE, "Scanning error in DiscoveryService", socketException);
            }
        }
        catch (Exception e) {
            this.logger.log(Level.SEVERE, "Scanning error in DiscoveryService", e);
        } finally {
            this.isWorking = false;
            this.scanningSocket.close();
            this.responseSocket.close();
            this.scanningSocket = null;
            this.responseSocket = null;
            this.logger.log(Level.INFO, "Discovery Listening stopped!");
        }
    }

    /**
     * Call this method when a server is found.
     * @param inetAddress InetAddress of the server.
     * @param serverListeningPort Listening port of the server.
     */
    private void foundServer(InetAddress inetAddress, int serverListeningPort) {
        Server foundServer = new Server(inetAddress, serverListeningPort);

        // Add to current server, if needed.
        if(this.currentServer == null){
            this.currentServer = foundServer;
            this.onServerConnected(foundServer);
        }
        else if(!this.currentServer.equals(foundServer)){
            // New Server found.
            this.onServerDisconnected(this.currentServer);
            this.currentServer = foundServer;
            this.onServerConnected(foundServer);
        }
    }

    /**
     * Adds an OnServerConnectedListener.
     * If added multiple times, it will fire multiple events.
     * @param listener new listener.
     */
    public void addOnServerConnectedListener(OnServerConnected listener) {
        this.onServerConnectedList.add(listener);
    }

    /**
     * Removes the listener for the OnServerConnected listener. If the listener
     * was added multiple times, it removes only ONCE.
     * @param listener listener to remove.
     */
    public void removeOnServerConnectedListener(OnServerConnected listener) { this.onServerConnectedList.remove(listener); }

    /**
     * Adds an OnServerDisconnectedListener.
     * If addeed multiple times, it will fire multiple events.
     * @param listener
     */
    public void addOnServerDisconnectedListener(OnServerDisconnected listener) {
        this.onServerDisconnectedList.add(listener);
    }

    /**
     * Removes the OnServerDisconnectedListener. If the listener
     * was added multiple times, it removes only ONCE.
     * @param listener
     */
    public void removeOnServerDisconnectedListener(OnServerDisconnected listener) {
        this.onServerDisconnectedList.remove(listener);
    }

    /**
     * Call this to fire an onServerConnected event.
     * @param server Connected server.
     */
    private void onServerConnected(Server server){
        for(OnServerConnected listener : this.onServerConnectedList) {
            listener.serverConnected(server);
        }
    }

    /**
     * Call this to fire an onServerDisconnected Event.
     * @param server Disconnected Server.
     */
    private void onServerDisconnected(Server server) {
        for(OnServerDisconnected listener : this.onServerDisconnectedList) {
            listener.serverDisconnected(server);
        }
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
        this.stop();
    }
}