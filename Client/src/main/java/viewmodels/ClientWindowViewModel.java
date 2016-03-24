package viewmodels;

import controllers.networking.discovery.DiscoveryService;
import controllers.networking.streaming.music.MusicStreamingService;
import controllers.networking.streaming.music.tcp.TCPMusicStreamingService;
import javafx.fxml.FXML;
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
    private TCPMusicStreamingService service;

    @FXML
    private Label labelStatus;
    @FXML
    private Label labelSongTitle;
    @FXML
    private Label labelArtist;

    private Stage stage;

    private MediaPlayer mediaPlayer;


    /**
     * Constructor
     * @throws IOException
     */
    public ClientWindowViewModel() throws IOException {
        this.discoveryService = new DiscoveryService();
//        this.musicStreamingService = new TCPMusicStreamingController();
    }

    @FXML
    protected void initialize() throws IOException {
        this.initializeStreamingService();
        this.initializeDiscoveryService();
    }

    /**
     * Sets the view (stage) this ViewModel is handling.
     * @param stage View or stage of this ViewModel.
     */
    public void setStage(Stage stage) {

        if(this.stage != null)
            this.stage.setOnCloseRequest(null);

        this.stage = stage;

        if(this.stage != null){
            this.stage.setOnCloseRequest(event -> {
                try {
                    this.service.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                this.musicStreamingService.stop();
                this.discoveryService.stop();
            });
        }
    }

    /**
     * Starts playing the song in the cache.
     */
    private void startPlaying() {
        this.stopPlaying();

        this.mediaPlayer = new MediaPlayer(new Media(this.musicStreamingService.getCache()
                .getFileURI()
                .toString()));
        this.mediaPlayer.play();
        System.out.println("Playing!");
    }

    /**
     * Stops playing.
     */
    private void stopPlaying() {

        if(this.mediaPlayer != null){
            this.mediaPlayer.stop();
            this.mediaPlayer = null;
            this.labelSongTitle.setText("");
            this.labelArtist.setText("");
            this.labelStatus.setText("STOPPED");
            System.out.println("Stopped!");
        }
    }

    //region initializers
    private void initializeStreamingService() throws IOException {

        System.out.print("Starting Streaming Service... ");
        this.service  = new TCPMusicStreamingService();

//        // Handle onStatusChanged
//        this.musicStreamingService.addServiceStatusChangedListener(newStatus -> System.out.println("New Status: " + newStatus.name()));
//
//        // Handle onPlay message.
//        this.musicStreamingService.addOnPlayListener((songTitle, artist) -> Platform.runLater(() -> {
//            this.labelSongTitle.setText(songTitle);
//            this.labelArtist.setText(artist);
//            this.labelStatus.setText("PLAYING");
//            this.startPlaying();
//        }));
//
//        // Handle onStop
//        this.musicStreamingService.addOnStopListener(() -> Platform.runLater(this::stopPlaying));

        System.out.println("Check!");
    }

    private void initializeDiscoveryService() {
        System.out.print("Starting discovery service... ");

        this.discoveryService.addOnServerConnectedListener(server -> {

            try {
                this.service.setCurrentServer(server);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            this.musicStreamingService.stop();
//            this.musicStreamingService.setServer(server);
//
//            // Start Service.
//            this.musicStreamingService.start();
        });
        this.discoveryService.start();
        System.out.println("Check!");
    }

    //endregion
}
