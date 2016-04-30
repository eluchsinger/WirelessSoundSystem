package controllers.io.cache.file;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
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
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Pattern;

/**
 * <pre>
 * Created by Esteban Luchsinger on 25.04.2016.
 * A cache manager for Songs.
 * The cache creates a file for the song. The file name concatenates a prefix,
 * the hash of the song and the file format.
 * </pre>
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

    private final static String REGEX_HASH_GROUP_NAME = "HASH";
    /**
     * Regex part for the capturing of the HASH-CODE of the song.
     */
    private final static String REGEX_HASH_GROUP = "(?<" + REGEX_HASH_GROUP_NAME + ">[0-9]*)";

    /**
     * The preferred maximum cache size (<strong>in Megabytes</strong>).
     * @implNote "preferred" means, the cache will try to stay inside the maximum size,
     * but when caching a new song, it might be over that size briefly.
     */
    private final static long PREFERRED_MAX_CACHE_SIZE = 30;
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

        this.tempFolderPath = Paths.get(System.getProperty("java.io.tmpdir"), "wss", "cache");

        this.logger.info("Started cache at " + this.tempFolderPath.toString());

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
     * <strong>Every time a song is cached, a cleanup will be done.</strong>
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

        cleanCache();

        return song;
    }

    /**
     * Checks, if there exists a cachedSong (already cached), like the cachedSong in the parameter.
     * @param cachedSong The cachedSong to check for existence.
     * @return Returns true, if there is a cached <code>Song</code>. False if the cachedSong was not found in the cache.
     */
    public boolean exists(CachedSong cachedSong) {
        Path p = this.tempFolderPath.resolve(this.calculateSongFileName(cachedSong));
        return p.toFile().exists();
    }

    /**
     * Checks, if there exists a <code>Song</code> already in the cache, which corresponds to the hashCode in the parameters.
     * @param hash The hashCode corresponding to the searched song.
     * @return Returns true, if there is a cached <code>Song</code> with the corresponding hashCode. False if there is no cached <code>Song</code> with corrseponding hashCode.
     */
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

    /**
     * Calculates the song file name (only the name, not a full path) using the <code>hash</code> in the parameter.
     * @param hash The hashCode of the song.
     * @return Returns the filename of the corresponding to the hashCode in the parameter.
     */
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

    /**
     * Returns the full song path, including the song name and extension.
     * The song path is calculated. <strong>The file must not exist.</strong>
     * @param hash The hashCode of the song path.
     * @return Returns the full song path as a string.
     */
    private String getFullSongPath(int hash) {
        Path p = this.tempFolderPath.resolve(this.calculateSongFileName(hash));
        return p.toString();
    }

    /**
     * Cleans the cache according to the field values in the <code>SongCacheManager</code>
     * @return Returns true, if the method had to do any cleanup.
     */
    private boolean cleanCache() {
        boolean cleanupDone = false;

        File tempFolder = this.tempFolderPath.toFile();

        // Get the current size of the cache in MB.
        long currentSizeOfCache = this.getFolderSize(tempFolder) / 1000 / 1000;

        if(currentSizeOfCache > PREFERRED_MAX_CACHE_SIZE) {
            // Only do these operations, if really needed (might be expensive).
            File[] cacheFiles = tempFolder.listFiles();
            if(cacheFiles != null && cacheFiles.length > 0) {
                Queue<File> cleanupQueue = new ArrayDeque<>(cacheFiles.length);
                Arrays.sort(cacheFiles, (o1, o2) -> Long.valueOf(o1.lastModified()).compareTo(o2.lastModified()));

                // Copy files into the queue
                Collections.addAll(cleanupQueue, cacheFiles);

                do {
                    File f;
                    // Always delete the oldest file in the cache first.
                    if((f = cleanupQueue.poll()) != null) {
                        //noinspection ResultOfMethodCallIgnored
                        f.delete();
                        cleanupDone = true;
                    } else {
                        // If the queue is empty, we will have to quit this loop.
                        this.logger.warn("There was an error cleaning up the cache");
                        break;
                    }

                    // Get the current size of the cache in MB.
                    currentSizeOfCache = tempFolder.length() / 1000 / 1000;
                }
                while (currentSizeOfCache > PREFERRED_MAX_CACHE_SIZE);
            }
        }

        if(cleanupDone) {
            this.logger.info("Cleaned up the cache!");
        }

        return cleanupDone;
    }

    /**
     * Retrieves the size of a folder.
     * @param folder Folder to search in.
     * @return Returns the length of the folder in bytes.
     */
    private long getFolderSize(File folder) {
        long length = 0;

        File[] filesInFolder = folder.listFiles();

        if(filesInFolder != null) {
            for (File file : filesInFolder) {
                if (file.isFile()) {
                    length += file.length();
                } else {
                    // Recursive
                    length += getFolderSize(file);
                }
            }
        }

        return length;
    }

    public List<Integer> getCachedHashes() {

        List<Integer> hashes = null;
        File[] filesInFolder = this.tempFolderPath.toFile().listFiles();

        if(filesInFolder != null) {
            hashes = new ArrayList<>(filesInFolder.length);
            for(File file : filesInFolder) {

                String hashString = this.pattern.matcher(file.getName()).group("HASH");
                if(hashString != null) {
                    try {
                        hashes.add(Integer.valueOf(hashString));
                    }
                    catch(NumberFormatException e) {
                        this.logger.warn("Couldn't get the hashCode of the file " + file.getName());
                    }
                }
            }
        }

        return hashes;
    }
}
