package controllers.networking.streaming.music;

import controllers.networking.streaming.music.callback.OnMusicStreamingStatusChanged;
import models.clients.Server;
import models.networking.messages.StreamingMessage;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 01.03.2016.
 * The TCP streaming uses Base-64 encoding.
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
    private Server currentServer;

    private Socket socket;

    @Override
    public void start() {

        try {
            this.listeningThread = new Thread(this::listen, LISTENING_THREAD_NAME);
            this.listeningThread.setDaemon(true);
            this.running = true;

            this.socket = new Socket(this.currentServer.getServerAddress(), this.currentServer.getServerListeningPort());
            this.listeningThread.start();
        }
        catch(IOException exception) {
            this.running = false;
        }
    }

    /**
     * Listening method.
     */
    private void listen() {
        StringBuilder stringBuilder = new StringBuilder();
        InputStreamReader inputStreamReader = null;

        while (running && this.getSocket().isConnected()) {
            try {
                // Only reinit, if it's null.
                if(inputStreamReader == null)
                    new InputStreamReader(this.getSocket().getInputStream(), "US-ASCII");


                assert inputStreamReader != null;
                int character = inputStreamReader.read();

                // If there was something in the stream --> Do reading.
                if(character > -1) {
                    while (character > -1) {

                        stringBuilder.append((char) character);
                        character = inputStreamReader.read();
                        if(stringBuilder.toString().endsWith(StreamingMessage.STREAMING_FINALIZATION_MESSAGE)){
                            this.streamFinished(stringBuilder.toString());
                        }
                    }
                }
                // If the reading failed --> Sleep.
                else { Thread.sleep(200); }

            } catch (IOException | InterruptedException ignore) { }
        }
    }

    /**
     * Call this method, when the streaming finished (the StreamMessage FINAL was received).
     * Example: "start:<DATA>finish:"
     * Concurrency: Not thread-safe!
     * @param data Data received.
     */
    private void streamFinished(String data){

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
    public void setServer(Server server) {
        this.currentServer = server;
    }

    /**
     * Synchronized getSocked method.
     * @return Current socket.
     */
    private synchronized Socket getSocket(){
        return this.socket;
    }

    @Override
    public void addServiceStatusChangedListener(OnMusicStreamingStatusChanged listener) {

    }

    @Override
    public void removeServiceStatusChangedListener(OnMusicStreamingStatusChanged listener) {

    }
}