package controllers.networking.streaming.music.callback;

/**
 * Created by Esteban on 22.04.2016.
 * Tells that the song should be paused.
 */
@FunctionalInterface
public interface OnPause {
    void pause();
}
