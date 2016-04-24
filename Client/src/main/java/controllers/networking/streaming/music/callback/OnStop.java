package controllers.networking.streaming.music.callback;

/**
 * <pre>
 * Created by Esteban Luchsinger on 17.03.2016.
 * Tells the client that it should stop the MediaPlayer.
 * </pre>
 */
@FunctionalInterface
public interface OnStop {
    void stop();
}
