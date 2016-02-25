package controllers.networking.streaming.music;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;

/**
 * Created by Esteban Luchsinger on 25.02.2016.
 *
 * The music reconstruction controller handles the reconstruction
 * of missing music-segments on the client side.
 * If the client is missing a segment, it can request the segment
 * from here.
 */
public class MusicReconstructionService {

    //region Constants
    /**
     * Default port used for the server.
     */
    public static final int DEFAULT_PORT = 6070;

    /**
     * Socket timeout in milliseconds.
     */
    private static final int SOCKET_TIMEOUT = 500;

    //endregion

    //region Variables
    /**
     * This is the socket listening for requests from the client.
     */
    private ServerSocket listeningSocket;

    /**
     * This thread accepts incoming TCP-Connections.
     */
    private Thread accepterThread;

    /**
     * This thread listens (and answers) to connected clients.
     * The thread is designated for Q and A from clients!
     */
    private Thread listeningThread;

    /**
     * List with connected clients.
     */
    private List<ConnectedClient> connectedClients;

    /**
     * The current state of the service.
     */
    private boolean running;

    //endregion

    //region Constructor

    /**
     * /**
     * Constructor using the default port (DEFAULT_PORT).
     * @throws IOException If the port is already in use.
     */
    public MusicReconstructionService() throws IOException {
        this(DEFAULT_PORT);
    }

    /**
     * Constructor.
     * @param port the port on which the TCP Listener is listening.
     * @throws IOException if the port is already in use.
     */
    public MusicReconstructionService(int port) throws IOException {
        this.listeningSocket = new ServerSocket(port);
        this.listeningSocket.setPerformancePreferences(1, 2, 3);
        this.listeningSocket.setSoTimeout(SOCKET_TIMEOUT);
    }

    //endregion

    //region Public Methods

    /**
     * Starts the reconstruction service.
     */
    public void start(){
        if(!this.isRunning()) {
            // Todo: Implement starting mechanism.
            this.accepterThread = new Thread(this::acceptConnections);
            this.accepterThread.start();
        }
    }

    /**
     * Stops the reconstruction service.
     */
    public void stop(){
        if(this.isRunning()){
            // Todo: Implement stopping mechanism.

            this.running = false;
//            try {
//                this.accepterThread.join(500);
//            } catch (InterruptedException ignore) {
//            }

        }
    }

    /**
     * Method accepting connections.
     * This method is blocking and should be called multi-threaded.
     * Exit the method by setting isRunning false.
     */
    private void acceptConnections() {

        this.running = true;
        while(isRunning()){
            try {
                if(!isRunning())
                    break;

                // Add the new client to the list.
                Socket newClientSocket = this.listeningSocket.accept();
                this.connectedClients.add(new ConnectedClient(newClientSocket));
            } catch(IOException ignore) { }
        }
    }

    //endregion

    //region Getters & Setters

    /**
     * @return True, if the service is running.
     */
    public boolean isRunning() { return this.running; }

    //endregion

    /**
     * A connected client that is able to send requests for reconstruction.
     */
    private class ConnectedClient {

        private Socket clientSocket;
        private Thread listeningThread;

        public ConnectedClient(Socket clientSocket){
            this.clientSocket = clientSocket;

            this.listeningThread = new Thread(this::listen);
        }

        /**
         * Listen to what the client says.
         */
        private void listen() {

            while(true) {
                try {
                    BufferedReader bufferedReader =
                            new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    String input = bufferedReader.readLine();

                    // Todo: implement event if a request is received.
                } catch (IOException ignore) { }
            }
        }
    }
}