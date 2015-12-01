package wirelesssoundsystem.server.controllers;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Created by Esteban Luchsinger on 01.12.2015.
 */
public interface MediaPlayer {
    public void play();
    public void pause();
    public void stop();
    public boolean isPlaying();
    public SimpleBooleanProperty getIsPlayingProperty();

    /**
     * Toggles playing.
     * If it is playing, it pauses.
     * If it is paused, it plays.
     */
    default public void togglePlay(){
        if(this.isPlaying())
            this.pause();
        else
            this.play();
    }

    public void nextTrack();
    public void previousTrack();

    public void getCurrentTrack();
    public SimpleObjectProperty<?> getCurrentTrackProperty();

    public void getVolume();
    public void setVolume();
    public SimpleDoubleProperty getVolumeProperty();

}
