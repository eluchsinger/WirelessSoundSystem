package viewmodels;

import controllers.io.CacheHandler;
import controllers.networking.streaming.music.MusicStreamingService;
import controllers.statistics.NetworkStatisticsController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.StackedAreaChart;
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
    private StackedAreaChart<Integer, Integer> statisticsChart;

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
            Platform.runLater(() -> labelStatus.setText(newStatus.name()));
        });

        this.statisticsChart.setData(NetworkStatisticsController.getInstance().getStatisticsList());
    }
}
