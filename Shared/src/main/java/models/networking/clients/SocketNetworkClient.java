package models.networking.clients;

import models.clients.Client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;


/**
 * Created by Esteban Luchsinger on 18.03.2016.
 * Wraps a Client on the Network.
 */
public class SocketNetworkClient extends Client implements NetworkClient, Closeable {
    private final Socket socket;
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;

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

        this.socket = socket;
        this.outputStream =
                new ObjectOutputStream(this.socket.getOutputStream());
        // Need to flush the OOS before opening the OIS. (By both sides)
        // http://stackoverflow.com/a/7586021/2632991
        this.outputStream.flush();
        this.inputStream =
                new ObjectInputStream(this.socket.getInputStream());
    }

    /**
     * @return Returns the socket of the SocketNetworkClient.
     */
    public Socket getSocket() {
        return this.socket;
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
        if(object instanceof Serializable)
            throw new RuntimeException("The object must implement the serializable interface");

        this.getObjectOutputStream().writeObject(object);

        // Todo: Implement multi-threading.
    }

    /**
     * Closes the SocketNetworkClient.
     */
    public void close() throws IOException {
        if(this.getSocket() != null && !this.getSocket().isClosed()) {
            this.socket.getOutputStream().flush();
            this.socket.close();
        }
    }
}