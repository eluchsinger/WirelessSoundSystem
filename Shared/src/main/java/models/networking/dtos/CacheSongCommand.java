package models.networking.dtos;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by Esteban Luchsinger on 08.04.2016.
 * The cache command tells the client to cache the song. (Not start playing!)
 */
public class CacheSongCommand implements Serializable {
    private static final long serialVersionUID = 2322268523175827924L;

    public byte[] data;
    public String title;
    public String artist;

    /**
     * The <code>CacheSongCommand</code> requires at least the data field.
     * @param data Song data as a byte stream.
     */
    public CacheSongCommand(byte[] data) { this.data = data; }

    /**
     * Compares two objects, if they are equal.
     * The objects must be exactly the same. One single byte difference will return false.
     * Relevant for the equality are the fields data, title and artist.
     * @param o The other object used for the comparison.
     * @return Returns true if both objects are <strong>exactly</strong> the same.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheSongCommand that = (CacheSongCommand) o;

        if (!Arrays.equals(data, that.data)) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        return artist != null ? artist.equals(that.artist) : that.artist == null;

    }

    /**
     * Creates the hashCode of this song.
     * Relevant for the hashCode are the fields data, title and artist.
     * @return Returns the hashCode of this song.
     */
    @Override
    public int hashCode() {
        int result = Arrays.hashCode(data);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        return result;
    }
}
