package ch.wirelesssoundsystem.server.controllers.media;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Created by Esteban Luchsinger on 01.12.2015.
 */
public interface MediaPlayer<T> {
    void play();
    void play(T track);
    void pause();
    void stop();
    boolean isPlaying();
    ReadOnlyBooleanProperty isPlayingProperty();

    /**
     * Toggles playing.
     * If it is playing, it pauses.
     * If it is paused, it plays.
     */
    default void togglePlay(){
        if(this.isPlaying())
            this.pause();
        else
            this.play();
    }

    T getNextTrack();
    T getPreviousTrack();

    T getCurrentTrack();
    SimpleObjectProperty<T> currentTrackProperty();

    void getVolume();
    void setVolume();
    SimpleDoubleProperty volumeProperty();
}
