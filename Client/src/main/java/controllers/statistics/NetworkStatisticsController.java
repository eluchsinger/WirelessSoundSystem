package controllers.statistics;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Esteban Luchsinger on 01.03.2016.
 * This controller controlls the network statistics.
 */
public class NetworkStatisticsController {

    public static final String MULTICAST_SERIES_NAME = "Correct";
    public static final String RECONSTRUCTION_SERIES_NAME = "Recovered";
    public static final long DEFAULT_REFRESHING_INTERVAL = 1000;

    private Timer refreshingTimer;
    private int timePassed;

    private XYChart.Series<Integer, Integer> multicastSeries;
    private XYChart.Series<Integer, Integer> recoveredSeries;
    private ObservableList<XYChart.Series<Integer, Integer>> statisticsList;

    private static NetworkStatisticsController instance;
    private int currentReceivedMulticastPackets;
    private int currentReceivedRecoveryPackets;
    private long totalPacketsExpected;

    private NetworkStatisticsController(){
        // Create an observable List that is synchronized.
        this.statisticsList = FXCollections
                .synchronizedObservableList(FXCollections.observableArrayList());

        XYChart.Series<Integer, Integer> multicastSeries = new XYChart.Series<>(MULTICAST_SERIES_NAME, FXCollections.observableArrayList());
        XYChart.Series<Integer, Integer> recoveredSeries = new XYChart.Series<>(RECONSTRUCTION_SERIES_NAME, FXCollections.observableArrayList());

        this.multicastSeries = multicastSeries;
        this.recoveredSeries = recoveredSeries;
        this.statisticsList.add(this.multicastSeries);
        this.statisticsList.add(this.recoveredSeries);

        this.totalPacketsExpected = 0;
        this.currentReceivedMulticastPackets = 0;
        this.currentReceivedRecoveryPackets = 0;

        this.refreshingTimer = new Timer(true);
    }

    public static NetworkStatisticsController getInstance(){
        if(instance == null)
            instance = new NetworkStatisticsController();

        return instance;
    }

    /**
     * Returns the list with the statistics.
     * THE LIST IS NOT SYNCHRONIZED, USE ONLY ON UI THREAD!
     * (For manipulating operations, use the provided Threadsafe methods)
     * @return Observable List for use on UI Thread.
     */
    public ObservableList<XYChart.Series<Integer, Integer>> getStatisticsList(){
        return this.statisticsList;
    }

    /**
     * Returns the amount of packets that have been received if the cache is complete.
     * @return
     */
    public synchronized long getTotalPacketsExpected() {
        return this.totalPacketsExpected;
    }

    /**
     * Sets the total amount of packets expected to be received.
     * @param value
     */
    public synchronized void setTotalPacketsExpected(long value) {
        this.totalPacketsExpected = value;
    }

    /**
     * Call, when a packet is received trough UDP multicast group
     */
    public synchronized void addReceivedPacketMulticast(){
        this.currentReceivedMulticastPackets += 5;
    }

    /**
     * Call, when a packet is received trough TCP Unicast. (Recovery)
     */
    public synchronized void addReceivedPacketRecovery(){
        this.currentReceivedRecoveryPackets++;
    }

    /**
     * Reset the measurements.
     * Synchronized
     */
    public synchronized void resetMeasurement(){

        Platform.runLater(() -> {
            this.multicastSeries.getData().removeAll();
            this.recoveredSeries.getData().removeAll();
            this.multicastSeries.getData().add(new XYChart.Data<>(0, 0));
            this.recoveredSeries.getData().add(new XYChart.Data<>(0, 0));
        });
    }

    /**
     * Start the statistics controller.
     */
    public void start(){
        this.timePassed = 0;
        this.resetMeasurement();

        this.refreshingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timePassed++;
                refreshMeasurement();
            }
        }, 1000L, DEFAULT_REFRESHING_INTERVAL);
    }

    public void stop(){
        this.refreshingTimer.cancel();
    }

    /**
     * Refreshes the measurement, adding a data-point with the current amount of received packets.
     */
    private void refreshMeasurement(){
        if(this.getTotalPacketsExpected() > 0) {
            int percentageMulticastPackets = (int)(this.currentReceivedMulticastPackets * 100 / this.getTotalPacketsExpected());
            int percentageRecoveryPackets = (int)(this.currentReceivedRecoveryPackets * 100 / this.getTotalPacketsExpected());
            Platform.runLater(() -> {
                this.multicastSeries.getData().add(new XYChart.Data<>(timePassed, percentageMulticastPackets));
                this.recoveredSeries.getData().add(new XYChart.Data<>(timePassed, percentageRecoveryPackets));
            });
        }
    }
}
