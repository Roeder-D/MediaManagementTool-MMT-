package de.srh_dr.mediamanagementtoolmmt.controller;

import de.srh_dr.mediamanagementtoolmmt.data.*;
import de.srh_dr.mediamanagementtoolmmt.dto.ApiSource;
import de.srh_dr.mediamanagementtoolmmt.dto.ExternalMediaSearchResult;
import de.srh_dr.mediamanagementtoolmmt.model.*;
import de.srh_dr.mediamanagementtoolmmt.services.MediaIntegrationFacade;
import de.srh_dr.mediamanagementtoolmmt.services.MediaService;
import de.srh_dr.mediamanagementtoolmmt.util.AlertManager;
import de.srh_dr.mediamanagementtoolmmt.services.ImageManager;
import de.srh_dr.mediamanagementtoolmmt.util.ConfigManager;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;
import de.srh_dr.mediamanagementtoolmmt.view.ArtistDialog;
import de.srh_dr.mediamanagementtoolmmt.view.SeriesDialog;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.StringConverter;
import org.controlsfx.control.Rating;
import org.controlsfx.control.SearchableComboBox;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javafx.collections.FXCollections.observableArrayList;

public class MediaFormController implements MainControllerAware{
    private static final Logger LOGGER = Logger.getLogger(MediaFormController.class.getName());

    @FXML private ScrollPane viewContainer;
    @FXML private ImageView coverImage;
    @FXML private Rating mediaRating;
    @FXML private TextField isbnField;
    @FXML private TextField titleField;
    @FXML private TextField originalTitleField;
    @FXML private DatePicker releaseDateField;
    @FXML private SearchableComboBox<MediaType> mediaTypeComboBox;
    @FXML private SearchableComboBox<Publisher> publisherComboBox;
    @FXML private SearchableComboBox<Series> seriesComboBox;
    @FXML private VBox genreContainer;
    @FXML private VBox languageContainer;
    @FXML private VBox artistContainer;
    @FXML private TextArea descriptionArea;
    @FXML private TextField seriesOrderField;
    @FXML private VBox franchiseContainer;
    @FXML private VBox tagContainer;
    @FXML private Button api_search_isbn_button;
    @FXML private Button api_search_title_button;
    @FXML private ComboBox<Media.MediaStatus> statusComboBox;

    // DAOs
    private final MediaService mediaService = MediaService.getInstance();
    private final PublisherDAO publisherDAO = PublisherDAO.getInstance();
    private final GenreDAO genreDAO = GenreDAO.getInstance();
    private final ArtistDAO artistDAO = ArtistDAO.getInstance();
    private final ArtistRoleDAO artistRoleDAO = ArtistRoleDAO.getInstance();
    private final TagDAO tagDAO = TagDAO.getInstance();
    private final FranchiseDAO franchiseDAO = FranchiseDAO.getInstance();
    private final MediaTypeDAO  mediaTypeDAO = MediaTypeDAO.getInstance();
    private final SeriesDAO seriesDAO = SeriesDAO.getInstance();
    private final LanguageDAO languageDAO = LanguageDAO.getInstance();
    private final MediaIntegrationFacade mediaIntegrationFacade = MediaIntegrationFacade.getInstance();

    MainController mainController;
    ImageManager imageManager = ImageManager.getInstance();
    private File selectedCoverImage;
    private String remoteCoverUrl;

    Media currentMedia;
    private ObservableList<Genre> allGenres;
    private ObservableList<Language> allLanguages;
    private ObservableList<Artist> allArtists;
    private ObservableList<ArtistRole> allArtistRoles;
    private ObservableList<Publisher> allPublishers;
    private ObservableList<MediaType> allMediaTypes;
    private ObservableList<Series> allSeries;
    private ObservableList<Tag> allTags;
    private ObservableList<Franchise> allFranchises;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    //region Populate View
    public void initialize() {
        allGenres = observableArrayList(genreDAO.findAll());
        allLanguages = observableArrayList(languageDAO.findAll());
        allArtists = observableArrayList(artistDAO.findAll());
        allArtistRoles = observableArrayList(artistRoleDAO.findAll());
        allPublishers = observableArrayList(publisherDAO.findAll());
        allMediaTypes = observableArrayList(mediaTypeDAO.findAll());
        allSeries = observableArrayList(seriesDAO.findAll());
        allTags = observableArrayList(tagDAO.findAll());
        allFranchises = observableArrayList(franchiseDAO.findAll());
        mediaRating.setRating(0);
        statusComboBox.getItems().addAll(Media.MediaStatus.AVAILABLE, Media.MediaStatus.LOST);

        statusComboBox.getSelectionModel().select(Media.MediaStatus.AVAILABLE);
        statusComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Media.MediaStatus status) {
                if (status == null) return "";

                return switch (status) {
                    case AVAILABLE -> LanguageManager.getString("ui.available");
                    case LENT -> LanguageManager.getString("ui.lent");
                    case LOST -> LanguageManager.getString("ui.LOST");
                };
            }

            @Override //unused
            public Media.MediaStatus fromString(String s) {
                return null;
            }
        });
        isbnField.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                handleSearchRemoteByIsbn();
                event.consume();
            }
        });
        Platform.runLater(() -> isbnField.requestFocus());


        titleField.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER && !titleField.getText().trim().isEmpty()){
                handleSearchRemoteByTitle();
                event.consume();
            }
        });

        publisherComboBox.setItems(allPublishers);
        seriesComboBox.setItems(allSeries);

        mediaTypeComboBox.setItems(allMediaTypes);
        mediaTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                boolean showISBN = ConfigManager.getISBNEnabledMediaTypes().contains(newValue.getTypeName());

                isbnField.setDisable(!showISBN);
            }
        });

        addGenreDropdown();
        addLanguageRow();
        addArtistDropRow();
        addFranchiseDropdown();
        addTagDropdown();
    }

    public void loadMedia(int mediaId){
        this.selectedCoverImage = null;
        this.remoteCoverUrl = null;

        if(mediaId != 0){
            try{
                Media media = mediaService.getMediaById(mediaId);
                this.currentMedia = media;

                if(media.getIsbn() != null &&  !media.getIsbn().isEmpty()){
                    this.isbnField.setText(media.getIsbn());
                }

                this.titleField.setText(media.getTitle());

                if(media.getOriginalTitle() != null &&  !media.getOriginalTitle().isEmpty()){
                    this.originalTitleField.setText(media.getOriginalTitle());
                }

                if(media.getReleaseDate() != null) {
                    this.releaseDateField.setValue(media.getReleaseDate());
                }

                this.mediaTypeComboBox.setValue(media.getMediatype());

                if(media.getPublisher() != null) {
                    this.publisherComboBox.setValue(media.getPublisher());
                }

                if(media.getSeries() != null) {
                    this.seriesComboBox.setValue(media.getSeries());
                }

                genreContainer.getChildren().clear();
                for(Genre genre : media.getGenres()){
                    addDynamicDropdownRow(genreContainer, allGenres, genre, null);
                }

                languageContainer.getChildren().clear();
                for(Language language : media.getLanguages()){
                    addDynamicDropdownRow(languageContainer, allLanguages, language, null);
                }

                artistContainer.getChildren().clear();
                for(MediaArtist credit : media.getCredits()){
                    addMediaArtistRow(credit);
                }
                if(artistContainer.getChildren().isEmpty()){
                    addArtistDropRow();
                }

                franchiseContainer.getChildren().clear();
                for(Franchise franchise : media.getFranchises()){
                    addDynamicDropdownRow(franchiseContainer, allFranchises, franchise, null);
                }
                if(franchiseContainer.getChildren().isEmpty()){
                    addFranchiseDropdown();
                }

                tagContainer.getChildren().clear();
                for(Tag tag : media.getTags()){
                    addDynamicDropdownRow(tagContainer, allTags, tag, null);
                }

                if(tagContainer.getChildren().isEmpty()){
                    addTagDropdown();
                }

                mediaRating.setRating(media.getRating());

                if(media.getSeriesOrder() != 0){
                    seriesOrderField.setText(String.valueOf(media.getSeriesOrder()));
                }else{
                    seriesOrderField.setText("");
                }

                if(currentMedia.getStatus().equals(Media.MediaStatus.LENT)){
                    statusComboBox.getSelectionModel().select(Media.MediaStatus.LENT);
                    statusComboBox.setDisable(true);
                }else {
                    statusComboBox.getItems().remove(Media.MediaStatus.LENT);
                    statusComboBox.getSelectionModel().select(currentMedia.getStatus());
                    statusComboBox.setDisable(false);
                }

                displayImage();

                if(currentMedia != null) {
                    api_search_isbn_button.setVisible(false);
                    api_search_isbn_button.setManaged(false);
                    api_search_title_button.setVisible(false);
                    api_search_title_button.setManaged(false);
                }
            }catch(Exception e){
                LOGGER.log(Level.SEVERE, "Failed to load Media :" + e.getMessage(), e);
                AlertManager.showAlert(Alert.AlertType.ERROR, LanguageManager.getString("ui.error"), LanguageManager.getString("error.failedToLoad") + ": " + e.getMessage(), getWindow());
            }
        }
    }
    //endregion
    //region Action Handlers
    //image handler
    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()){
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }
    @FXML
    private void handleDrop(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;

        if(db.hasFiles()) {
            File file = db.getFiles().getFirst();
            String fileName = file.getName().toLowerCase();

            if(fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")){
                selectedCoverImage = file;

                coverImage.setImage(new Image(file.toURI().toString()));
                success = true;
            }else{
                LOGGER.log(Level.INFO,"Invalid file type dropped");
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }
    @FXML
    private void handleImageUpload(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(LanguageManager.getString("ui.selectCoverImage"));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(getWindow());

        if(file != null){
            selectedCoverImage = file;
            this.coverImage.setImage(new Image(file.toURI().toString()));
        }
    }

    //new dynamic rows
    @FXML
    private void addGenreDropdown(){
        addDynamicDropdownRow(genreContainer, allGenres, null, () -> {
            HBox activeRow = (HBox) genreContainer.getChildren().getLast();
            @SuppressWarnings("unchecked")
            SearchableComboBox<Genre> genreComboBox = (SearchableComboBox<Genre>) activeRow.getChildren().getFirst();
            handleAddNewGenre(genreComboBox);
        });
    }
    @FXML
    private void addLanguageRow(){
        addDynamicDropdownRow(languageContainer, allLanguages, null, () ->{
            HBox activeRow = (HBox) languageContainer.getChildren().getLast();
            @SuppressWarnings("unchecked")
            SearchableComboBox<Language> languageComboBox = (SearchableComboBox<Language>) activeRow.getChildren().getFirst();
            handleAddNewLanguage(languageComboBox);
        });
    }
    @FXML
    private void addArtistDropRow(){
        addMediaArtistRow(null);
    }
    @FXML
    private void addFranchiseDropdown(){
        addDynamicDropdownRow(franchiseContainer, allFranchises, null, () ->{
            HBox activeRow = (HBox) franchiseContainer.getChildren().getLast();
            @SuppressWarnings("unchecked")
            SearchableComboBox<Franchise> franchiseComboBox = (SearchableComboBox<Franchise>) activeRow.getChildren().getFirst();
            handleAddNewFranchise(franchiseComboBox);
        });
    }
    @FXML
    private void addTagDropdown(){
        addDynamicDropdownRow(tagContainer, allTags, null, () ->{
            HBox activeRow = (HBox) tagContainer.getChildren().getLast();
            @SuppressWarnings("unchecked")
            SearchableComboBox<Tag> tagComboBox = (SearchableComboBox<Tag>) activeRow.getChildren().getFirst();
            handleAddNewTag(tagComboBox);
        });
    }

    //dialogs for adding new entities
    @FXML
    private void handleAddNewArtist(SearchableComboBox<Artist> targetComboBox){
        ArtistDialog dialog = new ArtistDialog(allArtists, getWindow());

        dialog.initOwner(getWindow());
        Optional<Artist> result = dialog.showAndWait();

        result.ifPresent(artist -> {
            try {
                artistDAO.save(artist);
                allArtists.add(artist);
                targetComboBox.setValue(artist);
            }catch(Exception e){
                LOGGER.log(Level.SEVERE, "Failed to add new artist :" + e.getMessage(), e);
                AlertManager.showAlert(Alert.AlertType.ERROR, LanguageManager.getString("ui.error"), e.getMessage(), getWindow());
            }
        });
    }
    @FXML
    public void handleAddNewSeries(){
        SeriesDialog dialog = new SeriesDialog(allSeries, getWindow());
        dialog.initOwner(getWindow());

        Optional<Series> result = dialog.showAndWait();

        result.ifPresent(series -> {
            try{
                seriesDAO.save(series);
                allSeries.add(series);
                seriesComboBox.setValue(series);
            }catch(Exception e){
                LOGGER.log(Level.SEVERE, "Failed to add new series :" + e.getMessage(), e);
                AlertManager.showAlert(Alert.AlertType.ERROR, LanguageManager.getString("ui.error"), e.getMessage(), getWindow());
            }
        });
    }
    @FXML
    private void handleAddNewArtistRole(SearchableComboBox<ArtistRole> targetComboBox){
        handleAddNewSimpleEntity(
                targetComboBox,
                allArtistRoles,
                "ui.newArtistRole",
                "ui.addNewArtistRole",
                "ui.artistRoleName",
                "info.artistRoleExistsAndSelected",
                ArtistRole::getRole,
                name -> new ArtistRole(0, name, true),
                artistRole -> {
                    try {
                        artistRoleDAO.save(artistRole);
                    }catch(Exception e){
                        throw new RuntimeException(e);
                    }
                }
        );
    }
    @FXML
    public void handleAddNewPublisher(){
        handleAddNewSimpleEntity(
                publisherComboBox,
                allPublishers,
                "ui.newPublisher",
                "ui.addNewPublisher",
                "ui.publisherName",
                "info.publisherExistsAndSelected",
                Publisher::getPublisherName,
                name -> new Publisher(0, name, true),
                publisher -> {
                    try {
                        publisherDAO.save(publisher);
                    }catch(Exception e){
                        throw new RuntimeException(e);
                    }
                }
        );
    }
    @FXML
    public void handleAddNewMediaType(){
        handleAddNewSimpleEntity(
                mediaTypeComboBox,
                allMediaTypes,
                "ui.newMediaType",
                "ui.addNewMediaType",
                "ui.mediaTypeName",
                "info.mediaTypeExistsAndSelected",
                MediaType::getTypeName,
                name -> new MediaType(0, name, true),
                mediaType -> {
                    try {
                        mediaTypeDAO.save(mediaType);
                    }catch(Exception e){
                        throw new RuntimeException(e);
                    }
                }
        );
    }
    @FXML
    private void handleAddNewGenre(SearchableComboBox<Genre> targetComboBox){
        handleAddNewSimpleEntity(
                targetComboBox,
                allGenres,
                "ui.newGenre",
                "ui.addNewGenre",
                "ui.genreName",
                "info.genreExistsAndSelected",
                Genre::getGenreName,
                name -> new Genre(0, name, true),
                genre -> {
                    try {
                        genreDAO.save(genre);
                    }catch(Exception e){
                        throw new RuntimeException(e);
                    }
                }
        );
    }
    @FXML
    private void handleAddNewLanguage(SearchableComboBox<Language> targetComboBox) {
        handleAddNewSimpleEntity(
                targetComboBox,
                allLanguages,
                "ui.newLanguage",
                "ui.addNewLanguage",
                "ui.languageName",
                "info.languageExistsAndSelected",
                Language::getLanguage,
                name -> new Language(0, name, true),
                language -> {
                    try {
                        languageDAO.save(language);
                    }catch(Exception e){
                        throw new RuntimeException(e);
                    }
                }
        );
    }
    @FXML
    private void handleAddNewTag(SearchableComboBox<Tag> targetComboBox) {
        handleAddNewSimpleEntity(
                targetComboBox,
                allTags,
                "ui.newTag",
                "ui.addNewTag",
                "ui.tagName",
                "info.tagExistsAndSelected",
                Tag::getName,
                name -> new Tag(0, name, true),
                tag -> {
                    try {
                        tagDAO.save(tag);
                    }catch(Exception e){
                        throw new RuntimeException(e);
                    }
                }
        );
    }
    @FXML
    private void handleAddNewFranchise(SearchableComboBox<Franchise> targetComboBox) {
        handleAddNewSimpleEntity(
                targetComboBox,
                allFranchises,
                "ui.newFranchise",
                "ui.addNewFranchise",
                "ui.franchiseName",
                "info.franchiseExistsAndSelected",
                Franchise::getName,
                name -> new Franchise(0, name, new ArrayList<>(), true),
                franchise -> {
                    try {
                        franchiseDAO.save(franchise);
                    }catch(Exception e){
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    //API search
    @FXML
    private void handleSearchRemoteByIsbn(){
        String isbn = isbnField.getText().trim().replace("-", "").replace("_", "");

        if(isbn.isEmpty() || isbn.length() != 10 && isbn.length() != 13){
            AlertManager.showAlert(Alert.AlertType.INFORMATION, LanguageManager.getString("ui.info"), LanguageManager.getString("ui.invalidIsbn"), getWindow());
            return;
        }

        ExternalMediaSearchResult result = mediaIntegrationFacade.fetchAndSyncBookIsbn(isbn, ApiSource.GOOGLE_BOOKS);
        if (result == null) {
            AlertManager.showAlert(Alert.AlertType.INFORMATION,
                    LanguageManager.getString("ui.info"),
                    LanguageManager.getString("ui.noIsbnFound"),
                    getWindow());
            return;
        }


        LocalDate releaseDate = parseDate(result.releaseDate());

        isbnField.setText(result.remoteId());
        titleField.setText(result.title());
        releaseDateField.setValue(releaseDate);
        descriptionArea.setText(result.description());

        if(!allPublishers.contains(result.publisher())){
            allPublishers.add(result.publisher());
        }
        publisherComboBox.setValue(result.publisher());

        artistContainer.getChildren().clear();
        for(Artist artist : result.artists()){
            MediaArtist partialCredit = new MediaArtist(artist, null, true);
            addMediaArtistRow(partialCredit);
        }

        languageContainer.getChildren().clear();
        for(Language language : result.languages()){
            if(!allLanguages.contains(language)){
                allLanguages.add(language);
            }
            addDynamicDropdownRow(languageContainer, allLanguages, language, null);
        }

        this.remoteCoverUrl = result.imageUrl();
        displayImage();
    }

    @FXML
    private void handleSearchRemoteByTitle(){
       String searchText = titleField.getText().trim();
       if(searchText.isEmpty()){
           AlertManager.showAlert(Alert.AlertType.INFORMATION, LanguageManager.getString("ui.info"), LanguageManager.getString("ui.titleEmpty"), getWindow());
           return;
       }

       try {
           FXMLLoader loader = new FXMLLoader(getClass().getResource("/de/srh_dr/mediamanagementtoolmmt/view/MediaSearchDialog.fxml"));
           loader.setResources(LanguageManager.getBundle());
           Parent root = loader.load();

           MediaSearchDialogController dialogController = loader.getController();
           dialogController.setupDialog(mediaIntegrationFacade, searchText);

           Stage dialogStage = new Stage();
           dialogStage.setTitle(LanguageManager.getString("ui.searchAPI"));
           dialogStage.initModality(Modality.WINDOW_MODAL); //disable the main window while running
           dialogStage.initOwner(getWindow());

           Scene scene = new Scene(root);
           scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/de/srh_dr/mediamanagementtoolmmt/css/style.css")).toExternalForm());
           dialogStage.setScene(scene);
           dialogStage.sizeToScene();
           Window parentWindow = getWindow();
           dialogStage.initOwner(parentWindow);

           //waits until window is sized and then calculates position
           dialogStage.setOnShown(event -> {
               assert parentWindow != null;
               double centerX = parentWindow.getX() + parentWindow.getWidth() / 2 - dialogStage.getWidth() / 2;
               double centerY = parentWindow.getY() + parentWindow.getHeight() / 2 - dialogStage.getHeight() / 2;
               dialogStage.setX(centerX);
               dialogStage.setY(centerY);
           });

           dialogStage.showAndWait();

           ExternalMediaSearchResult selectedResult = dialogController.getSelectedMedia();

           if(selectedResult == null){
               return;
           }

           LocalDate releaseDate = parseDate(selectedResult.releaseDate());

           if(selectedResult.source() == ApiSource.GOOGLE_BOOKS){
               isbnField.setText(selectedResult.remoteId());
           }else{
               isbnField.clear();
           }

           titleField.setText(selectedResult.title());
           releaseDateField.setValue(releaseDate);
           descriptionArea.setText(selectedResult.description());

           if(!allPublishers.contains(selectedResult.publisher())){
               allPublishers.add(selectedResult.publisher());
           }
           publisherComboBox.setValue(selectedResult.publisher());

           artistContainer.getChildren().clear();
           for(Artist artist : selectedResult.artists()){
               MediaArtist partialCredit = new MediaArtist(artist, null, true);
               addMediaArtistRow(partialCredit);
           }

           languageContainer.getChildren().clear();
           for(Language language : selectedResult.languages()){
               if(!allLanguages.contains(language)){
                   allLanguages.add(language);
               }
               addDynamicDropdownRow(languageContainer, allLanguages, language, null);
           }

           this.remoteCoverUrl = selectedResult.imageUrl();
           displayImage();
       } catch (Exception e) {
           LOGGER.log(Level.SEVERE, "Failed to remote search by title: " + e.getMessage(), e);
           AlertManager.showAlert(Alert.AlertType.ERROR, LanguageManager.getString("ui.error"), e.getMessage(), getWindow());
       }


    }

    //cancel/submit
    @FXML
    public void handleCancel(){
        if(mainController != null){
            mainController.showDefaultView();
        }else{
            LOGGER.log(Level.INFO,"MainController reference is missing. Cannot navigate back.");
        }
    }

    @FXML
    public void handleSubmit(){
        if(titleField.getText() == null ||  titleField.getText().trim().isEmpty()){
            AlertManager.showAlert(
                    Alert.AlertType.ERROR,
                    LanguageManager.getString("ui.error"),
                    LanguageManager.getString("error.title_null"),
                    getWindow()
            );
            return;
        }

        //centralized data collection
        List<Genre> selectedGenres = getSelectedGenres();
        List<Tag> selectedTags = getSelectedTags();
        List<Franchise> selectedFranchises = getSelectedFranchises();
        List<Language> selectedLanguages = getSelectedLanguages();
        MediaType selectedMediaType = mediaTypeComboBox.getValue();
        Media.MediaStatus selectedStatus = statusComboBox.getValue();

        int seriesOrder = 0;
        try {
            String orderText = seriesOrderField.getText().trim();
            if(!orderText.isEmpty()){
                seriesOrder = Integer.parseInt(orderText);
            }
        }catch(NumberFormatException e){
            LOGGER.log(Level.SEVERE, "Invalid order number, defaulting to 0",e);
        }

        // check mandatory fields
        if (selectedMediaType == null || selectedGenres.isEmpty() || selectedLanguages.isEmpty()) {
            StringBuilder errorMsg = new StringBuilder();
            if(selectedGenres.isEmpty()){errorMsg.append(LanguageManager.getString("error.media.genre_unset"));}
            if(selectedLanguages.isEmpty()){errorMsg.append(LanguageManager.getString("error.language_unset"));}
            if(selectedMediaType == null){errorMsg.append(LanguageManager.getString("error.media.type_unset"));}

            AlertManager.showAlert(
                    Alert.AlertType.ERROR,
                    LanguageManager.getString("ui.error"),
                    errorMsg.toString(),
                    getWindow()
            );
            return;
        }

        // save logic
        try{
            //image upload
            String cleanTitle = sanitizeForFilename(titleField.getText());
            int maxLength = Math.min(cleanTitle.length(), 30);
            String truncatedTitle = cleanTitle.substring(0, maxLength);
            String timestamp = String.valueOf(System.currentTimeMillis());
            String imageFileName = (currentMedia != null) ? currentMedia.getCoverFileName() : null;

            //User image upload
            if(selectedCoverImage != null){
                String originalName =  selectedCoverImage.getName();
                String extension = originalName.substring(originalName.lastIndexOf("."));
                String newName = truncatedTitle + "_" + timestamp + extension;

                if(imageManager.uploadImage(selectedCoverImage, newName)){
                    if(currentMedia != null && currentMedia.getCoverFileName() != null){
                        imageManager.deleteImage(currentMedia.getCoverFileName());
                    }
                    imageFileName = newName;
                }
            }
            // API image upload
            else if (remoteCoverUrl != null && !remoteCoverUrl.isEmpty()) {
                String extension = ".jpg";
                if (remoteCoverUrl.toLowerCase().contains(".png")) {
                    extension = ".png";
                } else if (remoteCoverUrl.toLowerCase().contains(".webp")) {
                    extension = ".webp";
                }
                String newName = truncatedTitle + "_" + timestamp + extension;

                if(imageManager.uploadImageFromUrl(remoteCoverUrl, newName)){
                    if(currentMedia != null && currentMedia.getCoverFileName() != null){
                        imageManager.deleteImage(currentMedia.getCoverFileName());
                    }
                    imageFileName = newName;
                }else {
                    LOGGER.log(Level.SEVERE, "Failed to download or upload remote cover image from: " + remoteCoverUrl);
                }
            }

            int targetViewId;
            boolean enabledIsbn = ConfigManager.getISBNEnabledMediaTypes().contains(selectedMediaType.getTypeName());
            String isbn = enabledIsbn && isbnField.getText() != null && (isbnField.getText().trim().length() == 10 || isbnField.getText().trim().length() == 13) ? isbnField.getText().trim() : null;

            if(currentMedia == null){ //fresh item
                Media newMedia = new Media.Builder()
                        .isNewItem(true)
                        .id(0)
                        .isbn(isbn)
                        .title(titleField.getText().trim())
                        .originalTitle(originalTitleField.getText().trim())
                        .coverFileName(imageFileName)
                        .description(descriptionArea.getText().trim())
                        .rating((int) mediaRating.getRating())
                        .releaseDate(releaseDateField.getValue())
                        .mediaType(mediaTypeComboBox.getValue())
                        .publisher(publisherComboBox.getValue())
                        .series(seriesComboBox.getValue())
                        .genres(selectedGenres)
                        .languages(selectedLanguages)
                        .credits(getSelectedMediaArtists())
                        .status(selectedStatus)
                        .tags(selectedTags)
                        .franchises(selectedFranchises)
                        .seriesOrder(seriesOrder)
                        .build();

                mediaIntegrationFacade.persistConfirmedMedia(newMedia);
                targetViewId = newMedia.getId();
            }else{ //existing item
                currentMedia.setIsbn(isbn);
                currentMedia.setTitle(titleField.getText().trim());
                currentMedia.setOriginalTitle(originalTitleField.getText().trim());
                currentMedia.setCoverFileName(imageFileName);
                currentMedia.setDescription(descriptionArea.getText().trim());
                currentMedia.setRating((int) mediaRating.getRating());
                currentMedia.setReleaseDate(releaseDateField.getValue());
                currentMedia.setPublisher(publisherComboBox.getValue());
                currentMedia.setSeries(seriesComboBox.getValue());
                currentMedia.setSeriesOrder(seriesOrder);
                currentMedia.setStatus(selectedStatus);

                syncList(currentMedia.getGenres(), selectedGenres, currentMedia::removeGenre, currentMedia::addGenre);
                syncList(currentMedia.getCredits(), getSelectedMediaArtists(), currentMedia::removeCredit, currentMedia::addCredit);
                syncList(currentMedia.getLanguages(), selectedLanguages, currentMedia::removeLanguage, currentMedia::addLanguage);
                syncList(currentMedia.getTags(), selectedTags, currentMedia::removeTag, currentMedia::addTag);
                syncList(currentMedia.getFranchises(), selectedFranchises, currentMedia::removeFranchise, currentMedia::addFranchise);

                mediaService.saveMedia(currentMedia); // Don't allow API-Search on existing Media
                targetViewId = currentMedia.getId();
            }

            mainController.showMediaDetail(targetViewId);
        }catch(Exception e){
            LOGGER.log(Level.SEVERE, "Failed to save Media: " + e.getMessage(), e);
            System.err.println("Failed to save Media: " + e.getMessage());
        }
    }
    //endregion
    //region HELPERS
    //dynamic row creation
    private void addMediaArtistRow(MediaArtist selectedCredit){
        HBox row = new HBox(10);

        SearchableComboBox<Artist> artistComboBox = new SearchableComboBox<>();
        if(allArtists != null){
            artistComboBox.setItems(allArtists);
        }
        artistComboBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(artistComboBox, Priority.ALWAYS);

        Button addArtistButton = new Button(LanguageManager.getString("ui.addArtist"));
        addArtistButton.setOnAction(e -> handleAddNewArtist(artistComboBox));

        SearchableComboBox<ArtistRole> artistRoleComboBox = new SearchableComboBox<>();
        if(allArtistRoles != null){
            artistRoleComboBox.setItems(allArtistRoles);
        }
        artistRoleComboBox.setMaxWidth(150);
        artistRoleComboBox.setPromptText(LanguageManager.getString("ui.selectArtistRole"));

        Button addArtistRoleButton = new Button(LanguageManager.getString("ui.addArtistRole"));
        addArtistRoleButton.setOnAction(e -> handleAddNewArtistRole(artistRoleComboBox));

        if(selectedCredit != null){
            artistComboBox.setValue(selectedCredit.getArtist());
            artistRoleComboBox.setValue(selectedCredit.getArtistRole());
        }

        Button removeButton = new Button(LanguageManager.getString("ui.remove"));
        removeButton.setOnAction(event -> artistContainer.getChildren().remove(row));

        row.getChildren().addAll(artistComboBox, addArtistButton, artistRoleComboBox, addArtistRoleButton, removeButton);

        artistContainer.getChildren().add(row);

    }

    private <T> void addDynamicDropdownRow(VBox container, ObservableList<T> items, T selectedItem, Runnable onAddAction){
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        SearchableComboBox<T> comboBox = new SearchableComboBox<>();
        if(items != null){
            comboBox.setItems(items);
        }
        comboBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(comboBox, Priority.ALWAYS);

        if(selectedItem != null){
            comboBox.setValue(selectedItem);
        }

        //add new button
        Button addButton = null;
        if (onAddAction != null) {
            addButton = new Button(LanguageManager.getString("ui.add"));
            addButton.setOnAction(e -> onAddAction.run());
        }

        //remove row button
        Button removeBtn = new Button(LanguageManager.getString("ui.remove"));
        removeBtn.setOnAction(event -> container.getChildren().remove(row));

        if (addButton != null) {
            row.getChildren().addAll(comboBox, addButton, removeBtn);
        } else {
            row.getChildren().addAll(comboBox, removeBtn);
        }

        container.getChildren().add(row);

    }

    //image handling
    private String sanitizeForFilename(String input) {
        if (input == null) {
            return "";
        }

        String localized = input.trim()
                .replace("ä", "ae").replace("ö", "oe").replace("ü", "ue")
                .replace("Ä", "Ae").replace("Ö", "Oe").replace("Ü", "Ue")
                .replace("ß", "ss");

        return localized.replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9_]", "");
    }

    private void displayImage(){
        coverImage.setImage(null);
        if(currentMedia != null && currentMedia.getCoverFileName() != null && !currentMedia.getCoverFileName().isEmpty()){
            String fullServerURL = imageManager.getFullImageUrl(currentMedia.getCoverFileName());
            Image serverImage = new Image(fullServerURL, true); //true for background loading
            coverImage.setImage(serverImage);
        }else if(remoteCoverUrl != null && !remoteCoverUrl.isEmpty()){
            String secureURL = remoteCoverUrl.replace("http://", "https://");
            coverImage.setImage(new Image(secureURL, true)); //true for background loading
        }else {
            String defaultImagePath = "/de/srh_dr/mediamanagementtoolmmt/Images/1920px-No-Image-Placeholder.svg.png";
            URL defaultImageURL = getClass().getResource(defaultImagePath);
            if(defaultImageURL != null){
                Image defaultImage = new Image(defaultImageURL.toExternalForm());
                coverImage.setImage(defaultImage);
            }
        }
    }

    // entity creation dialogs
    private <T> void handleAddNewSimpleEntity(SearchableComboBox<T> targetComboBox,
                                              List<T> allItemsList,
                                              String titleKey,
                                              String headerKey,
                                              String contentKey,
                                              String existsMsgKey,
                                              Function<T, String> nameExtractor,
                                              Function<String, T> entityCreator,
                                              Consumer<T> daoSaver){
        TextInputDialog dialog = new TextInputDialog();
        //swap out default buttons for i18n
        dialog.getDialogPane().getButtonTypes().clear();
        ButtonType saveButton = new ButtonType(LanguageManager.getString("ui.submit"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType(LanguageManager.getString("ui.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);
        dialog.setResultConverter(button -> {
            if(button == saveButton){
                return dialog.getEditor().getText();
            }
            return null;
        });

        dialog.setTitle(LanguageManager.getString(titleKey));
        dialog.setHeaderText(LanguageManager.getString(headerKey));
        dialog.setContentText(LanguageManager.getString(contentKey));

        dialog.initOwner(getWindow());

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(itemName -> {
            String cleanItemName = itemName.trim();

            if(!itemName.isEmpty()){
                try{
                    Optional<T> existingItem = allItemsList.stream()
                            .filter(m -> nameExtractor.apply(m).equalsIgnoreCase(cleanItemName))
                            .findFirst();
                    if(existingItem.isPresent()){
                        targetComboBox.setValue(existingItem.get());
                        AlertManager.showAlert(
                                Alert.AlertType.INFORMATION,
                                LanguageManager.getString("ui.info"),
                                LanguageManager.getString(existsMsgKey),
                                getWindow()
                        );
                    } else{
                        T item = entityCreator.apply(itemName);
                        daoSaver.accept(item);
                        allItemsList.add(item);
                        targetComboBox.setValue(item);
                    }
                }catch(Exception e){
                    LOGGER.log(Level.SEVERE, "Failed to add new entity: " +e.getMessage(), e);
                    AlertManager.showAlert(Alert.AlertType.ERROR, LanguageManager.getString("ui.error"), e.getMessage(), getWindow());
                }
            }
        });
    }

    // reset view
    public void refresh(){
        this.currentMedia = null;
        this.selectedCoverImage = null;
        this.remoteCoverUrl = null;

        isbnField.clear();
        titleField.clear();
        originalTitleField.clear();
        seriesOrderField.clear();
        descriptionArea.clear();

        releaseDateField.setValue(null);
        mediaTypeComboBox.setValue(null);
        publisherComboBox.setValue(null);
        seriesComboBox.setValue(null);

        mediaRating.setRating(0);
        displayImage();

        genreContainer.getChildren().clear();
        addGenreDropdown();

        languageContainer.getChildren().clear();
        addLanguageRow();

        artistContainer.getChildren().clear();
        addArtistDropRow();

        franchiseContainer.getChildren().clear();
        addFranchiseDropdown();

        tagContainer.getChildren().clear();
        addTagDropdown();

        api_search_isbn_button.setVisible(true);
        api_search_isbn_button.setManaged(true);
        api_search_title_button.setVisible(true);
        api_search_title_button.setManaged(true);
    }

    //helpers for saving data
    private LocalDate parseDate(String dateString){
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            dateString = dateString.trim();
            if(dateString.length() == 4){
                return LocalDate.of(Integer.parseInt(dateString), 1, 1);
            } else if (dateString.length() == 7) {
                String[] parts = dateString.split("-");
                return  LocalDate.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), 1);
            }else if (dateString.length() == 10) {
                return LocalDate.parse(dateString);
            }else{
                return null;
            }
        }catch (Exception e){
            LOGGER.log(Level.WARNING, "Failed to parse date: " + e.getMessage(), e);
            return null;
        }
    }

    private List<Genre> getSelectedGenres(){
        List<Genre> genres = new ArrayList<>();
        for(Node node : genreContainer.getChildren()){
            if(node instanceof HBox){
                @SuppressWarnings("unchecked")
                SearchableComboBox<Genre> comboBox = (SearchableComboBox<Genre>) ((HBox) node).getChildren().getFirst();
                if(comboBox.getValue() != null){
                    genres.add(comboBox.getValue());
                }
            }
        }
        return genres;
    }

    private List<Tag> getSelectedTags(){
        List<Tag> tags = new ArrayList<>();
        for(Node node : tagContainer.getChildren()){
            if(node instanceof HBox){
                @SuppressWarnings("unchecked")
                SearchableComboBox<Tag> comboBox = (SearchableComboBox<Tag>) ((HBox) node).getChildren().getFirst();
                if(comboBox.getValue() != null){
                    tags.add(comboBox.getValue());
                }
            }
        }
        return  tags;
    }

    private List<Franchise> getSelectedFranchises(){
        List<Franchise> franchises = new ArrayList<>();
        for(Node node : franchiseContainer.getChildren()){
            if(node instanceof HBox){
                @SuppressWarnings("unchecked")
                SearchableComboBox<Franchise> comboBox = (SearchableComboBox<Franchise>) ((HBox) node).getChildren().getFirst();
                if(comboBox.getValue() != null){
                    franchises.add(comboBox.getValue());
                }
            }
        }
        return franchises;
    }

    private List<Language> getSelectedLanguages(){
        List<Language> languages = new ArrayList<>();
        for(Node node : languageContainer.getChildren()){
            if(node instanceof HBox){
                @SuppressWarnings("unchecked")
                SearchableComboBox<Language> comboBox = (SearchableComboBox<Language>) ((HBox) node).getChildren().getFirst();
                if(comboBox.getValue() != null){
                    languages.add(comboBox.getValue());
                }
            }
        }
        return languages;
    }

    private List<MediaArtist> getSelectedMediaArtists(){
        List<MediaArtist> mediaArtists = new ArrayList<>();
        for(Node node : artistContainer.getChildren()){
            if(node instanceof HBox row){
                @SuppressWarnings("unchecked")
                SearchableComboBox<Artist> artistComboBox = (SearchableComboBox<Artist>) row.getChildren().getFirst();
                @SuppressWarnings("unchecked")
                SearchableComboBox<ArtistRole>  artistRoleComboBox = (SearchableComboBox<ArtistRole>) row.getChildren().get(2);

                if(artistComboBox.getValue() != null && artistRoleComboBox.getValue() != null){
                    mediaArtists.add(new MediaArtist(artistComboBox.getValue(), artistRoleComboBox.getValue(),true));
                }
            }
        }
        return mediaArtists;
    }

    //splits items for deletion / creation due to change tracking in Media class
    private <T> void syncList(Collection<T> currentItems, Collection<T> uiItems, Consumer<T> remover, Consumer<T> adder){
        new ArrayList<>(currentItems).forEach(item -> {
            if(!uiItems.contains(item)){remover.accept(item);}
        });
        uiItems.forEach(item -> {
            if(!currentItems.contains(item)){adder.accept(item);}
        });
    }

    //current window
    private Window getWindow(){
        if (viewContainer != null && viewContainer.getScene() != null) {
            return viewContainer.getScene().getWindow();
        }
        return null;
    }
    //endregion
}