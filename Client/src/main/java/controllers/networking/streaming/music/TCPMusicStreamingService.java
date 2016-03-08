package controllers.networking.streaming.music;

import controllers.io.CacheHandler;
import controllers.networking.streaming.music.callback.OnMusicStreamingStatusChanged;
import models.clients.Server;
import models.networking.messages.StreamingMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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
    private Server currentServer;

    private Socket socket;

    @Override
    public void start() {

        try {
            this.socket = this.initSocket(this.currentServer.getServerAddress(),
                    this.currentServer.getServerListeningPort());
            this.initThread();
        }
        catch(IOException exception) {
            this.running = false;
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Starting Streaming Service", exception);
        }
    }

    /**
     * Listening method.
     */
    private void listen() {

        while (running && this.getSocket().isConnected()) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                byte[] buf = new byte[4096];
                while (true) {
                    int n = this.getSocket().getInputStream().read(buf);
                    if (n < 0) break;
                    baos.write(buf, 0, n);
                }

                CacheHandler.getInstance().writeData(baos.toByteArray());

                // Reconnect
                this.socket = new Socket(this.currentServer.getServerAddress(),
                        this.currentServer.getServerListeningPort());
            }
            catch(SocketTimeoutException ignore){

            }
            catch(IOException ignore) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "OMG!", ignore);
            }
        }
    }

    /**
     * Call this method, when the streaming finished (the StreamMessage FINAL was received).
     * Example: "<stream>DATA</stream>"
     * Concurrency: Not thread-safe!
     * @param data Data received.
     */
    private void streamFinished(byte[] data){
    }

    @Override
    public void stop() {
        try {
            this.running = false;
            if(this.listeningThread != null && !this.listeningThread.isAlive())
                this.listeningThread.join(SOCKET_TIMEOUT + 1000);
        } catch (InterruptedException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
                    "Error joining TCP Listening Thread", e);
        }
    }

    @Override
    public void setServer(Server server)
    {
        if(this.currentServer != server) {
            this.currentServer = server;
        }
    }

    /**
     * Synchronized getSocked method.
     * @return Current socket.
     */
    private synchronized Socket getSocket() {
        return this.socket;
    }

    /**
     * Initializes a thread.
     * If the thread is running, it is stopped and initialized again.
     */
    private void initThread(){

        if(this.listeningThread != null && this.listeningThread.isAlive()){
            try {
                this.listeningThread.join(SOCKET_TIMEOUT + 1000);
            } catch (InterruptedException e) {
                if(this.listeningThread.isAlive())
                    this.listeningThread.interrupt();
            }
        }

        this.listeningThread = new Thread(this::listen, LISTENING_THREAD_NAME);
        this.listeningThread.setDaemon(true);
        this.running = true;
        this.listeningThread.start();
    }

    /**
     * Initializes the socket.
     * @param address Address
     * @param port Port
     * @return Returns a new socket.
     * @throws IOException
     */
    private Socket initSocket(InetAddress address, int port) throws IOException {
        try {
            Socket socket = new Socket(address, Server.STREAMING_PORT);
            socket.setSoTimeout(SOCKET_TIMEOUT);
            return socket;
        }
        catch (IOException exception) {
            throw new IOException("Error initializing socket with address "
                    + address.getHostAddress()
                    + " to port " + port, exception);
        }
    }

    @Override
    public void addServiceStatusChangedListener(OnMusicStreamingStatusChanged listener) {

    }

    @Override
    public void removeServiceStatusChangedListener(OnMusicStreamingStatusChanged listener) {

    }
}