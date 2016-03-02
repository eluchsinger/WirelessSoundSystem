package controllers.networking.discovery.callback;

import models.clients.Server;

/**
 * Created by Esteban Luchsinger on 02.03.2016.
 * OnServerConnected interface.
 * This event is called when a server connects.
 */
@FunctionalInterface
public interface OnServerConnected {
    void serverConnected(Server server);
}
