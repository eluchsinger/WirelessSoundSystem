package controllers.networking.streaming.music.callback;

import models.networking.dtos.PlayCommand;

/**
 * Created by Esteban Luchsinger on 17.03.2016.
 * Tells that the song should be played.
 */
@FunctionalInterface
public interface OnPlay {
    void play(String songTitle, String artist);
}
