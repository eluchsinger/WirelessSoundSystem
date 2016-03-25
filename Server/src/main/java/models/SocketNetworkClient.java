package models;

import models.clients.Client;
import models.networking.clients.NetworkClient;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    public SocketNetworkClient(Socket socket) throws IOException {

        this.socket = socket;
        this.outputStream =
                new ObjectOutputStream(this.socket.getOutputStream());
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
     * Closes the SocketNetworkClient.
     */
    public void close() throws IOException {
        if(this.getSocket() != null && !this.getSocket().isClosed()) {
            this.socket.close();
        }

    }
}
