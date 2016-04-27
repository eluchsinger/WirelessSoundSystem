package controllers.io.cache.file;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
import models.networking.dtos.commands.CacheSongCommand;
import models.networking.dtos.models.CachedSong;
import models.songs.Mp3Song;
import models.songs.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * <pre>
 * Created by Esteban Luchsinger on 25.04.2016.
 * A cache manager for Songs.
 * </pre>
 * @implNote The cache creates a file for the song. The file name concatenates a prefix, the hash of the song and the file format.
 * @author Esteban Luchsinger
 * @since 25.04.2016
 */
public class SongCacheManager {

    //region Constants
    /**
     * Prefix of the song files.
     */
    private final static String PREFIX = "wss";

    /**
     * Suffix of the song files.
     */
    private final static String SUFFIX = ".mp3";

    /**
     * Regex part for the capturing of the HASH-CODE of the song.
     */
    private final static String REGEX_HASH_GROUP = "(?<HASH>[0-9]*)";
    //endregion Constants

    private final Logger logger;
    private final Path tempFolderPath;

    /**
     * The regex Pattern.
     */
    private final Pattern pattern;

    /**
     * Default constructor
     */
    public SongCacheManager() {
        this.logger = LoggerFactory.getLogger(this.getClass());

        this.tempFolderPath = Paths.get(System.getProperty("java.io.tmpdir") + "wss/cache");

        // Creates the folder structure.
        //noinspection ResultOfMethodCallIgnored
        new File(this.tempFolderPath.toUri()).mkdirs();

        String patternStringBuilder = PREFIX +
                REGEX_HASH_GROUP +
                SUFFIX;

        /**
         * Example for a name: wss4343244323.mp3
         * In REGEX: wss([0-9]*).mp3
         */
        this.pattern = Pattern.compile(patternStringBuilder);
    }

    /**
     * Stores the cachedSong in the cache.
     * If the cachedSong already existed, it will be replaced.
     * @param cachedSong The cachedSong to store in the cache.
     * @return Returns a <code>Song</code> object that contains the URI of the file.
     */
    public Song store(CachedSong cachedSong) {
        Song song = null;

        try(FileOutputStream fos = new FileOutputStream(this.getFullSongPath(cachedSong))) {
            fos.write(cachedSong.data);
            song = new Mp3Song(this.getFullSongPath(cachedSong));
        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            this.logger.error("Failed saving the cachedSong (Title: " + cachedSong.title + ")", e);
        }

        return song;
    }

    /**
     * Checks, if there exists a cachedSong (already cached), like the cachedSong in the parameter.
     * @param cachedSong The cachedSong to check for existence.
     * @return Returns true, if the cachedSong exists. False if the cachedSong was not found in the cache.
     */
    public boolean exists(CachedSong cachedSong) {
        Path p = this.tempFolderPath.resolve(this.calculateSongFileName(cachedSong));
        return p.toFile().exists();
    }

    public boolean exists(int hash) {
        Path p = this.tempFolderPath.resolve(this.calculateSongFileName(hash));
        return p.toFile().exists();
    }

    /**
     * Retrieves the <code>Song</code> object from the hash in the parameters.
     * @param hash The cachedSong to retrieve
     * @return Returns a <code>Song</code> object that contains the URI of the file.
     */
    public Song retrieve(int hash) {
        Song song = null;

        if(this.exists(hash)) {
            try {
                song = new Mp3Song(this.getFullSongPath(hash));
            } catch (InvalidDataException | IOException | UnsupportedTagException e) {
                this.logger.error("Failed retrieving the song (Hash = " + hash + ")", e);
            }
        }

        return song;
    }

    /**
     * Calculates the song file name (only the name, not a full path) using the <code>hash</code> of the <code>Song</code>.
     * @param song The <code>song</code> object.
     * @return Returns the filename of the <code>Song</code> object.
     */
    private String calculateSongFileName(CachedSong song) {
        return PREFIX + song.hashCode() + SUFFIX;
    }

    private String calculateSongFileName(int hash) {
        return PREFIX + hash + SUFFIX;
    }

    /**
     * Returns the full song path, including the song name and extension.
     * The song path is just calculated. <strong>The file must not exist.</strong>
     * @param song The song to get the path from.
     * @return Returns the full song path as a string.
     */
    private String getFullSongPath(CachedSong song) {
        Path p = this.tempFolderPath.resolve(this.calculateSongFileName(song));
        return p.toString();
    }

    private String getFullSongPath(int hash) {
        Path p = this.tempFolderPath.resolve(this.calculateSongFileName(hash));
        return p.toString();
    }
}
