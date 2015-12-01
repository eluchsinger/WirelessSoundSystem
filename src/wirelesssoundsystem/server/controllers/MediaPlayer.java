package wirelesssoundsystem.server.controllers;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Created by Esteban Luchsinger on 01.12.2015.
 */
public interface MediaPlayer {
    void play();
    void pause();
    void stop();
    boolean isPlaying();
    SimpleBooleanProperty getIsPlayingProperty();

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

    void nextTrack();
    void previousTrack();

    void getCurrentTrack();
    SimpleObjectProperty<?> getCurrentTrackProperty();

    void getVolume();
    void setVolume();
    SimpleDoubleProperty getVolumeProperty();

}
