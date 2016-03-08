package controllers.networking.streaming.music;

import models.clients.Server;
import models.songs.Song;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 07.03.2016.
 */
public class TCPMusicStreamController implements MusicStreamController {

    /**
     * The destination port for datagrams sent to the clients.
     */
    private final static int CLIENT_PORT = 6049;

    /**
     * Timeout for the streaming responses in milliseconds.
     * (i.e.: The stream init response)
     */
    private final static int TIMEOUT_FOR_RESPONSES = 1000;

    private ServerSocket serverSocket;

    /**
     * Thread whose only purpose is to accept incoming connections.
     */
    private Thread connectionAcceptingThread;

    /**
     * List with all connections. Should be SynchronizedList.
     * Caution: Even if it's a synchronized list, you NEED to manually
     * synchronize when iterating over it (look at specifications)
     */
    private List<Socket> connections;

    /**
     * Starts the service.
     * @throws IOException
     */
    public TCPMusicStreamController() throws IOException {
        this.serverSocket = new ServerSocket(Server.STREAMING_PORT);
        this.connections = Collections.synchronizedList(new ArrayList<>());
        this.connectionAcceptingThread = new Thread(this::acceptConnections);
        this.connectionAcceptingThread.setDaemon(true);
        this.connectionAcceptingThread.start();
    }

    /**
     * Streams the song.
     * @param song Song to stream.
     */
    @Override
    public void play(Song song) {
        try {
            byte[] data = this.getSongData(song);

            // Sync connections list for the iteration.
            synchronized (this.connections){

                for(Socket socket : this.connections) {
                    OutputStream out = socket.getOutputStream();

//                    // Do Initialization
//                    String initString = StreamingMessage.initializationMessage(data.length);
//                    out.write(initString.getBytes(StreamingCharset.DEFAULT_CHARSET));

                    // Send data
                    out.write(data);
                    socket.close();

//                    // Do finishing
//                    String finishString = StreamingMessage.streamingEndedMessage();
//                    out.write(finishString.getBytes(StreamingCharset.DEFAULT_CHARSET));
                }

                this.connections.clear();
            }
        }
        catch(IOException iOException){
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error playing song", iOException);
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
     */
    private void acceptConnections() {
        try {
            Socket socket = this.getServerSocket().accept();
            this.connections.add(socket);
        } catch (IOException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error accepting connection", e);
        }
    }

    /**
     * Synchronized server socket getter.
     * @return Returns the server socket.
     */
    private synchronized ServerSocket getServerSocket(){
        return this.serverSocket;
    }
}
