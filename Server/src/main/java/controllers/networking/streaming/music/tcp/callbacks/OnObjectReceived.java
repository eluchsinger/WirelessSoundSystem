package controllers.networking.streaming.music.tcp.callbacks;

/**
 * Created by Esteban Luchsinger on 24.03.2016.
 */
@FunctionalInterface
public interface OnObjectReceived {
    void onObjectReceived(Object obj);
}
