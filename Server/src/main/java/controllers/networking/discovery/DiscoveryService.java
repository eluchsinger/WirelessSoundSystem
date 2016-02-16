package controllers.networking.discovery;

import controllers.networking.Utility;
import models.clients.Client;
import models.clients.Clients;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 04.12.2015.
 * Singleton handler for the DiscoveryService.
 * This singleton handles the whole DiscoveryProcess of the Clients of the WSS.
 */
public class DiscoveryService {

    //region Members & Constants
    /**
     * This is the port used for discovery. Client receiving port.
     */
    private static final int DISCOVERY_PORT = 6583;

    /**
     * This s the port used for the listening. Clients answer to this port.
     */
    private static final int READING_PORT = 6584;

    /**
     * This is the reading timeout. After this amount of time (in milliseconds)
     * the reading socket will throw a timeout exception, and the reading will start again (if not cancelled).
     */
    private static final int READING_TIMEOUT = 2000;

    /**
     * Waiting delay for the discovery task to run again in seconds.
     */
    private static final long DISCOVERY_TICK = 2;

    /**
     * Client timeout time in milliseconds.
     * The clients will be removed from the Clients list, after they expire.
     * The timeout time for the clients are renewed, every time they are seen on the network.
     */
    private static final long CLIENT_TIMEOUT = DiscoveryService.DISCOVERY_TICK * 2 * 1000;

    /**
     * This is the message sent in the discovery protocol.
     */
    private static final String DISCOVERY_MESSAGE = "WSSServer";

    /**
     * This is the message that should be received if a client foundClient the server.
     */
    private static final String SERVER_FOUND_MESSAGE = "WSSClient";

    /**
     * This is the reading buffer. In best case it would be the size of the receiving message.
     */
    private static final int READING_BUFFER_SIZE = SERVER_FOUND_MESSAGE.length() + 1;


    /**
     * The socket from which the discovery datagrams are sent.
     */
    private static DatagramSocket discoverySocket;

    /**
     * The socket that is reading the incoming datagrams.
     */
    private static DatagramSocket readingSocket;

    /**
     * Instance of the singleton.
     */
    private static DiscoveryService ourInstance = new DiscoveryService();

    /**
     * This boolean controls the finishing of the
     * responseThread. If the thread is started, it is set to true.
     * If the isListening is set to false, the thread will stop (probable delay: DiscoveryService.READING_TIMEOUT)
     */
    private volatile boolean isListening = false;

    /**
     * This is the ScheduledService of the Discovery Logic.
     * It works like a Timer and is multithreaded.
     */
    private ScheduledExecutorService discoveryScheduledService;

    /**
     * This thread listens for responses on the READING_PORT.
     */
    private Thread responseThread;

    /**
     * This task checks the clients for
     */
    private Task<List<Client>> keepAliveTask;

    /**
     * Gets the instance of the DiscoveryService Singleton.
     */
    public static DiscoveryService getInstance() {
        return ourInstance;
    }

    //endregion

    private DiscoveryService() {
    }

    /**
     * Starts the DiscoveryService and all its threads.
     */
    public void start() {

        // Start discovery Service
        if (this.discoveryScheduledService == null || this.discoveryScheduledService.isShutdown() || this.discoveryScheduledService.isTerminated()) {
            this.discoveryScheduledService = Executors.newScheduledThreadPool(1);

            this.discoveryScheduledService.scheduleAtFixedRate(this::discover
                    , 0
                    , DiscoveryService.DISCOVERY_TICK
                    , TimeUnit.SECONDS);
        }

        // Start response service.
        if (this.responseThread == null || !this.responseThread.isAlive()) {
            this.responseThread = new Thread(this::listenToResponse);
            this.isListening = true;
            this.responseThread.start();
        }
    }

    /**
     * Stops the DiscoveryService and all its threads.
     */
    public void stop() {

        // Discovery Schedule (Timer)
        if (this.discoveryScheduledService != null) {
            this.discoveryScheduledService.shutdown();
            this.discoveryScheduledService = null;
            System.out.println("Stopped sending...");
        }

        // Response thread.
        if (this.responseThread != null) {

            if (this.responseThread.isAlive()) {
                this.isListening = false;

                try {
                    this.responseThread.join((int)(DiscoveryService.CLIENT_TIMEOUT * 1.5));
                }
                catch(InterruptedException interrupted){
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Response thread interruption...", interrupted);
                }
            }

            this.responseThread = null;
        }
    }

    /**
     * This is the discover method.
     * This method contains the logic of the discovery protocol.
     * Sending packets
     */
    private void discover() {
        try {
            // Initialize the Socket.
            if (DiscoveryService.discoverySocket == null) {
                DiscoveryService.discoverySocket = new DatagramSocket();
                DiscoveryService.discoverySocket.setBroadcast(true);
                // Set the Traffic Class to LOW_COST (0x02)
                DiscoveryService.discoverySocket.setTrafficClass(0x02);
            }

            // Get the bytes of the data to send.
            byte[] sendData = DiscoveryService.DISCOVERY_MESSAGE.getBytes();

            try {
                InetAddress broadcastAddress = Utility.getBroadcastAddress4();
                // Create datagram packet for UDP.
                DatagramPacket datagram = new DatagramPacket(
                        sendData,
                        sendData.length,
                        InetAddress.getByName(broadcastAddress.getHostAddress()),
                        DiscoveryService.DISCOVERY_PORT
                );

                DiscoveryService.discoverySocket.send(datagram);

                Logger.getLogger(DiscoveryService.class.getName()).log(Level.INFO, "BEEP (Broadcast an: " + broadcastAddress.getHostAddress() + ":"
                        + datagram.getPort() + ")");

                Logger.getLogger(DiscoveryService.class.getName()).log(Level.INFO, "Checking timeouts...");
                KeepAliveTask keepAliveTask = new KeepAliveTask(Clients.getInstance().getClients(), CLIENT_TIMEOUT, LocalDateTime.now());
                keepAliveTask.run();
                List<Client> expiredClients = keepAliveTask.get();

                if(expiredClients.size() > 0) {
                    System.out.println(expiredClients.size() + " expired clients...");
                    this.expiredClient(expiredClients);
                }

            }
            catch(NullPointerException nullPointerException){
                Logger.getLogger(DiscoveryService.class.getName()).log(Level.INFO,
                        "The Server is not connected to a network.");
            }
        } catch (Exception e) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, e);
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
                byte[] receivingBuffer = new byte[DiscoveryService.READING_BUFFER_SIZE];
                DatagramPacket receivedPacket = new DatagramPacket(receivingBuffer, receivingBuffer.length);

                try {
                    // If the message is bigger than the READING_BUFFER_SIZE, it gets truncated (the last part is lost!)
                    DiscoveryService.readingSocket.receive(receivedPacket);
                    String message = new String(receivedPacket.getData());
                    message = message.trim(); // Trim stuff, because the buffer was too big.

                    // Only check this address if it does not equal the DISCOVERY_MESSAGE.
                    // If it is the DISCOVERY_MESSAGE, it is probably the broadcast echo.
                    if (!message.equals(DiscoveryService.DISCOVERY_MESSAGE)) {
                        // Handle the message, if it is a SERVER_FOUND_MESSAGE.
                        if (message.equals(DiscoveryService.SERVER_FOUND_MESSAGE)) {
                            this.foundClient(receivedPacket.getAddress());
                        }
                        // Handle unknown messages
                        else {
                            // Log Unknown message.
                            String log = "Received Datagram (IP = " + receivedPacket.getAddress().getHostAddress()
                                    + ").\nContent: " + message;

                            Logger.getLogger(DiscoveryService.class.getName()).log(Level.INFO, log);
                        }
                    }
                }
                // This catch is called if the socket was timed out. It's normal.
                catch (SocketTimeoutException ignore) {
                    // Uncomment this if you want to log the timeout exception.
                    //  Logger.getLogger(DiscoveryService.class.getName()).log(Level.INFO, "[WARNING]: Reading Socket timed out. Reinitializing reading...");
                }
            }
        } catch (IOException e) {
            Logger.getLogger(DiscoveryService.class.getName()).log(Level.SEVERE, null, e);
        }
        finally {
            this.isListening = false;
            System.out.println("Listening stopped!");
        }
    }

    /**
     * Called, when a client was found.
     * THREADSAFE
     * @param inetAddress The InetAddress of the client foundClient.
     */
    private synchronized void foundClient(InetAddress inetAddress) {
        Logger.getLogger(DiscoveryService.class.getName()).log(Level.INFO,"Found new Client. IP = " + inetAddress.getHostAddress());

        Client tmp = new Client(inetAddress, "FoundClient");

        // This method runs the lambda on the JavaFX thread.
        Platform.runLater(() -> {
            Clients.getInstance().seenClient(tmp);
        });
    }

    /**
     * Called when a client is expired. Handles what happens next.
     * (Currently: Removes them from the Clients list).
     * THREADSAFE.
     * @param clients expired clients.
     */
    private synchronized void expiredClient(List<Client> clients){
        // Dont call runLater, if the list is empty.
        if(clients.size() > 0) {
            Platform.runLater(() -> Clients.getInstance().getClients().removeAll(clients));
        }
    }
}