package controllers;

import controllers.networking.streaming.music.tcp.TCPSocketServer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.networking.clients.NetworkClient;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 26.03.2016.
 * This controller handles the clients connected to the server.
 */
public class ClientController {
    private final Logger logger;
    private final ObservableList<NetworkClient> clients;

    /**
     * Default constructor.
     * @param socketServer The server socket is used to register the different listeners required to work.
     */
    public ClientController(TCPSocketServer socketServer) {
        this.logger = Logger.getLogger(this.getClass().getName());
        this.clients = FXCollections.observableArrayList();

        socketServer.addOnClientConnectedListener(this::onClientConnected);
    }

    /**
     * Called when a client connects.
     * @param client connected client.
     */
    private void onClientConnected(NetworkClient client) {
        this.clients.add(client);
        this.logger.log(Level.INFO, "Client connected: " + client);
    }

    /**
     * @return Returns the clients.
     */
    public ObservableList<NetworkClient> getClients() {
        return this.clients;
    }
}
