package models.networking.clients;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Esteban Luchsinger on 24.03.2016.
 * Represents a network client.
 */
public interface NetworkClient extends Closeable {

    /**
     * @return Returns the open ObjectOutputStream corresponding to the socket.
     */
    ObjectOutputStream getObjectOutputStream();

    /**
     * @return Returns the open ObjectInputStream corresponding to the socket.
     */
    ObjectInputStream getObjectInputStream();

    /**
     * Sends an object to the connected socket.
     * @param object Object to send. Must implement the serializable interface.
     * @throws IOException
     */
    void send(Object object) throws IOException;

    /**
     * @return Returns the socket corresponding to the NetworkClient.
     */
    Socket getSocket();


    /**
     * @return Returns the name of the Network Client.
     */
    String getName();

    /**
     * @return Sets the name of the Network Client.
     */
    void setName(String name);

}
