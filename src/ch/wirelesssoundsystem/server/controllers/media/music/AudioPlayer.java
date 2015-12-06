package ch.wirelesssoundsystem.server.controllers.media.music;

import javafx.beans.property.*;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import ch.wirelesssoundsystem.server.models.songs.Song;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by eluch on 01.12.2015.
 */
public class AudioPlayer implements ch.wirelesssoundsystem.server.controllers.media.MediaPlayer {

    /**
     * Access only with the Accessor-Methods!
     */
    private MediaPlayer mediaPlayer;

    private ReadOnlyBooleanProperty isPlaying;
    private ReadOnlyObjectProperty<Duration> currentMediaTime;
    private ReadOnlyObjectProperty<Duration> totalMediaDuration;

    private List<Song> playlist;
    private Song lastPlayed;

    public AudioPlayer(){
        // Call parameterized constructor with a new list.
        this(new ArrayList<>());
    }

    public AudioPlayer(Collection<Song> songs){
        this.playlist = new ArrayList<>(songs);

        this.isPlaying = new SimpleBooleanProperty();
        this.currentMediaTime = new SimpleObjectProperty<>();
        this.totalMediaDuration = new SimpleObjectProperty<>();
    }

    @Override
    public void play() {
        if(this.getMediaPlayer() != null
                && this.getMediaPlayer().getStatus() != MediaPlayer.Status.DISPOSED
                && this.getMediaPlayer().getStatus() != MediaPlayer.Status.PLAYING){

            this.getMediaPlayer().play();

        } else if(this.playlist.size() > 0) {

            Song nextSong = this.findNextSong();
            if(nextSong != null){
                Media media = this.createMediaFromSong(nextSong);
                this.setMediaPlayer(new MediaPlayer(media));
                this.getMediaPlayer().play();
            }
        }
    }

    @Override
    public void pause() {
        if(this.getMediaPlayer() != null){
            this.getMediaPlayer().pause();
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isPlaying() {
        return this.isPlaying.getValue();
    }

    @Override
    public ReadOnlyBooleanProperty isPlayingProperty() {
        return this.isPlaying;
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
    public SimpleObjectProperty<?> currentTrackProperty() {
        return null;
    }

    @Override
    public void getVolume() {

    }

    @Override
    public void setVolume() {

    }

    @Override
    public SimpleDoubleProperty volumeProperty() {
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

    public Duration getCurrentMediaTime() {
        return currentMediaTime.get();
    }

    public ReadOnlyObjectProperty<Duration> currentMediaTimeProperty() {
        return currentMediaTime;
    }

    public Duration getTotalMediaDuration() {
        return totalMediaDuration.get();
    }

    public ReadOnlyObjectProperty<Duration> totalMediaDurationProperty() {
        return totalMediaDuration;
    }

    private void setMediaPlayer(MediaPlayer mediaPlayer){
        this.mediaPlayer = mediaPlayer;

        if(this.getMediaPlayer() != null){
            this.currentMediaTime = this.getMediaPlayer().currentTimeProperty();
            this.totalMediaDuration = this.getMediaPlayer().totalDurationProperty();
        }
    }

    private MediaPlayer getMediaPlayer(){
        return this.mediaPlayer;
    }
}