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
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.controlsfx.control.SearchableComboBox;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class LendingViewController implements MainControllerAware{
    @FXML private TextField mediaTitleField;
    @FXML private SearchableComboBox<Lendee> lendeeComboBox;
    @FXML private DatePicker borrowDateField;
    @FXML private DatePicker returnDateField;
    @FXML private TextArea noteArea;

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

        //fill Lendee selection and provide a null option
        Lendee nullLendee = new Lendee(0, "", "", LanguageManager.getString("ui.select_lendee"), false);
        lendeeComboBox.getItems().add(nullLendee);
        lendeeComboBox.getItems().setAll(allLendees);
        lendeeComboBox.getSelectionModel().selectFirst();

        borrowDateField.setValue(LocalDate.now());
    }

    public void loadLendingData(int id, boolean viaMedia){
        if(id == 0) return;

        try{
            if(viaMedia){
                targetMedia = mediaDAO.read(id);
                if(targetMedia != null) {
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
                    targetMedia = currentLending.getMedia();
                    mediaTitleField.setText(targetMedia.getTitle());
                    lendeeComboBox.setValue(currentLending.getLendee());
                    borrowDateField.setValue(currentLending.getBorrowDate());
                    returnDateField.setValue(currentLending.getReturnDate());
                    if(currentLending.getNote() != null){
                        noteArea.setText(currentLending.getNote());
                    }

                    lendeeComboBox.setDisable(true);
                }
            }
        }catch(Exception e){
            AlertManager.showAlert(Alert.AlertType.ERROR, LanguageManager.getString("ui.error"), LanguageManager.getString("error.failedToLoad") + ": " + e.getMessage());
        }
    }

    @FXML
    private void handleManageLendee(){
        Lendee selectedLendee = lendeeComboBox.getValue();
        Dialog<Lendee> dialog = new Dialog<>();
        dialog.setTitle(LanguageManager.getString(selectedLendee == null ? "ui.new_lendee" : "ui.edit_lendee"));
        dialog.setHeaderText(LanguageManager.getString("ui.headerAddNewLendee"));

        ButtonType saveButtonType = new ButtonType(LanguageManager.getString("ui.submit"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        TextField firstNameField = new TextField();
        firstNameField.setPromptText(LanguageManager.getString("ui.first_Name"));
        TextField lastNameField = new TextField();
        lastNameField.setPromptText(LanguageManager.getString("ui.last_Name"));
        TextField aliasField = new TextField();
        aliasField.setPromptText(LanguageManager.getString("ui.alias"));

        gridPane.add(new Label(LanguageManager.getString("ui.first_Name")+ ": "), 0, 0);
        gridPane.add(firstNameField, 1, 0);
        gridPane.add(new Label(LanguageManager.getString("ui.last_Name")+ ": "), 0, 1);
        gridPane.add(lastNameField, 1, 1);
        gridPane.add(new Label(LanguageManager.getString("ui.alias")+ ": "), 0, 2);
        gridPane.add(aliasField, 1, 2);

        dialog.getDialogPane().setContent(gridPane);
        Platform.runLater(firstNameField::requestFocus);

        if(selectedLendee != null){
            firstNameField.setText(selectedLendee.getFirstName());
            lastNameField.setText(selectedLendee.getLastName());
            aliasField.setText(selectedLendee.getAlias());
        }

        dialog.setResultConverter(dialogButton -> {
            if(dialogButton == saveButtonType){
                String firstName = firstNameField.getText();
                String lastName = lastNameField.getText();
                String alias = aliasField.getText();

                if(!firstName.isEmpty() ||!lastName.isEmpty() ||!alias.isEmpty()){
                    int id = (selectedLendee.getId() != 0) ? selectedLendee.getId() : 0;
                    return new Lendee(id, firstName, lastName, alias, false);
                }
            }
            return null;
        });
        Optional<Lendee> result = dialog.showAndWait();

        result.ifPresent(lendee -> {
            lendeeDAO.save(lendee);
            if(!lendeeComboBox.getItems().contains(lendee)){
                lendeeComboBox.getItems().add(lendee);
            }
            //Refreshing the displayed text
            lendeeComboBox.setValue(null);
            lendeeComboBox.setValue(lendee);
        });
    }

    @FXML
    private void handleSubmit(){
        if(targetMedia == null ||lendeeComboBox.getValue() == null || lendeeComboBox.getValue().getId() == 0 || borrowDateField.getValue() == null){
            AlertManager.showAlert(Alert.AlertType.WARNING, LanguageManager.getString("ui.warning"), LanguageManager.getString("warning.missing_lending_fields"));
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
            AlertManager.showAlert(Alert.AlertType.ERROR, LanguageManager.getString("ui.error"), LanguageManager.getString("error.failedToSave") + ": " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel(){
        if(mainController != null){
            mainController.showLendings();
        }
    }

}
