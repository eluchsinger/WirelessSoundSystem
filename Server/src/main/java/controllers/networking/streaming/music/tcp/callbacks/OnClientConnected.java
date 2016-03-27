package controllers.networking.streaming.music.tcp.callbacks;

import models.networking.clients.NetworkClient;

/**
 * Created by Esteban Luchsinger on 26.03.2016.
 */
@FunctionalInterface
public interface OnClientConnected {
    /**
     * Call, when a client connects to the TCPServer.
     * @param client Connected network client.
     */
    void onClientConnected(NetworkClient client);
}
