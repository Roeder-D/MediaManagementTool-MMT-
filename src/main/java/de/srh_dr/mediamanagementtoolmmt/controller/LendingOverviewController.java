package de.srh_dr.mediamanagementtoolmmt.controller;

import de.srh_dr.mediamanagementtoolmmt.data.LendingDAO;
import de.srh_dr.mediamanagementtoolmmt.model.LendingDashboardItem;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LendingOverviewController implements MainControllerAware{
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private TableView<LendingDashboardItem> lendingTable;
    @FXML private TableColumn<LendingDashboardItem, String> titleCol;
    @FXML private TableColumn<LendingDashboardItem, String> lendeeCol;
    @FXML private TableColumn<LendingDashboardItem, String> borrowedCol;
    @FXML private TableColumn<LendingDashboardItem, String> returnedCol;
    @FXML private TableColumn<LendingDashboardItem, String> statusCol;

    MainController mainController;
    private final LendingDAO lendingDAO = new LendingDAO();

    private final ObservableList<LendingDashboardItem> allItems = FXCollections.observableArrayList();
    private FilteredList<LendingDashboardItem> filteredItems;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize(){
        titleCol.setCellValueFactory(cellData -> cellData.getValue().mediaTitleProperty());
        lendeeCol.setCellValueFactory(cellData -> cellData.getValue().lendeeInfoProperty());
        borrowedCol.setCellValueFactory(cellData -> cellData.getValue().borrowedOnProperty());
        returnedCol.setCellValueFactory(cellData -> cellData.getValue().returnedOnProperty());
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        lendingTable.setRowFactory(tv -> {
            TableRow<LendingDashboardItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && (!row.isEmpty())) {
                    LendingDashboardItem item = row.getItem();
                    if(mainController != null){
                        mainController.showLendingView(item.getLendingId(), false);
                    }
                }
            });
            return row;
        });

        statusFilterComboBox.getItems().addAll(
                LanguageManager.getString("ui.All"),
                LanguageManager.getString("ui.ACTIVE"),
                LanguageManager.getString("ui.RETURNED"),
                LanguageManager.getString("ui.LOST")
        );
        statusFilterComboBox.getSelectionModel().selectFirst();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilter();
        });

        loadLendings();
    }

    @FXML
    private void applyFilter(){
        if(filteredItems == null) return;

        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String selectedStatus = statusFilterComboBox.getValue();
        String allLabel = LanguageManager.getString("ui.All");

        filteredItems.setPredicate(item -> {
                    boolean statusMatch = true;
                    if(selectedStatus != null && !selectedStatus.equals(allLabel)){
                        statusMatch = item.statusProperty().get().equalsIgnoreCase(selectedStatus);
                    }
                    boolean textMatch = true;
                    if(!searchText.isEmpty()){
                        String title = item.mediaTitleProperty().get().toLowerCase();
                        String lendee = item.lendeeInfoProperty().get().toLowerCase();
                        textMatch = title.contains(searchText) || lendee.contains(searchText);
                    }
                    return statusMatch && textMatch;
        });
    }

    @FXML
    private void loadLendings(){
        allItems.setAll(lendingDAO.getLendingDashboard());
        filteredItems = new FilteredList<>(allItems, p -> true);

        SortedList<LendingDashboardItem> sortedItems = new SortedList<>(filteredItems);
        sortedItems.comparatorProperty().bind(lendingTable.comparatorProperty());

        lendingTable.setItems(sortedItems);
        applyFilter();
    }
}
