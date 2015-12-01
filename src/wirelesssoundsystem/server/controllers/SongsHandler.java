package wirelesssoundsystem.server.controllers;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
import wirelesssoundsystem.server.models.songs.Mp3Song;
import wirelesssoundsystem.server.models.songs.Song;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Esteban on 30.11.2015.
 */
public class SongsHandler {

    /**
     * Loads all songs from a directory.
     * @param path Path to the directory
     * @return Returns an array of songs.
     */
    public List<Song> loadSongsFromDir(String path){
        File f = new File(path);

        // List all files which end with .mp3
        File[] files = f.listFiles((dir, name) -> name.endsWith(".mp3"));

        if(files.length > 0){
            for(File file : files) {
                System.out.println(file.getName());
            }
        }
        else{
            System.out.println("No Music Files found.");
        }

        System.out.println("Reading files...");

        List<Song> songList = new ArrayList<>();
        // Transform all songs into Song.
        for(File file : files){
            try {
                Song song = new Mp3Song(file.getPath());
                songList.add(song);
                System.out.println("Song: " + song.getTitle());

            } catch (InvalidDataException e) {
                System.out.println("SongsHandler.java: InvalidDataException");
            } catch (IOException e) {
                System.out.println("SongsHandler.java: IOException");
            } catch (UnsupportedTagException e) {
                System.out.println("SongsHandler.java: UnsupportedTagException");
            }
        }

        return songList;
    }
}
