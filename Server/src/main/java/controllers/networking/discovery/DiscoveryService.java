package controllers.networking.discovery;

import controllers.networking.Utility;
import controllers.networking.discovery.callbacks.OnClientExpired;
import controllers.networking.discovery.callbacks.OnClientFound;
import models.clients.Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
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

    //region Constants
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

    //endregion Constants

    //region Members
    /**
     * The socket from which the discovery datagrams are sent.
     */
    private DatagramSocket discoverySocket;

    /**
     * The socket that is reading the incoming datagrams.
     */
    private DatagramSocket readingSocket;

    /**
     * This boolean controls the finishing of the
     * responseThread. If the thread is started, it is set to true.
     * If the isListening is set to false, the thread will stopPlaying (probable delay: DiscoveryService.READING_TIMEOUT)
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
     * List of listeners listening for client found events.
     */
    private List<OnClientFound> clientFoundListeners;

    /**
     * List of listeners listening for client found events.
     */
    private List<OnClientExpired> clientExpiredListeners;

    /**
     * Internal list used to keep track of connected clients.
     */
    private Set<Client> clientsConnected;
    //endregion Members

    //region Constructor

    /**
     * Default Constructor
     */
    public DiscoveryService() {
        this.clientsConnected = new TreeSet<>();
        this.clientFoundListeners = new ArrayList<>();
        this.clientExpiredListeners = new ArrayList<>();
    }
    //endregion Constructor

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
            if (this.discoverySocket == null) {
                this.discoverySocket = new DatagramSocket();
                this.discoverySocket.setBroadcast(true);
                // Set the Traffic Class to LOW_COST (0x02)
                this.discoverySocket.setTrafficClass(0x02);
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

                this.discoverySocket.send(datagram);

                Logger.getLogger(this.getClass().getName())
                        .log(Level.INFO, "BEEP (Broadcast an: " + broadcastAddress.getHostAddress() + ":"
                        + datagram.getPort() + ")");

                Logger.getLogger(this.getClass().getName())
                        .log(Level.INFO, "Checking timeouts...");


                this.checkExpiredClients();
            }
            catch(NullPointerException nullPointerException){
                Logger.getLogger(this.getClass().getName())
                        .log(Level.INFO, "The Server is not connected to a network.");
            }
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * This method runs an infinite loop and listens to a response
     * on the specified port.
     */
    private void listenToResponse() {
        System.out.println("Listening for client responses... (Port: " + DiscoveryService.READING_PORT + ")");

        try {
            if (this.readingSocket == null) {
                this.readingSocket = new DatagramSocket(DiscoveryService.READING_PORT);
                this.readingSocket.setSoTimeout(DiscoveryService.READING_TIMEOUT);
            }

            // Listening Loop
            while (this.isListening) {
                byte[] receivingBuffer = new byte[DiscoveryService.READING_BUFFER_SIZE];
                DatagramPacket receivedPacket = new DatagramPacket(receivingBuffer, receivingBuffer.length);

                try {
                    // If the message is bigger than the READING_BUFFER_SIZE, it gets truncated (the last part is lost!)
                    this.readingSocket.receive(receivedPacket);
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

                            Logger.getLogger(this.getClass().getName())
                                    .log(Level.INFO, log);
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
            Logger.getLogger(this.getClass().getName())
                    .log(Level.SEVERE, null, e);
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
        Logger.getLogger(this.getClass().getName())
                .log(Level.INFO,"Found new Client. IP = " + inetAddress.getHostAddress());

        Client client = new Client(inetAddress, "FoundClient");

        // Try to add (if it is not already there).
        if(this.clientsConnected.add(client)) {
            for (OnClientFound listener : this.clientFoundListeners) {
                listener.onFoundClient(client);
            }
        }
    }

    /**
     * Called when a client is expired. Handles what happens next.
     * (Currently: Removes them from the Clients list).
     * THREADSAFE.
     * @param client expired client.
     */
    private synchronized void expiredClient(Client client){
        // Dont call runLater, if the list is empty.
        if(this.clientsConnected.remove(client)){
            for(OnClientExpired listener : this.clientExpiredListeners){
                listener.onClientExpired(client);
            }
        }
    }

    /**
     * Checks for expired clients.
     */
    private synchronized void checkExpiredClients(){
        LocalDateTime currentDateTime = LocalDateTime.now();
        Iterator<Client> iterator = this.clientsConnected.iterator();

        while(iterator.hasNext()) {
            Client client = iterator.next();
            long difference = client.getLastSeen().until(currentDateTime, ChronoUnit.MILLIS);

            if(difference > CLIENT_TIMEOUT){
                this.expiredClient(client);
            }
        }
    }

    //region Listener Handling

    public void addClientFoundListener(OnClientFound listener) {
        this.clientFoundListeners.add(listener);
    }

    public void removeClientFoundListener(OnClientFound listener) {
        this.clientFoundListeners.remove(listener);
    }

    public void addClientExpiredListener(OnClientExpired listener) {
        this.clientExpiredListeners.add(listener);
    }

    public void removeClientExpiredListener(OnClientExpired listener) {
        this.clientExpiredListeners.remove(listener);
    }

    //endregion Listener Handling
}