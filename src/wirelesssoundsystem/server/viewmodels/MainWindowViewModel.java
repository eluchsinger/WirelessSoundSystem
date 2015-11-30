package wirelesssoundsystem.server.viewmodels;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * Created by eluch on 30.11.2015.
 */
public class MainWindowViewModel {
    @FXML
    Button buttonSearch;

    @FXML
    TextField textFieldFolder;

    private String pathToFolder;

    public String getPathToFolder(){
        return this.pathToFolder;
    }

    public void setPathToFolder(String path){
        this.pathToFolder = path;
    }

    @FXML
    public void onSearchButtonClicked(){

    }
}
