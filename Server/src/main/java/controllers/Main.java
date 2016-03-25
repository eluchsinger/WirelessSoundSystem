package controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import viewmodels.MainWindowViewModel;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        System.setProperty("java.net.preferIPv4Stack" , "true");
        // Setting logger
        Logger log = LogManager.getLogManager().getLogger("");
        for(Handler h : log.getHandlers()){
//            h.setLevel(Level.INFO);
            h.setLevel(Level.WARNING);
        }


        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainWindow.fxml"));
        Parent root = loader.load();

        // Set the stage to handle closing even inside the viewmodel.
        Object o = loader.getController();
        if(o.getClass() == MainWindowViewModel.class){
            ((MainWindowViewModel)o).setStage(primaryStage);
        }

        primaryStage.setTitle("Wireless Sound System");
        primaryStage.getIcons().add(new Image(String.valueOf(getClass().getResource("/views/icons/png/Logo.png"))));
        primaryStage.setScene(new Scene(root, 800, 600));

        primaryStage.show();
        primaryStage.setMinHeight(primaryStage.getHeight());
        primaryStage.setMinWidth(primaryStage.getWidth());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
