package ch.wirelesssoundsystem.server.viewmodels;

import ch.wirelesssoundsystem.server.controllers.io.SongsHandler;
import ch.wirelesssoundsystem.server.controllers.media.MediaPlayer;
import ch.wirelesssoundsystem.server.controllers.media.music.AudioPlayer;
import ch.wirelesssoundsystem.server.models.songs.Song;
import ch.wirelesssoundsystem.shared.models.clients.Client;
import ch.wirelesssoundsystem.shared.models.clients.Clients;
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
    private MediaPlayer mediaPlayer;

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
        this.tableViewSongs.setRowFactory( tv -> {
            TableRow<Song> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() >= 2 && (!row.isEmpty())){
                    // Check if its a song.
                    if(Song.class.isInstance(row.getItem()))
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

        this.addDemoData();
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

        if(file != null) {

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

        if(this.mediaPlayer.isPlaying()){
            this.mediaPlayer.pause();
        }
        else if(this.getSelectedSong() != null) {
            System.out.println("Trying to play: " + this.getSelectedSong().getTitle());
            this.mediaPlayer.play(this.getSelectedSong(), true);
        }
    }

    public void onIsPlayingChanged(){
        if(this.mediaPlayer.isPlaying()){
            this.buttonPlayPause.setText("||");
        }
        else{
            this.buttonPlayPause.setText(">");
        }
    }

    private void addDemoData(){
        //DEMO DATA
        this.clientObservableList.add(new Client("Wohnzimmer"));
        this.clientObservableList.add(new Client("Küche"));
        this.clientObservableList.add(new Client("Schlafzimmer"));
        this.clientObservableList.add(new Client("Eingang"));
        this.clientObservableList.add(new Client("Dusche"));
    }
}
