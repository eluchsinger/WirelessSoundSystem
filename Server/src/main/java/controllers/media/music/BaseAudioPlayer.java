package controllers.media.music;

import javafx.beans.property.*;
import javafx.util.Duration;
import viewmodels.songs.PlayableSong;

import java.util.List;

/**
 * <pre>
 * Created by Esteban Luchsinger on 14.04.2016.
 * This is a base class for an audio player. It provides the needed properties.
 * </pre>
 */
public abstract class BaseAudioPlayer implements controllers.media.MediaPlayer<PlayableSong> {

    private BooleanProperty isPlaying;
    private ObjectProperty<Duration> currentMediaTime;
    private StringProperty currentMediaTimeString;
    private ObjectProperty<Duration> totalMediaDuration;
    private ObjectProperty<PlayableSong> currentTrack;
    private DoubleProperty volume;

    private List<PlayableSong> playlist;

    public BaseAudioPlayer(List<PlayableSong> playlist) {

        // Init properties
        this.isPlaying = new SimpleBooleanProperty();
        this.currentMediaTime = new SimpleObjectProperty<>();
        this.currentMediaTimeString = new SimpleStringProperty();
        this.totalMediaDuration = new SimpleObjectProperty<>();
        this.volume = new SimpleDoubleProperty(.5);
        this.currentTrack = new SimpleObjectProperty<>();

        this.setPlaylist(playlist);
    }


    @Override
    public BooleanProperty isPlayingProperty() {
        return isPlaying;
    }

    public boolean isPlaying() { return this.isPlayingProperty().get(); }

    public Duration getCurrentMediaTime() {
        return currentMediaTimeProperty().get();
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
        return totalMediaDurationProperty().get();
    }

    public ObjectProperty<Duration> totalMediaDurationProperty() {
        return totalMediaDuration;
    }

    public void setTotalMediaDuration(Duration totalMediaDuration) {
        this.totalMediaDurationProperty().set(totalMediaDuration);
    }

    @Override
    public PlayableSong getCurrentTrack() {
        return currentTrackProperty().get();
    }

    @Override
    public ObjectProperty<PlayableSong> currentTrackProperty() {
        return currentTrack;
    }

    public void setCurrentTrack(PlayableSong currentTrack) {
        this.currentTrackProperty().set(currentTrack);
    }

    @Override
    public PlayableSong getNextTrack() {
        if(this.getCurrentTrack() != null) {
            int currentIndex = this.getPlaylist().indexOf(this.getCurrentTrack());

            if((this.getPlaylist().size() - 1) > currentIndex) {
                return this.getPlaylist().get(currentIndex + 1);
            }
        }
        return null;
    }

    @Override
    public boolean playNextTrack() {
        if(this.getNextTrack() != null) {
            this.stop();
            this.play(this.getNextTrack());
            return true;
        }

        return false;
    }

    @Override
    public PlayableSong getPreviousTrack() {
        if(this.getCurrentTrack() != null) {
            int currentIndex = this.getPlaylist().indexOf(this.getCurrentTrack());

            if(currentIndex > 0) {
                return this.getPlaylist().get(currentIndex - 1);
            }
        }

        return null;
    }

    @Override
    public boolean playPreviousTrack() {
        if(this.getPreviousTrack() != null) {
            this.stop();
            this.play(this.getPreviousTrack());
            return true;
        }

        return false;
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
    protected void setPlaylist(List<PlayableSong> playlist) {
        this.playlist = playlist;
    }

    /**
     * Retrieves the playlist for this AudioPlayer.
     * @return The current playlist.
     */
    public List<PlayableSong> getPlaylist() {
        return this.playlist;
    }
}
