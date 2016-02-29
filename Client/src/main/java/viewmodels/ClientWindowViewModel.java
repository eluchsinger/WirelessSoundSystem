package viewmodels;

import controllers.io.CacheHandler;
import controllers.networking.streaming.music.MusicStreamingService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Created by Esteban Luchsinger on 08.12.2015.
 */
public class ClientWindowViewModel {

    ObservableList<XYChart.Series<Integer, Integer>> statisticsList;

    @FXML
    private Label labelStatus;

    @FXML
    private StackedAreaChart<Integer, Integer> statisticsChart;

    @FXML
    public void onButtonPlayClicked(){
        MediaPlayer player = new MediaPlayer(
                new Media(CacheHandler.getInstance()
                        .getTempFile()
                        .toURI()
                        .toString()));

        player.play();
    }


    @FXML
    protected void initialize(){
        MusicStreamingService.getInstance().addServiceStatusChangedListener(newStatus -> {
            System.out.println("New Status: " + newStatus.name());
            // Todo: This call has to be from UI-Thread!
            Platform.runLater(() -> {
                labelStatus.setText(newStatus.name());
            });
        });
        this.statisticsList = FXCollections.observableArrayList();

        this.statisticsChart.setData(this.statisticsList);

        XYChart.Series<Integer, Integer> multicastSeries = new XYChart.Series<>("Correct", FXCollections.observableArrayList());
        XYChart.Series<Integer, Integer> recoveredSeries = new XYChart.Series<>("Recovered", FXCollections.observableArrayList());

        this.statisticsList.add(multicastSeries);
        this.statisticsList.add(recoveredSeries);

        multicastSeries.getData().add(new XYChart.Data<>(0, 1));
        multicastSeries.getData().add(new XYChart.Data<>(1,2));

    }


    public ClientWindowViewModel(){
    }
}
