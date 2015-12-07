package ch.wirelesssoundsystem.server.models.playlists;

import javafx.collections.ObservableList;

/**
 * Created by Esteban Luchsinger on 03.12.2015.
 */
public interface Playlist<T extends Comparable<T>> {

    ObservableList<T> getTracks();
    T getTrack(int index);

    /**
     * Finds a track
     * @param track to find.
     * @return Returns the found track or null, if none were found.
     */
    T getTrack(T track);

    /**
     * Adds a track.
     * @param track track to add
     */
    void addTrack(T track);

    /**
     * Removes a track.
     * @param track track to remove.
     */
    void removeTrack(T track);
}
