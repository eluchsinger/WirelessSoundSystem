package controllers.networking.streaming.music;

import models.songs.Song;

import java.io.IOException;

/**
 * <pre>
 * Created by Esteban Luchsinger on 16.12.2015.
 * This interface controls the song streaming.
 * </pre>
 */
public interface MusicStreamController {

    /**
     * Starts playing the song on the client.
     * @param song Song to stream.
     * @throws IOException Throws an IO Exception if there was a problem with IO. (Probably with the song).
     */
    void play(Song song) throws IOException;

    /**
     * Stops playing the song on the client.
     */
    void stop();

    /**
     * Caches a song on the clients.
     * @param song The song to cache on the clients.
     */
    void cacheSong(Song song) throws IOException;
}
