package viewmodels;

import controllers.io.CacheHandler;
import controllers.networking.streaming.music.MusicStreamingService;
import controllers.networking.streaming.music.OnMusicStreamingStatusChanged;
import controllers.networking.streaming.music.ServiceStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Created by Esteban Luchsinger on 08.12.2015.
 */
public class ClientWindowViewModel {

    @FXML
    private Label labelStatus;

    @FXML
    public void onButtonPlayClicked(){
        MediaPlayer player = new MediaPlayer(
                new Media(CacheHandler.getInstance()
                        .getTempFile()
                        .toURI()
                        .toString()));

        player.play();
    }


    @FXML
    protected void initialize(){
        MusicStreamingService.getInstance().addServiceStatusChangedListener(newStatus -> {
            System.out.println("New Status: " + newStatus.name());
            // Todo: This call has to be from UI-Thread!
            Platform.runLater(() -> {
                labelStatus.setText(newStatus.name());
            });
        });
    }


    public ClientWindowViewModel(){
    }
}
