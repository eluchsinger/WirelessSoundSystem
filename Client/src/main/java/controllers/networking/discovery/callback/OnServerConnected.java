package controllers.networking.discovery.callback;

import models.clients.Server;

/**
 * <pre>
 * Created by Esteban Luchsinger on 02.03.2016.
 * OnServerConnected interface.
 * This event is called when a server connects.
 * </pre>
 */
@FunctionalInterface
public interface OnServerConnected {
    void serverConnected(Server server);
}
