package wirelesssoundsystem.server.models.songs;


import com.mpatric.mp3agic.*;

import java.io.IOException;

/**
 * Created by eluch on 30.11.2015.
 */
public class Mp3Song implements Song {
    private final String extension = "mp3";
    private Mp3File file;

    /**
     * Constructor overrides Default constructor
     * @param path Path of the MP3-File
     * @throws InvalidDataException
     * @throws IOException
     * @throws UnsupportedTagException
     */
    public Mp3Song(String path) throws InvalidDataException, IOException, UnsupportedTagException {
        this.file = new Mp3File(path);
    }

    @Override
    public String getTitle() {
        if(this.file.hasId3v1Tag())
            return file.getId3v1Tag().getTitle();
        else if(this.file.hasId3v2Tag())
            return file.getId3v2Tag().getTitle();
        else
            return "unknown title";
    }

    @Override
    public String getArtist(){
        if(this.file.hasId3v1Tag())
            return file.getId3v1Tag().getArtist();
        else if(this.file.hasId3v2Tag())
            return file.getId3v2Tag().getArtist();
        else
            return "unknown artist";
    }

    @Override
    public String getExtension(){
        return this.extension;
    }

    @Override
    public String getPath(){
        return this.file.getFilename();
    }

    @Override
    public String toString(){
        return this.getTitle() + " - " + this.getArtist();
    }
}
