package models.networking.clients;

import models.clients.Client;
import models.networking.clients.callbacks.OnDisconnected;
import models.networking.dtos.KeepAliveBeacon;
import models.networking.dtos.RenameCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.concurrent.ExecutorServiceUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Esteban Luchsinger on 18.03.2016.
 * Wraps a Client on the Network.
 */
public class SocketNetworkClient extends Client implements NetworkClient, Closeable {
    private final Logger logger;

    private final Socket socket;
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;

    private volatile boolean isWorking;

    private final ExecutorService readingExecutor;

    private final List<OnDisconnected> onDisconnectedListeners;

    /**
     * Default Constructor
     * @param inetAddress InetAddress of the client.
     * @param port Port of the client.
     * @throws IOException
     */
    public SocketNetworkClient(InetAddress inetAddress, int port) throws IOException {
        this(new Socket(inetAddress, port));
    }

    /**
     * Default constructor
     * @param socket Connected socket.
     * @throws IOException
     */
    public SocketNetworkClient(Socket socket) throws IOException {
        this.logger = LoggerFactory.getLogger(this.getClass());

        this.onDisconnectedListeners = new ArrayList<>();

        this.socket = socket;
        this.outputStream =
                new ObjectOutputStream(this.socket.getOutputStream());
        // Need to flush the OOS before opening the OIS. (By both sides)
        // http://stackoverflow.com/a/7586021/2632991
        this.outputStream.flush();
        this.inputStream =
                new ObjectInputStream(this.socket.getInputStream());

        this.setName(socket.getInetAddress().getHostName());
        this.isWorking = true;

        this.readingExecutor = Executors.newSingleThreadExecutor();
        this.readingExecutor.submit(this::listen);
    }

    /**
     * @return Returns the socket of the SocketNetworkClient.
     */
    public Socket getSocket() {
        return this.socket;
    }

    @Override
    public void addOnDisconnectedListener(OnDisconnected listener) {
        this.onDisconnectedListeners.add(listener);
    }

    @Override
    public void removeOnDisconnectedListener(OnDisconnected listener) {
        this.onDisconnectedListeners.remove(listener);
    }


    @Override
    public ObjectOutputStream getObjectOutputStream() {
        return this.outputStream;
    }

    @Override
    public ObjectInputStream getObjectInputStream() { return this.inputStream; }

    /**
     * Sends an object to the connected socket.
     * This method will send an object in a non-blocking mode (async).
     * @param object Object to send. MUST implement the serializable interface.
     * @throws IOException
     */
    @Override
    public void send(Object object) throws IOException {

        if(object == null)
            throw new NullPointerException("Object is null.");
        if(!(object instanceof Serializable))
            throw new RuntimeException("The object must implement the serializable interface");

        this.getObjectOutputStream().writeObject(object);

        // Todo: Implement multi-threading.
    }

    /**
     * Listens for incoming messages from the client.
     */
    private void listen() {
        while(isWorking && !this.socket.isClosed()) {
            try {
                Object receivedObject = this.inputStream.readObject();

                if(receivedObject instanceof RenameCommand) {
                    RenameCommand command = (RenameCommand) receivedObject;
                    this.setName(command.getName());
                }
            }
            catch(EOFException eofException) {
                // An EOF Exception could be due to the client input stream being closed.
                // Try to send a beacon to the client, to check if he is still available.
                try {
                    this.send(new KeepAliveBeacon());
                } catch (IOException e) {
                    // If the beacon failed, this client disconnected.
                    try {
                        this.close();
                    } catch (IOException e1) {
                        this.logger.warn("Error terminating the client.", e1);
                    }
                }
            }
            catch(IOException | ClassNotFoundException exception) {
                this.logger.warn("Error receiving Object in client.", exception);
            }
        }
    }

    /**
     * Closes the SocketNetworkClient.
     */
    public void close() throws IOException {
        this.isWorking = false;
        if(this.getSocket() != null && !this.getSocket().isClosed()) {
            this.socket.getOutputStream().flush();
            this.socket.close();
        }

        ExecutorServiceUtils.stopExecutorService(this.readingExecutor);

        this.onDisconnected();
    }

    /**
     * @return For NetworkClients, return only the name.
     */
    @Override
    public String toString() {
        return this.getName();
    }

    private void onDisconnected() {
        this.onDisconnectedListeners.forEach(OnDisconnected::onDisconnected);
    }
}