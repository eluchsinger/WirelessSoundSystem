package controllers.media;

import controllers.io.cache.CacheService;
import models.songs.Song;

/**
 * <pre>
 * Created by Esteban Luchsinger on 24.04.2016.
 * This <code>MediaPlayer</code> uses a cache as a source for the Media.
 * </pre>
 */
public class CachedMediaPlayer {

    private final CacheService cacheService;

    public CachedMediaPlayer(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * Plays a song.
     * If this song is already cached, it plays it from the cache.
     * If the song is not yet cached, it will cache it first.
     * @param song
     */
    public void play(Song song) {

    }

    /**
     * Pauses the current MediaPlayer from playing a song.
     */
    public void pause() {

    }

    public void stop() {

    }

}
