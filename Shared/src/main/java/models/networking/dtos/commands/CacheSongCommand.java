package models.networking.dtos.commands;

import models.networking.dtos.models.CachedSong;

import java.io.Serializable;

/**
 * Created by Esteban Luchsinger on 08.04.2016.
 * The cache command tells the client to cache the song. (Not start playing!)
 */
public class CacheSongCommand implements Serializable {
    private static final long serialVersionUID = 2322268523175827924L;

    /**
     * Required: The <code>Song</code> to be cached.
     */
    public CachedSong cachedSong;

    /**
     * Initializes the <code>CacheSongCommand</code>.
     * @param cachedSong The cached song to be cached.
     */
    public CacheSongCommand(CachedSong cachedSong) {
        this.cachedSong = cachedSong;
    }

}
