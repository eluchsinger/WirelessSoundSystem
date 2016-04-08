package viewmodels;

import controllers.clients.ClientController;
import controllers.io.SongsHandler;
import controllers.media.MediaPlayer;
import controllers.media.music.AudioPlayer;
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
import java.util.List;
import java.util.Optional;

/**
 * Created by Esteban Luchsinger on 30.11.2015.
 */
public class MainWindowViewModel {
    private final Logger logger;

    private MediaPlayer<Song> mediaPlayer;
    private MusicStreamController musicStreamController;
    private ServerDiscoveryService serverDiscoveryService;

    private TCPSocketServer tcpServer;
    private ClientController clientController;

    // Properties
    private StringProperty pathToFolder;
    private ObservableList<Song> songObservableList;

    /* Elements */
    @FXML
    private Button buttonSearch;

    @FXML
    private ToggleButton buttonPlayPause;

    @FXML
    private Button buttonSkipNext;

    @FXML
    private Button buttonSkipPrevious;

    @FXML
    private TextField textFieldFolder;

    @FXML
    private ListView<NetworkClient> listViewClients;

    @FXML
    private TableView<Song> tableViewSongs;

    @FXML
    private TableColumn<Song, String> tableColumnTitle;

    @FXML
    private TableColumn<Song, String> tableColumnArtist;

    @FXML
    private Slider sliderVolume;

    @FXML
    private Slider songTrackerSlider;

    @FXML
    private Label labelCurrentDuration;
    private Stage stage;


    //region Constructor
    public MainWindowViewModel() {

        // Init SLF4J logger
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.logger.info("Enable logger detect name mismatch setting");
    }
    //endregion Constructor

    /**
     * Is called, when the window has been initialized.
     */
    @FXML
    protected void initialize() throws IOException {

        this.songObservableList = FXCollections.observableArrayList();

        // Init Media Player
        this.mediaPlayer = new AudioPlayer(this.songObservableList);
        this.mediaPlayer.isPlayingProperty().addListener((observable, oldValue, newValue) -> this.onIsPlayingChanged());

        this.tcpServer = new TCPSocketServer();
        this.clientController = new ClientController(this.tcpServer);

        this.tcpServer.start();

        this.initializeTable();
        this.initializeClientListView();
        this.initializeBindings();
        this.initializeDiscoveryService();

        this.musicStreamController = new TCPMusicStreamController(this.clientController);
    }

    /* Properties */
    public final String getPathToFolder() {
        return this.pathToFolder.get();
    }

    public final void setPathToFolder(String path) {
        this.pathToFolder.set(path);
    }

    public StringProperty getPathToFolderProperty() {
        return this.pathToFolder;
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
                    if(this.musicStreamController != null && this.musicStreamController instanceof Closeable) {
                        try {
                            ((Closeable)this.musicStreamController).close();
                        } catch (IOException e) {
                            this.logger.error("Could not close the StreamController", e);
                        }
                    }
                }

                if(this.tcpServer != null) {
                    try {
                        this.tcpServer.close();
                    } catch (IOException e) {
                        this.logger.error("Could not close TCP Server", e);
                    }
                }
            });
        }
    }

    //region Events

    @FXML
    public void onSearchButtonClicked() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Ordner auswählen");
        File file = directoryChooser.showDialog(this.buttonSearch.getScene().getWindow());

        if (file != null) {

            // Sets the property for the textBox.
            this.setPathToFolder(file.getPath());

            // Loads the songs.
            SongsHandler handler = new SongsHandler();
            List<Song> songs = handler.loadSongsFromDir(file.getPath());
            this.songObservableList.setAll(songs);
        }
    }

    @FXML
    public void onButtonPlayPauseClicked() {

        if (this.mediaPlayer.isPlaying()) {
            this.mediaPlayer.pause();
        } else if (this.getSelectedSong() != null) {
            this.logger.info("Trying to play: " + this.getSelectedSong().getTitle());
            this.mediaPlayer.play(this.getSelectedSong(), true);
        }
    }

    @FXML
    public void onButtonSkipPreviousClicked(){

        // First check, if there are items on the list.
        if(this.songObservableList.size() > 1){
            Song previous = this.mediaPlayer.getPreviousTrack();
            if(previous != null){
                this.mediaPlayer.play(previous);
            }
        }
    }

    @FXML
    public void onButtonSkipNextClicked(){
        if(this.songObservableList.size() > 1){
            Song next = this.mediaPlayer.getNextTrack();
            if(next != null){
                this.mediaPlayer.play(next);
            }
        }
    }

    /**
     * This method is called, when a client is renamed, using the ListView.
     */
    @FXML
    public void onListViewClientEditCommit() {
        System.out.println("Client edit commit");
    }

    /**
     * This method gets called, when the isPlaying property of the mediaPlayer changes.
     * It handles the play/pause button behavior.
     */
    public void onIsPlayingChanged() {
        if (this.mediaPlayer.isPlaying()) {
            this.buttonPlayPause.setId("pause-button");
            this.buttonPlayPause.setSelected(true);

            if(this.getSelectedSong() != null) {
                // Start streaming...
                this.logger.info("Streaming the new song: " + this.getSelectedSong().getTitle());
                try {
                    this.musicStreamController.play(this.getSelectedSong());
                }
                catch(IOException ioException) {
                    this.logger.error("Error trying to stream", ioException);
                }
            }
        } else {
            this.buttonPlayPause.setId("play-button");
            this.buttonPlayPause.setSelected(false);
            this.musicStreamController.stop();
        }
    }
    //endregion

    //region initialization

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
                new PropertyValueFactory<Song, String>("title")
        );

        tableColumnArtist.setCellValueFactory(
                new PropertyValueFactory<Song, String>("artist")
        );

        // Implement DoubleClick for rows.
        this.tableViewSongs.setRowFactory(tv -> {
            TableRow<Song> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() >= 2 && (!row.isEmpty())) {
                    // Check if its a song.
                    if (Song.class.isInstance(row.getItem()))
                        this.mediaPlayer.play(row.getItem());
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
        this.pathToFolder = new SimpleStringProperty();
        this.textFieldFolder.textProperty().bindBidirectional(this.getPathToFolderProperty());

        // Bind Slider to Volume property
        this.sliderVolume.valueProperty().bindBidirectional(this.mediaPlayer.volumeProperty());

        // Bind CurrentDuration Label to CurrentDuration Property
        Bindings.bindBidirectional(this.labelCurrentDuration.textProperty(), this.mediaPlayer.currentMediaTime(), new DurationStringConverter());

        this.bindSongTrackerSlider();
    }

    /**
     * Initializes specifically the tracker slider for the song.
     */
    private void bindSongTrackerSlider() {
        // Create a DoubleBinding which calculates the value of the duration-slider.
        DoubleBinding durationPercentageBinding = Bindings.createDoubleBinding(() -> {
                    if (this.mediaPlayer.totalMediaDuration().get() != null && this.mediaPlayer.totalMediaDuration().get().toSeconds() > 0) {
                        return this.mediaPlayer.currentMediaTime().get().toSeconds() * 100 / this.mediaPlayer.totalMediaDuration().get().toSeconds();
                    } else {
                        return (double) 0;
                    }
                },
                this.mediaPlayer.currentMediaTime()
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


    //endregion

}
