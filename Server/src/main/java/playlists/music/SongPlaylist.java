package playlists.music;

import playlists.Playlist;
import models.songs.Song;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by Esteban Luchsinger on 03.12.2015.
 */
public class SongPlaylist implements Playlist<Song> {
    private ObservableList<Song> playlist;

    public SongPlaylist(){
        this(null);
    }

    public SongPlaylist(Collection<Song> songs){
        this.playlist = FXCollections.observableArrayList(songs);
    }

    @Override
    public ObservableList<Song> getTracks() {
        return null;
    }

    @Override
    public Song getTrack(int index) {
        return this.playlist.get(index);
    }

    /**
     * Finds a track
     *
     * @param track to find.
     * @return Returns the found track or null, if none were found.
     */
    @Override
    public Song getTrack(Song track) {
        Optional<Song> maybe = this.playlist.stream()
                .filter(s -> s.equals(track))
                .findFirst();

        return maybe.orElse(null);
    }

    /**
     * Adds a track.
     *
     * @param track track to add
     */
    @Override
    public void addTrack(Song track) {
        this.playlist.add(track);
    }

    /**
     * Removes a track.
     *
     * @param track track to remove.
     */
    @Override
    public void removeTrack(Song track) {
        this.playlist.remove(track);
    }
}
