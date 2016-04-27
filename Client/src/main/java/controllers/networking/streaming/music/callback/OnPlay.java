package controllers.networking.streaming.music.callback;

/**
 * <pre>
 * Created by Esteban Luchsinger on 17.03.2016.
 * Tells that the song should be played.
 * </pre>
 */
@FunctionalInterface
public interface OnPlay {
    /**
     * Plays the song with the hashcode corresponding to the parameter.
     * @param hash Hashcode of the song to be played.
     */
    void play(int hash);
}
