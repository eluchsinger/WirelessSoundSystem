package ch.wirelesssoundsystem.client.controllers.networking.discovery;

import ch.wirelesssoundsystem.shared.models.clients.Server;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 08.12.2015.
 * This is the DiscoveryService of the Client.
 */
public class DiscoveryService {
    private static DiscoveryService ourInstance = new DiscoveryService();

    private static final int SCANNING_TIMEOUT = 2000;
    private static DatagramSocket scanningSocket;
    private static DatagramSocket responseSocket;

    /**
     * This is the port used for discovery.
     * Discovery packets will be incoming on this port
     */
    private static final int SCANNING_PORT = 6583;

    /**
     * This is the port, on which the server is listening.
     * Send ACKS to this port!
     */
    private static final int SERVER_PORT = 6584;

    /**
     * This is the message sent in the discovery protocol.
     */
    private static final String DISCOVERY_MESSAGE = "WSSServer";

    /**
     * This is the message that should be sent if a client found the server.
     */
    private static final String SERVER_FOUND_MESSAGE = "WSSClient";

    /**
     * This is the reading buffer. In best case it would be the size of the receiving message.
     */
    private static final int SCANNING_BUFFER_SIZE = DISCOVERY_MESSAGE.length() + 1;

    private Thread scanningThread;
    private volatile boolean isWorking = false;

    public static DiscoveryService getInstance() {
        return ourInstance;
    }

    private DiscoveryService() {
    }

    public void start() {
        // Start response service.
        if (this.scanningThread == null || !this.scanningThread.isAlive()) {

            try {

                // Init Scanning Socket
                if (DiscoveryService.scanningSocket == null) {
                    DiscoveryService.scanningSocket = new DatagramSocket(DiscoveryService.SCANNING_PORT);
                    DiscoveryService.scanningSocket.setSoTimeout(DiscoveryService.SCANNING_TIMEOUT);
                }

                // Init response socket
                if (DiscoveryService.responseSocket == null) {
                    DiscoveryService.responseSocket = new DatagramSocket();
                }

                this.scanningThread = new Thread(this::scan);
                this.isWorking = true;

                this.scanningThread.start();
            } catch (SocketException e) {
                Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, e);

                try {
                    this.stop();
                } catch (Exception ignored) {
                }
            }

        }
    }

    public void stop() {

        // Response thread.
        if (this.scanningThread != null) {

            if (this.scanningThread.isAlive()) {
                this.isWorking = false;
                System.out.println("Stopping listening...");
            }

            this.scanningSocket = null;
            this.responseSocket = null;
            this.scanningThread = null;
        }
    }

    private void scan() {
        System.out.println("Listening for servers... (Port: " + DiscoveryService.SCANNING_PORT + ")");

        try {

            while (isWorking) {
                byte[] receivingBuffer = new byte[DiscoveryService.SCANNING_BUFFER_SIZE];
                DatagramPacket receivedPacket = new DatagramPacket(receivingBuffer, receivingBuffer.length);

                try {
                    // If the message is bigger than the READING_BUFFER_SIZE, it gets truncated (the last part is lost!)
                    DiscoveryService.scanningSocket.receive(receivedPacket);
                    String message = new String(receivedPacket.getData());
                    message = message.trim(); // Trim stuff, because the buffer was too big.

                    // Handle the message, if it is a SERVER_FOUND_MESSAGE.
                    if (message.equals(DiscoveryService.DISCOVERY_MESSAGE)) {
                        this.foundServer(receivedPacket.getAddress(), DiscoveryService.SERVER_PORT);
                    }
                    // Handle unknown messages
                    else {
                        // Log Unknown message.
                        String log = "Received Datagram (IP = " + receivedPacket.getAddress().getHostAddress()
                                + ").\nContent: " + message;

                        Logger.getLogger(DiscoveryService.class.getName()).log(Level.INFO, log);
                    }
                }
                // This catch is called if the socket was timed out. It's normal.
                catch (SocketTimeoutException e) {
                    // Uncomment this if you want to log the timeout exception.
                    //Logger.getLogger(DiscoveryService.class.getName()).log(Level.INFO, "[WARNING]: Reading Socket timed out. Reinitializing reading...");
                }
            }
        } catch (Exception e) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            this.isWorking = false;
            DiscoveryService.scanningSocket = null;
            DiscoveryService.responseSocket = null;
            System.out.println("Listening stopped!");
        }
    }

    private void foundServer(InetAddress inetAddress, int serverListeningPort) {
        Server foundServer = new Server(inetAddress, serverListeningPort);

        System.out.println("Server found: " + foundServer + "...");

        try {
            byte[] sendingData = SERVER_FOUND_MESSAGE.getBytes();
            DatagramPacket packet = new DatagramPacket(sendingData, sendingData.length,
                    inetAddress, serverListeningPort);
            DiscoveryService.responseSocket.send(packet);
        } catch (IOException e) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
