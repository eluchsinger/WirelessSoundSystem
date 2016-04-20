package viewmodels.songs;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import models.songs.Mp3Song;

import java.io.IOException;

/**
 * <pre>
 * Created by $MyName on 20.04.2016.
 * Extends the <code>Mp3Song</code> class. Adds functionality to save the current playing status.
 * </pre>
 */
public class PlayableMp3Song extends Mp3Song implements PlayableSong {
    private BooleanProperty isPlaying;

    /**
     * This class requires a path on construction initialization time.
     *
     * @param path Path of the MP3-File
     * @throws InvalidDataException
     * @throws IOException
     * @throws UnsupportedTagException
     */
    public PlayableMp3Song(String path) throws InvalidDataException, IOException, UnsupportedTagException {
        super(path);
        this.isPlaying = new SimpleBooleanProperty(false);
    }


    @Override
    public BooleanProperty isPlayingProperty() {
        return this.isPlaying;
    }

    /**
     * Creates a new PlayableMp3Song object from a mp3Song.
     * @param mp3Song The mp3Song used to create the new object.
     * @return Returns a <strong>new</strong> object of type <code>PlayableMp3Song</code>.
     * @throws InvalidDataException Throws an invalid Data exception, according to the <code>Mp3Song</code> constructor exception.
     * @throws IOException Throws an IOException, according to the <code>Mp3Song</code> constructor exception.
     * @throws UnsupportedTagException Throws an UnsupportedTagException, according to the <code>Mp3Song</code> constructor exception.
     */
    public static PlayableMp3Song fromMp3Song(Mp3Song mp3Song) throws InvalidDataException, IOException, UnsupportedTagException {
        return new PlayableMp3Song(mp3Song.getPath());
    }
}
