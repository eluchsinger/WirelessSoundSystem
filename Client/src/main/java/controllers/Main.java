package controllers;

import controllers.networking.discovery.DiscoveryService;
import controllers.networking.streaming.MusicStreamingService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.out.println("Starting DiscoveryService...");
        DiscoveryService.getInstance().start();
        MusicStreamingService.getInstance().start();

        Parent root = FXMLLoader.load(getClass().getResource("/views/ClientWindow.fxml"));
        primaryStage.setTitle("Wireless Sound System (Client)");
        primaryStage.setScene(new Scene(root, 600, 400));

        primaryStage.setOnCloseRequest((event) -> {
            System.out.println("Stopping DiscoveryService...");
            DiscoveryService.getInstance().stop();
            MusicStreamingService.getInstance().stop();
        });
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
