package controllers.networking.streaming.music;

import controllers.io.cache.file.FileCacheService;
import controllers.io.cache.file.SongCacheManager;
import controllers.networking.streaming.music.callback.*;
import models.clients.Server;

/**
 * <pre>
 * Created by Esteban Luchsinger on 01.03.2016.
 * Interface provides all needed public methods.
 * </pre>
 */
public interface MusicStreamingService {

    /**
     * Starts the streaming service.
     */
    void start();

    /**
     * Stops the streaming service.
     */
    void stop();

    /**
     * Sets the current server.
     * @param server The new server object.
     */
    void setServer(Server server);

    void addServiceStatusChangedListener(OnMusicStreamingStatusChanged listener);
    void removeServiceStatusChangedListener(OnMusicStreamingStatusChanged listener);

    void addOnPlayListener(OnPlay listener);
    void removeOnPlayListener(OnPlay listener);

    void addOnPauseListener(OnPause listener);
    void removeOnPauseListener(OnPause listener);

    void addOnStopListener(OnStop listener);
    void removeOnStopListener(OnStop listener);

    void addOnRenameListener(OnRename listener);
    void removeOnRenameListener(OnRename listener);

    /**
     * Returns the cache (FileCache) of the MusicStreamingService.
     * @return FileCacheService.
     */
    SongCacheManager getCache();

    void sendName(String name);
}
