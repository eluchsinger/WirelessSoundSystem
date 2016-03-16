package controllers.io.cache.file;

import controllers.io.cache.CacheService;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 15.12.2015.
 * This class handles the handling of the cache that can grow and shrink dynamically.
 */
public class DynamicFileCacheService implements FileCacheService {
    private File file;
    private boolean isOpen;

    private FileChannel fileChannel;
    private FileLock lock;

    /**
     * True, if the cache is already used.
     * In this case, you should use a new cache for a new song.
     */
    private volatile boolean cacheUsed;

    /**
     * Opens or creates the file.
     * The file will be locked for writing, but allowed to read while this class
     * has a handle on it (isOpen).
     */
    private DynamicFileCacheService() {
        this.createFile();
    }

    private void createFile() {
        String tmpDir = System.getProperty("java.io.tmpdir");

        try {
            this.file = File.createTempFile("wss", ".mp3", new File(tmpDir));

            System.out.println("Created TempFile: " + this.file.getAbsolutePath());

            this.file.createNewFile();
            this.fileChannel = new RandomAccessFile(file, "rw").getChannel();
            this.file.deleteOnExit();
            this.cacheUsed = false;
        } catch (IOException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
                    "Fehler beim Erstellen des Temp Files.",
                    e);
        }
    }

    /**
     * Resets the cache, if the cache is already used.
     */
    public void reset() {
        if(this.cacheUsed)
            this.createFile();
    }

    private void open() throws IOException, OverlappingFileLockException {
        try {
            this.lock = fileChannel.tryLock();
        }
        catch(OverlappingFileLockException locked){
            throw locked;
        }

        if(lock == null){
            throw new IOException("Could not get the exclusive lock of the file.");
        }

        this.isOpen = true;
    }

    private void close() throws IOException {

        if(this.isOpen){
            if(this.lock != null){
                this.lock.release();
            }
            this.isOpen = false;
        }
    }

    public void writeData(byte[] data) throws IOException {
        if(data.length > 0) try {
            this.open();
            ByteBuffer buffer = ByteBuffer.wrap(data);
            this.fileChannel.write(buffer);
        } catch (Exception ignore) {
        } finally {
            try {
                this.close();
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
            this.cacheUsed = true;
        }
    }


    public boolean isOpen(){
        return this.isOpen;
    }

    public File getTempFile(){
        return this.file;
    }

    /**
     * @return Returns the absolute path of cache.
     */
    @Override
    public String getAbsoluteFilePath() throws Exception {
        return this.file.getAbsolutePath();
    }

    /**
     * @return Returns the URL of the cache.
     */
    @Override
    public URI getFileURI() {
        return this.file.toURI();
    }
}
