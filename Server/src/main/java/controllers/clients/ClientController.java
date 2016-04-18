package controllers.clients;

import controllers.networking.streaming.music.tcp.TCPSocketServer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.networking.clients.NetworkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

/**
 * <pre>
 * Created by Esteban Luchsinger on 26.03.2016.
 * This controller handles the clients connected to the server.
 * </pre>
 */
public class ClientController implements Closeable{
    private final Logger logger;
    private final ObservableList<NetworkClient> clients;

    /**
     * Default constructor.
     * @param socketServer The server socket is used to register the different listeners required to work.
     */
    public ClientController(TCPSocketServer socketServer) {
        this.logger = LoggerFactory.getLogger(this.getClass());

        this.clients = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());

        socketServer.addOnClientConnectedListener(this::onClientConnected);
    }

    /**
     * Called when a client connects.
     * @param client connected client.
     */
    private void onClientConnected(NetworkClient client) {
        Platform.runLater(() -> {
            this.clients.add(client);
            // Add the listener for when a client disconnects.
            client.addOnDisconnectedListener(() -> this.onClientDisconnected(client));
            this.logger.info("Client connected: " + client);
        });
    }

    /**
     * Called when a client disconnects
     * @param client disconnected client
     */
    private void onClientDisconnected(NetworkClient client) {
        Platform.runLater(() -> {
            this.clients.remove(client);
            this.logger.info("Client disconnected: " + client);
        });
    }

    /**
     * @return Returns the clients.
     */
    public ObservableList<NetworkClient> getClients() {
        return this.clients;
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     * As noted in {@link AutoCloseable#close()}, cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * <em>mark</em> the {@code Closeable} as closed, prior to throwing
     * the {@code IOException}.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        for(NetworkClient client : this.getClients()) {
            client.close();
        }
    }
}
