package de.srh_dr.mediamanagementtoolmmt.controller;

import de.srh_dr.mediamanagementtoolmmt.data.StatisticsDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;

import java.util.Map;


public class DefaultViewController implements MainControllerAware{
    @FXML private Label totalTitlesField;
    @FXML private Label lentTitlesField;
    @FXML private Label lostTitlesField;
    @FXML private PieChart mediaTypePieChart;

    private MainController mainController;
    private final StatisticsDAO statisticsDAO =  new StatisticsDAO();

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        Platform.runLater(this::loadDasboardData);
    }

    public void loadDasboardData(){
        try{
            Map<String, Integer> collectionStatistics = statisticsDAO.getCollectionStatistics();
            Map<String, Integer> distributionStatistics = statisticsDAO.getMediaTypeDistribution();

            totalTitlesField.setText(String.valueOf(collectionStatistics.getOrDefault("total", 0)));
            lentTitlesField.setText(String.valueOf(distributionStatistics.getOrDefault("lent", 0)));
            lostTitlesField.setText(String.valueOf(distributionStatistics.getOrDefault("Lost", 0)));

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            for(Map.Entry<String, Integer> entry : distributionStatistics.entrySet()){
                String chartLabel = entry.getKey();
                pieChartData.add(new PieChart.Data(chartLabel, entry.getValue()));
            }
            mediaTypePieChart.setData(pieChartData);
        }catch(Exception e){
            System.err.println("Failed to load dashboard statistics: " + e.getMessage());
        }
    }
}
