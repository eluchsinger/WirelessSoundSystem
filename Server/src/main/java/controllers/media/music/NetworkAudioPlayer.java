package controllers.media.music;

import controllers.clients.ClientController;
import controllers.media.MediaPlayer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import models.songs.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Esteban Luchsinger on 13.04.2016.
 * The simple audio player enhanced for use in network.
 */
public class NetworkAudioPlayer implements MediaPlayer<Song> {
    private final Logger logger;

    private final ClientController clientController;

    public NetworkAudioPlayer(ClientController clientController) {
        this(FXCollections.observableArrayList(), clientController);
    }

    public NetworkAudioPlayer(ObservableList<Song> songs, ClientController clientController) {
        this.logger = LoggerFactory.getLogger(this.getClass());

        this.clientController = clientController;
    }

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
}
