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

    /**
     * Plays the selected trackc, if found.
     * @param track The track that should be played. (Must not be the same reference as in the playlist).
     * @param tryResume Tries to resume the song, if it is paused.
     */
    public void play(Song track, boolean tryResume){

        // Only do something if the playlist is not empty.
        if(this.playlist.size() > 0) {
            boolean needNewMediaPlayer = true;
            if (this.getMediaPlayer() != null
                    && this.getMediaPlayer().getStatus() != MediaPlayer.Status.DISPOSED
                    && this.getMediaPlayer().getStatus() != MediaPlayer.Status.PLAYING) {

                // Check if the currentTrack is the same track and paused (and tryResume == true).
                if (tryResume && this.getCurrentTrack() != null && this.getCurrentTrack().equals(track)) {

                    // Resume the paused track.
                    this.getMediaPlayer().play();
                    needNewMediaPlayer = false;
                }
            }

            // There has to be a new MediaPlayer.
            if (needNewMediaPlayer) {
                Song song = this.findSongFromPlaylist(track);

                if(song != null){
                    Media media = this.createMediaFromSong(song);
                    this.setMediaPlayer(new MediaPlayer(media));
                    this.getMediaPlayer().play();
                }
            }
        }
    }

    @Override
    public void play(Song track){
        this.play(track, false);
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
    }

    @Override
    public ReadOnlyBooleanProperty isPlayingProperty() {
        return this.isPlaying;
    }

    /**
     * Toggles playing.
     * If it is playing, it pauses.
     * If it is paused, it plays.
     * Already possesses default implementation
     */
//    @Override
//    public void togglePlay() {
//
//    }

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
        if(this.getMediaPlayer() == null){
            return null;
        }
        else {
            String uri = this.getMediaPlayer().getMedia().getSource();

            // Todo: Evaluate parallelStream().
            Optional<Song> maybeSong = this.playlist.stream()
                    .filter(song -> new File(song.getPath()).toURI().toString().equals(uri))
                    .findFirst();


            // Return the present song or null.
            return maybeSong.orElse(null);
        }
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

    /**
     * Finds the reference to a song in the playlist.
     * @param song Song with external reference (not in the playlist).
     * @return Returns a reference to the equivalent song in the playlist. If no equal song was found in the playlist, return NULL.
     */
    private Song findSongFromPlaylist(Song song){

        // Todo: Evaluate parallelStream().
        return this.playlist.stream()
                .filter(s -> s.equals(song))
                .findFirst()
                .orElse(null);
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
