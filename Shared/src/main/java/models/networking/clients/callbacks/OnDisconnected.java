package models.networking.clients.callbacks;

/**
 * Created by Esteban Luchsinger on 06.04.2016.
 * A disconnected client uses this callback, when it is disconnected.
 */
@FunctionalInterface
public interface OnDisconnected {

    void onDisconnected();
}
