package ch.wirelesssoundsystem.server.viewmodels;

import ch.wirelesssoundsystem.server.controllers.io.SongsHandler;
import ch.wirelesssoundsystem.server.controllers.media.MediaPlayer;
import ch.wirelesssoundsystem.server.controllers.media.music.AudioPlayer;
import ch.wirelesssoundsystem.server.controllers.networking.music.Mp3NetworkStream;
import ch.wirelesssoundsystem.shared.models.clients.Client;
import ch.wirelesssoundsystem.server.models.songs.Song;
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

    /* Constructor */

    /**
     * Is called, when the window has been initialized.
     */
    @FXML
    protected void initialize() {
        this.pathToFolder = new SimpleStringProperty();

        this.textFieldFolder.textProperty().bindBidirectional(this.getPathToFolderProperty());

        this.songObservableList = FXCollections.observableArrayList();
        this.tableViewSongs.setItems(this.songObservableList);
        this.tableViewSongs.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        tableColumnTitle.setCellValueFactory(
                new PropertyValueFactory<Song, String>("title")
        );

        tableColumnArtist.setCellValueFactory(
                new PropertyValueFactory<Song, String>("artist")
        );

        this.clientObservableList = Clients.getInstance().getClients();
        this.listViewClients.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.listViewClients.setItems(this.clientObservableList);

        this.addDemoData();
        this.mediaPlayer = new AudioPlayer(this.songObservableList);

//        this.mediaPlayer.isPlayingProperty().addListener((observable, oldValue, newValue) -> {
//                System.out.println("New isPlayingPropertyValue (" + newValue + ")");
//        });

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
            this.buttonPlayPause.setText(">");
        }
        else {
            this.mediaPlayer.play();
            this.buttonPlayPause.setText("||");
        }
//        if (this.mediaPlayer != null){
//            switch(this.mediaPlayer.getStatus()){
//                case PLAYING:
//                    this.mediaPlayer.pause();
//                    this.buttonPlayPause.setText(">");
//                    break;
//                case PAUSED:
//                    mediaPlayer.play();
//                    this.buttonPlayPause.setText("||");
//                    break;
//                default:
//                    startPlaying(this.getSelectedSong());
//                    this.buttonPlayPause.setText("||");
//                    break;
//            }
//        }
//        else{
//            startPlaying(this.getSelectedSong());
//            this.buttonPlayPause.setText("||");
//        }
    }

//    private void startPlaying(Song song){
//        // Get selected file
//        System.out.println("Current Selected Song: " + song.getTitle());
//
//        // Have to create a temporary file to convert the path to a URI.
//        File tempFile = new File(song.getPath());
//
//        Media media = new Media(tempFile.toURI().toString());
//
//        this.mediaPlayer = new MediaPlayer(media);
//        mediaPlayer.play();
//    }

    private void addDemoData(){
        //DEMO DATA
        this.clientObservableList.add(new Client("Wohnzimmer"));
        this.clientObservableList.add(new Client("Küche"));
        this.clientObservableList.add(new Client("Schlafzimmer"));
        this.clientObservableList.add(new Client("Eingang"));
        this.clientObservableList.add(new Client("Dusche"));
    }
}
