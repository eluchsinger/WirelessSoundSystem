package ch.wirelesssoundsystem.client.viewmodels;

import ch.wirelesssoundsystem.client.controllers.io.SynchronizedTempFileHandler;
import javafx.fxml.FXML;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Created by Esteban Luchsinger on 08.12.2015.
 */
public class ClientWindowViewModel {

    @FXML
    public void onButtonPlayClicked(){
        MediaPlayer player = new MediaPlayer(
                new Media(SynchronizedTempFileHandler.getInstance()
                        .getTempFile()
                        .toURI()
                        .toString()));

        player.play();
    }
}
