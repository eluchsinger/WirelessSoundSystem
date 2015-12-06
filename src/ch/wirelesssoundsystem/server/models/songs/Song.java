package ch.wirelesssoundsystem.server.models.songs;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.IOException;

/**
 * Created by eluch on 30.11.2015.
 */
public interface Song extends Comparable<Song>{
    /**
     * Gets the title of the song.
     * @return Returns the title of the song, if possible  (if not, returns "unknown title").
     */
    String getTitle();

    /**
     * Gets the artist of the song.
     * @return Returns the artist of the song, if possible (if not, returns "unknown artist").
     */
    String getArtist();

    /**
     * Returns the file extension without the dot.
     * @return i.e. MP3 --> "mp3"
     */
    String getExtension();

    String getPath();
}
