package viewmodels;

import controllers.clients.ClientController;
import controllers.io.SongsHandler;
import controllers.media.MediaPlayer;
import controllers.media.music.SimpleAudioPlayer;
import controllers.networking.discovery.ServerDiscoveryService;
import controllers.networking.streaming.music.MusicStreamController;
import controllers.networking.streaming.music.tcp.TCPMusicStreamController;
import controllers.networking.streaming.music.tcp.TCPSocketServer;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import models.networking.clients.NetworkClient;
import models.networking.dtos.RenameCommand;
import models.songs.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.DurationStringConverter;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * <pre>
 * Created by Esteban Luchsinger on 30.11.2015.
 * This is the ViewModel for the MainWindow view.
 * </pre>
 */
public class MainWindowViewModel {
    //region Members
    private final Logger logger;

    private MediaPlayer<Song> mediaPlayer;
    private MusicStreamController musicStreamController;
    private ServerDiscoveryService serverDiscoveryService;

    private TCPSocketServer tcpServer;
    private ClientController clientController;
    //endregion Members

    //region Properties
    /**
     * Property containing the path to the song folder.
     */
    private StringProperty pathToSongFolder;

    /**
     * List containing the songs.
     */
    private ObservableList<Song> songObservableList;
    //endregion Properties

    //region Elements
    /**
     * The search button provides the user with an option to search for his music.
     */
    @FXML
    private Button buttonSearch;

    /**
     * Play and Pause button (Function changes depending on MediaPlayer state).
     */
    @FXML
    private ToggleButton buttonPlayPause;

    /**
     * Button to skip to the next song.
     */
    @FXML
    private Button buttonSkipNext;

    /**
     * Button to play the previous song.
     */
    @FXML
    private Button buttonSkipPrevious;

    /**
     * The textfield containing the song folder.
     */
    @FXML
    private TextField textFieldFolder;

    /**
     * The table view containing the songs.
     */
    @FXML
    private TableView<Song> tableViewSongs;

    /**
     * The column of the song table containing the title of the song.
     */
    @FXML
    private TableColumn<Song, String> tableColumnTitle;

    /**
     * The column of the song table containing the artist.
     */
    @FXML
    private TableColumn<Song, String> tableColumnArtist;

    /**
     * The ListView showing the clients (right now: Speakers) connected to this server instance.
     */
    @FXML
    private ListView<NetworkClient> listViewClients;

    /**
     * The slider that changes the volume (loudness) of the MediaPlayer.
     */
    @FXML
    private Slider sliderVolume;

    /**
     * The slider that tracks the song's current playtime.
     */
    @FXML
    private Slider songTrackerSlider;

    /**
     * A label describing the current time in the currently playing song.
     */
    @FXML
    private Label labelCurrentDuration;

    /**
     * (Workaround)
     * This is the stage (the window / View) of this ViewModel.
     */
    private Stage stage;

    //endregion

    //region Constructor

    /**
     * Default constructor
     */
    public MainWindowViewModel() {

        // Init SLF4J logger
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.logger.info("Enable logger detect name mismatch setting");
    }
    //endregion Constructor

    //region Getters & Setters

    /**
     * Retrieves the path to the song folder (in the file system).
     * @return a string containing the path to the song folder.
     */
    public String getPathToSongFolder() {
        return this.pathToSongFolder.get();
    }

    /**
     * Sets the path to the song folder (in the file system).
     * @param file File object to set the path to. (This object must be a folder!)
     */
    public void setPathToSongFolder(File file) {
        this.pathToSongFolder.set(file.getAbsolutePath());


        // Loads the songs.
        SongsHandler handler = new SongsHandler();
        List<Song> songs = handler.loadSongsFromDir(file.getPath());
        this.songObservableList.setAll(songs);
    }

    /**
     * Gets the Property containing the song folder (in the File System).
     * @return StringProperty object containing the path to the song folder.
     */
    public StringProperty getPathToFolderProperty() {
        return this.pathToSongFolder;
    }

    /**
     * Gets the selected song.
     *
     * @return Returns the selected song item. If there many items selected, the last one selected is returned.
     */
    public final Song getSelectedSong() {
        return this.tableViewSongs.getSelectionModel().getSelectedItem();
    }

    /**
     * Sets the view (stage) this ViewModel is handling.
     * @param stage View or stage of this ViewModel.
     */
    public void setStage(Stage stage) {
        if(this.stage != null) {
            this.stage.setOnCloseRequest(null);
        }

        this.stage = stage;

        if(this.stage != null){
            this.stage.setOnCloseRequest(event -> {
                if(this.serverDiscoveryService != null) {
                    this.serverDiscoveryService.stop();
                    this.logger.info("Stopped Discovery Service!");

                }

                if(this.musicStreamController != null && this.musicStreamController instanceof Closeable) {
                    try {
                        ((Closeable)this.musicStreamController).close();
                    } catch (IOException e) {
                        this.logger.error("Could not close the StreamController", e);
                    }
                }

                if(this.tcpServer != null) {
                    try {
                        this.tcpServer.close();
                    } catch (IOException e) {
                        this.logger.error("Could not close TCP Server", e);
                    }
                }

                if(this.clientController != null) {
                    try {
                        this.clientController.close();
                    } catch (IOException e) {
                        this.logger.error("Could not close client controller", e);
                    }
                }
            });
        }
    }

    //endregion Getters & Setters

    //region Events

    /**
     * This event gets called, when the button <code>search</code> is clicked.
     * Provides the user with a possibility to choose the songs folder.
     */
    @FXML
    public void onSearchButtonClicked() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Ordner auswählen");
        File file = directoryChooser.showDialog(this.buttonSearch.getScene().getWindow());

        if (file != null) {
            this.setPathToSongFolder(file);
        }
    }

    /**
     * This event gets called, when the button PlayPause is clicked.
     * The button's action depends on the current state of the MediaPlayer.
     * <pre>
     * There are two possible actions:
     * <ul>
     *  <li>If the player is playing, the button will pause the MediaPlayer.</li>
     *  <li>If the player is not playing (paused or stopped), the button will <strong>resume</strong> the MediaPlayer.</li>
     * </ul>
     * </pre>
     */
    @FXML
    public void onButtonPlayPauseClicked() {

        // Pause if the pause button was clicked.
        if (this.mediaPlayer.isPlaying()) {
            this.mediaPlayer.pause();
            this.musicStreamController.stop();
        } else if (this.getSelectedSong() != null) {
            // Play if the play button was clicked.
            this.startPlaying(this.getSelectedSong());
        }
    }

    /**
     * This event gets called, when the button SkipPrevious is clicked.
     * The button's action is to go back to the previous song.
     */
    @FXML
    public void onButtonSkipPreviousClicked(){
        // First check, if there are items on the list.
        if(this.songObservableList != null && this.songObservableList.size() > 1){
            Song previous = this.mediaPlayer.getPreviousTrack();
            if(previous != null){
                this.mediaPlayer.play(previous);
            }
        } else {
            // Error is either null or it's the size.
            String error = (this.songObservableList == null) ? "not initialized" : Integer.toString(this.songObservableList.size());
            this.logger.warn("Tried to skip to the previous song, but the song list is currently " + error);
        }
    }

    /**
     * This event gets called, when the button SkipNext ist clicked.
     * The button's action is to skip to the next track.
     */
    @FXML
    public void onButtonSkipNextClicked(){
        if(this.songObservableList != null && this.songObservableList.size() > 1){
            Song next = this.mediaPlayer.getNextTrack();
            if(next != null){
                this.mediaPlayer.play(next);
            }
        } else {
            // Error is either null or it's the size.
            String error = (this.songObservableList == null) ? "not initialized" : Integer.toString(this.songObservableList.size());
            this.logger.warn("Tried to skip to the next song, but the song list is currently " + error);
        }
    }

    /**
     * This method gets called, when the isPlaying property of the mediaPlayer changes.
     * It handles the play/pause button behavior.
     */
    public void onIsPlayingChanged() {
        if (this.mediaPlayer.isPlaying()) {
            this.buttonPlayPause.setId("pause-button");
            this.buttonPlayPause.setSelected(true);

        } else {
            this.buttonPlayPause.setId("play-button");
            this.buttonPlayPause.setSelected(false);
        }
    }

    /**
     * Starts playing the song.
     * Unites the Table-Double-Click function and the play button.
     * @param song The song that should start playing.
     */
    private void startPlaying(Song song) {

        this.logger.info("Trying to play: " + song);

        if(this.getSelectedSong() != null) {
            // Start streaming...
            this.logger.info("Streaming the new song: " + song.getTitle());
            try {
                this.musicStreamController.play(song);
                this.mediaPlayer.play(song);
            }
            catch(IOException ioException) {
                this.logger.error("Error trying to stream", ioException);
            }
        }
    }
    //endregion

    //region initialization

    /**
     * Is called, when the window has been initialized.
     */
    @FXML
    protected void initialize() throws IOException {

        this.songObservableList = FXCollections.observableArrayList();

        // Init Media Player
        this.mediaPlayer = new SimpleAudioPlayer(this.songObservableList);
        this.mediaPlayer.isPlayingProperty().addListener((observable, oldValue, newValue) -> this.onIsPlayingChanged());

        this.tcpServer = new TCPSocketServer();
        this.clientController = new ClientController(this.tcpServer);

        this.tcpServer.start();

        this.initializeTable();
        this.initializeClientListView();
        this.initializeBindings();
        this.initializeDiscoveryService();

        this.musicStreamController = new TCPMusicStreamController(this.clientController);

        File file = this.guessSongsFolder();
        if(file != null) {
            this.setPathToSongFolder(file);
        }
    }

    /**
     * Initializes the discovery service.
     */
    private void initializeDiscoveryService() {

        this.serverDiscoveryService = new ServerDiscoveryService();

        this.logger.info("Starting discovery Service...");
        this.serverDiscoveryService.start();
    }

    /**
     * Initializes the table containing the songs.
     */
    private void initializeTable() {

        this.tableViewSongs.setItems(this.songObservableList);
        this.tableViewSongs.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        tableColumnTitle.setCellValueFactory(
                new PropertyValueFactory<>("title")
        );

        tableColumnArtist.setCellValueFactory(
                new PropertyValueFactory<>("artist")
        );

        // Implement DoubleClick for rows.
        this.tableViewSongs.setRowFactory(tv -> {
            TableRow<Song> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() >= 2 && (!row.isEmpty())) {
                    // Check if its a song.
                    if (Song.class.isInstance(row.getItem()))
                        this.startPlaying(row.getItem());
                }
            });
            return row;
        });
    }

    /**
     * Initializes the different bindings needed to connect the View and the ViewModel.
     */
    private void initializeBindings(){
        // Do binding for the music folder.
        this.pathToSongFolder = new SimpleStringProperty();
        this.textFieldFolder.textProperty().bindBidirectional(this.getPathToFolderProperty());

        // Bind Slider to Volume property
        this.sliderVolume.valueProperty().bindBidirectional(this.mediaPlayer.volumeProperty());

        // Bind CurrentDuration Label to CurrentDuration Property
        Bindings.bindBidirectional(this.labelCurrentDuration.textProperty(), this.mediaPlayer.currentMediaTimeProperty(), new DurationStringConverter());

        this.bindSongTrackerSlider();
    }

    /**
     * <pre>
     * Initializes the tracker slider for the song.
     * (The tracker that changes the current media time.)
     * </pre>
     */
    private void bindSongTrackerSlider() {
        // Create a DoubleBinding which calculates the value of the duration-slider.
        DoubleBinding durationPercentageBinding = Bindings.createDoubleBinding(() -> {
                    if (this.mediaPlayer.totalMediaDurationProperty().get() != null && this.mediaPlayer.totalMediaDurationProperty().get().toSeconds() > 0) {
                        return this.mediaPlayer.currentMediaTimeProperty().get().toSeconds() * 100 / this.mediaPlayer.totalMediaDurationProperty().get().toSeconds();
                    } else {
                        return (double) 0;
                    }
                },
                this.mediaPlayer.currentMediaTimeProperty()
        );

        this.songTrackerSlider.valueProperty().bind(durationPercentageBinding);
    }

    /**
     * Initializes the ListView showing the connected clients.
     */
    private void initializeClientListView() {
        this.listViewClients.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.listViewClients.setItems(this.clientController.getClients());

        this.listViewClients.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2) {
                // Show edit dialog.
                NetworkClient client = this.listViewClients.getSelectionModel().getSelectedItem();
                if(client != null) {
                    TextInputDialog dialog = new TextInputDialog(client.toString());
                    dialog.setTitle("Lautsprecher umbenennen");
                    dialog.setHeaderText("Geben Sie den neuen Namen des Lautsprechers ein.");
                    dialog.setContentText("Name:");

                    // Set dialog icon
                    Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(new Image(this.getClass().getResource("/views/icons/png/rename.png").toString()));

                    // Change the name
                    Optional<String> result = dialog.showAndWait();
                    result.ifPresent(name -> {
                        client.send(new RenameCommand(name));
                        client.setName(name);

                        // Todo: Workaround! Make Observable.
                        this.listViewClients.refresh();
                    });
                }
            }
        });
    }

    /**
     * Guesses the song folder. May return the folder or may return null, if there was no good guess.
     * @return Returns the songs folder or null, if it was not found.
     */
    private File guessSongsFolder() {
        File file = null;
        try {
            String userFolder = System.getProperty("user.home");
            Path path = Paths.get(userFolder, "Music");
            file = path.toFile();

            if(file != null) {

                if(file.exists()) {
                    this.logger.info("Music folder in " +  file.getAbsolutePath() + " was not found.");
                } else {
                    this.logger.info("Music Folder (in " + file.getAbsolutePath() + ") was found.");
                }
            } else {
                throw new Exception("The path returned was null");
            }

        }
        catch(Exception ex) {
            this.logger.warn("Failed guessing the users song folder", ex);
        }

        return file;
    }
    //endregion

}
