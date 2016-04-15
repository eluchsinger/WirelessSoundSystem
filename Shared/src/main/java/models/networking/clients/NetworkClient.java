package models.networking.clients;

import javafx.beans.property.SimpleStringProperty;
import models.networking.clients.callbacks.OnDisconnected;

import java.io.Closeable;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Esteban Luchsinger on 24.03.2016.
 * Represents a network client.
 */
public interface NetworkClient extends Closeable {
//
//    /**
//     * @return Returns the open ObjectOutputStream corresponding to the socket.
//     */
//    ObjectOutputStream getObjectOutputStream();
//
//    /**
//     * @return Returns the open ObjectInputStream corresponding to the socket.
//     */
//    ObjectInputStream getObjectInputStream();

    /**
     * Sends an object to the connected socket.
     * @param object Object to send. Must implement the serializable interface.
     */
    void send(Object object);

    /**
     * Waits until all object were sent.
     * If needed, this method returns immediately.
     * The timeout is not defined.
     */
    void waitForSending();

    /**
     * Waits until all objects were sent.
     * If needed, this method returns immediately.
     * @param timeout Timeout time
     * @param timeUnit TimeUnit for the timeout
     */
    void waitForSending(long timeout, TimeUnit timeUnit) throws TimeoutException;

    /**
     * @return Returns the socket corresponding to the NetworkClient.
     */
    Socket getSocket();


    /**
     * @return Returns the name of the Network Client.
     */
    String getName();

    /**
     * Sets the name of the Network Client.
     */
    void setName(String name);

    /**
     * Because the name of the NetworkClient must be observable.
     * @return
     */
    SimpleStringProperty nameProperty();

    void addOnDisconnectedListener(OnDisconnected listener);
    void removeOnDisconnectedListener(OnDisconnected listener);
}
