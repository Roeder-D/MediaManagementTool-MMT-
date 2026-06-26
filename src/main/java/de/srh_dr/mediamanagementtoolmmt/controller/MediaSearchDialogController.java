package de.srh_dr.mediamanagementtoolmmt.controller;

import de.srh_dr.mediamanagementtoolmmt.dto.ApiSource;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

//Handles the API search for MediaFormController
public class MediaSearchDialogController {
    Logger LOGGER = LoggerFactory.getLogger(MediaSearchDialogController.class);

    @FXML private VBox layerSelection;
    @FXML private VBox layerLoading;
    @FXML private VBox layerResults;
    @FXML private Label resultsLabel;
    @FXML private Label loadingLabel;
    @FXML private ListView<ExternalMediaSearchResult> resultsListView;
    @FXML private Button importButton;

    private MediaIntegrationFacade facade;
    private String searchQuery;
    private ApiSource selectedSource;
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
                } else if (item.artists() == null || item.artists().isEmpty()) {
                    setText(item.title() + " (" + item.releaseDate() + ")");
                } else{
                    setText(item.title() + " (" + item.releaseDate() + ") , " + item.artists().getFirst().getFullName());
                }
            }
        });

        resultsListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> importButton.setDisable(newValue == null)
        );

        showLayer(layerSelection);
    }

    //API-selection
    @FXML
    private void handleSelectGoogleBooks(){
        this.selectedSource = ApiSource.GOOGLE_BOOKS;
        executeShallowSearch();
    }

    @FXML
    private void handleSelectTMDB(){
        this.selectedSource = ApiSource.TMDB;
        executeShallowSearch();
    }
    //TMDB only returns limited data for a title search
    private void executeShallowSearch(){
        loadingLabel.setText(LanguageManager.getString("ui.searching") + " " + selectedSource.name() + "...");
        showLayer(layerLoading);

        Task<List<ExternalMediaSearchResult>> searchTask = new Task<>() {
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
        searchTask.setOnFailed(event -> {
            Throwable exception = searchTask.getException();
            if(exception != null){
                LOGGER.error(exception.getMessage(), exception);
            }
        });
        new Thread(searchTask).start();
    }

    //load all data on selection
    @FXML
    private void handleImport(){
        ExternalMediaSearchResult selectedItem = resultsListView.getSelectionModel().getSelectedItem();
        if(selectedItem == null){return;}

        loadingLabel.setText(LanguageManager.getString("ui.fetchingDetails"));
        showLayer(layerLoading);

        if(ApiSource.TMDB.equals(selectedSource)) {
            Task<ExternalMediaSearchResult> fetchTask = new Task<>() {
                @Override
                protected ExternalMediaSearchResult call() {
                    return facade.fetchAndSyncDetails(selectedItem.remoteId(), selectedSource);
                }
            };

            fetchTask.setOnSucceeded(event -> {
                this.selectedResult = fetchTask.getValue();
                closeDialog();
            });
            new Thread(fetchTask).start();
        }else {
            this.selectedResult = selectedItem;
            closeDialog();
        }
    }

    //cancel transaction
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
