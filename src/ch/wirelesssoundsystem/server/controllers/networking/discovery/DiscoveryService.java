package ch.wirelesssoundsystem.server.controllers.networking.discovery;

import ch.wirelesssoundsystem.server.controllers.networking.Utility;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 04.12.2015.
 * Singleton handler for the DiscoveryService.
 */
public class DiscoveryService {
    /**
     * This is the port used for discovery.
     */
    private static final int DISCOVERY_PORT = 6583;

    /**
     * This s the port used for the listening. Clients answer to this port.
     */
    private static final int READING_PORT = DiscoveryService.DISCOVERY_PORT;

    /**
     * This is the reading timeout. After this amount of time (in milliseconds)
     * the reading socket will throw a timeout exception, and the reading will start again (if not cancelled).
     */
    private static final int READING_TIMEOUT = 2000;

    /**
     * This is the reading buffer. In best case it would be the size of the receiving message.
     */
    private static final int READING_BUFFER = 15;

    /**
     * This is the message sent in the discovery protocol.
     */
    private static final String DISCOVERY_MESSAGE = "WSSServer";

    /**
     * This is the message that should be received if a client found the server.
     */
    private static final String SERVER_FOUND_MESSAGE = "WSSClient";

    private static DatagramSocket discoverySocket;
    private static DatagramSocket readingSocket;

    private static DiscoveryService ourInstance = new DiscoveryService();
    private InetAddress localAddress;

    private volatile boolean isListening = false;

    /**
     * Deprecated, Use ScheduledExecutorService instead.
     * http://stackoverflow.com/questions/22378422/how-to-use-timertask-with-lambdas
     */
    private ScheduledExecutorService discoveryScheduledService;
    private Thread responseThread;

    // Waiting delay for the task to run again in seconds.
    private final long delay = 5;

    public static DiscoveryService getInstance() {
        return ourInstance;
    }

    private DiscoveryService() {
    }

    public void start() {

        // Start discovery Service
        if (this.discoveryScheduledService == null || this.discoveryScheduledService.isShutdown() || this.discoveryScheduledService.isTerminated()) {
            this.discoveryScheduledService = Executors.newScheduledThreadPool(1);

            this.discoveryScheduledService.scheduleAtFixedRate(this::discover
                    , 0, this.delay, TimeUnit.SECONDS);
        }

        // Start response service.
        if (this.responseThread == null || !this.responseThread.isAlive()) {
            this.responseThread = new Thread(this::listenToResponse);
            this.isListening = true;
            this.responseThread.start();
        }
    }

    public void stop() {
        if (this.discoveryScheduledService != null) {
            this.discoveryScheduledService.shutdown();
            this.discoveryScheduledService = null;
            System.out.println("Stopped sending...");
        }

        // Todo: Implement Response thread.
        if (this.responseThread != null) {

            if (this.responseThread.isAlive()) {
                // Todo: Implement boolean variable that stops the thread.
                this.isListening = false;
                System.out.println("Stopping listening...");
            }

            this.responseThread = null;
        }
    }

    /**
     * This is the discover method.
     * This method contains the logic of the discovery protocol.
     */
    private void discover() {
        try {
            // Initialize the Socket.
            if (DiscoveryService.discoverySocket == null) {
                DiscoveryService.discoverySocket = new DatagramSocket();
                DiscoveryService.discoverySocket.setBroadcast(true);
                // Set the Traffic Class to LOW_COST
                DiscoveryService.discoverySocket.setTrafficClass(0x02);
            }

            // Get the bytes of the data to send.
            byte[] sendData = DiscoveryService.DISCOVERY_MESSAGE.getBytes();

            // Create datagram packet for UDP.
            DatagramPacket datagram = new DatagramPacket(
                    sendData,
                    sendData.length,
                    InetAddress.getByName(Utility.getBroadcastAddress4().getHostAddress()),
                    DiscoveryService.DISCOVERY_PORT
            );

            DiscoveryService.discoverySocket.send(datagram);
            System.out.println("BEEP (Broadcast an: " + Utility.getBroadcastAddress4().getHostAddress() + ")");
        } catch (Exception e) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, e);
            e.printStackTrace();
        }
    }

    /**
     * This method runs an infinite loop and listens to a response
     * on the specified port.
     */
    private void listenToResponse() {
        System.out.println("Listening for client responses... (Port: " + DiscoveryService.READING_PORT + ")");

        try {
            if (DiscoveryService.readingSocket == null) {
                DiscoveryService.readingSocket = new DatagramSocket(DiscoveryService.READING_PORT);
                DiscoveryService.readingSocket.setSoTimeout(DiscoveryService.READING_TIMEOUT);
            }

            // Listening Loop
            while (this.isListening) {
                byte[] receivingBuffer = new byte[DiscoveryService.READING_BUFFER];
                DatagramPacket receivedPacket = new DatagramPacket(receivingBuffer, receivingBuffer.length);

                try {
                    DiscoveryService.readingSocket.receive(receivedPacket);
                    String message = new String(receivedPacket.getData());
                    message = message.trim(); // Trim stuff, because the buffer was too big.

                    // Only check this address if it does not equal the DISCOVERY_MESSAGE.
                    // If it is the DISCOVERY_MESSAGE, it is probably the broadcast echo.
                    if(!message.equals(DiscoveryService.DISCOVERY_MESSAGE)) {
                        // Handle the message, if it is a SERVER_FOUND_MESSAGE.
                        if (message.equals(DiscoveryService.SERVER_FOUND_MESSAGE)) {
                            this.found(receivedPacket.getAddress());
                        }
                        // Handle unknown messages
                        else {
                            // Log Unknown message.
                            String log = "Received Datagram (IP=" + receivedPacket.getAddress().getHostAddress()
                                    + ").\nContent: " + message;

                            Logger.getLogger(DiscoveryService.class.getName()).log(Level.INFO, log);
                        }
                    }
                }
                // This catch is called if the socket was timed out. It's normal.
                catch(SocketTimeoutException e){
                    // Uncomment this if you want to log the timeout exception.
//                    Logger.getLogger(DiscoveryService.class.getName()).log(Level.INFO, "[WARNING]: Reading Socket timed out. Reinitializing reading...");
                }
            }
        } catch (SocketException e) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, e);
            e.printStackTrace();
        } catch (IOException e) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, e);
            e.printStackTrace();
        }

        System.out.println("Listening stopped!");
    }

    private synchronized void found(InetAddress inetAddress) {
        System.out.println("Found new Client. IP = " + inetAddress.getHostAddress());
    }

    public String getDiscoveryText() {
        return "WSSServer:" + this.localAddress.getHostAddress();
    }

    public InetAddress getLocalAddress() {
        return this.localAddress;
    }

    public void setLocalAddress(InetAddress localAddress) {
        this.localAddress = localAddress;
    }
}
