package viewmodels;

import controllers.networking.discovery.ClientDiscoveryService;
import controllers.networking.streaming.music.MusicStreamingService;
import controllers.networking.streaming.music.tcp.TCPMusicStreamingController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 08.12.2015.
 */
public class ClientWindowViewModel {

    private final Logger logger;

    private ClientDiscoveryService clientDiscoveryService;
    private MusicStreamingService musicStreamingService;

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

        this.logger = Logger.getLogger(this.getClass().getName());

        this.clientDiscoveryService = new ClientDiscoveryService();
        this.musicStreamingService = new TCPMusicStreamingController();
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
                this.clientDiscoveryService.stop();
                this.musicStreamingService.stop();
            });
        }
    }

    /**
     * Starts playing the song in the cache from the beginnning.
     */
    private void startPlaying() {
        this.stopPlaying();

        this.mediaPlayer = new MediaPlayer(new Media(this.musicStreamingService.getCache()
                .getFileURI()
                .toString()));
        this.mediaPlayer.play();
        this.logger.log(Level.INFO, "Now Playing");
    }

    private void startPlaying(Duration startTime) {
        this.stopPlaying();

        this.mediaPlayer = new MediaPlayer(new Media(this.musicStreamingService.getCache()
                .getFileURI()
                .toString()));

        this.mediaPlayer.play();
        this.logger.log(Level.INFO, "Now Playing from " + startTime);

    }

    /**
     * Stops playing the currently played song.
     */
    private void stopPlaying() {

        if(this.mediaPlayer != null){
            this.mediaPlayer.stop();
            this.mediaPlayer = null;
            this.labelSongTitle.setText("");
            this.labelArtist.setText("");
            this.labelStatus.setText("STOPPED");
            this.logger.log(Level.INFO, "Stopped playing");
        }
    }

    /**
     * Resumes playing a new song.
     */
    private void resumePlaying() {

    }

    /**
     * Pauses playing the song.
     */
    private void pausePlaying() {

    }

    //region initializers
    private void initializeStreamingService() throws IOException {

        System.out.print("Starting Streaming Service... ");

        // Handle onStatusChanged
        this.musicStreamingService.addServiceStatusChangedListener(newStatus -> System.out.println("New Status: " + newStatus.name()));

        // Handle onPlay message.
        this.musicStreamingService.addOnPlayListener((songTitle, artist) -> Platform.runLater(() -> {
            this.labelSongTitle.setText(songTitle);
            this.labelArtist.setText(artist);
            this.labelStatus.setText("PLAYING");
            this.startPlaying();
        }));

        // Handle onStop
        this.musicStreamingService.addOnStopListener(() -> Platform.runLater(this::stopPlaying));

        System.out.println("Check!");
    }

    private void initializeDiscoveryService() {
        System.out.print("Starting discovery service... ");

        this.clientDiscoveryService.addOnServerConnectedListener(server -> {

            this.musicStreamingService.stop();
            this.musicStreamingService.setServer(server);

            // Start Service.
            this.musicStreamingService.start();
        });
        this.clientDiscoveryService.start();
        System.out.println("Check!");
    }

    //endregion
}
