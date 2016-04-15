package controllers.media.music;

import javafx.beans.property.*;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import models.songs.Song;

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

    private MediaPlayer mediaPlayer;

    public BaseAudioPlayer() {

        // Init properties
        this.isPlaying = new SimpleBooleanProperty();
        this.currentMediaTime = new SimpleObjectProperty<>();
        this.currentMediaTimeString = new SimpleStringProperty();
        this.totalMediaDuration = new SimpleObjectProperty<>();
        this.volume = new SimpleDoubleProperty();
    }

    public boolean getIsPlaying() {
        return isPlaying.get();
    }

    @Override
    public BooleanProperty isPlayingProperty() {
        return isPlaying;
    }

    protected void setIsPlaying(boolean isPlaying) {
        this.isPlaying.set(isPlaying);
    }

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
}
