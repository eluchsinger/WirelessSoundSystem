package controllers.networking.discovery.callback;

import models.clients.Server;

/**
 * <pre>
 * Created by Esteban Luchsinger on 02.03.2016.
 * Called when a server disconnects.
 * </pre>
 */
@FunctionalInterface
public interface OnServerDisconnected {
    void serverDisconnected(Server server);
}