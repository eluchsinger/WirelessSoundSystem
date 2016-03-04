package viewmodels;

import controllers.io.CacheHandler;
import controllers.networking.discovery.DiscoveryService;
import controllers.networking.streaming.music.MusicStreamingService;
import controllers.networking.streaming.music.UDPMusicStreamingService;
import controllers.statistics.NetworkStatisticsController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

/**
 * Created by Esteban Luchsinger on 08.12.2015.
 */
public class ClientWindowViewModel {

    DiscoveryService discoveryService = new DiscoveryService();
    MusicStreamingService musicStreamingService = new UDPMusicStreamingService();

    @FXML
    private Label labelStatus;

    @FXML
    private StackedAreaChart<Integer, Integer> statisticsChart;
    private Stage stage;

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

        this.musicStreamingService.addServiceStatusChangedListener(newStatus -> {
            System.out.println("New Status: " + newStatus.name());
            Platform.runLater(() -> labelStatus.setText(newStatus.name()));
        });

        System.out.println("Starting discovery service...");

        this.discoveryService.start();

        this.statisticsChart.setData(NetworkStatisticsController.getInstance().getStatisticsList());

        // Start Service.
        this.musicStreamingService.start();

    }

    public void setStage(Stage stage) {

        if(this.stage != null)
            this.stage.setOnCloseRequest(null);

        this.stage = stage;

        if(this.stage != null){
            this.stage.setOnCloseRequest(event -> {

                System.out.println("Stopping DiscoveryService...");
                this.discoveryService.stop();

                this.musicStreamingService.stop();
            });
        }
    }
}
