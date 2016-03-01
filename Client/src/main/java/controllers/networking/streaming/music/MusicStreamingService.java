package controllers.networking.streaming.music;

import controllers.io.CacheHandler;
import controllers.statistics.NetworkStatisticsController;
import models.networking.SongCache;
import models.networking.SongDatagram;
import models.networking.messages.StreamingMessage;
import utils.networking.SongDatagramBuilder;

import java.io.IOException;
import java.net.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 15.12.2015.
 */
public class MusicStreamingService {
    private final static String MULTICAST_GROUP_ADDRESS = "239.255.42.100";
    private final static int STREAM_READING_PORT = 6049;

    /**
     * TIMEOUT (in milliseconds) for the reading socket.
     */
    private static final int READING_TIMEOUT = 2000;

    /**
     * Reading buffer size in bytes.
     */
    private static final int READING_BUFFER_SIZE = 700;

    private static MusicStreamingService ourInstance = new MusicStreamingService();

    private ServiceStatus currentServiceStatus;
    private List<OnMusicStreamingStatusChanged> statusChangedListeners;
    private Thread readingThread;

    /**
     * The socket that is reading the incoming datagrams containing the music..
     */
    private MulticastSocket readingSocket;

    private SongCache currentCache;

    public static MusicStreamingService getInstance() {
        return ourInstance;
    }

    private MusicStreamingService() {
        this.statusChangedListeners = new ArrayList<>();
        this.setCurrentServiceStatus(ServiceStatus.STOPPED);
    }

    /**
     * Starts the streaming service.
     * If the service is already started, it has no effect.
     */
    public void start() {
        if(this.readingSocket == null){
            try {
                this.readingSocket = new MulticastSocket(MusicStreamingService.STREAM_READING_PORT);
                this.readingSocket.setSoTimeout(MusicStreamingService.READING_TIMEOUT);

                AccessController.doPrivileged((PrivilegedAction<Object>) (() -> {
                    try {
                        this.readingSocket.joinGroup(InetAddress.getByName(MusicStreamingService.MULTICAST_GROUP_ADDRESS));
                    } catch (IOException e) {
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
                                "Could not join Multicast group.", e);

                        // If it's a socket-exception class --> Not connected to wifi!
                        if(e.getClass() == SocketException.class){
                            // Todo: Handle not connected to wifi.
                            System.out.println("CHECK INTERNET CONNECTION!!");
                        }
                    }

                    return null;
                }));

                // Start thread
                if(this.currentServiceStatus == ServiceStatus.STOPPED && (this.readingThread == null || !this.readingThread.isAlive())){
                    this.readingThread = new Thread(this::listen);
                    this.setCurrentServiceStatus(ServiceStatus.RUNNING);
                    this.readingThread.start();
                }

                System.out.println("Streaming Controller started...");
            } catch (IOException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, null, e);
            }
        }
    }

    /**
     * Stops the StreamingService.
     * If the service is already stopped, it has no effect.
     */
    public void stop(){

        // Stop reading socket...
        if(this.readingSocket != null) {
            try {
                if (this.readingSocket.isConnected()) {
                    this.readingSocket.close();
                }
            }
            catch(Exception e){
            }
            this.readingSocket = null;
        }

        // Stop reading thread...
        this.setCurrentServiceStatus(ServiceStatus.STOPPED);
        if(this.readingThread != null){
            try {
                this.readingThread.join((int)(MusicStreamingService.READING_TIMEOUT * 1.5));
            } catch (InterruptedException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
                        "Reading thread interrupted!",
                        e);
            }
            this.readingThread = null;
        }

        this.setCurrentServiceStatus(ServiceStatus.STOPPED);

        System.out.println("Streaming Controller stopped...");
    }

    /**
     * This is the listening class. This class works in a separate thread and
     * listens (i.e. waits for packets).
     * When a packet is received, it handles it.
     */
    private void listen(){
        try {
            while (this.currentServiceStatus != ServiceStatus.STOPPED) {
                try{
                    DatagramPacket packet = null;
                    byte[] buffer;
                    switch(this.currentServiceStatus){
                        case RUNNING:
                            int bufferSize = StreamingMessage.initializationMessage(Integer.MAX_VALUE).getBytes().length;
                            buffer = new byte[bufferSize];
                            packet = new DatagramPacket(buffer, buffer.length);
                            readingSocket.receive(packet);

                            if(this.validateInitializationPacket(packet)){
                                NetworkStatisticsController
                                        .getInstance()
                                        .setTotalPacketsExpected(this.currentCache.getExpectedCacheSize());
                                NetworkStatisticsController.getInstance().start();
                            }
                            break;
                        case RECEIVING:

                            buffer = new byte[SongDatagram.MAX_TOTAL_SIZE];
                            packet = new DatagramPacket(buffer, buffer.length);
                            readingSocket.receive(packet);

                        {
                            try {
                                SongDatagram songDatagram = SongDatagramBuilder.convertToSongDatagram(packet);
                                this.currentCache.add(songDatagram);
                                this.dataReceived(songDatagram.getSongData());

                                // *** Statistics ***
                                NetworkStatisticsController.getInstance().addReceivedPacketMulticast();
                                // ******************
                            }
                            catch(OutOfMemoryError outOfMemoryError){

                                String receivedString = new String(packet.getData());
                                receivedString = receivedString.trim();


                                // Check if the string received was a start message. IF yes --> Reinit the listening service!
                                // Main reason for this could be that the server started streaming a new song.
                                if(receivedString.startsWith(StreamingMessage.STREAMING_INITIALIZATION_MESSAGE)){
                                    this.setCurrentServiceStatus(ServiceStatus.RUNNING);
                                    this.validateInitializationPacket(packet);
                                    Logger.getLogger(getClass().getName()).log(Level.INFO, outOfMemoryError.toString());
                                }
                            }
                            break;
                        }
                    }
                } catch (SocketTimeoutException ignore) { }
            }
        }
        catch(IOException e){

        }
    }

    /**
     * Data was received. Handle the data (and file).
     * @param data The data that was received.
     */
    private void dataReceived(byte[] data) {
        try {
            CacheHandler.getInstance().writeData(data);
            System.out.println("Missing packets: " + this.currentCache.getMissingSequenceNumbers().size());
            //System.out.println("Received DataPacket size: " + data.length);
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage());
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage());
        }
    }

    /**
     * Validate init packets.
     * This method handles an initialization packet and allocates a new cache
     * with the corresponding cache size (the cache size is in the init packet).
     * @param packet
     */
    private boolean validateInitializationPacket(DatagramPacket packet){
        String message = new String(packet.getData());
        message = message.trim();

        if(message.startsWith(StreamingMessage.STREAMING_INITIALIZATION_MESSAGE)){
            String secondString = message.substring(StreamingMessage.STREAMING_INITIALIZATION_MESSAGE.length());
            int amountOfPackets = Integer.valueOf(secondString);

            this.currentCache = new SongCache(amountOfPackets);
            this.setCurrentServiceStatus(ServiceStatus.RECEIVING);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Get the current service status.
     * @return Returns the current service status.
     */
    public ServiceStatus getCurrentServiceStatus(){
        return this.currentServiceStatus;
    }

    /**
     * Sets the current service status.
     * @param status new status
     */
    public void setCurrentServiceStatus(ServiceStatus status) {

        boolean fireEvent = false;
        // Check if the currentService status really changed.
        if(this.currentServiceStatus != status)
            fireEvent = true;

        this.currentServiceStatus = status;

        // Fire even if needed.
        if(fireEvent){
            for(OnMusicStreamingStatusChanged listener : this.statusChangedListeners)
                listener.statusChanged(this.getCurrentServiceStatus());
        }

    }

    /**
     * Adds a listener for when the Service Status changes.
     * Multiple addings of the same listener possible.
     * @param listener new listener.
     */
    public void addServiceStatusChangedListener(OnMusicStreamingStatusChanged listener){
        this.statusChangedListeners.add(listener);
    }

    /**
     * Removes a listener for when the Service Status changes.
     * If the listener was added twice, you need to remove it twice.
     * Removing the listener, when it was not added has no effect.
     * @param listener Listener to remove.
     */
    public void removeServiceStatusChangedListener(OnMusicStreamingStatusChanged listener){
        this.statusChangedListeners.remove(listener);
    }

}
