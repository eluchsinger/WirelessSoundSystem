package controllers.networking.streaming.music;

/**
 * Created by Esteban Luchsinger on 29.02.2016.
 * Functional Interface
 */
@FunctionalInterface
public interface OnMusicStreamingStatusChanged {
    void statusChanged(ServiceStatus newStatus);
}
