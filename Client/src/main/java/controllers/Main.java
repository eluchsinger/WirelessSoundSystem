package controllers;

import controllers.networking.discovery.DiscoveryService;
import controllers.networking.streaming.music.MusicStreamingService;
import controllers.networking.streaming.music.UDPMusicStreamingService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import viewmodels.ClientWindowViewModel;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        System.setProperty("java.net.preferIPv4Stack", "true");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ClientWindow.fxml"));
        Parent root = loader.load();

        // Add Stage object to the client window view model.
        Object o = loader.getController();
        if(ClientWindowViewModel.class == o.getClass()){
            ((ClientWindowViewModel)o).setStage(primaryStage);
        }
        primaryStage.setTitle("Wireless Sound System (Client)");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

}