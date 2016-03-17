package controllers.networking.streaming.music;

import controllers.io.cache.file.FileCacheService;
import controllers.networking.streaming.music.callback.OnMusicStreamingStatusChanged;
import controllers.networking.streaming.music.callback.OnPlay;
import controllers.networking.streaming.music.callback.OnStop;
import models.clients.Server;

/**
 * Created by Esteban Luchsinger on 01.03.2016.
 * Interface provides all needed public methods.
 */
public interface MusicStreamingService {

    void start();
    void stop();

    void setServer(Server server);

    void addServiceStatusChangedListener(OnMusicStreamingStatusChanged listener);
    void removeServiceStatusChangedListener(OnMusicStreamingStatusChanged listener);

    void addOnPlayListener(OnPlay listener);
    void removeOnPlayListener(OnPlay listener);

    void addOnStopListener(OnStop listener);
    void removeOnStopListener(OnStop listener);

    /**
     * Returns the cache (FileCache) of the MusicStreamingService.
     * @return FileCacheService.
     */
    FileCacheService getCache();
}
