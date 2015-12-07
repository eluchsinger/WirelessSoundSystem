package ch.wirelesssoundsystem.server.controllers.networking.discovery;

import ch.wirelesssoundsystem.server.controllers.networking.Utility;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    /**
     * Deprecated, Use ScheduledExecutorService instead.
     * http://stackoverflow.com/questions/22378422/how-to-use-timertask-with-lambdas
     */
    private ScheduledExecutorService discoveryScheduledService;

    // Waiting delay for the task to run again in seconds.
    private final long delay = 5;

    public static DiscoveryService getInstance() {
        return ourInstance;
    }

    private DiscoveryService() {
    }

    public void start() {
        if (this.discoveryScheduledService == null || this.discoveryScheduledService.isShutdown() || this.discoveryScheduledService.isTerminated()) {
            this.discoveryScheduledService = Executors.newScheduledThreadPool(1);

            this.discoveryScheduledService.scheduleAtFixedRate(() -> {
                this.discover();
            }, 0, this.delay, TimeUnit.SECONDS);
        }
    }

    public void stop() {
        this.discoveryScheduledService.shutdown();

        this.discoveryScheduledService = null;
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
            System.out.println("BEEP (Broadcast: " + Utility.getBroadcastAddress4().getHostAddress() + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
