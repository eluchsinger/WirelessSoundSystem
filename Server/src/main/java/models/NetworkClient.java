package models;

import models.clients.Client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;


/**
 * Created by Esteban Luchsinger on 18.03.2016.
 * Wraps a NetworkClient
 */
public class NetworkClient extends Client {
    private final Socket socket;
    private final ObjectOutputStream outputStream;

    /**
     * Default Constructor
     * @param inetAddress InetAddress of the client.
     * @param port Port of the client.
     * @throws IOException
     */
    public NetworkClient(InetAddress inetAddress, int port) throws IOException {
        this(new Socket(inetAddress, port));
    }

    public NetworkClient(Socket socket) throws IOException {

        this.socket = socket;
        this.outputStream =
                new ObjectOutputStream(this.socket.getOutputStream());
    }

    /**
     * @return Returns the socket of the NetworkClient.
     */
    public Socket getSocket() {
        return this.socket;
    }

    public ObjectOutputStream getOutputStream() {
        return this.outputStream;
    }

    /**
     * Closes the NetworkClient.
     */
    public void close() throws IOException {
        this.outputStream.close();
    }
}
