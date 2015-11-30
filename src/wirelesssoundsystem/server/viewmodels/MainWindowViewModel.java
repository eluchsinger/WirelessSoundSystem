package wirelesssoundsystem.server.viewmodels;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import wirelesssoundsystem.server.controllers.SongsHandler;
import wirelesssoundsystem.server.models.songs.Song;

import java.io.File;
import java.util.List;

/**
 * Created by eluch on 30.11.2015.
 */
public class MainWindowViewModel {
    // Properties
    private StringProperty pathToFolder;
    private ObservableList<Song> songObservableList;

    /* Elements */
    @FXML
    private Button buttonSearch;

    @FXML
    private TextField textFieldFolder;

    @FXML
    private ListView<Song> listViewSongs;

    /* Constructor */
    /**
     * Is called, when the window has been initialized.
     */
    @FXML
    protected void initialize(){
        this.pathToFolder = new SimpleStringProperty();
        this.textFieldFolder.textProperty().bindBidirectional(getPathToFolderProperty());
        this.songObservableList = FXCollections.observableArrayList();
        this.listViewSongs.setItems(songObservableList);
    }

    /* Properties */
    public final String getPathToFolder(){
        return this.pathToFolder.get();
    }

    public final void setPathToFolder(String path){
        this.pathToFolder.set(path);
    }

    public StringProperty getPathToFolderProperty(){
        return this.pathToFolder;
    }

    /* Events */
    @FXML
    public void onSearchButtonClicked(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Ordner auswählen");
        File file = directoryChooser.showDialog(null);

        this.setPathToFolder(file.getPath());

        SongsHandler handler = new SongsHandler();
        List<Song> songs = handler.loadSongsFromDir(file.getPath());
        this.songObservableList.setAll(songs);
    }
}
