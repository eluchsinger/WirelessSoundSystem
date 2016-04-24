package controllers.networking.streaming.music.callback;

/**
 * <pre>
 * Created by Esteban Luchsinger on 17.03.2016.
 * Tells that the song should be played.
 * </pre>
 */
@FunctionalInterface
public interface OnPlay {
    void play(String songTitle, String artist);
}
