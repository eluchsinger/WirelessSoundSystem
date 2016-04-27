package models.networking.dtos.commands;

import models.networking.dtos.models.CachedSong;

import java.io.Serializable;

/**
 * <pre>
 * Created by Esteban Luchsinger on 17.03.2016.
 * The command "Play". It tells the client to play the song.
 * </pre>
 */
public class PlayCommand implements Serializable {
    private static final long serialVersionUID = -3809733967947659045L;

    /**
     * The hash defines the song to be played.
     * The client will look in his cache, if there is a song with the hash of this <code>PlayCommand</code>.
     */
    public final int hash;

    public PlayCommand(CachedSong cachedSong) {
        this.hash = cachedSong.hashCode();
    }
}
