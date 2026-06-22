package de.srh_dr.mediamanagementtoolmmt.controller;

import de.srh_dr.mediamanagementtoolmmt.dto.ExternalMediaSearchResult;
import de.srh_dr.mediamanagementtoolmmt.services.MediaIntegrationFacade;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class MediaSearchDialogController {
    @FXML private VBox layerSelection;
    @FXML private VBox layerLoading;
    @FXML private VBox layerResults;
    @FXML private Label resultsLabel;
    @FXML private Label loadingLabel;
    @FXML private ListView<ExternalMediaSearchResult> resultsListView;
    @FXML private Button importButton;

    private MediaIntegrationFacade facade;
    private String searchQuery;
    private MediaIntegrationFacade.ApiSource selectedSource;
    private ExternalMediaSearchResult selectedResult = null;

    public void setupDialog(MediaIntegrationFacade facade, String searchQuery) {
        this.facade = facade;
        this.searchQuery = searchQuery;

        resultsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ExternalMediaSearchResult item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || item == null) {
                    setText(null);
                }else{
                    setText(item.title() + " (" + item.releaseDate() + ")");
                }
            }
        });

        resultsListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> importButton.setDisable(newValue == null)
        );

        showLayer(layerSelection);
    }

    @FXML
    private void handleSelectGoogleBooks(){
        this.selectedSource = MediaIntegrationFacade.ApiSource.GOOGLE_BOOKS;
        executeShallowSearch();
    }

    @FXML
    private void handleSelectTMDB(){
        this.selectedSource = MediaIntegrationFacade.ApiSource.TMDB;
        executeShallowSearch();
    }

    private void executeShallowSearch(){
        loadingLabel.setText(LanguageManager.getString("ui.searching") + " " + selectedSource.name() + "...");
        showLayer(layerLoading);

        Task<List<ExternalMediaSearchResult>> searchTask = new Task<List<ExternalMediaSearchResult>>() {
            @Override
            protected List<ExternalMediaSearchResult> call(){
                return facade.fetchAndSyncByTitle(searchQuery, selectedSource);
            }
        };

        searchTask.setOnSucceeded(event -> {
            List<ExternalMediaSearchResult> results = searchTask.getValue();
            if(results == null || results.isEmpty()){
                resultsListView.getItems().clear();
                resultsLabel.setText("Ain't nobody here but us chickens");
                showLayer(layerResults);
            }else{
                resultsListView.getItems().setAll(results);
                showLayer(layerResults);
            }
        });
        new Thread(searchTask).start();
    }

    @FXML
    private void handleImport(){
        ExternalMediaSearchResult selectedItem = resultsListView.getSelectionModel().getSelectedItem();
        if(selectedItem == null){return;}

        loadingLabel.setText(LanguageManager.getString("ui.fetchingDetails"));
        showLayer(layerLoading);

        Task<ExternalMediaSearchResult> fetchTask = new Task<>() {
            @Override
            protected ExternalMediaSearchResult call(){
                return facade.fetchAndSyncDetails(selectedItem.remoteId(), selectedSource);
            }
        };

        fetchTask.setOnSucceeded(event -> {
            this.selectedResult = fetchTask.getValue();
            closeDialog();
        });

        new Thread(fetchTask).start();
    }

    @FXML
    private void handleCancel(){
        closeDialog();
    }

    //helper
    private void closeDialog(){
        Stage stage = (Stage) layerSelection.getScene().getWindow();
        stage.close();
    }

    private void showLayer(Node layerToShow) {
        layerSelection.setVisible(layerToShow == layerSelection);
        layerSelection.setManaged(layerToShow == layerSelection);

        layerLoading.setVisible(layerToShow == layerLoading);
        layerLoading.setManaged(layerToShow == layerLoading);

        layerResults.setVisible(layerToShow == layerResults);
        layerResults.setManaged(layerToShow == layerResults);
    }

    public ExternalMediaSearchResult getSelectedMedia(){
        return selectedResult;
    }
}
