package ch.wirelesssoundsystem.server.controllers.media;

import ch.wirelesssoundsystem.server.models.songs.Song;
import javafx.beans.property.*;
import javafx.util.Duration;

/**
 * Created by Esteban Luchsinger on 01.12.2015.
 */
public interface MediaPlayer<T> {
    void play(T track, boolean tryResume);
    void play(T track);
    void pause();

    /**
     * Stops and disposes the player, regardless of the current status.
     * Can be used always, without checking the status.
     */
    void stop();
    boolean isPlaying();
    BooleanProperty isPlayingProperty();

    /**
     * Toggles playing.
     * If it is playing, it pauses.
     * If it is paused, it plays.
     */
    default void togglePlay(){
        if(this.isPlaying())
            this.pause();
        else
            this.play(this.getCurrentTrack(), true);
    }

    T getNextTrack();
    T getPreviousTrack();

    T getCurrentTrack();
    SimpleObjectProperty<T> currentTrackProperty();

    StringProperty currentMediaTimeString();
    ObjectProperty<Duration> currentMediaTime();

    double getVolume();
    void setVolume(double value);
    DoubleProperty volumeProperty();
}
