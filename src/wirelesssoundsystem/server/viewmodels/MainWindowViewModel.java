package wirelesssoundsystem.server.viewmodels;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.media.AudioTrack;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.Track;
import javafx.stage.DirectoryChooser;
import wirelesssoundsystem.server.controllers.SongsHandler;
import wirelesssoundsystem.server.models.songs.Song;

import java.io.File;
import java.net.URI;
import java.util.List;

/**
 * Created by eluch on 30.11.2015.
 */
public class MainWindowViewModel {
    private MediaPlayer mediaPlayer;

    // Properties
    private StringProperty pathToFolder;
    private ObservableList<Song> songObservableList;

    /* Elements */
    @FXML
    private Button buttonSearch;

    @FXML
    private ToggleButton buttonPlayPause;

    @FXML
    private TextField textFieldFolder;

    @FXML
    private ListView<Song> listViewSongs;

    /* Constructor */

    /**
     * Is called, when the window has been initialized.
     */
    @FXML
    protected void initialize() {
        this.pathToFolder = new SimpleStringProperty();

        this.textFieldFolder.textProperty().bindBidirectional(this.getPathToFolderProperty());

        this.songObservableList = FXCollections.observableArrayList();
        this.listViewSongs.setItems(songObservableList);
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
        return this.listViewSongs.getSelectionModel().getSelectedItem();
    }

    /* Events */
    @FXML
    public void onSearchButtonClicked() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Ordner auswählen");
        File file = directoryChooser.showDialog(null);

        // Sets the property for the textBox.
        this.setPathToFolder(file.getPath());

        // Loads the songs.
        SongsHandler handler = new SongsHandler();
        List<Song> songs = handler.loadSongsFromDir(file.getPath());
        this.songObservableList.setAll(songs);
    }

    @FXML
    public void onButtonPlayPauseClicked() {
        if (this.mediaPlayer != null){
            switch(this.mediaPlayer.getStatus()){
                case PLAYING:
                    this.mediaPlayer.pause();
                    this.buttonPlayPause.setText("PLAY");
                    break;
                case PAUSED:
                    mediaPlayer.play();
                    this.buttonPlayPause.setText("PAUSE");
                    break;
                default:
                    startPlaying(this.getSelectedSong());
                    this.buttonPlayPause.setText("PAUSE");
                    break;
            }
        }
        else{
            startPlaying(this.getSelectedSong());
            this.buttonPlayPause.setText("PAUSE");
        }
    }

    private void startPlaying(Song song){
        // Get selected file
        System.out.println("Current Selected Song: " + song.getTitle());

        // Have to create a temporary file to convert the path to a URI.
        File tempFile = new File(song.getPath());

        Media media = new Media(tempFile.toURI().toString());

        for (Track track : media.getTracks()) {
            if (AudioTrack.class.isInstance(track.getClass())) {

                System.out.println("Is an AudioTrack");


                System.out.println("With name: " + ((AudioTrack) track).getName());
            }
        }

        this.mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
    }
}
