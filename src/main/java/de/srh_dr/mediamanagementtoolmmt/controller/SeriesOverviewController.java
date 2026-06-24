package de.srh_dr.mediamanagementtoolmmt.controller;

import de.srh_dr.mediamanagementtoolmmt.data.SeriesDAO;
import de.srh_dr.mediamanagementtoolmmt.model.AltTitle;
import de.srh_dr.mediamanagementtoolmmt.model.Series;
import de.srh_dr.mediamanagementtoolmmt.util.AlertManager;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SeriesOverviewController{
    private static final Logger LOGGER = Logger.getLogger(SeriesOverviewController.class.getName());

    @FXML private VBox viewContainer;
    @FXML private TextField searchField;
    @FXML private TextField yearFilterField;
    @FXML private TableView<Series> seriesTable;
    @FXML private TableColumn<Series, String> nameCol;
    @FXML private TableColumn<Series, Number> yearCol;
    @FXML private TableColumn<Series, Number> countCol;

    private final SeriesDAO seriesDAO = SeriesDAO.getInstance();
    private final ObservableList<Series> seriesList = FXCollections.observableArrayList();
    private FilteredList<Series> filteredSeries;

    //populate view
    @FXML
    private void initialize(){
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        yearCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getStartYear()));
        countCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getNumberOfTitles()));

        // double-click listener
        seriesTable.setRowFactory(tv -> {
            TableRow<Series> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && (!row.isEmpty())) {
                    openSeriesPopup(row.getItem());
                }
            });
            return row;
        });
        loadSeries();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilter());
        yearFilterField.textProperty().addListener((observable, oldValue, newValue) -> applyFilter());
    }

    @FXML
    private void loadSeries(){
        seriesList.setAll(seriesDAO.findAll());
        filteredSeries = new FilteredList<>(seriesList, p -> true);

        SortedList<Series> sortedSeries = new SortedList<>(filteredSeries);
        sortedSeries.comparatorProperty().bind(seriesTable.comparatorProperty());
        seriesTable.setItems(sortedSeries);

        applyFilter();
    }

    //filters
    @FXML
    private void applyFilter(){
        String searchText = searchField.getText().toLowerCase();
        String yearText = yearFilterField.getText().trim();

        filteredSeries.setPredicate(series -> {
            if (!yearText.isEmpty()) {
                String seriesYear = String.valueOf(series.getStartYear());
                if (!seriesYear.startsWith(yearText)) {
                    return false;
                }
            }

            if (searchText.isEmpty()) {
                return true;
            }

            String name = series.getName() != null ? series.getName().toLowerCase() : "";

            List<AltTitle> altTitleList = series.getAltTitles();
            StringBuilder altTitles = new StringBuilder();
            for (AltTitle altTitle : altTitleList) {
                if(altTitle.getTitle() != null && !altTitle.getTitle().isEmpty()) {
                    altTitles.append(altTitle.getTitle()).append(" ");
                }
            }

            return name.contains(searchText) || altTitles.toString().contains(searchText);
        });
    }

    //edit series popup
    private void openSeriesPopup(Series selectedSeries) {
        Dialog<Series> dialog = new Dialog<>();
        dialog.setTitle(LanguageManager.getString("ui.edit_series"));
        dialog.setHeaderText(LanguageManager.getString("ui.edit_series"));

        ButtonType saveButtonType = new ButtonType(LanguageManager.getString("ui.submit"),  ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButtonType = new ButtonType(LanguageManager.getString("ui.delete"),  ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, saveButtonType, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        TextField nameField = new TextField();
        nameField.setText(selectedSeries.getName() !=  null ? selectedSeries.getName() : "--ERROR--");
        TextField yearField = new TextField();
        yearField.setText(selectedSeries.getStartYear() > 0 ? String.valueOf(selectedSeries.getStartYear()) : "");
        TextField countField = new TextField();
        countField.setText(selectedSeries.getNumberOfTitles() > 0 ? String.valueOf(selectedSeries.getNumberOfTitles()) : "");

        ListView<AltTitle> altTitleListView = new ListView<>();
        ObservableList<AltTitle> altTitles = FXCollections.observableArrayList();
        altTitleListView.setItems(altTitles);
        altTitleListView.setPrefHeight(100);

        altTitleListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(AltTitle item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || item == null) {
                    setText(null);
                }else{
                    setText(item.getTitle());
                }
            }
        });

        TextField newAltTitleField = new TextField();
        newAltTitleField.setPromptText(LanguageManager.getString("ui.new_alt_title"));

        Button addAltTitleBtn = new Button(LanguageManager.getString("ui.add"));
        Button removeAltTitleBtn = new Button(LanguageManager.getString("ui.remove"));

        addAltTitleBtn.setOnAction(event -> {
            String newText = newAltTitleField.getText();
            if(!newText.isEmpty()) {
                AltTitle newAltTitle = new AltTitle(0, newText, true);

                selectedSeries.addAltTitle(newAltTitle);
                altTitles.add(newAltTitle);
                newAltTitleField.clear();
            }
        });

        removeAltTitleBtn.setOnAction(event -> {
            AltTitle selectedAltTitle = altTitleListView.getSelectionModel().getSelectedItem();
            if(selectedAltTitle != null) {
                selectedSeries.removeAltTitleById(selectedAltTitle.getId());
                altTitles.remove(selectedAltTitle);
            }
        });

        HBox altTitleControls = new HBox(10, newAltTitleField, addAltTitleBtn, removeAltTitleBtn);

        gridPane.add(new Label(LanguageManager.getString("ui.series_name")), 0, 0);
        gridPane.add(nameField, 1, 0);
        gridPane.add(new Label(LanguageManager.getString("ui.series_year")), 0, 1);
        gridPane.add(yearField, 1, 1);
        gridPane.add(new Label(LanguageManager.getString("ui.series_count")), 0, 2);
        gridPane.add(countField, 1, 2);

        gridPane.add(new Label(LanguageManager.getString("ui.altTitles")), 0, 3);
        gridPane.add(altTitleListView, 1, 3);
        gridPane.add(altTitleControls, 1, 4);

        dialog.getDialogPane().setContent(gridPane);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                selectedSeries.setName(nameField.getText());

                try {
                    selectedSeries.setStartYear(yearField.getText().isEmpty() ? 0 : Integer.parseInt(yearField.getText().trim()));
                    selectedSeries.setNumberOfTitles(countField.getText().isEmpty() ? 0 : Integer.parseInt(countField.getText().trim()));

                    seriesDAO.save(selectedSeries);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Inalid number format exception", e);
                    AlertManager.showAlert(
                            Alert.AlertType.ERROR,
                            LanguageManager.getString("ui.error"),
                            "warning.invalidNumberFormat",
                            getWindow());
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to add series: " + e.getMessage(), e);
                    AlertManager.showAlert(
                            Alert.AlertType.ERROR,
                            LanguageManager.getString("ui.error"),
                            LanguageManager.getString("error.failedToSave") + e.getMessage(),
                            getWindow());
                }
                return null;
            }

            if (dialogButton == deleteButtonType) {
                boolean confirmDelete = AlertManager.requestConfirmation(
                        LanguageManager.getString("ui.warning"),
                        LanguageManager.getString("warning.confirmDeleteSeries"),
                        getWindow());

                if (confirmDelete) {
                    try {
                        seriesDAO.delete(selectedSeries.getId());
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Failed to delete series: " + e.getMessage(), e);
                        AlertManager.showAlert(
                                Alert.AlertType.ERROR,
                                LanguageManager.getString("ui.error"),
                                e.getMessage(),
                                getWindow());
                    }
                }
            }
            return null;
        });

        dialog.initOwner(getWindow());
        dialog.showAndWait();
        loadSeries();
    }

    //current window
    private Window getWindow(){
        if (viewContainer != null && viewContainer.getScene() != null) {
            return viewContainer.getScene().getWindow();
        }
        return null;
    }
}
