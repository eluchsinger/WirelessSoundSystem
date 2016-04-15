package controllers.media.music;

import controllers.networking.streaming.music.MusicStreamController;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import models.songs.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by Esteban Luchsinger on 13.04.2016.
 * The simple audio player enhanced for use in network.
 * The network clients are controlled by the required music stream controller.
 */
public class NetworkAudioPlayer implements controllers.media.MediaPlayer<Song> {
    //region Members
    private final Logger logger;

    private final MusicStreamController musicStreamController;
    private MediaPlayer mediaPlayer;

    //endregion Members

    //region Constructors

    public NetworkAudioPlayer(MusicStreamController musicStreamController) {
        this(FXCollections.observableArrayList(), musicStreamController);
    }

    public NetworkAudioPlayer(ObservableList<Song> songs, MusicStreamController musicStreamController) {
        this.logger = LoggerFactory.getLogger(this.getClass());

        this.musicStreamController = musicStreamController;
    }

    //endregion Constructors

    //region Getters & Setters

    /**
     * Initializes the media player for the song in the parameters.
     * If the current MediaPlayer already uses the song in the parameters, it wont be reinitialized.
     * If the current MediaPlayer is playing, it will be stopped and disposed.
     * @param song Song for the MediaPlayer
     * @return the MediaPlayer for the desired song.
     */
    private MediaPlayer getMediaPlayer(Song song) {
        if(this.mediaPlayer == null) {
            this.mediaPlayer = new MediaPlayer(this.createMediaFromSong(song));
        }

        return this.mediaPlayer;
    }

    /**
     * Retrieves the current MediaPlayer with whatever song it may have initialized (null possible).
     * @return Current MediaPlayer (null possible).
     */
    private MediaPlayer getCurrentMediaPlayer() {
        return this.mediaPlayer;
    }

    //endregion Getters & Setters

    //region Private methods

    /**
     * Creates a JavaFX Media Object from a Song object.
     * This method is not in the shared code module, because it uses JavaFX.
     * @param song The song object containing the path.
     * @return Returns a Media object with the Song.
     */
    private Media createMediaFromSong(Song song) {

        // Have to create a temporary file to convert the path to a URI.
        File tempFile = new File(song.getPath());

        Media media = new Media(tempFile.toURI().toString());
        return media;
    }

    //endregion Private methods

    //region MediaPlayer Interface
    /**
     * Plays a track and tries to resume it, if desired.
     *
     * @param track     Track to play.
     * @param tryResume Set this true, if the track should be resumed (only if possible).
     */
    @Override
    public void play(Song track, boolean tryResume) {

    }

    /**
     * Plays a track.
     *
     * @param track The track to play.
     */
    @Override
    public void play(Song track) {
    }

    /**
     * Pauses the currently played track.
     * If there is no currently playing track, doesn't do anything.
     */
    @Override
    public void pause() {

    }

    /**
     * Stops and disposes the player, regardless of the current status.
     * Can be used always, without checking the status.
     */
    @Override
    public void stop() {

        // Check, if the MediaPlayer needs to get disposed first.
        if (this.mediaPlayer != null) {
            this.mediaPlayer.dispose();
            this.mediaPlayer = null;
        }
    }

    /**
     * @return Returns true if the player is currently playing.
     */
    @Override
    public boolean isPlaying() {
        return false;
    }

    /**
     * Property to bind the isPlaying value.
     *
     * @return Returns the property to the corresponding isPlaying field.
     */
    @Override
    public BooleanProperty isPlayingProperty() {
        return null;
    }

    /**
     * Tries to find the next track.
     *
     * @return Returns the next track on the playlist, or null if it wasn't found.
     */
    @Override
    public Song getNextTrack() {
        return null;
    }

    /**
     * Tries to find the previous track.
     *
     * @return Returns the previous track on the playlist, or null if it wasn't found.
     */
    @Override
    public Song getPreviousTrack() {
        return null;
    }

    /**
     * @return Returns the current track.
     */
    @Override
    public Song getCurrentTrack() {
        return null;
    }

    /**
     * @return Returns the property with the value of the current track.
     */
    @Override
    public SimpleObjectProperty<Song> currentTrackProperty() {
        return null;
    }

    /**
     * @return Returns the current media time ObjectProperty. This property shows the current progress of the currently playing track.
     */
    @Override
    public ObjectProperty<Duration> currentMediaTime() {
        return null;
    }

    /**
     * @return Returns the total duration of the currently playing media.
     */
    @Override
    public ObjectProperty<Duration> totalMediaDuration() {
        return null;
    }

    /**
     * @return Returns the current volume value.
     */
    @Override
    public double getVolume() {
        return 0;
    }

    /**
     * Sets the current volume value.
     * Bigger value = Louder!
     *
     * @param value New Value
     */
    @Override
    public void setVolume(double value) {

    }

    /**
     * @return Returns the property containing the current volume. (Write and read possible!)
     */
    @Override
    public DoubleProperty volumeProperty() {
        return null;
    }

    //endregion MediaPlayer Interface
}
