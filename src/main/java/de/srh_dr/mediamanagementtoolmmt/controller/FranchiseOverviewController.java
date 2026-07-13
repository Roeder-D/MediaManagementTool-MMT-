package de.srh_dr.mediamanagementtoolmmt.controller;

import de.srh_dr.mediamanagementtoolmmt.data.FranchiseDAO;
import de.srh_dr.mediamanagementtoolmmt.model.AltTitle;
import de.srh_dr.mediamanagementtoolmmt.model.Franchise;
import de.srh_dr.mediamanagementtoolmmt.util.AlertManager;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FranchiseOverviewController implements MainControllerAware{
    private static final Logger LOGGER = Logger.getLogger(FranchiseOverviewController.class.getName());

    @FXML private VBox viewContainer;
    @FXML private TextField searchField;
    @FXML private ListView<Franchise> franchiseList;

    MainController mainController;
    private final FranchiseDAO franchiseDAO = FranchiseDAO.getInstance();
    private final ObservableList<Franchise> franchises = FXCollections.observableArrayList();
    private final FilteredList<Franchise> filteredFranchises = new FilteredList<>(franchises, p -> true);

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    //populate view
    @FXML
    private void initialize() {
        setupListView();
        franchiseList.setItems(filteredFranchises);
        loadFranchises();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilter());
    }

    private void setupListView() {
        franchiseList.setCellFactory(lv -> {
            ListCell<Franchise> cell = new ListCell<>() {
                @Override
                protected void updateItem(Franchise item, boolean empty) {
                    super.updateItem(item, empty);
                    if(empty || item == null){
                        setText(null);
                    }else{
                        setText(item.getName());
                    }
                }
            };

            cell.setOnMouseClicked(event -> {
                if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !cell.isEmpty()) {
                    openFranchisePopup(cell.getItem());
                }
            });
            return cell;
        });
    }

    @FXML
    public void loadFranchises(){
        franchises.setAll((franchiseDAO.findAll()));
        applyFilter();
    }

    //edit franchise popup
    private void openFranchisePopup(Franchise selectedFranchise) {
        Dialog<Franchise> dialog = new Dialog<>();
        dialog.setTitle(LanguageManager.getString("ui.edit_franchise"));
        dialog.setHeaderText(LanguageManager.getString("ui.edit_franchise"));

        ButtonType saveButtonType = new ButtonType(LanguageManager.getString("ui.submit"), ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButtonType = new ButtonType(LanguageManager.getString("ui.delete"), ButtonBar.ButtonData.LEFT);
        ButtonType cancelButtonType = new ButtonType(LanguageManager.getString("ui.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, saveButtonType, cancelButtonType);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        TextField franchiseNameField = new TextField();
        franchiseNameField.setText(selectedFranchise.getName());

        ListView<AltTitle> altTitleListView = new ListView<>();
        ObservableList<AltTitle> altTitles = FXCollections.observableArrayList(selectedFranchise.getAltTitles());
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

                selectedFranchise.addAltTitle(newAltTitle);
                altTitles.add(newAltTitle);
                newAltTitleField.clear();
            }
        });

        removeAltTitleBtn.setOnAction(event -> {
            AltTitle selectedAltTitle = altTitleListView.getSelectionModel().getSelectedItem();
            if(selectedAltTitle != null) {
                selectedFranchise.removeAltTitle(selectedAltTitle);
                altTitles.remove(selectedAltTitle);
            }
        });

        HBox altTitleControls = new HBox(10, newAltTitleField, addAltTitleBtn, removeAltTitleBtn);

        gridPane.add(new Label(LanguageManager.getString("ui.franchise")), 0, 0);
        gridPane.add(franchiseNameField, 1, 0);

        gridPane.add(new Label(LanguageManager.getString("ui.altTitles")), 0, 1);
        gridPane.add(altTitleListView,  1, 1);
        gridPane.add(altTitleControls, 1, 2);

        dialog.getDialogPane().setContent(gridPane);

        // Stop user from creating an empty franchise name
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String newName = franchiseNameField.getText().trim();
            if(newName.isEmpty()){
                AlertManager.showAlert(
                        Alert.AlertType.WARNING,
                        LanguageManager.getString("ui.warning"),
                        LanguageManager.getString("ui.empty_franchise_name"),
                        getWindow());
                event.consume();
            }
        });
        Button deleteButton = (Button) dialog.getDialogPane().lookupButton(deleteButtonType);
        deleteButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            boolean confirmDelete = AlertManager.requestConfirmation(
                    LanguageManager.getString("ui.warning"),
                    LanguageManager.getString("warning.confirmDeleteFranchise"),
                    getWindow()
            );

            if (!confirmDelete) {
                event.consume();
            }else{
                try {
                    franchiseDAO.delete(selectedFranchise.getId());
                } catch(Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to delete franchise: " + e.getMessage(), e);
                    AlertManager.showAlert(
                            Alert.AlertType.ERROR,
                            LanguageManager.getString("ui.error"),
                            e.getMessage(),
                            getWindow());
                    event.consume();
                }
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if(dialogButton == saveButtonType) {
                try{
                    selectedFranchise.setName(franchiseNameField.getText().trim());
                    franchiseDAO.save(selectedFranchise);
                }catch(Exception e){
                    LOGGER.log(Level.SEVERE, "Failed to save franchise: " + e.getMessage(), e);
                    AlertManager.showAlert(
                            Alert.AlertType.ERROR,
                            LanguageManager.getString("ui.error"),
                            LanguageManager.getString("error.failedToSave") + e.getMessage(),
                            getWindow());
                }
            }
            return null;
        });
        dialog.initOwner(getWindow());
        dialog.showAndWait();
        loadFranchises();
    }

    //Filtering
    @FXML
    private void applyFilter(){
        String searchText = searchField.getText().toLowerCase().trim();

        filteredFranchises.setPredicate(franchise -> {
            if(searchText.isEmpty()) return true;

            StringBuilder targetText = new StringBuilder(franchise.getName().toLowerCase() + " ");
            for(AltTitle altTitle : franchise.getAltTitles()){
                targetText.append(altTitle.getTitle().toLowerCase()).append(" ");
            }
            return targetText.toString().contains(searchText);
        });
    }

    //current window for popups
    private Window getWindow(){
        if (viewContainer != null && viewContainer.getScene() != null) {
            return viewContainer.getScene().getWindow();
        }
        return null;
    }
}
