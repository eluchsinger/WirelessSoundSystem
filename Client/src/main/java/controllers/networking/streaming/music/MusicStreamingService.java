package controllers.networking.streaming.music;

import controllers.networking.streaming.music.callback.OnMusicStreamingStatusChanged;

/**
 * Created by Esteban Luchsinger on 01.03.2016.
 * Interface provides all needed public methods.
 */
public interface MusicStreamingService {

    void start();
    void stop();

    void addServiceStatusChangedListener(OnMusicStreamingStatusChanged listener);
    void removeServiceStatusChangedListener(OnMusicStreamingStatusChanged listener);
}
