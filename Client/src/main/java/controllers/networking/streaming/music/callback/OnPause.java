package controllers.networking.streaming.music.callback;

/**
 * <pre>
 * Created by Esteban on 22.04.2016.
 * Tells that the song should be paused.
 * </pre>
 */
@FunctionalInterface
public interface OnPause {
    void pause();
}
