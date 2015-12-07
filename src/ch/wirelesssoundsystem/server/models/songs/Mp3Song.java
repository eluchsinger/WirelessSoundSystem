package ch.wirelesssoundsystem.server.models.songs;


import com.mpatric.mp3agic.*;

import java.io.IOException;

/**
 * Created by eluch on 30.11.2015.
 */
public class Mp3Song implements Song, Comparable<Song> {
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

    /**
     * Compares the Title of both songs.
     * If it is the same title, they are the same song.
     * @param otherSong
     * @return
     */
    @Override
    public int compareTo(Song otherSong) {

        // Compare to the title.
        int result = this.getTitle().compareTo(otherSong.getTitle());

        // Todo: Check if I should compare other stuff too.

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mp3Song)) return false;

        Mp3Song mp3Song = (Mp3Song) o;

        return this.getTitle().equals(mp3Song.getTitle())
                && this.getPath().equals(mp3Song.getPath());
    }

    @Override
    public int hashCode() {
        int result = this.getTitle() != null ? this.getTitle().hashCode() : 0;
        result = 31 * result + (this.getPath() != null ? this.getPath().hashCode() : 0);
        return result;
    }
}
