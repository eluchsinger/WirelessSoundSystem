package viewmodels;

import controllers.io.SongsHandler;
import controllers.media.MediaPlayer;
import controllers.media.music.AudioPlayer;
import controllers.networking.streaming.music.MusicStreamController;
import models.clients.Client;
import models.clients.Clients;
import models.songs.Song;
import utils.DurationStringConverter;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.util.List;

/**
 * Created by Esteban Luchsinger on 30.11.2015.
 */
public class MainWindowViewModel {
    private MediaPlayer<Song> mediaPlayer;
    private MusicStreamController musicStreamController;

    // Properties
    private StringProperty pathToFolder;
    private ObservableList<Song> songObservableList;
    private ObservableList<Client> clientObservableList;

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
    private ListView<Client> listViewClients;

    @FXML
    private TableView<Song> tableViewSongs;

    @FXML
    private TableColumn tableColumnTitle;

    @FXML
    private TableColumn tableColumnArtist;

    @FXML
    private Slider sliderVolume;

    @FXML
    private Slider songTrackerSlider;

    @FXML
    private Label labelCurrentDuration;

    /* Constructor */

    /**
     * Is called, when the window has been initialized.
     */
    @FXML
    protected void initialize() {
        this.pathToFolder = new SimpleStringProperty();

        this.textFieldFolder.textProperty().bindBidirectional(this.getPathToFolderProperty());

        this.songObservableList = FXCollections.observableArrayList();

        // Init Media Player
        this.mediaPlayer = new AudioPlayer(this.songObservableList);
        this.mediaPlayer.isPlayingProperty().addListener((observable, oldValue, newValue) -> this.onIsPlayingChanged());

        // Init MusicStreamService
        this.musicStreamController = new MusicStreamController();

        // Init Table
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

        this.clientObservableList = Clients.getInstance().getClients();
        this.listViewClients.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.listViewClients.setItems(this.clientObservableList);

        // Bind Slider to Volume property
        this.sliderVolume.valueProperty().bindBidirectional(this.mediaPlayer.volumeProperty());

        // Bind CurrentDuration Label to CurrentDuration Property
        Bindings.bindBidirectional(this.labelCurrentDuration.textProperty(), this.mediaPlayer.currentMediaTime(), new DurationStringConverter());

        this.bindSongTrackerSlider();
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

    /* Events */

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
            System.out.println("Trying to play: " + this.getSelectedSong().getTitle());
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
     * This method gets called, when the isPlaying property of the mediaPlayer changes.
     * It handles the play/pause button behavior.
     */
    public void onIsPlayingChanged() {
        if (this.mediaPlayer.isPlaying()) {
            this.buttonPlayPause.setId("pause-button");
            this.buttonPlayPause.setSelected(true);

            if(this.getSelectedSong() != null) {
                // Start streaming...
                System.out.println("Streaming the new song: " + this.getSelectedSong().getTitle());
//                Mp3NetworkStream.streamSong(this.getSelectedSong());
                this.musicStreamController.play(this.getSelectedSong());
            }
        } else {
            this.buttonPlayPause.setId("play-button");
            this.buttonPlayPause.setSelected(false);
        }
    }

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

    private void unbindSongTrackerSlider() {

    }
}
