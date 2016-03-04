package controllers.networking.streaming.music;

import controllers.networking.streaming.music.callback.OnMusicStreamingStatusChanged;
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
}
