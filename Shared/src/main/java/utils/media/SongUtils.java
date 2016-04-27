package utils.media;

import models.songs.Song;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;

/**
 * Created by Esteban on 29.03.2016.
 */
public class SongUtils {

    /**
     * Returns the data of the song.
     * @param song
     * @return Return an array of bytes with the data of the song.
     * @throws IOException Throws IOException when the File could not be read (or found)
     */
    public static byte[] getSongData(Song song) throws IOException {

        // Get File Data.
        File songFile = new File(song.getPath());
        return Files.readAllBytes(songFile.toPath());
    }

    /**
     * Returns the song URI.
     * @param song Song of whom the URI will be returned.
     * @return Returns the URI of the song.
     */
    public static URI getSongURI(Song song) {
        File f = new File(song.getPath());
        return f.toURI();
    }


}
