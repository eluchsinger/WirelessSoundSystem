package controllers.networking.streaming.music.callback;

import controllers.networking.streaming.music.ServiceStatus;

/**
 * <pre>
 * Created by Esteban Luchsinger on 29.02.2016.
 * Functional Interface
 * </pre>
 */
@FunctionalInterface
public interface OnMusicStreamingStatusChanged {

    void statusChanged(ServiceStatus newStatus);
}
