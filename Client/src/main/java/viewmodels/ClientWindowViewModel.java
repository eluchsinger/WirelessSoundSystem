package viewmodels;

import controllers.io.cache.file.DynamicFileCacheService;
import controllers.networking.discovery.DiscoveryService;
import controllers.networking.streaming.music.MusicStreamingService;
import controllers.networking.streaming.music.ServiceStatus;
import controllers.networking.streaming.music.TCPMusicStreamingService;
import controllers.networking.streaming.music.UDPMusicStreamingService;
import controllers.statistics.NetworkStatisticsController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
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
    MusicStreamingService musicStreamingService = new TCPMusicStreamingService();

    @FXML
    private Label labelStatus;

    @FXML
    private StackedAreaChart<Number, Number> statisticsChart;
    private Stage stage;

    private MediaPlayer mediaPlayer;

    private ServiceStatus lastStatus = ServiceStatus.STOPPED;


    @FXML
    public void onButtonPlayClicked(){
        MediaPlayer player = new MediaPlayer(
                new Media(DynamicFileCacheService.getInstance()
                        .getTempFile()
                        .toURI()
                        .toString()));

        player.play();
    }

    @FXML
    protected void initialize(){

        this.musicStreamingService.addServiceStatusChangedListener(newStatus -> {
            System.out.println("New Status: " + newStatus.name());
            Platform.runLater(() -> {
                this.labelStatus.setText(newStatus.name());
                if(lastStatus.equals(ServiceStatus.RECEIVING) && newStatus.equals(ServiceStatus.READY)){
                    this.startPlaying();
                }

                this.lastStatus = newStatus;
            });
        });

        System.out.println("Starting discovery service...");

        this.discoveryService.start();

        // Check UDP or TCP Streaming.

        // IF UDP: Use packets for statistics (0-100%).
        if(this.musicStreamingService instanceof UDPMusicStreamingService) {
            System.out.println("Statistics Mode: UDP");
            NumberAxis axis = ((NumberAxis)this.statisticsChart.getYAxis());
            axis.setAutoRanging(false);
            axis.setUpperBound(100);
            axis.setTickUnit(20);

            axis.setLabel("Empfangene Packete (%)");
        }

        // IF TCP: Use bytes for statistics (0-x%) --> Open upper-bound.
        if(this.musicStreamingService instanceof TCPMusicStreamingService) {
            System.out.println("Statistics Mode: TCP");

            this.statisticsChart.getYAxis().setLabel("Empfangene Bytes (Total)");
        }

        this.statisticsChart.setData(NetworkStatisticsController.getInstance().getStatisticsList());

        this.discoveryService.addOnServerConnectedListener(server -> {

            this.musicStreamingService.stop();
            this.musicStreamingService.setServer(server);

            // Start Service.
            this.musicStreamingService.start();
        });

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

    private void startPlaying() {
        this.stopPlaying();

        this.mediaPlayer = new MediaPlayer(new Media(DynamicFileCacheService.getInstance()
                .getTempFile()
                .toURI()
                .toString()));

        this.mediaPlayer.stop();
    }

    private void stopPlaying() {

        if(this.mediaPlayer != null){
            this.mediaPlayer.stop();
            this.mediaPlayer = null;
        }
    }
}
