package ch.wirelesssoundsystem.server.controllers.media.music;

import ch.wirelesssoundsystem.server.models.songs.Song;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.util.Optional;

/**
 * Created by eluch on 01.12.2015.
 */
public class AudioPlayer implements ch.wirelesssoundsystem.server.controllers.media.MediaPlayer<Song> {

    /**
     * Access only with the Accessor-Methods!
     */
    private MediaPlayer mediaPlayer;

    private ReadOnlyBooleanProperty isPlaying;
    private ReadOnlyObjectProperty<Duration> currentMediaTime;
    private ReadOnlyObjectProperty<Duration> totalMediaDuration;

    private ObservableList<Song> playlist;
    private Song lastPlayed;

    public AudioPlayer(){
        // Call parameterized constructor with a new list.
        this(FXCollections.observableArrayList());
    }

    public AudioPlayer(ObservableList<Song> songs){
        this.playlist = songs;

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
    public void play(Song track){

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
        if(this.getMediaPlayer() == null || this.getMediaPlayer().getStatus() == null){
            return false;
        }

        switch(this.getMediaPlayer().getStatus()){
            case PLAYING:
                return true;
            default:
                return false;
        }
//        return this.isPlaying.getValue();
    }

    @Override
    public ReadOnlyBooleanProperty isPlayingProperty() {
        return this.isPlaying;
    }

    /**
     * Toggles playing.
     * If it is playing, it pauses.
     * If it is paused, it plays.
     */
    @Override
    public void togglePlay() {

    }

    @Override
    public Song getNextTrack() {
        return null;
    }

    @Override
    public Song getPreviousTrack() {
        return null;
    }

    @Override
    public Song getCurrentTrack() {
        return null;
    }

    @Override
    public SimpleObjectProperty<Song> currentTrackProperty() {
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
