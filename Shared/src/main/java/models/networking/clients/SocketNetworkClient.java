package models.networking.clients;

import models.clients.Client;
import models.networking.clients.callbacks.OnDisconnected;
import models.networking.dtos.commands.CurrentCacheCommand;
import models.networking.dtos.commands.KeepAliveBeacon;
import models.networking.dtos.commands.RenameCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.concurrent.ExecutorServiceUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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

    /**
     * This executor handles the reading of new messages.
     */
    private final ExecutorService readingExecutor;

    /**
     * This executor handles the sending of messages to the client.
     */
    private final ExecutorService sendingExecutor;

    /**
     * The last async sending will be stored inside of this future.
     * The future can be used to wait until the client received the data.
     */
    private Future<?> lastSentFuture;

    private final List<OnDisconnected> onDisconnectedListeners;

    /**
     * This is a list which contains the expected cache to be on the client.
     * The cached objects are represented by their corresponding hashCode.
     */
    private final List<Integer> expectedCache;

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

        // Initialize the executor as single thread executor.
        // A single thread executor ensures that every submit is executed in
        // the correct order.
        this.sendingExecutor = Executors.newSingleThreadExecutor();

        this.expectedCache = new ArrayList<>();
    }

    /**
     * @return Returns the socket of the SocketNetworkClient.
     */
    public Socket getSocket() {
        return this.socket;
    }

    /**
     * Retrieves the expected cache on the client.
     *
     * @return Returns a list of the hashCodes of the currently cached songs on the client.
     */
    @Override
    public List<Integer> getExpectedCache() {
        return this.expectedCache;
    }

    @Override
    public void addOnDisconnectedListener(OnDisconnected listener) {
        this.onDisconnectedListeners.add(listener);
    }

    @Override
    public void removeOnDisconnectedListener(OnDisconnected listener) {
        this.onDisconnectedListeners.remove(listener);
    }

    private ObjectOutputStream getObjectOutputStream() {
        return this.outputStream;
    }

    private ObjectInputStream getObjectInputStream() { return this.inputStream; }

    /**
     * Sends an object to the connected socket (asynchronously).
     * This method will send an object in a non-blocking mode (async).
     * @param object Object to send. MUST implement the serializable interface.
     */
    @Override
    public void send(Object object) {
        this.lastSentFuture = this.sendingExecutor.submit(() -> {
            try {
                this.sendSync(object);
            } catch (IOException e) {
                this.logger.warn("Error sending object " + object, e);
            }
        });
    }

    /**
     * Waits until all object were sent.
     * If there are no objects being sent, this method returns immediately.
     * The timeout is not defined.
     */
    @Override
    public void waitForSending() {
        if(this.lastSentFuture != null) {
            try {
                this.lastSentFuture.get();
            } catch (ExecutionException e) {
                this.logger.warn("Error sending data to client " + this.toString(), e);
            } catch (InterruptedException e) {
                this.logger.warn("Sending data to client  " + this + " was interrupted.", e);
            }
        }
    }

    /**
     * Waits until all objects were sent.
     * If needed, this method returns immediately.
     *
     * @param timeout  Timeout time
     * @param timeUnit TimeUnit for the timeout
     */
    @Override
    public void waitForSending(long timeout, TimeUnit timeUnit) throws TimeoutException {
        if(this.lastSentFuture != null) {
            try {
                this.lastSentFuture.get(timeout, timeUnit);
            } catch (ExecutionException e) {
                this.logger.warn("Error sending data to client " + this.toString(), e);
            } catch (InterruptedException e) {
                this.logger.warn("Sending data to client  " + this + " was interrupted.", e);
            }
        }
    }

    /**
     * Sends the desired object synchronously.
     * @param object Object to send
     * @throws IOException
     */
    private void sendSync(Object object) throws IOException{

        if(object == null)
            throw new NullPointerException("Object is null.");
        if(!(object instanceof Serializable))
            throw new RuntimeException("The object must implement the serializable interface");

        this.getObjectOutputStream().writeObject(object);
    }

    /**
     * Listens for incoming messages from the client.
     */
    private void listen() {
        while(isWorking && !this.socket.isClosed()) {
            try {
                Object receivedObject = this.getObjectInputStream().readObject();

                if(receivedObject instanceof RenameCommand) {
                    RenameCommand command = (RenameCommand) receivedObject;
                    this.setName(command.getName());
                    this.logger.info("Client " + this + "renamed to " + this.getName());
                } else if(receivedObject instanceof CurrentCacheCommand) {
                    CurrentCacheCommand command = (CurrentCacheCommand) receivedObject;
                    this.expectedCache.clear();
                    this.expectedCache.addAll(command.currentCache);
                    this.logger.info("Received the current cache (Client: " + this.toString()
                            + ") Currently " + this.expectedCache.size() + " Files in cache.");
                } else {
                    // The received object is unknown.
                    this.logger.info("Received unknown command from client " + this.getName() +
                            "\n" + receivedObject.toString());
                }
            }
            catch(EOFException eofException) {
                // An EOF Exception could be due to the client input stream being closed.
                // Try to send a beacon to the client, to check if he is still available.
                try {
                    // Sends this beacon synchronously because if the client disconnected,
                    // it will be the last object sent.
                    this.sendSync(new KeepAliveBeacon());
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
        ExecutorServiceUtils.stopExecutorService(this.sendingExecutor);

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