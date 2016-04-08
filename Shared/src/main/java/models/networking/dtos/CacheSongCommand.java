package models.networking.dtos;

import java.io.Serializable;

/**
 * Created by Esteban Luchsinger on 08.04.2016.
 * The cache command tells the client to cache the song. (Not start playing!)
 */
public class CacheSongCommand implements Serializable {
    private static final long serialVersionUID = 2322268523175827924L;

    public byte[] data;

    public CacheSongCommand(byte[] data) { this.data = data; }
}
