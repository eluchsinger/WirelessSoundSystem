package ch.wirelesssoundsystem.client.controllers;

import ch.wirelesssoundsystem.server.controllers.networking.discovery.DiscoveryService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../views/ClientWindow.fxml"));
        primaryStage.setTitle("Wireless Sound System (Client)");
        primaryStage.setScene(new Scene(root, 600, 400));


        primaryStage.setOnCloseRequest((event) -> {
            System.out.println("Closing client...");
            DiscoveryService.getInstance().stop();
        });
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
