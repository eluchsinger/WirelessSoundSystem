package models.networking.dtos.models;

import java.io.Serializable;
import java.util.Arrays;

/**
 * <pre>
 * Created by Esteban Luchsinger on 27.04.2016.
 * This class represents a cached song. A cached song is completely in the main memory, not serialized as a file.
 * The <code>equals</code> and <code>hashCode</code> methods look only at the data in bytes and not at the title or artist.
 * </pre>
 */
public class CachedSong implements Serializable {

    private static final long serialVersionUID = 4091902149219843068L;

    public byte[] data;
    public String title;
    public String artist;

    public CachedSong(byte[] data) {
        this.data = data;
    }

    public CachedSong(byte[] data, String title, String artist) {
        this(data);
        this.title = title;
        this.artist = artist;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CachedSong that = (CachedSong) o;

        return Arrays.equals(data, that.data);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public String toString() {
        return this.title + ", " + this.artist;
    }
}
