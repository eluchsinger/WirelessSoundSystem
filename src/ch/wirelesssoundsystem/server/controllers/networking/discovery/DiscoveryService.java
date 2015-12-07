package ch.wirelesssoundsystem.server.controllers.networking.discovery;

import ch.wirelesssoundsystem.server.controllers.networking.Utility;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
    private static final int DISCOVERY_PORT = 6583;
    private static final String DISCOVERY_MESSAGE = "WSSServer";
    private static DatagramSocket discoverySocket;

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
        if(this.responseThread == null || !this.responseThread.isAlive()) {
            this.responseThread = new Thread(this::listenToResponse);
            this.responseThread.start();
        }
    }

    public void stop() {
        if(this.discoveryScheduledService != null){
            this.discoveryScheduledService.shutdown();
            this.discoveryScheduledService = null;
        }

        // Todo: Implement Response thread.
        if(this.responseThread != null){

            if(this.responseThread.isAlive()){
                // Todo: Implement boolean variable that stops the thread.
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
                DiscoveryService.discoverySocket = new DatagramSocket(DiscoveryService.DISCOVERY_PORT);
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
    private void listenToResponse(){
        System.out.println("Listening... (psst!)");

        while(this.isListening){

        }

        System.out.println("Stopping listening!");

    }

    private void found(InetAddress inetAddress) {

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
