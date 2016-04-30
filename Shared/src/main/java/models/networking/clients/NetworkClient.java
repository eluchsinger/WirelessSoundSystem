package models.networking.clients;

import javafx.beans.property.SimpleStringProperty;
import models.networking.clients.callbacks.OnDisconnected;

import java.io.Closeable;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Esteban Luchsinger on 24.03.2016.
 * Represents a network client.
 */
public interface NetworkClient extends Closeable {

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
     * Retrieves the expected cache on the client.
     * @return Returns a list of the hashCodes of the currently cached songs on the client.
     */
    List<Integer> getExpectedCache();

    /**
     * The name property of the client. Contains the current name of the client.
     * May be changed by the client with a <code>RenameCommand</code>.
     * Because the name of the NetworkClient must be observable.
     * @return Returns the name property.
     */
    SimpleStringProperty nameProperty();

    void addOnDisconnectedListener(OnDisconnected listener);
    void removeOnDisconnectedListener(OnDisconnected listener);
}
