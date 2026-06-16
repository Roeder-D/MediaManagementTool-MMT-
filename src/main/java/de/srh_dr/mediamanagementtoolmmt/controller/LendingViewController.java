package de.srh_dr.mediamanagementtoolmmt.controller;

import de.srh_dr.mediamanagementtoolmmt.data.LendeeDAO;
import de.srh_dr.mediamanagementtoolmmt.data.LendingDAO;
import de.srh_dr.mediamanagementtoolmmt.data.MediaDAO;
import de.srh_dr.mediamanagementtoolmmt.model.Lendee;
import de.srh_dr.mediamanagementtoolmmt.model.Lending;
import de.srh_dr.mediamanagementtoolmmt.model.Media;
import de.srh_dr.mediamanagementtoolmmt.util.AlertManager;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import org.controlsfx.control.SearchableComboBox;
import org.controlsfx.control.spreadsheet.Grid;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class LendingViewController implements MainControllerAware{
    @FXML private BorderPane viewContainer;
    @FXML private TextField mediaTitleField;
    @FXML private SearchableComboBox<Lendee> lendeeComboBox;
    @FXML private DatePicker borrowDateField;
    @FXML private DatePicker returnDateField;
    @FXML private TextArea noteArea;
    @FXML private Button manageLendeeButton;
    @FXML private TextField lendeeReadOnlyField;

    MainController mainController;
    LendingDAO lendingDAO =  new LendingDAO();
    LendeeDAO lendeeDAO = new LendeeDAO();
    MediaDAO mediaDAO = new MediaDAO();

    private Lending currentLending;
    private Media targetMedia;
    private List<Lendee> allLendees;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        allLendees = lendeeDAO.findAll();

        lendeeComboBox.getItems().clear();
        //fill Lendee selection and provide a null option
        Lendee nullLendee = new Lendee(0, "", "", LanguageManager.getString("ui.select_lendee"), false);
        lendeeComboBox.getItems().add(nullLendee);
        lendeeComboBox.getItems().addAll(allLendees);
        lendeeComboBox.getSelectionModel().selectFirst();

        borrowDateField.setValue(LocalDate.now());

        manageLendeeButton.textProperty().bind(Bindings.createStringBinding(() -> {
            Lendee selectedLendee = lendeeComboBox.getValue();
            if (selectedLendee != null && selectedLendee.getId() > 0) {
                return LanguageManager.getString("ui.edit_lendee");
            }else{
                return LanguageManager.getString("ui.new_lendee");
            }
        }, lendeeComboBox.valueProperty())); // Event listener

        toggleLendeeInputMode(false);
    }

    public void loadLendingData(int id, boolean viaMedia){
        if(id == 0) return;

        try{
            if(viaMedia){
                targetMedia = mediaDAO.read(id);
                if(targetMedia != null) {
                    toggleLendeeInputMode(false);

                    if (targetMedia.getStatus().name().equalsIgnoreCase("LENT")) {
                        int LendingId = lendingDAO.findActiveIdByMediaId(targetMedia.getId());
                        loadLendingData(LendingId, false);
                        return;
                    }
                    mediaTitleField.setText(targetMedia.getTitle());
                }
            }else{
                currentLending = lendingDAO.findById(id);
                if(currentLending != null){
                    toggleLendeeInputMode(true);

                    targetMedia = currentLending.getMedia();
                    mediaTitleField.setText(targetMedia.getTitle());
                    lendeeReadOnlyField.setText(currentLending.getLendee().toString().replace("()", ""));
                    lendeeComboBox.setValue(currentLending.getLendee());
                    borrowDateField.setValue(currentLending.getBorrowDate());
                    returnDateField.setValue(currentLending.getReturnDate());
                    if(currentLending.getNote() != null){
                        noteArea.setText(currentLending.getNote());
                    }
                }
            }
        }catch(Exception e){
            AlertManager.showAlert(
                    Alert.AlertType.ERROR,
                    LanguageManager.getString("ui.error"),
                    LanguageManager.getString("error.failedToLoad") + ": " + e.getMessage(),
                    getWindow());
        }
    }

    @FXML
    private void handleManageLendee(){
        Lendee selectedLendee = lendeeComboBox.getValue();
        boolean isEditMode = selectedLendee != null && selectedLendee.getId() > 0;

        if(isEditMode){
            executeEditLendeeWorkflow(selectedLendee);
        }else{
            executeCreateLendeeWorkflow();
        }
    }

    @FXML
    private void executeCreateLendeeWorkflow(){
        Dialog<Lendee> dialog = new Dialog<>();
        dialog.setTitle(LanguageManager.getString("ui.addNewLendee"));
        dialog.setHeaderText(LanguageManager.getString("ui.headerAddNewLendee"));

        ButtonType saveButtonType = new ButtonType(LanguageManager.getString("ui.submit"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField firstNameField = new TextField();
        firstNameField.setPromptText(LanguageManager.getString("ui.firstName"));
        TextField lastNameField = new TextField();
        lastNameField.setPromptText(LanguageManager.getString("ui.lastName"));
        TextField aliasField = new TextField();
        aliasField.setPromptText(LanguageManager.getString("ui.alias"));

        grid.add(new Label(LanguageManager.getString("ui.firstName_label")), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label(LanguageManager.getString("ui.lastName_label")), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label(LanguageManager.getString("ui.alias_label")), 0, 2);
        grid.add(aliasField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(firstNameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if(dialogButton == saveButtonType){
                String firstName = firstNameField.getText();
                if (firstName != null) {
                    firstName = firstName.trim();
                }
                String lastName = lastNameField.getText();
                if (lastName != null) {
                    lastName = lastName.trim();
                }
                String alias = aliasField.getText();
                if (alias != null) {
                    alias = alias.trim();
                }

                if((firstName != null && !firstName.isEmpty()) || (lastName != null && !lastName.isEmpty()) || (alias != null && !alias.isEmpty())){
                    return new Lendee(0, firstName, lastName, alias, true);
                }
            }return null;
        });
        dialog.initOwner(getWindow());
        Optional<Lendee> result  = dialog.showAndWait();

        result.ifPresent(lendee -> {
            try {
                boolean isDuplicate = allLendees.stream()
                        .anyMatch(l -> l.getFirstName().equalsIgnoreCase(lendee.getFirstName()) &&
                                l.getLastName().equalsIgnoreCase(lendee.getLastName()) &&
                                l.getAlias().equalsIgnoreCase(lendee.getAlias()));

                if(isDuplicate){
                    boolean continueAnyway = AlertManager.requestConfirmation(
                            LanguageManager.getString("ui.warning"),
                            LanguageManager.getString("warning.duplicateLendee"),
                            getWindow()
                    );

                    if(!continueAnyway){
                        return;
                    }
                }
                lendeeDAO.save(lendee);
                if(!lendeeComboBox.getItems().contains(lendee)){
                    lendeeComboBox.getItems().add(lendee);
                }
                lendeeComboBox.setValue(lendee);
            }catch (Exception e){
                AlertManager.showAlert(Alert.AlertType.ERROR, LanguageManager.getString("ui.error"), e.getMessage(), getWindow());
            }
        });
    }

    @FXML
    private void executeEditLendeeWorkflow(Lendee selectedLendee){
        Dialog<Lendee> dialog = new Dialog<>();
        dialog.setTitle(LanguageManager.getString("ui.edit_lendee"));
        dialog.setHeaderText(LanguageManager.getString("ui.edit_lendee_header"));

        ButtonType saveButtonType = new ButtonType(LanguageManager.getString("ui.submit"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField firstNameField = new TextField();
        firstNameField.setPromptText(LanguageManager.getString("ui.firstName"));
        TextField lastNameField = new TextField();
        lastNameField.setPromptText(LanguageManager.getString("ui.lastName"));
        TextField aliasField = new TextField();
        aliasField.setPromptText(LanguageManager.getString("ui.alias"));

        grid.add(new Label(LanguageManager.getString("ui.firstName_label")), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label(LanguageManager.getString("ui.lastName_label")), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label(LanguageManager.getString("ui.alias_label")), 0, 2);
        grid.add(aliasField, 1, 2);

        firstNameField.setText(selectedLendee.getFirstName());
        lastNameField.setText(selectedLendee.getLastName());
        aliasField.setText(selectedLendee.getAlias());

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(firstNameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if(dialogButton == saveButtonType){
                String firstName = firstNameField.getText();
                if(firstName != null){
                    firstName = firstName.trim();
                }
                String lastName = lastNameField.getText();
                if(lastName != null){
                    lastName = lastName.trim();
                }
                String alias = aliasField.getText();
                if(alias != null){
                    alias = alias.trim();
                }

                if((firstName != null && !firstName.isEmpty()) || (lastName != null && !lastName.isEmpty()) || (alias != null && !alias.isEmpty())){
                    selectedLendee.setFirstName(firstName);
                    selectedLendee.setLastName(lastName);
                    selectedLendee.setAlias(alias);
                    return selectedLendee;
                }
            }
            return null;
        });

        dialog.initOwner(getWindow());
        Optional<Lendee> result  = dialog.showAndWait();

        result.ifPresent(lendee -> {
            lendeeDAO.save(lendee);

            ObservableList<Lendee> items =  lendeeComboBox.getItems();
            int index = items.indexOf(lendee);

            if(index > -1){
                items.set(index, null);
                items.set(index, lendee);
            }else{
                items.add(lendee);
            }
            lendeeComboBox.setValue(lendee);
        });
    }

    @FXML
    private void handleSubmit(){
        if(targetMedia == null ||lendeeComboBox.getValue() == null || lendeeComboBox.getValue().getId() == 0 || borrowDateField.getValue() == null){
            AlertManager.showAlert(
                    Alert.AlertType.WARNING,
                    LanguageManager.getString("ui.warning"),
                    LanguageManager.getString("warning.missing_lending_fields"),
                    getWindow());
            return;
        }
        try {
            if(currentLending == null){
                Lending newLending = new Lending(
                        0,
                        targetMedia,
                        lendeeComboBox.getValue(),
                        borrowDateField.getValue(),
                        returnDateField.getValue(),
                        true
                );
                if(noteArea.getText() != null && !noteArea.getText().trim().isEmpty()){
                    newLending.setNote(noteArea.getText().trim());
                }
                lendingDAO.save(newLending);
            }else{
                currentLending.setReturnDate(returnDateField.getValue());
                if(noteArea.getText() != null && !noteArea.getText().trim().isEmpty()){
                    currentLending.setNote(noteArea.getText().trim());
                }
                lendingDAO.save(currentLending);
            }
            handleCancel();
        }catch(Exception e){
            AlertManager.showAlert(
                    Alert.AlertType.ERROR,
                    LanguageManager.getString("ui.error"),
                    LanguageManager.getString("error.failedToSave") + ": " + e.getMessage(),
                    getWindow());
        }
    }

    @FXML
    private void handleCancel(){
        if(mainController != null){
            mainController.showLendings();
        }
    }

    private Window getWindow(){
        if (viewContainer != null && viewContainer.getScene() != null) {
            return viewContainer.getScene().getWindow();
        }
        return null;
    }

    private void toggleLendeeInputMode(boolean readOnly) {
        lendeeComboBox.setVisible(!readOnly);
        lendeeComboBox.setManaged(!readOnly);

        lendeeReadOnlyField.setVisible(readOnly);
        lendeeReadOnlyField.setManaged(readOnly);
    }
}
