package wirelesssoundsystem.server.models.songs;


import com.mpatric.mp3agic.*;

import java.io.IOException;

/**
 * Created by eluch on 30.11.2015.
 */
public class Mp3Song implements Song {
    private final String extension = "mp3";
    private Mp3File file;

    public Mp3Song(String path) throws InvalidDataException, IOException, UnsupportedTagException {
        this.file = new Mp3File(path);
    }

    @Override
    public String getTitle() {
        return file.getId3v1Tag().getTitle();
    }

    @Override
    public String getExtension(){
        return this.extension;
    }
}
