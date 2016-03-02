package controllers.networking.discovery.callback;

import models.clients.Server;

/**
 * Created by Esteban Luchsinger on 02.03.2016.
 * Called when a server disconnects.
 */
@FunctionalInterface
public interface OnServerDisconnected {
    void serverDisconnected(Server server);
}
