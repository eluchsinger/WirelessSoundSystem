package controllers.networking.streaming.music;

import controllers.networking.streaming.music.callback.OnMusicStreamingStatusChanged;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 01.03.2016.
 */
public class TCPMusicStreamingService implements MusicStreamingService {

    //region Thread Requirements
    private static final String LISTENING_THREAD_NAME = "TCPListeningThread";
    private Thread listeningThread;
    private volatile boolean running;
    //endregion

    /**
     * Socket timeout in milliseconds.
     */
    private static final int SOCKET_TIMEOUT = 1000;
    private final static int STREAM_READING_PORT = 6049;
    private final InetAddress serverAddress;

    private Socket socket;

    @Override
    public void start() {

        this.listeningThread = new Thread(LISTENING_THREAD_NAME);
        this.listeningThread.setDaemon(true);

        this.listeningThread.start();
        this.running = true;
    }

    @Override
    public void stop() {
        try {
            this.running = false;
            this.listeningThread.join(SOCKET_TIMEOUT + 1000);
        } catch (InterruptedException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
                    "Error joining TCP Listening Thread", e);
        }
    }

    @Override
    public void addServiceStatusChangedListener(OnMusicStreamingStatusChanged listener) {

    }

    @Override
    public void removeServiceStatusChangedListener(OnMusicStreamingStatusChanged listener) {

    }

    public TCPMusicStreamingService(String serverAddress) throws IOException {
        this.serverAddress = InetAddress.getByName(serverAddress);

    }
}