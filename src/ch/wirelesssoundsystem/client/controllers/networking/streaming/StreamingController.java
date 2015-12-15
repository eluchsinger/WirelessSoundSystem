package ch.wirelesssoundsystem.client.controllers.networking.streaming;

import ch.wirelesssoundsystem.client.controllers.io.SynchronizedTempFileHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 15.12.2015.
 */
public class StreamingController {
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

    private static StreamingController ourInstance = new StreamingController();

    private volatile boolean isListening = false;
    private Thread readingThread;
    private SynchronizedTempFileHandler tempFileHandler;

    /**
     * The socket that is reading the incoming datagrams containing the music..
     */
    private MulticastSocket readingSocket;


    public static StreamingController getInstance() {
        return ourInstance;
    }

    private StreamingController() {
    }

    public void start() {
        if(this.readingSocket == null){
            try {
                this.readingSocket = new MulticastSocket(StreamingController.STREAM_READING_PORT);
                this.readingSocket.setSoTimeout(StreamingController.READING_TIMEOUT);
                this.readingSocket.joinGroup(InetAddress.getByName(StreamingController.MULTICAST_GROUP_ADDRESS));


                if(this.tempFileHandler == null){
                    this.tempFileHandler = new SynchronizedTempFileHandler();
                    this.tempFileHandler.open();
                }

                if(!this.isListening && (this.readingThread == null || !this.readingThread.isAlive())){
                    this.readingThread = new Thread(this::listen);
                    this.isListening = true;
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
        this.isListening = false;
        if(this.readingThread != null){
            try {
                this.readingThread.join((int)(StreamingController.READING_TIMEOUT * 1.5));
            } catch (InterruptedException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
                        "Reading thread interrupted!",
                        e);
            }
            this.readingThread = null;
        }

        try {
            if(this.tempFileHandler != null) {
                this.tempFileHandler.close();
                this.tempFileHandler = null;
            }
        } catch (IOException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "TempFileHandler error", e);
        }

        System.out.println("Streaming Controller stopped...");
    }

    private void listen(){

        try {
            while (this.isListening) {
                byte[] buffer = new byte[StreamingController.READING_BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                try {
                    readingSocket.receive(packet);

                    dataReceived(packet.getData());
                } catch (SocketTimeoutException ignore) {

                }
            }
        }
        catch(IOException e){

        }
    }

    private void dataReceived(byte[] data) {
        try {
            this.tempFileHandler.writeData(data);
            System.out.println("Received DataPacket size: " + data.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
