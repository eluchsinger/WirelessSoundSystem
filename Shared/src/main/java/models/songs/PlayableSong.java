package models.songs;

import javafx.beans.property.BooleanProperty;

/**
 * <pre>
 * Created by Esteban Luchsinger on 19.04.2016.
 * An abstraction layer for a Song, which adds the ability to change the current status of a song object.
 * </pre>
 */
public interface PlayableSong extends Song {

    /**
     * This property is true, if the Song is currently being played.
     * @return The isPlaying property. This property is <code>true</code> if the song is being played.
     */
    BooleanProperty isPlayingProperty();

    default boolean isPlaying() {
        return isPlayingProperty().get();
    }

    default void setIsPlaying(boolean isPlayingProperty) {
        isPlayingProperty().set(isPlayingProperty);
    }

}