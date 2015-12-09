package ch.wirelesssoundsystem.server.controllers.networking.discovery;

import ch.wirelesssoundsystem.shared.models.clients.Client;

/**
 * Created by Esteban Luchsinger on 09.12.2015.
 * Functional Method. Called when a client was found.
 */
public interface ClientFoundListener {
    void found(Client client);
}
