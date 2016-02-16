package controllers.networking.streaming;

import controllers.io.CacheHandler;
import models.networking.SongCache;
import models.networking.SongDatagram;
import models.networking.messages.StreamingMessage;
import utils.networking.SongDatagramBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.security.AccessController;
import java.security.PrivilegedAction;
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

    private SERVICE_STATUS currentServiceStatus;
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
        this.currentServiceStatus = SERVICE_STATUS.STOPPED;
    }

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
                    }

                    return null;
                }));

                // Start thread
                if(this.currentServiceStatus == SERVICE_STATUS.STOPPED && (this.readingThread == null || !this.readingThread.isAlive())){
                    this.readingThread = new Thread(this::listen);
                    this.currentServiceStatus = SERVICE_STATUS.RUNNING;
                    this.readingThread.start();
                }

                System.out.println("Streaming Controller started...");
            } catch (IOException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, null, e);
            }
        }
    }

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
        this.currentServiceStatus = SERVICE_STATUS.STOPPED;
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

        this.currentServiceStatus = SERVICE_STATUS.STOPPED;

        System.out.println("Streaming Controller stopped...");
    }

    private void listen(){
        try {
            while (this.currentServiceStatus != SERVICE_STATUS.STOPPED) {
                try{
                    DatagramPacket packet = null;
                    byte[] buffer;
                    switch(this.currentServiceStatus){
                        case RUNNING:
                            int bufferSize = StreamingMessage.initializationMessage(Integer.MAX_VALUE).getBytes().length;
                            buffer = new byte[bufferSize];
                            packet = new DatagramPacket(buffer, buffer.length);
                            readingSocket.receive(packet);

                            this.validateInitializationPacket(packet);
                            break;
                        case RECEIVING:

                            buffer = new byte[SongDatagram.MAX_TOTAL_SIZE];
                            packet = new DatagramPacket(buffer, buffer.length);
                            readingSocket.receive(packet);

                            SongDatagram songDatagram = SongDatagramBuilder.convertToSongDatagram(packet);
                            this.currentCache.add(songDatagram);
                            this.dataReceived(songDatagram.getSongData());
                            break;
                    }
                } catch (SocketTimeoutException ignore) { }
            }
        }
        catch(IOException e){

        }
    }

    private void dataReceived(byte[] data) {
        try {
            CacheHandler.getInstance().writeData(data);
            System.out.println("Missing packets: " + this.currentCache.getMissingSequenceNumbers().size());
            //System.out.println("Received DataPacket size: " + data.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SongDatagram createSongDatagram(DatagramPacket originalPacket){
        SongDatagram songDatagram = null;
        songDatagram = SongDatagramBuilder.convertToSongDatagram(originalPacket);
        return songDatagram;
    }

    private void validateInitializationPacket(DatagramPacket packet){
        String message = new String(packet.getData());
        message = message.trim();

        if(message.startsWith(StreamingMessage.STREAMING_INITIALIZATION_MESSAGE)){
            String secondString = message.substring(StreamingMessage.STREAMING_INITIALIZATION_MESSAGE.length());
            int amountOfPackets = Integer.valueOf(secondString);

            this.currentCache = new SongCache(amountOfPackets);
            this.currentServiceStatus = SERVICE_STATUS.RECEIVING;
        }
    }

    private enum SERVICE_STATUS {
        /**
         * The Streaming Service is running and waiting for a stream initialization.
         */
        RUNNING,
        /**
         * The Streaming Service is receiving an initialized stream.
         */
        RECEIVING,
        /**
         * The Streaming Service is not running.
         */
        STOPPED
    }
}