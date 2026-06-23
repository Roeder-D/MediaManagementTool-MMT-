package de.srh_dr.mediamanagementtoolmmt.view;

import de.srh_dr.mediamanagementtoolmmt.model.Artist;
import de.srh_dr.mediamanagementtoolmmt.util.AlertManager;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

public class ArtistDialog extends BaseEntityDialog<Artist> {
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField aliasField;
    private TextField nationalityField;

    private final ObservableList<Artist> allArtists;
    private final Window owner;

    public ArtistDialog(ObservableList<Artist> allArtists,  Window owner) {
        super("ui.newArtist", "ui.addNewArtist");
        this.allArtists = allArtists;
        this.owner = owner;
    }

    @Override
    protected void hydrateGridPane(GridPane gridPane) {
        firstNameField = new TextField();
        firstNameField.setPromptText(LanguageManager.getString("ui.firstName"));
        lastNameField = new TextField();
        lastNameField.setPromptText(LanguageManager.getString("ui.lastName"));
        aliasField = new TextField();
        aliasField.setPromptText(LanguageManager.getString("ui.alias"));
        nationalityField = new TextField();
        nationalityField.setPromptText(LanguageManager.getString("ui.nationality"));

        gridPane.add(new Label(LanguageManager.getString("ui.firstName") + ": "), 0, 0);
        gridPane.add(firstNameField, 1, 0);
        gridPane.add(new Label(LanguageManager.getString("ui.lastName") + ": "), 0, 1);
        gridPane.add(lastNameField, 1, 1);
        gridPane.add(new Label(LanguageManager.getString("ui.alias") + ": "), 0, 2);
        gridPane.add(aliasField, 1, 2);
        gridPane.add(new Label(LanguageManager.getString("ui.nationality") + ": "), 0, 3);
        gridPane.add(nationalityField, 1, 3);
    }

    @Override
    protected Artist createEntity() {
        String cleanFirstName = firstNameField.getText().trim();
        String cleanLastName = lastNameField.getText().trim();
        String cleanAlias = aliasField.getText().trim();
        String cleanNationality = nationalityField.getText().trim();

        if(!cleanLastName.isEmpty() || !cleanAlias.isEmpty()){
            return new Artist(0, cleanFirstName, cleanLastName, cleanAlias, cleanNationality, true);
        }
        return null;
    }

    @Override
    protected boolean checkDuplicate(Artist newEntity) {
        boolean duplicate =  allArtists.stream().anyMatch(artist ->{

                boolean nameMatch = !newEntity.getLastName().isEmpty() &&
                        artist.getFirstName().equalsIgnoreCase(newEntity.getFirstName()) &&
                        artist.getLastName().equalsIgnoreCase(newEntity.getLastName());

                boolean aliasMatch = !newEntity.getAlias().isEmpty() && artist.getAlias().equalsIgnoreCase(newEntity.getAlias());

                return  nameMatch || aliasMatch;
        });

        if(duplicate){
            String fullName = "";
            if(!newEntity.getFirstName().isEmpty()){
                fullName = newEntity.getFirstName() + " ";
            }
            if(!newEntity.getLastName().isEmpty()){
                fullName += newEntity.getLastName() + " ";
            }
            if(!newEntity.getAlias().isEmpty()){
                fullName += newEntity.getAlias() + " ";
            }
            fullName = fullName.trim();

            boolean continueAnyway = AlertManager.requestConfirmation(
                    LanguageManager.getString("ui.warning"),
                    LanguageManager.getString("warning.theArtistAlreadyExists_p1") + fullName + LanguageManager.getString("warning.theArtistAlreadyExists_p2"),
                    owner
            );
            return !continueAnyway;
        }
        return false;
    }
}
