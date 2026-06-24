package de.srh_dr.mediamanagementtoolmmt.controller;

import de.srh_dr.mediamanagementtoolmmt.data.FranchiseDAO;
import de.srh_dr.mediamanagementtoolmmt.data.GenreDAO;
import de.srh_dr.mediamanagementtoolmmt.data.StatisticsDAO;
import de.srh_dr.mediamanagementtoolmmt.data.TagDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DefaultViewController{
    private static final Logger LOGGER =  Logger.getLogger(DefaultViewController.class.getName());

    @FXML private Label totalTitlesField;
    @FXML private Label lentTitlesField;
    @FXML private Label lostTitlesField;
    @FXML private PieChart mediaTypePieChart;
    @FXML private Label totalTagsField;
    @FXML private Label totalGenresField;
    @FXML private Label totalFranchisesField;

    private final StatisticsDAO statisticsDAO = StatisticsDAO.getInstance();
    private final TagDAO tagDAO =  TagDAO.getInstance();
    private final GenreDAO genreDAO =  GenreDAO.getInstance();
    private final FranchiseDAO franchiseDAO =  FranchiseDAO.getInstance();

    @FXML
    private void initialize() {
        loadDashboardData();
    }

    //populate dashboard
    public void loadDashboardData(){
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

            totalTagsField.setText(String.valueOf(tagDAO.countRows()));
            totalGenresField.setText(String.valueOf(genreDAO.countRows()));
            totalFranchisesField.setText(String.valueOf(franchiseDAO.countRows()));
        }catch(Exception e){
            LOGGER.log(Level.WARNING, "Failed to load dashboard statistics: " + e.getMessage(), e);
        }
    }
}
