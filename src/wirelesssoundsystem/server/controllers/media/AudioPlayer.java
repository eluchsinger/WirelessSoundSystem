package wirelesssoundsystem.server.controllers.media;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import wirelesssoundsystem.server.models.songs.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by eluch on 01.12.2015.
 */
public class AudioPlayer implements wirelesssoundsystem.server.controllers.media.MediaPlayer {

    // WATCH OUT! JavaFx MediaPlayer!
    private MediaPlayer mediaPlayer;

    private List<Song> playlist;
    private Song lastPlayed;

    public AudioPlayer(){
        // Call parameterized constructor with a new list.
        this(new ArrayList<Song>());
    }

    public AudioPlayer(Collection<Song> songs){
        this.playlist = new ArrayList<>(songs);
    }

    @Override
    public void play() {
        if(this.mediaPlayer != null
                && this.mediaPlayer.getStatus() != MediaPlayer.Status.DISPOSED
                && this.mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING){

            this.mediaPlayer.play();

        } else if(this.playlist.size() > 0) {

            Song nextSong = this.findNextSong();
            if(nextSong != null){
                Media media = this.createMediaFromSong(nextSong);
                this.mediaPlayer = new MediaPlayer(media);
                this.mediaPlayer.play();
            }
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public SimpleBooleanProperty getIsPlayingProperty() {
        return null;
    }

    @Override
    public void nextTrack() {

    }

    @Override
    public void previousTrack() {

    }

    @Override
    public void getCurrentTrack() {

    }

    @Override
    public SimpleObjectProperty<?> getCurrentTrackProperty() {
        return null;
    }

    @Override
    public void getVolume() {

    }

    @Override
    public void setVolume() {

    }

    @Override
    public SimpleDoubleProperty getVolumeProperty() {
        return null;
    }

    private Media createMediaFromSong(Song song){

        // Have to create a temporary file to convert the path to a URI.
        File tempFile = new File(song.getPath());

        Media media = new Media(tempFile.toURI().toString());
        return media;
    }

    /**
     * Finds the next song using the lastPlayed song.
     *
     * @return Returns the next song in the playlist, returns the first song if a next song could not be found.
     */
    private Song findNextSong(){
        Optional<Song> optSong = this.playlist.stream().filter(s -> s.equals(this.lastPlayed)).findFirst();

        if(optSong.isPresent()){
            return optSong.get();
        }
        else{
            System.out.println("Next Song was not found! Using First song!");
            return this.playlist.get(0);
        }
    }
}
