package ch.wirelesssoundsystem.server.controllers;

import ch.wirelesssoundsystem.server.controllers.networking.discovery.DiscoveryService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        System.out.println("Starting discovery Service...");
        DiscoveryService.getInstance().start();

        Parent root = FXMLLoader.load(getClass().getResource("../views/MainWindow.fxml"));
        primaryStage.setTitle("Wireless Sound System");
        primaryStage.setScene(new Scene(root, 800, 600));

        primaryStage.setOnCloseRequest((event) -> {
            System.out.println("Stopping discovery Service...");
            DiscoveryService.getInstance().stop();
        });

        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
