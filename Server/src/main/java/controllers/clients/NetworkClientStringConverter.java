package controllers.clients;

import javafx.util.StringConverter;
import models.networking.clients.NetworkClient;

import java.util.Optional;

/**
 * Created by Esteban on 02.04.2016.
 * Converts the NetworkClient to a String and gets the NetworkClient from a String.
 */
public class NetworkClientStringConverter extends StringConverter<NetworkClient> {

    private final ClientController clientController;

    public NetworkClientStringConverter(ClientController controller) {
        this.clientController = controller;
    }

    @Override
    public String toString(NetworkClient object) {
        return object.toString();
    }

    @Override
    public NetworkClient fromString(String string) {
        Optional<NetworkClient> optional = this.clientController.getClients()
                .stream()
                .filter(nc -> nc.toString().equals(string))
                .findFirst();

        return optional.orElse(null);
    }
}
