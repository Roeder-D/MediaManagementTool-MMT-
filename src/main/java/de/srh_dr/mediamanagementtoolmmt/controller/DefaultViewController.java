package de.srh_dr.mediamanagementtoolmmt.controller;

import de.srh_dr.mediamanagementtoolmmt.data.FranchiseDAO;
import de.srh_dr.mediamanagementtoolmmt.data.GenreDAO;
import de.srh_dr.mediamanagementtoolmmt.data.StatisticsDAO;
import de.srh_dr.mediamanagementtoolmmt.data.TagDAO;
import de.srh_dr.mediamanagementtoolmmt.services.DBConnection;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DefaultViewController implements MainControllerAware{
    private static final Logger LOGGER =  Logger.getLogger(DefaultViewController.class.getName());

    @FXML private Label totalTitlesField;
    @FXML private Label lentTitlesField;
    @FXML private Label lostTitlesField;
    @FXML private PieChart mediaTypePieChart;
    @FXML private Label totalTagsField;
    @FXML private Label totalGenresField;
    @FXML private Label totalFranchisesField;

    private static MainController mainController;

    @Override
    public void setMainController(MainController mainController){
        DefaultViewController.mainController = mainController;
    }
    @Override
    public MainController getMainController(){
        return mainController;
    }

    private final StatisticsDAO statisticsDAO = StatisticsDAO.getInstance();
    private final TagDAO tagDAO =  TagDAO.getInstance();
    private final GenreDAO genreDAO =  GenreDAO.getInstance();
    private final FranchiseDAO franchiseDAO =  FranchiseDAO.getInstance();

    @FXML
    private void initialize() {
        lentTitlesField.setText("...");

        if(DBConnection.isConnected()){
            loadDashboardData();
        }else{
            lentTitlesField.setText(LanguageManager.getString("info.waitingForDBConnection"));
        }
    }

    //populate dashboard
    public void loadDashboardData() {
        new Thread(() -> {
            try {
                Map<String, Integer> collectionStatistics = statisticsDAO.getCollectionStatistics();
                Map<String, Integer> distributionStatistics = statisticsDAO.getMediaTypeDistribution();
                int tagCount = tagDAO.countRows();
                int genreCount = genreDAO.countRows();
                int franchiseCount = franchiseDAO.countRows();

                ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
                distributionStatistics.forEach((key, value) -> pieChartData.add(new PieChart.Data(key, value)));

                Platform.runLater(() -> {
                    totalTitlesField.setText(String.valueOf(collectionStatistics.getOrDefault("total", 0)));
                    lentTitlesField.setText(String.valueOf(collectionStatistics.getOrDefault("lent", 0)));
                    lostTitlesField.setText(String.valueOf(collectionStatistics.getOrDefault("lost", 0)));

                    mediaTypePieChart.setData(pieChartData);

                    totalTagsField.setText(String.valueOf(tagCount));
                    totalGenresField.setText(String.valueOf(genreCount));
                    totalFranchisesField.setText(String.valueOf(franchiseCount));
                });
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to load dashboard statistics: " + e.getMessage(), e);
            }

        }).start();
    }
}
