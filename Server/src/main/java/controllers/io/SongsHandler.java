package controllers.io;

import models.songs.Mp3Song;
import models.songs.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * Created by Esteban Luchsinger on 30.11.2015.
 * Handles songs (ie: Reading songs from files)
 * </pre>
 */
public class SongsHandler {

    private final Logger logger;

    /**
     * Default constructor.
     */
    public SongsHandler() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Loads all songs from a directory.
     * @param path Path to the directory
     * @return Returns an array of songs.
     */
    public List<Song> loadSongsFromDir(String path){
        File f = new File(path);

        // List all files which end with .mp3
        File[] files = f.listFiles((dir, name) -> name.endsWith(".mp3"));

        if(files.length == 0) {
            this.logger.info("No Music Files found.");
        }

        List<Song> songList = new ArrayList<>();
        // Transform all songs into Song objects.
        for(File file : files){
            try {
                Song song = new Mp3Song(file.getPath());
                songList.add(song);
                this.logger.info("Song added: " + song.getPath());

            } catch (Exception e) {
                this.logger.warn("Fehler beim Laden eines Songs!", e);
            }
        }

        return songList;
    }
}
