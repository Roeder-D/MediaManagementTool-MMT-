package de.srh_dr.mediamanagementtoolmmt.controller;

import de.srh_dr.mediamanagementtoolmmt.util.AlertManager;
import de.srh_dr.mediamanagementtoolmmt.util.ConfigManager;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;
import de.srh_dr.mediamanagementtoolmmt.viewmodel.FilterOption;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MainController {
    private static final Logger LOGGER =  Logger.getLogger(MainController.class.getName());

    public enum ViewState{
        DEFAULT_VIEW, MEDIA_OVERVIEW, ARTIST_OVERVIEW, FRANCHISE_OVERVIEW, LENDING_OVERVIEW, MEDIA_DETAIL, MEDIA_FORM, PUBLISHER_OVERVIEW, LENDING_DETAIL_VIEW, SERIES_OVERVIEW
    }
    private ViewState currentView =  ViewState.DEFAULT_VIEW;
    private int currentPramId = -1;
    private boolean lendingParam = false;

    @FXML
    private void initialize(){
        showDefaultView();
    }

    // HUD
    @FXML private BorderPane viewContainer;

    // Menu
    @FXML
    void handleLanguageSettings() {
        Dialog<Void> languageDialog = new Dialog<>();
        languageDialog.setTitle(LanguageManager.getString("menu.settings.language"));
        languageDialog.setHeaderText(LanguageManager.getString("menu.language.header"));

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        Label langLabel = new Label(LanguageManager.getString("menu.settings.language"));
        ComboBox<FilterOption> langCombo = new ComboBox<>();
        langCombo.getItems().add(new FilterOption("System", "default"));
        langCombo.getItems().add(new FilterOption("English", "en"));
        langCombo.getItems().add(new FilterOption("Deutsch", "de"));
        langCombo.getSelectionModel().select(0);

        //set to current language
        String currentLanguage = "default";
        try {
            currentLanguage = ConfigManager.getAppLanguage();
        }catch (Exception e){
            LOGGER.log(Level.SEVERE, "Error while loading language : " + e.getMessage(), e);
        }
        for(FilterOption filterOption : langCombo.getItems()){
            if( filterOption.getInternalValue().equals(currentLanguage)){
                langCombo.getSelectionModel().select(filterOption);
                break;
            }
        }

        gridPane.add(langLabel, 0, 0);
        gridPane.add(langCombo, 1, 0);

        languageDialog.getDialogPane().setContent(gridPane);

        ButtonType saveButtonType = new ButtonType(LanguageManager.getString("ui.submit"), ButtonBar.ButtonData.OK_DONE);
        languageDialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        languageDialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String newLanguage = langCombo.getValue().getInternalValue();
                try {
                    ConfigManager.setProperty("APP_LANGUAGE", newLanguage);
                }catch (Exception e){
                    LOGGER.log(Level.SEVERE, "Error while setting language : " + e.getMessage(), e);
                    AlertManager.showAlert(
                            Alert.AlertType.ERROR,
                            LanguageManager.getString("ui.error"),
                            LanguageManager.getString("error.failedToSave") + e.getMessage(),
                            getWindow()
                    );
                }
                //Trigger UI reload
                LanguageManager.setLanguage(newLanguage);
                reloadApplicationUI();
            }
            return null;
        });

        languageDialog.initOwner(getWindow());
        languageDialog.showAndWait();

    }

    @FXML
    void handleISBNSettings() {
        Dialog<Void> isbnDialog = new Dialog<>();
        isbnDialog.setTitle(LanguageManager.getString("ui.settings.isbnTypes"));
        isbnDialog.setHeaderText(LanguageManager.getString("ui.settings.isbnType.header"));

        VBox container = new VBox(10);
        Label isbnLabel = new Label(LanguageManager.getString("ui.settings.isbnType.text"));

        try {
            ObservableList<String> values = FXCollections.observableArrayList(ConfigManager.getISBNEnabledMediaTypes());

            TableView<String> isbnTypeTable = new TableView<>(values);
            isbnTypeTable.setEditable(true);
            isbnTypeTable.setPrefHeight(200);

            TableColumn<String, String> typeCol = new TableColumn<>(LanguageManager.getString("ui.settings.isbnType.text"));
            typeCol.setPrefWidth(250);

            typeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
            typeCol.setCellFactory(TextFieldTableCell.forTableColumn());
            typeCol.setOnEditCommit(event ->
                values.set(event.getTablePosition().getRow(), event.getNewValue())
            );

            TableColumn<String, Void> deleteCol = new TableColumn<>(LanguageManager.getString("ui.delete"));
            deleteCol.setPrefWidth(60);

            deleteCol.setCellFactory(param -> new TableCell<>() {
                private final Button deleteBtn = new Button(LanguageManager.getString("ui.delete"));

                {
                    deleteBtn.setOnAction(event -> {
                        String item = getTableView().getItems().get(getIndex());
                        values.remove(item);
                    });
                }

                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(deleteBtn);
                    }
                }
            });

            isbnTypeTable.getColumns().add(typeCol);
            isbnTypeTable.getColumns().add(deleteCol);

            HBox controlBar = new HBox(10);

            TextField inputField = new TextField();
            Button addBtn = new Button(LanguageManager.getString("ui.add"));

            addBtn.setOnAction(event -> {
                if (!inputField.getText().isEmpty() && values.stream().noneMatch(value -> value.equalsIgnoreCase(inputField.getText().trim()))) {
                    isbnTypeTable.getItems().add(inputField.getText().trim());
                }
            });

            controlBar.getChildren().addAll(inputField, addBtn);
            container.getChildren().addAll(isbnLabel, isbnTypeTable, controlBar);

            isbnDialog.getDialogPane().setContent(container);

            ButtonType saveButtonType = new ButtonType(LanguageManager.getString("ui.submit"), ButtonBar.ButtonData.OK_DONE);
            isbnDialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            isbnDialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    String isbnString = String.join(", ", values);
                    try {
                        ConfigManager.setProperty("APP_ISBN_MEDIA_TYPES", isbnString);
                    }catch (Exception e){
                        LOGGER.log(Level.SEVERE, "Error while setting isbn type : " + e.getMessage(), e);
                        AlertManager.showAlert(
                                Alert.AlertType.ERROR,
                                LanguageManager.getString("ui.error"),
                                LanguageManager.getString("error.failedToSave" + e.getMessage()),
                                getWindow());
                    }

                }
                return null;
            });
            isbnDialog.initOwner(getWindow());
            isbnDialog.showAndWait();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while setting isbn type : " + e.getMessage(), e);
           AlertManager.showAlert(
                   Alert.AlertType.ERROR,
                   LanguageManager.getString("ui.error"),
                   LanguageManager.getString("error.failedToLoad") + e.getMessage(),
                   getWindow()
           );
        }
    }

    // Action
    @FXML
    public void showDefaultView(){
        try{
            URL resource = getClass().getResource("/de/srh_dr/mediamanagementtoolmmt/view/DefaultView.fxml");
            FXMLLoader loader = new FXMLLoader(resource);
            loader.setResources(LanguageManager.getBundle());

            Node view = loader.load();

            currentView = ViewState.DEFAULT_VIEW;
            currentPramId = -1;
            lendingParam = false;
            viewContainer.setCenter(view);
        }catch(IOException e){
            LOGGER.log(Level.SEVERE, "Error while setting default view : " + e.getMessage(), e);
            AlertManager.showAlert(
                    Alert.AlertType.ERROR,
                    LanguageManager.getString("ui.error"),
                    e.getMessage(),
                    getWindow());
        }
    }
    @FXML
    void showMediaOverview() {
        currentView = ViewState.MEDIA_OVERVIEW;
        currentPramId = -1;
        lendingParam = false;
        loadView("/de/srh_dr/mediamanagementtoolmmt/view/MediaOverview.fxml");
    }
    @FXML
    void showAddMedia() {
        showMediaFormView(0);
    }
    @FXML
    void showArtists() {
        currentView = ViewState.ARTIST_OVERVIEW;
        currentPramId = -1;
        lendingParam = false;
        loadView("/de/srh_dr/mediamanagementtoolmmt/view/ArtistOverview.fxml");
    }
    @FXML
    void showSeries() {
        currentView = ViewState.SERIES_OVERVIEW;
        currentPramId = -1;
        lendingParam = false;
        loadView("/de/srh_dr/mediamanagementtoolmmt/view/SeriesOverview.fxml");
    }
    @FXML
    void showFranchises() {
        currentView = ViewState.FRANCHISE_OVERVIEW;
        currentPramId = -1;
        lendingParam = false;
        loadView("/de/srh_dr/mediamanagementtoolmmt/view/FranchiseOverview.fxml");
    }
    @FXML
    void showPublishers() {
        currentView = ViewState.PUBLISHER_OVERVIEW;
        currentPramId = -1;
        lendingParam = false;
        loadView("/de/srh_dr/mediamanagementtoolmmt/view/PublisherOverview.fxml");
    }
    @FXML
    void showLendings() {
        currentView = ViewState.LENDING_OVERVIEW;
        currentPramId = -1;
        lendingParam = false;
        loadView("/de/srh_dr/mediamanagementtoolmmt/view/LendingOverview.fxml");
    }


    private void loadView(String fxmlFile) {
        try{
            URL resource = getClass().getResource(fxmlFile);
            if(resource == null){
                LOGGER.log(Level.SEVERE, "Couldn't find view: " + fxmlFile);
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);

            loader.setResources(LanguageManager.getBundle());
            Node view = loader.load();

            Object controller = loader.getController();
            if(controller instanceof MainControllerAware){
                ((MainControllerAware) controller).setMainController(this);
            }

            viewContainer.setCenter(view);
        }catch(IOException e){
            LOGGER.log(Level.SEVERE, "Error while loading view : " + e.getMessage(), e);
            AlertManager.showAlert(
                    Alert.AlertType.ERROR,
                    LanguageManager.getString("ConfigManager.failedToLoad"),
                    e.getMessage(),
                    getWindow());
        }
    }

    public void showMediaDetail(int mediaId){
        try{
            URL resource = getClass().getResource("/de/srh_dr/mediamanagementtoolmmt/view/MediaDetail.fxml");
            FXMLLoader loader = new FXMLLoader(resource);
            loader.setResources(LanguageManager.getBundle());

            Node view = loader.load();

            MediaDetailController detailController = loader.getController();
            if(detailController != null){
                detailController.setMainController(this);
                detailController.loadMediaDetails(mediaId);
            }

            currentView = ViewState.MEDIA_DETAIL;
            currentPramId = mediaId;
            lendingParam = false;
            viewContainer.setCenter(view);
        }catch(IOException e){
            LOGGER.log(Level.SEVERE, "Error while loading view : " + e.getMessage(), e);
            AlertManager.showAlert(
                    Alert.AlertType.ERROR,
                    LanguageManager.getString("ConfigManager.failedToLoad"),
                    e.getMessage(),
                    getWindow());
        }
    }

    public void showMediaFormView(int mediaId){
        try{
            URL resource = getClass().getResource("/de/srh_dr/mediamanagementtoolmmt/view/MediaForm.fxml");
            FXMLLoader loader = new FXMLLoader(resource);
            loader.setResources(LanguageManager.getBundle());

            Node view = loader.load();

            MediaFormController formController = loader.getController();
            if(formController != null){
                formController.setMainController(this);

                if(mediaId > 0){
                    formController.loadMedia(mediaId);
                }else{
                    formController.refresh();
                }

                currentView = ViewState.MEDIA_FORM;
                currentPramId = mediaId;
                lendingParam = false;
                viewContainer.setCenter(view);
            }
        }catch(IOException e){
            LOGGER.log(Level.SEVERE, "Error while loading view : " + e.getMessage(), e);
            AlertManager.showAlert(
                    Alert.AlertType.ERROR,
                    LanguageManager.getString("ui.error"),
                    e.getMessage(),
                    getWindow());
        }
    }

    public void showLendingView(int id, boolean viaMedia){
        try{
            URL resource = getClass().getResource("/de/srh_dr/mediamanagementtoolmmt/view/LendingView.fxml");
            FXMLLoader loader = new FXMLLoader(resource);
            loader.setResources(LanguageManager.getBundle());

            Node view = loader.load();

            LendingViewController lendingController = loader.getController();
            if(lendingController != null){
                lendingController.setMainController(this);
                lendingController.loadLendingData(id, viaMedia);
            }

            currentView = ViewState.LENDING_DETAIL_VIEW;
            currentPramId = id;
            lendingParam = true;
            viewContainer.setCenter(view);
        }catch(IOException e){
            LOGGER.log(Level.SEVERE, "Error while loading view : " + e.getMessage(), e);
            AlertManager.showAlert(
                    Alert.AlertType.ERROR,
                    LanguageManager.getString("ui.error"),
                    e.getMessage(),
                    getWindow());
        }
    }

    private void reloadApplicationUI() {
        try {
            Stage stage = (Stage) getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/de/srh_dr/mediamanagementtoolmmt/view/Main_Shell.fxml"));
            loader.setResources(LanguageManager.getBundle());

            Parent newRoot = loader.load();
            MainController newController = loader.getController();

            newController.restoreState(this.currentView, this.currentPramId, this.lendingParam);

            stage.getScene().setRoot(newRoot);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while loading view : " + e.getMessage(), e);
            AlertManager.showAlert(
                    Alert.AlertType.ERROR,
                    "UI Error",
                    "Failed to reload language.",
                    getWindow());
        }
    }

    public void restoreState(ViewState state, int paramId, boolean lendingParam) {
        switch (state) {
            case MEDIA_DETAIL -> showMediaDetail(paramId);
            case DEFAULT_VIEW -> showDefaultView();
            case ARTIST_OVERVIEW -> showArtists();
            case LENDING_OVERVIEW -> showLendings();
            case FRANCHISE_OVERVIEW -> showFranchises();
            case PUBLISHER_OVERVIEW -> showPublishers();
            case SERIES_OVERVIEW -> showSeries();
            case MEDIA_OVERVIEW -> showMediaOverview();
            case LENDING_DETAIL_VIEW -> showLendingView(paramId,  lendingParam);
            case MEDIA_FORM -> showMediaFormView(Math.max(paramId, 0));
        }
    }

    public Window getWindow(){
        if (viewContainer != null && viewContainer.getScene() != null) {
            return viewContainer.getScene().getWindow();
        }
        return null;
    }
}
