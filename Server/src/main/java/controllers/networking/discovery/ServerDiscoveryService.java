package controllers.networking.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.networking.NetUtil;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Esteban Luchsinger on 04.12.2015.
 * Discovery Service Handler.
 * Sends out beacons in an interval.
 */
public class ServerDiscoveryService implements Closeable {

    //region Constants
    /**
     * This is the port used for discovery. Client receiving port.
     */
    private static final int DISCOVERY_PORT = 6583;

    /**
     * Waiting delay for the discovery task to run again in milliseconds.
     */
    private static final long DISCOVERY_TICK = 2 * 1000;

    /**
     * This is the message sent in the discovery protocol.
     */
    private static final String DISCOVERY_MESSAGE = "WSSServer";

    //endregion Constants

    //region Members
    private final Logger logger;

    /**
     * The socket from which the discovery datagrams are sent.
     */
    private DatagramSocket discoverySocket;

    /**
     * This is the ScheduledService of the Discovery Logic.
     * It works like a Timer and is multithreaded.
     */
    private ScheduledExecutorService discoveryScheduledService;

    //endregion Members

    //region Constructor

    /**
     * Default Constructor
     */
    public ServerDiscoveryService() {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }
    //endregion Constructor

    /**
     * Starts the ServerDiscoveryService and all its threads.
     */
    public void start() {

        // Start discovery Service
        if (this.discoveryScheduledService == null || this.discoveryScheduledService.isShutdown() || this.discoveryScheduledService.isTerminated()) {
            this.discoveryScheduledService = Executors.newScheduledThreadPool(1);

            this.discoveryScheduledService.scheduleAtFixedRate(this::discover
                    , 0
                    , ServerDiscoveryService.DISCOVERY_TICK
                    , TimeUnit.MILLISECONDS);
        }

    }

    /**
     * Stops the ServerDiscoveryService and all its threads.
     */
    public void stop() {

        // Discovery Schedule (Timer)
        if (this.discoveryScheduledService != null) {
            this.discoveryScheduledService.shutdown();
            this.discoveryScheduledService = null;
            this.logger.info("Stopped DiscoveryService.");
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
            byte[] sendData = ServerDiscoveryService.DISCOVERY_MESSAGE.getBytes();

            try {
                InetAddress broadcastAddress = NetUtil.getBroadcastAddress4();
                // Create datagram packet for UDP.
                DatagramPacket datagram = new DatagramPacket(
                        sendData,
                        sendData.length,
                        InetAddress.getByName(broadcastAddress.getHostAddress()),
                        ServerDiscoveryService.DISCOVERY_PORT
                );

                this.discoverySocket.send(datagram);
            }
            catch(NullPointerException nullPointerException){
                this.logger.info("The Server is not connected to a network.");
            }
        } catch (Exception e) {
            this.logger.info("Error in discovery service", e);
        }
    }

    /**
     * Redundant with stop().
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