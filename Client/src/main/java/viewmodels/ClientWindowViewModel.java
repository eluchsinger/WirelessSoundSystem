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
import models.songs.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.media.SongUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

/**
 * <pre>
 * Created by Esteban Luchsinger on 08.12.2015.
 * The ViewModel of the ClientWindow view.
 * </pre>
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
     * @throws IOException Throws an IOException if there was an error on startup.
     */
    public ClientWindowViewModel() throws IOException {

        this.logger = LoggerFactory.getLogger(this.getClass());

        Properties temporaryProperties = new Properties();
        // Set properties
        try (FileInputStream in = new FileInputStream("appProperties")) {
            temporaryProperties.load(in);
            String tmp = temporaryProperties.getProperty(CLIENT_NAME_PROPERTY, null);

            if(tmp == null) {
                this.logger.info("This client has no name.");
            }
            else {
                this.logger.info("This client has name: " + tmp);
            }
        }
        catch(IOException ex){
            this.logger.error("Could not load properties");
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
    private void startPlaying(int songHash) {
        Song song = this.musicStreamingService.getCache().retrieve(songHash);
        this.mediaPlayer = new MediaPlayer(new Media(SongUtils.getSongURI(song).toString()));
        this.mediaPlayer.play();
        this.logger.info("Now Playing");
    }

    private void play() {
        if(this.mediaPlayer != null) {
            this.mediaPlayer.play();
            this.logger.info("Now Playing");
        }
    }

//    private void startPlaying(Duration startTime) {
//        this.mediaPlayer = new MediaPlayer(new Media(this.musicStreamingService.getCache()
//                .getFileURI()
//                .toString()));
//        this.mediaPlayer.play();
//        this.logger.info("Now Playing from " + startTime);
//    }

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
            this.logger.info("Stopped playing");
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
            this.logger.info("Renamed to: " + name);
        } catch (IOException e) {
            this.logger.warn("Error saving namechange!", e);
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
        if(this.mediaPlayer != null) {
            this.mediaPlayer.pause();
        }
    }

    //region initializers

    private void initializeStreamingService() throws IOException {

        logger.info("Starting Streaming Service... ");

        // Handle onStatusChanged
        this.musicStreamingService.addServiceStatusChangedListener(newStatus -> logger.info("New Status: " + newStatus.name()));

        // Handle onPlay message.
        this.musicStreamingService.addOnPlayListener((hash) -> Platform.runLater(() -> {
            this.labelStatus.setText("PLAYING");
            this.startPlaying(hash);
        }));

        // Handle onPause message
        this.musicStreamingService.addOnPauseListener(() -> Platform.runLater(() -> {
            this.labelStatus.setText("PAUSED");
            this.pausePlaying();
        }));

        // Handle onStop
        this.musicStreamingService.addOnStopListener(() -> Platform.runLater(this::stopPlaying));

        // Handle onRename
        this.musicStreamingService.addOnRenameListener((name) -> Platform.runLater(() -> this.rename(name)));

        logger.info("Check!");
    }


    private void initializeDiscoveryService() {
        logger.info("Starting discovery service... ");

        this.clientDiscoveryService.addOnServerConnectedListener(server -> {

            this.musicStreamingService.stop();
            this.musicStreamingService.setServer(server);

            // Start Service.
            this.musicStreamingService.start();

            // Todo: Error -> Sometimes (when I restart the client quickly, after closing it) the name gets not read properly.
            String name = this.properties.getProperty(CLIENT_NAME_PROPERTY, null);

            if(name != null) {
                this.musicStreamingService.sendName(name);
                this.musicStreamingService.sendCurrentCache();
            }
        });
        this.clientDiscoveryService.start();
        logger.info("Check!");
    }

    //endregion


}
