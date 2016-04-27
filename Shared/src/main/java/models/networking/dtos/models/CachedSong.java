package models.networking.dtos.models;

import java.io.Serializable;
import java.util.Arrays;

/**
 * <pre>
 * Created by Esteban Luchsinger on 27.04.2016.
 * This class represents a cached song. A cached song is completely in the main memory, not serialized as a file.
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CachedSong that = (CachedSong) o;

        if (!Arrays.equals(data, that.data)) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        return artist != null ? artist.equals(that.artist) : that.artist == null;

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(data);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        return result;
    }
}
