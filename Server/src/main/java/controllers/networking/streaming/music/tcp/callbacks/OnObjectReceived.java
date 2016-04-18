package controllers.networking.streaming.music.tcp.callbacks;

/**
 * Created by Esteban Luchsinger on 24.03.2016.
 * Call this client when an object is received from a client.
 */
@FunctionalInterface
public interface OnObjectReceived {
    /**
     * This method is called, when an object is received from a client.
     * @param obj Received object
     */
    void onObjectReceived(Object obj);
}
