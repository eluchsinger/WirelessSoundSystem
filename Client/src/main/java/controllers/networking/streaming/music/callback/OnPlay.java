package controllers.networking.streaming.music.callback;

/**
 * Created by Esteban Luchsinger on 17.03.2016.
 * Tells that the song should be played.
 */
@FunctionalInterface
public interface OnPlay {
    void play();
}
