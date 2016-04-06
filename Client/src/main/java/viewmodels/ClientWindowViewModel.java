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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 08.12.2015.
 */
public class ClientWindowViewModel {

    private static String CLIENT_NAME_PROPERTY = "";

    private final Logger logger;
    private final Properties properties;

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

        Properties temporaryProperties = new Properties();
        // Set properties
        try (FileInputStream in = new FileInputStream("appProperties")) {
            temporaryProperties.load(in);
            String tmp = temporaryProperties.getProperty(CLIENT_NAME_PROPERTY, null);

            if(tmp == null) {
                this.logger.log(Level.INFO, "This client has no name.");
            }
            else {
                this.logger.log(Level.INFO, "This client has name: " + tmp);
            }
        }
        catch(IOException ex){
            this.logger.log(Level.SEVERE, "Could not load properties");
        }

        this.properties = temporaryProperties;
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
     * Renames the client.
     * @param name New client name.
     */
    private void rename(String name) {

        try (FileOutputStream out = new FileOutputStream("appProperties")) {
            this.properties.setProperty(CLIENT_NAME_PROPERTY, name);
            this.properties.store(out, "No comment");
            this.logger.log(Level.INFO, "Renamed to: " + name);
        } catch (IOException e) {
            this.logger.log(Level.WARNING, "Error saving namechange!", e);
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

        logger.log(Level.INFO, "Starting Streaming Service... ");

        // Handle onStatusChanged
        this.musicStreamingService.addServiceStatusChangedListener(newStatus -> logger.log(Level.INFO, "New Status: " + newStatus.name()));

        // Handle onPlay message.
        this.musicStreamingService.addOnPlayListener((songTitle, artist) -> Platform.runLater(() -> {
            this.labelSongTitle.setText(songTitle);
            this.labelArtist.setText(artist);
            this.labelStatus.setText("PLAYING");
            this.startPlaying();
        }));

        // Handle onStop
        this.musicStreamingService.addOnStopListener(() -> Platform.runLater(this::stopPlaying));

        // Handle onRename
        this.musicStreamingService.addOnRenameListener((name) -> Platform.runLater(() -> this.rename(name)));

        logger.log(Level.INFO, "Check!");
    }


    private void initializeDiscoveryService() {
        logger.log(Level.INFO, "Starting discovery service... ");

        this.clientDiscoveryService.addOnServerConnectedListener(server -> {

            this.musicStreamingService.stop();
            this.musicStreamingService.setServer(server);

            // Start Service.
            this.musicStreamingService.start();
        });
        this.clientDiscoveryService.start();
        logger.log(Level.INFO, "Check!");
    }

    //endregion


}
