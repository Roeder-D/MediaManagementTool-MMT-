package de.srh_dr.mediamanagementtoolmmt.controller;

import de.srh_dr.mediamanagementtoolmmt.data.PublisherDAO;
import de.srh_dr.mediamanagementtoolmmt.model.Publisher;
import de.srh_dr.mediamanagementtoolmmt.util.AlertManager;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

public class PublisherOverviewController{
    @FXML private VBox viewContainer;
    @FXML private TextField searchField;
    @FXML private ListView<Publisher> publisherList;

    private final PublisherDAO publisherDAO = new PublisherDAO();
    private final ObservableList<Publisher> publishers = FXCollections.observableArrayList();
    private FilteredList<Publisher> filteredPublishers;

    @FXML
    private void initialize() {
        setupListView();
        loadPublishers();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilter());
    }

    private void setupListView() {
        publisherList.setCellFactory(lv -> {
            ListCell<Publisher> cell = new ListCell<Publisher>() {
                @Override
                protected void updateItem(Publisher publisher, boolean empty) {
                    super.updateItem(publisher, empty);
                    if(empty || publisher == null){
                        setText(null);
                    }else{
                        setText(publisher.getPublisherName());
                    }
                }
            };

            cell.setOnMouseClicked(event -> {
                if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !cell.isEmpty()) {
                    openPublisherPopup(cell.getItem());
                }
            });
            return cell;
        });
    }

    @FXML
    public void loadPublishers() {
        publishers.setAll(publisherDAO.findAll());
        filteredPublishers = new FilteredList<>(publishers, p -> true);
        publisherList.setItems(filteredPublishers);
        applyFilter();
    }

    @FXML
    public void applyFilter() {
        String searchText = searchField.getText().toLowerCase();

        filteredPublishers.setPredicate(publisher -> {
            return publisher.getPublisherName().toLowerCase().contains(searchText);
        });
    }

    private void openPublisherPopup(Publisher selectedPublisher) {
        Dialog<Publisher> dialog = new Dialog<>();
        dialog.setTitle(LanguageManager.getString("ui.edit_publisher"));
        dialog.setHeaderText(LanguageManager.getString("ui.edit_publisher"));

        ButtonType saveButtonType = new ButtonType(LanguageManager.getString("ui.submit"), ButtonBar.ButtonData.APPLY);
        ButtonType deleteButtonType = new ButtonType(LanguageManager.getString("ui.delete"), ButtonBar.ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, saveButtonType, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        TextField publisherNameField = new TextField();
        publisherNameField.setText(selectedPublisher.getPublisherName() != null ? selectedPublisher.getPublisherName() : "--ERROR--");

        gridPane.add(new Label(LanguageManager.getString("ui.publisher_name")), 0, 0);
        gridPane.add(publisherNameField, 1, 0);

        dialog.getDialogPane().setContent(gridPane);

        dialog.setResultConverter(dialogButton -> {
            if(dialogButton == saveButtonType){
                selectedPublisher.setPublisherName(publisherNameField.getText());

                try{
                   publisherDAO.save(selectedPublisher);
                }catch(Exception e){
                    AlertManager.showAlert(
                            Alert.AlertType.ERROR,
                            LanguageManager.getString("ui.error"),
                            LanguageManager.getString("error.failed to save"),
                            getWindow());
                }
                return null;
            }else if(dialogButton == deleteButtonType){
                boolean confirmDelete = AlertManager.requestConfirmation(
                        LanguageManager.getString("ui.warning"),
                        LanguageManager.getString("warning.confirmDeletePublisher"),
                        getWindow());
                if(confirmDelete) {
                    try {
                        publisherDAO.delete(selectedPublisher.getId());
                    } catch (Exception e) {
                        AlertManager.showAlert(
                                Alert.AlertType.ERROR,
                                LanguageManager.getString("ui.error"),
                                LanguageManager.getString("error.failedToDelete") + ": " + e.getMessage(),
                                getWindow());
                    }
                }
            }
            return null;
        });
        dialog.showAndWait();
        loadPublishers();
    }

    private Window getWindow(){
        if (viewContainer != null && viewContainer.getScene() != null) {
            return viewContainer.getScene().getWindow();
        }
        return null;
    }
}
