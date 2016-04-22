package models.songs;

import javafx.util.Duration;
import utils.DurationStringConverter;

/**
 * Created by Esteban Luchsinger on 30.11.2015.
 */
public interface Song extends Comparable<Song> {
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
     * Gets the songs length in seconds.
     * @return Returns the length of the song in seconds.
     */
    long getLengthInSeconds();

    default String getDuration() {
        Duration duration = Duration.seconds(this.getLengthInSeconds());

        DurationStringConverter converter = new DurationStringConverter();
        return converter.toString(duration);
    }

    /**
     * Returns the file extension without the dot.
     * @return i.e. MP3 --&gt; "mp3"
     */
    String getExtension();

    String getPath();
}
