package controllers.networking.streaming.music.callback;

/**
 * Created by Esteban on 01.04.2016.
 *
 */
@FunctionalInterface
public interface OnRename {
    /**
     * Renames the client.
     * @param name The new name of the client.
     */
    void rename(String name);
}
