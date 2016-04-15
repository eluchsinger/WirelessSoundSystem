package controllers.media.music;

import javafx.beans.property.*;
import javafx.util.Duration;
import models.songs.Song;

import java.util.List;

/**
 * Created by Esteban Luchsinger on 14.04.2016.
 * This is a base class for an audio player. It provides the needed properties.
 */
public abstract class BaseAudioPlayer implements controllers.media.MediaPlayer<Song> {

    private BooleanProperty isPlaying;
    private ObjectProperty<Duration> currentMediaTime;
    private StringProperty currentMediaTimeString;
    private ObjectProperty<Duration> totalMediaDuration;
    private DoubleProperty volume;

    private List<Song> playlist;

    public BaseAudioPlayer(List<Song> playlist) {

        // Init properties
        this.isPlaying = new SimpleBooleanProperty();
        this.currentMediaTime = new SimpleObjectProperty<>();
        this.currentMediaTimeString = new SimpleStringProperty();
        this.totalMediaDuration = new SimpleObjectProperty<>();
        this.volume = new SimpleDoubleProperty();

        this.setPlaylist(playlist);
    }


    @Override
    public BooleanProperty isPlayingProperty() {
        return isPlaying;
    }

    public boolean isPlaying() { return this.isPlayingProperty().get(); }

    public Duration getCurrentMediaTime() {
        return currentMediaTime.get();
    }

    public ObjectProperty<Duration> currentMediaTimeProperty() {
        return currentMediaTime;
    }

    public String getCurrentMediaTimeString() {
        return currentMediaTimeString.get();
    }

    public StringProperty currentMediaTimeStringProperty() {
        return currentMediaTimeString;
    }

    public Duration getTotalMediaDuration() {
        return totalMediaDuration.get();
    }

    public ObjectProperty<Duration> totalMediaDurationProperty() {
        return totalMediaDuration;
    }

    public void setTotalMediaDuration(Duration totalMediaDuration) {
        this.totalMediaDuration.set(totalMediaDuration);
    }

    @Override
    public double getVolume() {
        return volume.get();
    }

    @Override
    public DoubleProperty volumeProperty() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume.set(volume);
    }

    /**
     * Sets the playlist of songs.
     * @param playlist new playlist
     */
    protected void setPlaylist(List<Song> playlist) {
        this.playlist = playlist;
    }

    /**
     * Retrieves the playlist for this AudioPlayer.
     * @return The current playlist.
     */
    public List<Song> getPlaylist() {
        return this.playlist;
    }
}
