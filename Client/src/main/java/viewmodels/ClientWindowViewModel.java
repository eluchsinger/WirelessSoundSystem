package viewmodels;

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

import java.io.IOException;

/**
 * Created by Esteban Luchsinger on 08.12.2015.
 */
public class ClientWindowViewModel {

    private DiscoveryService discoveryService;
    private MusicStreamingService musicStreamingService;

    @FXML
    private Label labelStatus;

    @FXML
    private StackedAreaChart<Number, Number> statisticsChart;
    private Stage stage;

    private MediaPlayer mediaPlayer;

    private ServiceStatus lastStatus = ServiceStatus.STOPPED;

    /**
     * Constructor
     * @throws IOException
     */
    public ClientWindowViewModel() throws IOException {
        this.discoveryService = new DiscoveryService();
        this.musicStreamingService = new TCPMusicStreamingService();
    }

    @FXML
    protected void initialize() throws IOException {
        this.initializeStreamingService();
        this.initializeDiscoveryService();
        this.initializeStatistics();
    }

    public void setStage(Stage stage) {

        if(this.stage != null)
            this.stage.setOnCloseRequest(null);

        this.stage = stage;

        if(this.stage != null){
            this.stage.setOnCloseRequest(event -> {

                this.musicStreamingService.stop();
                this.discoveryService.stop();

            });
        }
    }

    private void startPlaying() {
        this.stopPlaying();

        this.mediaPlayer = new MediaPlayer(new Media(this.musicStreamingService.getCache()
                .getFileURI()
                .toString()));
        this.mediaPlayer.play();
    }

    private void stopPlaying() {

        if(this.mediaPlayer != null){
            this.mediaPlayer.stop();
            this.mediaPlayer = null;
        }
    }

    //region initializers
    private void initializeStreamingService() {

        System.out.print("Starting Streaming Service... ");

        this.musicStreamingService.addServiceStatusChangedListener(newStatus -> {
            System.out.println("New Status: " + newStatus.name());
            Platform.runLater(() -> {
                this.labelStatus.setText(newStatus.name());
                this.lastStatus = newStatus;
            });
        });


        System.out.println("Check!");
    }

    private void initializeDiscoveryService() {
        System.out.print("Starting discovery service... ");

        this.discoveryService.addOnServerConnectedListener(server -> {

            this.musicStreamingService.stop();
            this.musicStreamingService.setServer(server);

            // Start Service.
            this.musicStreamingService.start();
        });
        this.discoveryService.start();
        System.out.println("Check!");
    }

    private void initializeStatistics() {

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

    }

    //endregion
}
