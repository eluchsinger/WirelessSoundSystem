package controllers.media;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Duration;

/**
 * <pre>
 * Created by Esteban Luchsinger on 01.12.2015.
 * A media player interface built to handle different types of media (music/video).
 * </pre>
 */
public interface MediaPlayer<T> {

//    /**
//     * Plays a track and tries to resume it, if desired.
//     * @param track Track to play.
//     * @param tryResume Set this true, if the track should be resumed (only if possible).
//     */
//    void play(T track, boolean tryResume);

    /**
     * Plays a track.
     * @param track The track to play.
     */
    void play(T track);

    /**
     * Pauses the currently played track.
     * If there is no currently playing track, doesn't do anything.
     */
    void pause();

    /**
     * Stops and disposes the player, regardless of the current status.
     * Can be used always, without checking the status.
     */
    void stop();

    /**
     * @return Returns true if the player is currently playing.
     */
    boolean isPlaying();

    /**
     * Property to bind the isPlaying value.
     * @return Returns the property to the corresponding isPlaying field.
     */
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
            this.play(this.getCurrentTrack());
    }

    /**
     * Tries to find the next track.
     * @return Returns the next track on the playlist, or null if it wasn't found.
     */
    T getNextTrack();

    /**
     * Tries to find the previous track.
     * @return Returns the previous track on the playlist, or null if it wasn't found.
     */
    T getPreviousTrack();

    /**
     * @return Returns the current track.
     */
    T getCurrentTrack();

    /**
     * @return Returns the property with the value of the current track.
     */
    SimpleObjectProperty<T> currentTrackProperty();

    /**
     * @return Returns the current media time ObjectProperty. This property shows the current progress of the currently playing track.
     */
    ObjectProperty<Duration> currentMediaTimeProperty();

    /**
     * @return Returns the total duration of the currently playing media.
     */
    ObjectProperty<Duration> totalMediaDurationProperty();

    /**
     * @return Returns the current volume value.
     */
    double getVolume();

    /**
     * Sets the current volume value.
     * Bigger value = Louder!
     * @param value New Value
     */
    void setVolume(double value);

    /**
     * @return Returns the property containing the current volume. (Write and read possible!)
     */
    DoubleProperty volumeProperty();
}
