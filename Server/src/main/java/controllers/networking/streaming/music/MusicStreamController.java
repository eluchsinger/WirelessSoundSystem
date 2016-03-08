package controllers.networking.streaming.music;

import models.songs.Song;

/**
 * Created by Esteban Luchsinger on 16.12.2015.
 * This interface controls the song streaming.
 */
public interface MusicStreamController {
    /**
     * Streams the song.
     * @param song Song to stream.
     */
    void play(Song song);
}
