package de.srh_dr.mediamanagementtoolmmt.controller;

import de.srh_dr.mediamanagementtoolmmt.data.MediaOverviewDAO;
import de.srh_dr.mediamanagementtoolmmt.model.MediaOverview;
import de.srh_dr.mediamanagementtoolmmt.util.AlertManager;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;
import de.srh_dr.mediamanagementtoolmmt.viewmodel.FilterOption;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.controlsfx.control.SearchableComboBox;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MediaOverviewController implements MainControllerAware {
    private static final Logger LOGGER = Logger.getLogger(MediaOverviewController.class.getName());

    @FXML private VBox viewContainer;
    @FXML private TableView<MediaOverview> mediaTable;
    @FXML private TableColumn<MediaOverview, String> titleCol;
    @FXML private TableColumn<MediaOverview, String> typeCol;
    @FXML private TableColumn<MediaOverview, String> artistCol;
    @FXML private TableColumn<MediaOverview, String> releaseDateCol;
    @FXML private TableColumn<MediaOverview, String> publisherCol;
    @FXML private TableColumn<MediaOverview, String> availableCol;
    @FXML private TextField searchField;
    @FXML private SearchableComboBox<FilterOption> filterStatusComboBox;
    @FXML private SearchableComboBox<String> filterMediaTypeComboBox;
    @FXML private SearchableComboBox<String> filterTagComboBox;


    private MainController mainController;
    private final MediaOverviewDAO mediaOverviewDAO = MediaOverviewDAO.getInstance();
    private final ObservableList<MediaOverview> mediaOverviews = FXCollections.observableArrayList();
    private FilteredList<MediaOverview> filteredMediaOverviews;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        filterStatusComboBox.getItems().clear();
        filterStatusComboBox.getItems().add(new FilterOption(LanguageManager.getString("ui.All"), "ALL"));
        filterStatusComboBox.getItems().add(new FilterOption(LanguageManager.getString("ui.available"), "AVAILABLE"));
        filterStatusComboBox.getItems().add(new FilterOption(LanguageManager.getString("ui.lent"), "LENT"));
        filterStatusComboBox.getItems().add(new FilterOption(LanguageManager.getString("ui.LOST"), "LOST"));
        filterStatusComboBox.getSelectionModel().select(0);

        titleCol.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        typeCol.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        artistCol.setCellValueFactory(cellData -> cellData.getValue().artistsDisplayProperty());
        releaseDateCol.setCellValueFactory(cellData -> cellData.getValue().releaseDateProperty());
        publisherCol.setCellValueFactory(cellData -> cellData.getValue().publisherProperty());
        availableCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // double-click listener
        mediaTable.setRowFactory(tv ->{
            TableRow<MediaOverview> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && (!row.isEmpty())) {
                    MediaOverview clickedRow = row.getItem();
                    if(mainController != null) {
                        mainController.showMediaDetail(clickedRow.getMediaId());
                    }
                }
            });
            return row;
        });
        loadTableData();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilter());
    }

    private void loadTableData() {
        try{
           var dataList = mediaOverviewDAO.getMediaOverview();
           mediaOverviews.setAll(dataList);
            setMediaTypes();
            setTags();
            filteredMediaOverviews = new FilteredList<>(mediaOverviews, p -> true);
            SortedList<MediaOverview> sortedMediaOverview = new SortedList<>(filteredMediaOverviews);
            sortedMediaOverview.comparatorProperty().bind(mediaTable.comparatorProperty());
           mediaTable.setItems(sortedMediaOverview);
        }catch(SQLException e){
            LOGGER.log(Level.SEVERE, "Failed to load table data: " + e.getMessage(), e);
            AlertManager.showAlert(
                    Alert.AlertType.ERROR,
                    LanguageManager.getString("error.failedToLoad"),
                    e.getMessage(),
                    getWindow());
        }
    }

    private void setMediaTypes(){
        List<String> typeList = new ArrayList<>();

        for(MediaOverview mediaOverview : mediaOverviews){
            if(!typeList.contains(mediaOverview.getType())){
                typeList.add(mediaOverview.getType());
            }
        }
        filterMediaTypeComboBox.getItems().clear();
        filterMediaTypeComboBox.getItems().add(LanguageManager.getString("ui.All"));
        filterMediaTypeComboBox.getItems().addAll(typeList);
        filterMediaTypeComboBox.getSelectionModel().select(0);
    }

    private void setTags(){
        List<String> tagList = new ArrayList<>();

        for(MediaOverview mediaOverview : mediaOverviews){
            for(String tag : mediaOverview.getTags()){
                if(!tagList.contains(tag)){
                    tagList.add(tag);
                }
            }
        }
        filterTagComboBox.getItems().clear();
        filterTagComboBox.getItems().add(LanguageManager.getString("ui.All"));
        filterTagComboBox.getItems().addAll(tagList);
        filterTagComboBox.getSelectionModel().select(0);

    }

    @FXML
    public void applyFilter() {
        String searchText = searchField.getText().toLowerCase();
        FilterOption selectedStatus = filterStatusComboBox.getSelectionModel().getSelectedItem();
        String selectedMediaType = filterMediaTypeComboBox.getSelectionModel().getSelectedItem();
        String selectedTag = filterTagComboBox.getSelectionModel().getSelectedItem();

        filteredMediaOverviews.setPredicate(media -> {
            if(!searchText.isEmpty()) {
                String targetString = (media.getTitle() + " " + media.getPublisher() + " " + media.getType()).toLowerCase();

                if(!targetString.contains(searchText)){
                    return false;
                }
            }

            if(selectedTag != null && !selectedTag.equals(LanguageManager.getString("ui.All"))){
                if(!media.getTags().contains(selectedTag)){
                    return false;
                }
            }

            if(selectedStatus != null && !selectedStatus.getInternalValue().equals("ALL")){
                if(!media.getStatus().equals(selectedStatus.getInternalValue())){
                    return false;
                }
            }

            if(selectedMediaType != null && !selectedMediaType.equals(LanguageManager.getString("ui.All"))) {
                return media.getType().equalsIgnoreCase(selectedMediaType);
            }
            return true;
        });
    }

    private Window getWindow(){
        if (viewContainer != null && viewContainer.getScene() != null) {
            return viewContainer.getScene().getWindow();
        }
        return null;
    }
}
