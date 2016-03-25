package controllers.networking.streaming.music.tcp;

import controllers.networking.streaming.music.MusicStreamController;
import models.SocketNetworkClient;
import models.clients.Server;
import models.networking.dtos.PlayCommand;
import models.networking.dtos.StopCommand;
import models.songs.Song;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 07.03.2016.
 */
public class TCPMusicStreamController implements MusicStreamController, Closeable {

    /**
     * The destination port for datagrams sent to the clients.
     */
    private final static int CLIENT_PORT = 6049;

    /**
     * Timeout for the streaming responses in milliseconds.
     * (i.e.: The stream init response)
     */
    private final static int TIMEOUT_FOR_RESPONSES = 1000;

    /**
     * Server TCP Socket.
     */
    private ServerSocket serverSocket;


    private final ExecutorService acceptanceExecutor;
    /**
     * Thread whose only purpose is to accept incoming connections.
     */
    private Thread connectionAcceptingThread;

    /**
     * List with all connections. Should be SynchronizedList.
     * Caution: Even if it's a synchronized list, you NEED to manually
     * synchronize when iterating over it (look at specifications)
     */
    private List<SocketNetworkClient> connections;

    private boolean isStopped;

    /**
     * Starts the service.
     * @throws IOException
     */
    public TCPMusicStreamController() throws IOException {
        this.serverSocket = new ServerSocket(Server.STREAMING_PORT);
        this.connections = Collections.synchronizedList(new ArrayList<>());
        this.acceptanceExecutor = Executors.newSingleThreadExecutor();
//        this.connectionAcceptingThread = new Thread(this::acceptConnections);
//        this.connectionAcceptingThread.setDaemon(true);
//        this.connectionAcceptingThread.start();
        this.isStopped = false;
        this.acceptanceExecutor.submit(this::acceptConnections);
    }

    /**
     * Streams the song.
     * @param song Song to stream.
     */
    @Override
    public void play(Song song) {
        try {
            byte[] data = this.getSongData(song);

            String songTitle = song.getTitle();
            String artist = song.getArtist();

            // Sync connections list for the iteration.
            synchronized (this.connections) {
                for(SocketNetworkClient client: this.connections) {
                    client.getObjectOutputStream()
                            .writeObject(new PlayCommand(songTitle, artist, data));
                }
            }
        }
        catch(IOException iOException){
            Logger.getLogger(this.getClass().getName())
                    .log(Level.WARNING, "Error playing song", iOException);
        }
    }

    /**
     * Sends a Stop command to the connected clients.
     */
    @Override
    public void stopPlaying() {
        try {
            synchronized (this.connections) {
                Iterator<SocketNetworkClient> iterator = this.connections.iterator();
                while(iterator.hasNext()){
                    SocketNetworkClient client = iterator.next();
                    client.getObjectOutputStream().writeObject(new StopCommand());
                }
            }
        }
        catch (IOException iOException) {
            Logger.getLogger(this.getClass().getName())
                    .log(Level.WARNING, "Error stopping song", iOException);
        }
    }

    /**
     * Returns the data of the song.
     * @param song
     * @return Return an array of bytes with the data of the song.
     * @throws IOException Throws IOException when the File could not be read (or found)
     */
    private byte[] getSongData(Song song) throws IOException {

        // Get File Data.
        File songFile = new File(song.getPath());
        return Files.readAllBytes(songFile.toPath());
    }

    /**
     * This method accepts incoming connections.
     * Run on a separate thread, in order to make it non-blocking for the end-user.
     */
    private void acceptConnections() {
        try {
            Socket socket = this.getServerSocket().accept();
            socket.setKeepAlive(true);
            socket.setReuseAddress(true);
            this.connections.add(new SocketNetworkClient(socket));
        } catch (SocketException socketException) {
            if(socketException.getMessage().equals("socket closed")) {
                Logger.getLogger(this.getClass().getName())
                        .log(Level.WARNING, "Socket closed", socketException);
            }
        }
        catch (IOException e) {
            Logger.getLogger(this.getClass().getName())
                    .log(Level.WARNING, "Error accepting connection", e);
        }
    }

    /**
     * Synchronized server socket getter.
     * @return Returns the server socket.
     */
    private synchronized ServerSocket getServerSocket(){
        return this.serverSocket;
    }

    /**
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
        this.isStopped = true;

        if(this.getServerSocket() != null && !this.getServerSocket().isClosed()) {
            this.getServerSocket().close();
        }

        for(SocketNetworkClient socketNetworkClient : this.connections) {
            socketNetworkClient.close();
        }
        System.out.println("Closing the StreamController.");
    }
}