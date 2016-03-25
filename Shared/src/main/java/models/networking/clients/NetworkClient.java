package models.networking.clients;

import java.io.Closeable;
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
     * @return Returns the socket corresponding to the NetworkClient.
     */
    Socket getSocket();
}
