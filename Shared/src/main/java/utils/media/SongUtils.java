package utils.media;

import models.songs.Song;

import java.io.File;
import java.io.IOException;
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
}
