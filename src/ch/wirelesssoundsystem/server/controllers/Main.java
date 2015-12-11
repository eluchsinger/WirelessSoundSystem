package ch.wirelesssoundsystem.server.controllers;

import ch.wirelesssoundsystem.server.controllers.networking.discovery.DiscoveryService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        // Setting logger
        Logger log = LogManager.getLogManager().getLogger("");
        for(Handler h : log.getHandlers()){
            h.setLevel(Level.INFO);
        }

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
