package controllers.io.cache.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <pre>
 * Created by Esteban Luchsinger on 16.03.2016.
 * The static cache service uses an immutable cache.
 * The cache can only be written once. If the cache is already used, and writeData
 * is called again, it overwrites all the data.
 * </pre>
 */
public class StaticFileCacheService implements FileCacheService {
    private final Logger logger;
    private final static String FILE_PREFIX = "wss";
    private final static String FILE_SUFFIX = ".mp3";

    private File rootFile = null;

    private final AtomicBoolean cacheWasUsed = new AtomicBoolean();

    /**
     * Default constructor
     * @throws IOException
     */
    public StaticFileCacheService() throws IOException {
        this.logger = LoggerFactory.getLogger(this.getClass());
        // Initialize path.
        String tempDir = System.getProperty("java.io.tmpdir");

        this.rootFile = File.createTempFile(FILE_PREFIX, FILE_SUFFIX, new File(tempDir));
    }

    /**
     * Writes the data into the cache.
     * (Synchronized: Thread-safe)
     *
     * @param data Data to write in the cache.
     * @throws IOException If the cache could not be expanded, an IOException is thrown.
     */
    @Override
    public synchronized void writeData(byte[] data) throws IOException {

        File file = null;
        
        try {
            this.reset();
            // Get a new file.
            file = this.getFile();

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(data);
            }
        }
        catch(Exception e) {
            if(file != null) {
                String message = "Error writing data into the buffer.\n" +
                        "Path=" + this.rootFile.getAbsolutePath();
                this.logger.error(message, e);
            }
            else {
                this.logger.error("Cache file is null when writing data", e);
            }
        }
    }

    /**
     * Resets the cache.
     * All data currently cached is lost.
     */
    @Override
    public synchronized void reset() {

        // Delete cache
        if(this.rootFile != null && this.rootFile.exists()) {

            //noinspection ResultOfMethodCallIgnored
            this.rootFile.delete();
        }
    }

    /**
     * Creates a temporary file in the java.io.tmpdir (TempDir).
     * @return Returns the new file.
     * @throws IOException Throws IOException if there was an error creating a file.
     */
    private File createFile() throws IOException {
        try {
            if(this.rootFile.exists()){
                this.rootFile.delete();
            }

            if(this.rootFile.createNewFile()) {
                this.logger.info("Created TempFile: " + this.rootFile.getAbsolutePath());
            }
            else {
                this.logger.info("File already exists: " + this.rootFile.getAbsolutePath());
            }

            this.rootFile.deleteOnExit();
            return this.rootFile;
        } catch (IOException e) {
            this.logger.error("Failed creating temp file", e);
            throw e;
        }
    }

    /**
     * Returns the new file and if it doesn't exist, it creates a new file.
     * @return Returns the cache file.
     */
    public File getFile() {
        try {
            if(!this.rootFile.exists()) {
                    this.createFile();
            }
        } catch (IOException e) {
            this.logger.error("Error getting file", e);
        }

        return this.rootFile;
    }

    /**
     * @return Returns the absolute path of cache.
     */
    @Override
    public String getAbsoluteFilePath() {
        return this.rootFile.getAbsolutePath();
    }

    /**
     * @return Returns the URL of the cache.
     */
    @Override
    public URI getFileURI() {
        return this.rootFile.toURI();
    }

    public void setCacheUsed() {
        this.cacheWasUsed.set(true);
    }

    public boolean isCacheUsed() {
        return this.cacheWasUsed.get();
    }
}