package controllers.networking.streaming.music;

import models.clients.Server;
import models.networking.SongCache;
import models.networking.SongDatagram;
import models.networking.messages.StreamingMessage;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
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
     * List with connected clients.
     */
    private List<ConnectedClient> connectedClients;

    /**
     * The current state of the service.
     */
    private boolean running;

    /**
     * The current cache. The recovery will be made
     * with this cache.
     */
    private SongCache cache;

    //endregion

    //region Constructor

    /**
     * /**
     * Constructor using the default port (STREAMING_PORT).
     * @throws IOException If the port is already in use.
     */
    public MusicReconstructionService() throws IOException {
        this(Server.STREAMING_PORT);
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
                ConnectedClient client = new ConnectedClient(newClientSocket);

                // Implementation of the request handler (What happens when there are missing packets?).
                client.setReceivedClientRequestHandler(this::sendMissingPackets);

                this.connectedClients.add(client);
            } catch(IOException ignore) { }
        }
    }

    /**
     * Sends the missing packets.
     * This method is blocking and could take some time, do this multithreaded.
     * @param missingPackets A list with the sequence-nr of the missing packets.
     */
    private void sendMissingPackets(ConnectedClient c, List<Integer> missingPackets){
        try {
            DataOutputStream outputStream = new DataOutputStream(c.getSocket().getOutputStream());

            for(Integer i : missingPackets) {
                SongDatagram sd = this.cache.getSongDatagram(i);

                outputStream.write(sd.getDatagramPacket().getData());
            }
        } catch (IOException e) {

        }

    }

    //endregion

    //region Getters & Setters

    /**
     * @return True, if the service is running.
     */
    public boolean isRunning() { return this.running; }

    /**
     * Sets the cache used in the recovery process.
     * @param cache new cache.
     */
    public void setCache(SongCache cache){ this.cache = cache; }

    /**
     * Gets the cache used in the recovery process.
     * @return Returns the currently used cache.
     */
    public SongCache getCache() { return this.cache; }

    //endregion

    /**
     * A connected client that is able to send requests for reconstruction.
     */
    private class ConnectedClient {

        /**
         * The socket corresponding to this client.
         */
        private final Socket clientSocket;

        /**
         * This thread handles the listening of the socket.
         */
        private final Thread listeningThread;

        /**
         * Callback for when a client requests something.
         */
        private ReceivedClientRequest receivedClientRequest;

        /**
         * Constructor
         * @param clientSocket Connected client socket corresponding to this client.
         */
        public ConnectedClient(Socket clientSocket){
            this.clientSocket = clientSocket;

            this.listeningThread = new Thread(this::listen);
            this.listeningThread.start();
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

                    List<Integer> listOfMissingPackets = StreamingMessage.parseMissingPacketsMessage(input);

                    if(listOfMissingPackets.size() > 0){
                        // Call delegate method.
                        if(this.receivedClientRequest != null)
                            this.receivedClientRequest.receivedClientRequest(this, listOfMissingPackets);
                    }
                } catch (IOException ignore) { }
            }
        }

        /**
         * Sets the handler for a client request.
         * @param handler
         */
        public void setReceivedClientRequestHandler(ReceivedClientRequest handler){
            this.receivedClientRequest = handler;
        }

        /**
         * @return Returns the client socket.
         */
        public Socket getSocket(){
            return this.clientSocket;
        }
    }

    private interface ReceivedClientRequest {
        void receivedClientRequest(ConnectedClient client, List<Integer> missingPackets);
    }
}