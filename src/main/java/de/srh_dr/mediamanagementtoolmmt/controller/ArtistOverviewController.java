package de.srh_dr.mediamanagementtoolmmt.controller;

import de.srh_dr.mediamanagementtoolmmt.data.ArtistDAO;
import de.srh_dr.mediamanagementtoolmmt.model.Artist;
import de.srh_dr.mediamanagementtoolmmt.util.AlertManager;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.controlsfx.control.SearchableComboBox;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArtistOverviewController{
    private static final Logger LOGGER = Logger.getLogger(ArtistOverviewController.class.getName());

    @FXML private VBox viewContainer;
    @FXML private TextField searchField;
    @FXML private SearchableComboBox<String> nationalityFilterComboBox;
    @FXML private TableView<Artist> artistTable;
    @FXML private TableColumn<Artist, String> firstName;
    @FXML private TableColumn<Artist, String> lastName;
    @FXML private TableColumn<Artist, String> alias;
    @FXML private TableColumn<Artist, String> nationality;

    private final ArtistDAO artistDAO = ArtistDAO.getInstance();
    private final ObservableList<Artist> artists = FXCollections.observableArrayList();
    private FilteredList<Artist> filteredArtists;

    @FXML
    public void initialize(){
        loadNationalities();

        firstName.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        lastName.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
        alias.setCellValueFactory(cellData -> cellData.getValue().aliasProperty());
        nationality.setCellValueFactory(cellData -> cellData.getValue().nationalityProperty());

        loadArtists();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilter());
        nationalityFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilter());

        artistTable.setRowFactory(tv -> {
            TableRow<Artist> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && !row.isEmpty()){
                    Artist selectedArtist = row.getItem();
                    openArtistPopup(selectedArtist);
                }
            });
            return row;
        });
    }

    @FXML
    public void loadArtists(){
        artists.setAll(artistDAO.findAll());
        filteredArtists = new FilteredList<>(artists, p -> true);
        SortedList<Artist> sortedArtists = new SortedList<>(filteredArtists);
        sortedArtists.comparatorProperty().bind(artistTable.comparatorProperty());
        artistTable.setItems(sortedArtists);
        applyFilter();
    }

    @FXML
    public void applyFilter(){
        String searchText = searchField.getText().toLowerCase();
        String selectedNationality = nationalityFilterComboBox.getValue();
        String allLabel = LanguageManager.getString("ui.selectNationality");

        filteredArtists.setPredicate(artist -> {
            String firstName = (artist.getFirstName() == null) ? "" : artist.getFirstName().toLowerCase();
            String lastName = (artist.getLastName() == null) ? "" : artist.getLastName().toLowerCase();
            String alias = (artist.getAlias() == null) ? "" : artist.getAlias().toLowerCase();

            String targetString = firstName + " " + lastName + " " + alias;
            boolean matchSearch = searchText.isEmpty() ||  targetString.contains(searchText);

            boolean matchNationality = (selectedNationality == null || selectedNationality.equals(allLabel)) ||  artist.getNationality().equalsIgnoreCase(selectedNationality);

            return (matchSearch && matchNationality);
        });
    }

    private void openArtistPopup(Artist selectedArtist){
        Dialog<Artist> dialog = new Dialog<>();
        dialog.setTitle(LanguageManager.getString("ui.editArtist"));
        dialog.setHeaderText(LanguageManager.getString("ui.editArtist"));

        ButtonType saveButtonType = new ButtonType(LanguageManager.getString("ui.submit"), ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButtonType = new ButtonType(LanguageManager.getString("ui.delete"), ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, saveButtonType, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        TextField firstNameField = new TextField();
        firstNameField.setPromptText(LanguageManager.getString("ui.firstName"));
        firstNameField.setText(selectedArtist.getFirstName() != null ? selectedArtist.getFirstName() : "");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText(LanguageManager.getString("ui.lastName"));
        lastNameField.setText(selectedArtist.getLastName() != null ? selectedArtist.getLastName() : "");
        TextField aliasField = new TextField();
        aliasField.setPromptText(LanguageManager.getString("ui.alias"));
        aliasField.setText(selectedArtist.getAlias() != null ? selectedArtist.getAlias() : "");
        TextField nationalityField = new TextField();
        nationalityField.setPromptText(LanguageManager.getString("ui.nationality"));
        nationalityField.setText(selectedArtist.getNationality() != null ? selectedArtist.getNationality() : "");

        gridPane.add(new Label(LanguageManager.getString("ui.firstName")), 0, 0);
        gridPane.add(firstNameField, 1, 0);
        gridPane.add(new Label(LanguageManager.getString("ui.lastName")), 0, 1);
        gridPane.add(lastNameField, 1, 1);
        gridPane.add(new Label(LanguageManager.getString("ui.alias")), 0, 2);
        gridPane.add(aliasField, 1, 2);
        gridPane.add(new Label(LanguageManager.getString("ui.nationality")), 0, 3);
        gridPane.add(nationalityField, 1, 3);

        dialog.getDialogPane().setContent(gridPane);

        dialog.setResultConverter(dialogButton -> {
            if(dialogButton == saveButtonType) {
                selectedArtist.setFirstName(firstNameField.getText());
                selectedArtist.setLastName(lastNameField.getText());
                selectedArtist.setAlias(aliasField.getText());
                selectedArtist.setNationality(nationalityField.getText());

                try{
                    artistDAO.save(selectedArtist);
                }catch(Exception e){
                    LOGGER.log(Level.SEVERE,"Failed to save artist: " + e.getMessage(),e);
                    AlertManager.showAlert(Alert.AlertType.ERROR, LanguageManager.getString("ui.error"),LanguageManager.getString("error.failedToSave") + e.getMessage(), getWindow());
                }
                return null;
            }else if(dialogButton == deleteButtonType) {
                boolean confirmDelete = AlertManager.requestConfirmation(
                        LanguageManager.getString("ui.warning"),
                        LanguageManager.getString("warning.confirmDeleteArtist"),
                        getWindow());

                if(confirmDelete) {
                    try {
                        artistDAO.delete(selectedArtist.getId());
                    }catch(Exception e){
                        LOGGER.log(Level.SEVERE,"Failed to delete artist: " + e.getMessage(),e);
                        AlertManager.showAlert(Alert.AlertType.ERROR, LanguageManager.getString("ui.error"), e.getMessage(), getWindow());
                    }
                }
            }
            return null;
        });
        dialog.showAndWait();
        loadNationalities();
        loadArtists();
    }

    private void loadNationalities(){
        List<String> allNationalities = artistDAO.getAllNationalities();

        nationalityFilterComboBox.getItems().clear();
        nationalityFilterComboBox.getItems().add(LanguageManager.getString("ui.selectNationality"));
        nationalityFilterComboBox.getItems().addAll(allNationalities);
    }

    private Window getWindow(){
        if (viewContainer != null && viewContainer.getScene() != null) {
            return viewContainer.getScene().getWindow();
        }
        return null;
    }
}
