package de.srh_dr.mediamanagementtoolmmt.view;

import de.srh_dr.mediamanagementtoolmmt.model.AltTitle;
import de.srh_dr.mediamanagementtoolmmt.model.Series;
import de.srh_dr.mediamanagementtoolmmt.util.AlertManager;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SeriesDialog extends BaseEntityDialog<Series>{
    private final TextField seriesNameField = new TextField();
    private final TextField seriesTitleCountField = new TextField();
    private final Spinner<Integer> seriesStartYearSpinner = new Spinner<>(1800, 2200, LocalDate.now().getYear());
    private final VBox altTitleContainer = new VBox(5);
    private final Button addAltTitleButton = new Button(LanguageManager.getString("ui.addAltTitle"));

    private final ObservableList<Series> allSeries;
    private final Window owner;

    public SeriesDialog(ObservableList<Series> allSeries, Window owner) {
        super("ui.newSeries", "ui.addNewSeries");
        this.allSeries = allSeries;
        this.owner = owner;
    }

    @Override
    protected void hydrateGridPane(GridPane gridPane){
        seriesNameField.setPromptText(LanguageManager.getString("ui.seriesName"));
        seriesTitleCountField.setPromptText(LanguageManager.getString("ui.seriesTitleCount"));
        seriesStartYearSpinner.setEditable(true);

        addAltTitleButton.setOnAction(event -> {
            HBox row = new HBox();
            TextField altTitleField = new TextField();
            altTitleField.setPromptText(LanguageManager.getString("ui.altTitle"));
            Button removeBtn = new Button("X");
            removeBtn.setOnAction(event1 -> altTitleContainer.getChildren().remove(row));
            row.getChildren().addAll(altTitleField, removeBtn);
            altTitleContainer.getChildren().add(row);
        });

        gridPane.add(new Label(LanguageManager.getString("ui.seriesName") + ": "), 0, 0);
        gridPane.add(seriesNameField, 1, 0);
        gridPane.add(new Label(LanguageManager.getString("ui.seriesTitleCount") + ": "), 0, 1);
        gridPane.add(seriesTitleCountField, 1, 1);
        gridPane.add(new Label(LanguageManager.getString("ui.seriesStartYear") + ": "), 0, 2);
        gridPane.add(seriesStartYearSpinner, 1, 2);

        gridPane.add(new Label(LanguageManager.getString("ui.altTitles") + ": "), 0, 3, 1, 1);
        gridPane.add(new VBox(5, altTitleContainer, addAltTitleButton), 1, 3);
    }

    @Override
    protected Series createEntity(){
        String cleanTitle = seriesNameField.getText().trim();
        if(!cleanTitle.isEmpty()){
            int titleCount = 0;
            try {
                titleCount =Integer.parseInt(seriesTitleCountField.getText().trim());
            }catch (NumberFormatException ignored){}

            List<AltTitle> altTitles = new ArrayList<>();
            for(Node node : altTitleContainer.getChildren()){
                if(node instanceof HBox){
                    TextField tf = (TextField) ((HBox) node).getChildren().getFirst();
                    if(!tf.getText().trim().isEmpty()){
                        altTitles.add(new AltTitle(0, tf.getText().trim(), true));
                    }
                }
            }
            return new Series(true, 0, cleanTitle, titleCount, seriesStartYearSpinner.getValue(), altTitles);
        }
        return null;
    }

    @Override
    protected boolean checkDuplicate(Series series){
        boolean duplicate = allSeries.stream().anyMatch(s -> s.getName().equalsIgnoreCase(series.getName()));

        if(duplicate){
            boolean continueAnyway = AlertManager.requestConfirmation(
                    LanguageManager.getString("ui.warning"),
                    LanguageManager.getString("warning.theSeriesAlreadyExists_p1") + series.getName() + LanguageManager.getString("warning.theSeriesAlreadyExists_p2"),
                    owner
            );
            return !continueAnyway;
        }
        return false;
    }
}
