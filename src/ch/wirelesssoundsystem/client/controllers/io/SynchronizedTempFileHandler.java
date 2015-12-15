package ch.wirelesssoundsystem.client.controllers.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

/**
 * Created by Esteban Luchsinger on 15.12.2015.
 * This class handles the File handling.
 * The file can be handled from a different thread. Tough, the SynchronizedTempFileHandler must be
 * in the thread using it!
 */
public class SynchronizedTempFileHandler {
    private final File file;
    private boolean isOpen;

    private FileChannel fileChannel;
    private FileLock lock;


    /**
     * Opens or creates the file.
     * The file will be locked for writing, but allowed to read while this class
     * has a handle on it (isOpen).
     */
    public SynchronizedTempFileHandler() throws IOException {

        String tmpDir = System.getProperty("java.io.tmpdir");

        this.file = File.createTempFile("wss", ".mp3", new File(tmpDir));
        System.out.println("Created TempFile: " + this.file.getAbsolutePath());


        if(this.file.exists()){
            this.file.delete();
        }

        this.file.createNewFile();
//        this.file.deleteOnExit();
    }

    public void open() throws IOException, OverlappingFileLockException {

        this.fileChannel = new RandomAccessFile(file, "rw").getChannel();
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

    public void close() throws IOException {

        if(this.isOpen){
            if(this.lock != null){
                this.lock.release();
            }

            if(this.fileChannel != null){
                this.fileChannel.close();
            }

            this.isOpen = false;
        }

    }

    public void writeData(byte[] data) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        this.fileChannel.write(buffer);
    }


    public boolean isOpen(){
        return this.isOpen;
    }
}
