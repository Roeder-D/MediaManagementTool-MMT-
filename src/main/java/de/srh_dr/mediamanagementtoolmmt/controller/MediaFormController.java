package de.srh_dr.mediamanagementtoolmmt.controller;

import de.srh_dr.mediamanagementtoolmmt.data.*;
import de.srh_dr.mediamanagementtoolmmt.model.*;
import de.srh_dr.mediamanagementtoolmmt.util.AlertManager;
import de.srh_dr.mediamanagementtoolmmt.util.ImageManager;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.controlsfx.control.Rating;
import org.controlsfx.control.SearchableComboBox;

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;

//TODO: add APIs

public class MediaFormController implements MainControllerAware{
    @FXML private StackPane imageDropZone;
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
    @FXML private Label isbnLabel;

    // DAOs
    PublisherDAO publisherDAO = new PublisherDAO();
    GenreDAO genreDAO = new GenreDAO();
    ArtistDAO artistDAO = new ArtistDAO();
    ArtistRoleDAO artistRoleDAO = new ArtistRoleDAO();
    TagDAO tagDAO = new TagDAO();
    MediaTypeDAO  mediaTypeDAO = new MediaTypeDAO();
    SeriesDAO seriesDAO = new SeriesDAO();
    LanguageDAO languageDAO = new LanguageDAO();
    MediaDAO mediaDAO = new MediaDAO();

    MainController mainController;
    ImageManager imageManager = new ImageManager();
    private File selectedCoverImage;

    Media currentMedia;
    private List<Genre> allGenres;
    private List<Language> allLanguages;
    private List<Artist> allArtists;
    private List<ArtistRole> allArtistRoles;
    private List<Publisher> allPublishers;
    private List<MediaType> allMediaTypes;
    private List<Series> allSeries;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void initialize() {
        allGenres = genreDAO.findAll();
        allLanguages = languageDAO.findAll();
        allArtists = artistDAO.findAll();
        allArtistRoles = artistRoleDAO.findAll();
        allPublishers = publisherDAO.findAll();
        allMediaTypes = mediaTypeDAO.findAll();
        allSeries = seriesDAO.findAll();

        publisherComboBox.getItems().addAll(allPublishers);
        mediaTypeComboBox.getItems().addAll(allMediaTypes);
        seriesComboBox.getItems().addAll(allSeries);

        addGenreDropdown();
        addLanguageRow();
        addArtistDropRow();
    }

    public void loadMedia(int mediaId){
        if(mediaId != 0){
            try{
                Media media = mediaDAO.read(mediaId);
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


            }catch(Exception e){
                AlertManager.showAlert(Alert.AlertType.ERROR, LanguageManager.getString("ui.error"), LanguageManager.getString("error.failedToLoad") + ": " + e.getMessage());
            }
        }
    }

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
                System.err.println("Invalid file type dropped");
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

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
    private void handleAddNewArtist(SearchableComboBox<Artist> targetComboBox){
        Dialog<Artist> dialog = new Dialog<>();
        dialog.setTitle(LanguageManager.getString("ui.newArtist"));
        dialog.setHeaderText(LanguageManager.getString("ui.addNewArtist"));

        ButtonType saveButtonType = new ButtonType(LanguageManager.getString("ui.submit"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        TextField firstNameField = new TextField();
        firstNameField.setPromptText(LanguageManager.getString("ui.firstName"));
        TextField lastNameField = new TextField();
        lastNameField.setPromptText(LanguageManager.getString("ui.lastName"));
        TextField aliasField = new TextField();
        aliasField.setPromptText(LanguageManager.getString("ui.alias"));
        TextField nationalityField = new TextField();
        nationalityField.setPromptText(LanguageManager.getString("ui.nationality"));

        gridPane.add(new Label(LanguageManager.getString("ui.firstName") + ": "), 0, 0);
        gridPane.add(firstNameField, 1, 0);
        gridPane.add(new Label(LanguageManager.getString("ui.lastName") + ": "), 0, 1);
        gridPane.add(lastNameField, 1, 1);
        gridPane.add(new Label(LanguageManager.getString("ui.alias") + ": "), 0, 2);
        gridPane.add(aliasField, 1, 2);
        gridPane.add(new Label(LanguageManager.getString("ui.nationality") + ": "), 0, 3);
        gridPane.add(nationalityField, 1, 3);

        dialog.getDialogPane().setContent(gridPane);

        Platform.runLater(lastNameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String cleanFirstName = firstNameField.getText().trim();
                String cleanLastName = lastNameField.getText().trim();
                String cleanAlias = aliasField.getText().trim();
                String cleanNationality = nationalityField.getText().trim();

                if(!cleanLastName.isEmpty() || !cleanAlias.isEmpty()){
                    return new Artist(0, cleanFirstName, cleanLastName, cleanAlias, cleanNationality, true);
                }
            }
            return null;
        });

        Optional<Artist> result = dialog.showAndWait();

        result.ifPresent(artist -> {
            try{
                boolean isDuplicate = allArtists.stream()
                                .anyMatch(a -> a.getFirstName().equalsIgnoreCase(artist.getFirstName()) &&
                                        a.getLastName().equalsIgnoreCase(artist.getLastName()) ||
                                        a.getAlias().equalsIgnoreCase(artist.getAlias()));
                if(isDuplicate){
                    String fullName = "";
                    if(!artist.getFirstName().isEmpty()){
                        fullName = artist.getFirstName() + " ";
                    }
                    if(!artist.getLastName().isEmpty()){
                        fullName += artist.getLastName() + " ";
                    }
                    if(!artist.getAlias().isEmpty()){
                        fullName += artist.getAlias() + " ";
                    }
                    fullName = fullName.trim();

                    boolean continueAnyway = AlertManager.requestConfirmation(
                            LanguageManager.getString("ui.warning"),
                            LanguageManager.getString("warning.theArtistAlreadyExists_p1") + fullName + LanguageManager.getString("warning.theArtistAlreadyExists_p2")
                    );

                    if(!continueAnyway){
                        return;
                    }
                }

                artistDAO.save(artist);
                allArtists.add(artist);
                targetComboBox.getItems().add(artist);
                targetComboBox.setValue(artist);
            }catch(Exception e){
                AlertManager.showAlert(Alert.AlertType.ERROR, LanguageManager.getString("ui.error"), e.getMessage());
            }
        });
    }

    @FXML
    public void handleAddNewSeries(){
        SearchableComboBox<Series> targetComboBox = seriesComboBox;
        int currentYear = LocalDate.now().getYear();

        Dialog<Series> dialog = new Dialog<>();
        dialog.setTitle(LanguageManager.getString("ui.addNewSeries"));
        dialog.setHeaderText(LanguageManager.getString("ui.addNewSeries"));

        ButtonType saveButtonType = new ButtonType(LanguageManager.getString("ui.submit"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        TextField seriesNameField = new TextField();
        seriesNameField.setPromptText(LanguageManager.getString("ui.seriesName"));
        TextField seriesTitleCountField = new TextField();
        seriesTitleCountField.setPromptText(LanguageManager.getString("ui.seriesTitleCount"));
        Spinner<Integer> seriesStartYearSpinner = new Spinner<>(1800, 2200, currentYear);
        seriesStartYearSpinner.setEditable(true);

        VBox altTitleContainer = new VBox(5);
        Button addAltTitleButton = new Button(LanguageManager.getString("ui.addAltTitle"));

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

        dialog.getDialogPane().setContent(gridPane);
        Platform.runLater(seriesNameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if(dialogButton == saveButtonType) {
                String cleanTitle = seriesNameField.getText().trim();
                if(!cleanTitle.isEmpty()){
                    int titleCount = 0;
                    try{
                        titleCount = Integer.parseInt(seriesTitleCountField.getText().trim());
                    }catch (NumberFormatException ignored){}

                    List<AltTitle> altTitles = new ArrayList<>();
                    for(Node node : altTitleContainer.getChildren()){
                        if(node instanceof HBox){
                            TextField tf = (TextField)  ((HBox) node).getChildren().getFirst();
                            if(!tf.getText().trim().isEmpty()){
                                altTitles.add(new AltTitle(0, tf.getText().trim(), true));
                            }
                        }
                    }

                    return new Series(
                            true,
                            0,
                            cleanTitle,
                            titleCount,
                            (int) seriesStartYearSpinner.getValue(),
                            altTitles
                    );
                }
            }
            return null;
        });
        Optional<Series> result = dialog.showAndWait();

        result.ifPresent(series -> {
            try{
                boolean isDuplicate = allSeries.stream().anyMatch(s -> s.getName().equalsIgnoreCase(series.getName()));

                if(isDuplicate){
                    boolean continueAnyway = AlertManager.requestConfirmation(
                            LanguageManager.getString("ui.warning"),
                            LanguageManager.getString("warning.theSeriesAlreadyExists_p1") + series.getName() + LanguageManager.getString("warning.theSeriesAlreadyExists_p2")
                    );
                    if(!continueAnyway){
                        return;
                    }
                }
                seriesDAO.save(series);
                allSeries.add(series);
                targetComboBox.getItems().add(series);
                targetComboBox.setValue(series);
            }catch(Exception e){
                AlertManager.showAlert(Alert.AlertType.ERROR, LanguageManager.getString("ui.error"), e.getMessage());
            }
        });
    }
    @FXML
    private void handleAddNewArtistRole(SearchableComboBox<ArtistRole> targetComboBox){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(LanguageManager.getString("ui.newArtistRole"));
        dialog.setHeaderText(LanguageManager.getString("ui.addNewArtistRole"));
        dialog.setContentText(LanguageManager.getString("ui.artistRoleName"));

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(roleName -> {
            String cleanRoleName = roleName.trim();
            if(!cleanRoleName.trim().isEmpty()){
                try{
                    Optional<ArtistRole> existingArtistRole = allArtistRoles.stream()
                            .filter(ar -> ar.getRole().equalsIgnoreCase(cleanRoleName))
                            .findFirst();

                    if(existingArtistRole.isPresent()){
                        targetComboBox.setValue(existingArtistRole.get());
                        AlertManager.showAlert(
                                Alert.AlertType.INFORMATION,
                                LanguageManager.getString("ui.info"),
                                LanguageManager.getString("error.artistRoleExistsAndSelected")
                        );
                    }else{
                        ArtistRole artistRole = new ArtistRole(0, roleName.trim(), true);
                        artistRoleDAO.save(artistRole);
                        allArtistRoles.add(artistRole);
                        targetComboBox.getItems().add(artistRole);
                        targetComboBox.setValue(artistRole);
                    }
                }catch(Exception e){
                    AlertManager.showAlert(
                            Alert.AlertType.ERROR,
                            LanguageManager.getString("ui.error"),
                            e.getMessage()
                    );
                }
            }
        });
    }
    @FXML
    public void handleAddNewPublisher(){
        SearchableComboBox<Publisher> targetComboBox = publisherComboBox;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(LanguageManager.getString("ui.newPublisher"));
        dialog.setHeaderText(LanguageManager.getString("ui.addNewPublisher"));
        dialog.setContentText(LanguageManager.getString("ui.publisherName"));

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(publisherName -> {
            String cleanPublisherName = publisherName.trim();
            if(!cleanPublisherName.isEmpty()){
                try{
                    Optional<Publisher> existingPublisher = allPublishers.stream()
                            .filter(p -> p.getPublisherName().equalsIgnoreCase(cleanPublisherName))
                            .findFirst();

                    if(existingPublisher.isPresent()){
                        targetComboBox.setValue(existingPublisher.get());
                        AlertManager.showAlert(
                                Alert.AlertType.INFORMATION,
                                LanguageManager.getString("ui.info"),
                                LanguageManager.getString("ui.publisherExistsAndSelected"));
                    }else{
                        Publisher publisher = new Publisher(0, publisherName.trim(), true);
                        publisherDAO.save(publisher);
                        allPublishers.add(publisher);
                        targetComboBox.getItems().add(publisher);
                        targetComboBox.setValue(publisher);
                    }
                }catch(Exception e) {
                    AlertManager.showAlert(Alert.AlertType.ERROR, LanguageManager.getString("ui.error"), e.getMessage());
                }
            }
        });

    }
    @FXML
    public void handleAddNewMediaType(){
        SearchableComboBox<MediaType> targetComboBox = mediaTypeComboBox;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(LanguageManager.getString("ui.addNewMediaType"));
        dialog.setHeaderText(LanguageManager.getString("ui.addNewMediaType"));
        dialog.setContentText(LanguageManager.getString("ui.mediaTypeName"));

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(mediaTypeName -> {
            String cleanMediaTypeName = mediaTypeName.trim();
           if(!cleanMediaTypeName.isEmpty()){
               try{
                   Optional<MediaType> existingMediaType = allMediaTypes.stream()
                           .filter(m -> m.getTypeName().equalsIgnoreCase(cleanMediaTypeName))
                           .findFirst();
                   if(existingMediaType.isPresent()){
                       targetComboBox.setValue(existingMediaType.get());
                       AlertManager.showAlert(
                               Alert.AlertType.INFORMATION,
                               LanguageManager.getString("ui.info"),
                               LanguageManager.getString("info.mediaTypeExistsAndSelected")
                       );
                   } else{
                       MediaType mediaType = new MediaType(0, mediaTypeName.trim(), true);
                       mediaTypeDAO.save(mediaType);
                       allMediaTypes.add(mediaType);
                       targetComboBox.getItems().add(mediaType);
                       targetComboBox.setValue(mediaType);
                   }
               }catch(Exception e){
                   AlertManager.showAlert(Alert.AlertType.ERROR, LanguageManager.getString("ui.error"), e.getMessage());
               }
           }
        });
    }
    @FXML
    private void handleAddNewGenre(SearchableComboBox<Genre> targetComboBox){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(LanguageManager.getString("ui.newGenre"));
        dialog.setHeaderText(LanguageManager.getString("ui.addNewGenre"));
        dialog.setContentText(LanguageManager.getString("ui.genreName"));

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String cleanName = name.trim();
            if(!cleanName.isEmpty()){
                try {
                    Optional<Genre> existingGenre = allGenres.stream()
                            .filter(g -> g.getGenreName().equalsIgnoreCase(cleanName))
                            .findFirst();

                    if(existingGenre.isPresent()){
                        targetComboBox.setValue(existingGenre.get());
                        AlertManager.showAlert(
                                Alert.AlertType.INFORMATION,
                                LanguageManager.getString("ui.info"),
                                LanguageManager.getString("info.genreExistsAndSelected")
                        );
                    }else{
                        Genre genre = new Genre(0, name.trim(), true);
                        genreDAO.save(genre); // Assuming your GenreDAO matches others
                        allGenres.add(genre);
                        targetComboBox.getItems().add(genre);
                        targetComboBox.setValue(genre);
                    }
                } catch(Exception e) {
                    AlertManager.showAlert(Alert.AlertType.ERROR, LanguageManager.getString("ui.error"), e.getMessage());
                }
            }
        });
    }
    @FXML
    private void handleAddNewLanguage(SearchableComboBox<Language> targetComboBox) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(LanguageManager.getString("ui.newLanguage"));
        dialog.setHeaderText(LanguageManager.getString("ui.addNewLanguage"));
        dialog.setContentText(LanguageManager.getString("ui.languageName"));

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String cleanName = name.trim();
            if(!cleanName.isEmpty()){
                Optional<Language> existingLanguage = allLanguages.stream()
                        .filter(l -> l.getLanguage().equalsIgnoreCase(cleanName))
                        .findFirst();
                try {
                    if(existingLanguage.isPresent()){
                        targetComboBox.setValue(existingLanguage.get());
                        AlertManager.showAlert(
                                Alert.AlertType.INFORMATION,
                                LanguageManager.getString("ui.info"),
                                LanguageManager.getString("info.languageExistsAndSelected")
                        );
                    }else {
                        Language lang = new Language(0, name.trim(), true);
                        languageDAO.save(lang);
                        allLanguages.add(lang);
                        targetComboBox.getItems().add(lang);
                        targetComboBox.setValue(lang);
                    }
                } catch(Exception e) {
                    AlertManager.showAlert(Alert.AlertType.ERROR, LanguageManager.getString("ui.error"), e.getMessage());
                }
            }
        });
    }

    @FXML
    public void handleCancel(){
        if(mainController != null){
            mainController.showDefaultView();
        }else{
            System.err.println("MainController reference is missing. Cannot navigate back.");
        }
    }

    @FXML
    public void handleSubmit(){
        if(titleField.getText() == null ||  titleField.getText().trim().isEmpty()){
            AlertManager.showAlert(
                    Alert.AlertType.ERROR,
                    LanguageManager.getString("ui.error"),
                    LanguageManager.getString("error.title_null")
            );
            return;
        }

        List<Genre> selectedGenres = getSelectedGenres();
        List<Language> selectedLanguages = getSelectedLanguages();
        MediaType selectedMediaType = mediaTypeComboBox.getValue();
        if (selectedMediaType == null || selectedGenres.isEmpty() || selectedLanguages.isEmpty()) {
            StringBuilder errorMsg = new StringBuilder();
            if(selectedGenres.isEmpty()){errorMsg.append(LanguageManager.getString("error.media.genre_unset"));}
            if(selectedLanguages.isEmpty()){errorMsg.append(LanguageManager.getString("error.language_unset"));}
            if(selectedMediaType == null){errorMsg.append(LanguageManager.getString("error.media.type_unset"));}

            AlertManager.showAlert(
                    Alert.AlertType.ERROR,
                    LanguageManager.getString("ui.error"),
                    errorMsg.toString()
            );
            return;
        }

        try{
            String imageFileName = (currentMedia != null) ? currentMedia.getCoverFileName() : null;

            if(selectedCoverImage != null){
                String originalName =  selectedCoverImage.getName();
                String extension = originalName.substring(originalName.lastIndexOf("."));

                String cleanTitle = titleField.getText().trim().replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("Ä", "Ae").replace("Ö", "Oe").replace("Ü", "Ue").replace("ß", "ss")
                        .replaceAll("\\s+", "_") // Convert spaces to underscores
                        .replaceAll("[^a-zA-Z0-9_]", ""); // Remove everything else

                int maxLength = Math.min(cleanTitle.length(), 30);
                String truncatedTitle = cleanTitle.substring(0, maxLength);
                String timestamp = String.valueOf(System.currentTimeMillis());
                String newName = truncatedTitle + "_" + timestamp + extension;

                boolean success = imageManager.uploadImage(selectedCoverImage, newName);

                if(success){
                    if(currentMedia != null && currentMedia.getCoverFileName() != null){
                        imageManager.deleteImage(currentMedia.getCoverFileName());
                    }
                    imageFileName = newName;
                }else{
                    System.err.println("Failed to upload image: " + newName);
                }
            }
            if(currentMedia == null){ //fresh item
                Media newMedia = new Media.Builder()
                        .isNewItem(true)
                        .id(0)
                        .isbn(isbnField.getText().trim())
                        .title(titleField.getText().trim())
                        .originalTitle(originalTitleField.getText().trim())
                        .coverFileName(imageFileName)
                        .description(descriptionArea.getText().trim())
                        .rating((int) mediaRating.getRating())
                        .releaseDate(releaseDateField.getValue())
                        .mediatype(mediaTypeComboBox.getValue())
                        .publisher(publisherComboBox.getValue())
                        .series(seriesComboBox.getValue())
                        .genres(selectedGenres)
                        .languages(selectedLanguages)
                        .credits(getSelectedMediaArtists())
                        .status(MediaStatus.AVAILABLE)
                        .build();

                mediaDAO.save(newMedia);
            }else{ //existing item
                currentMedia.setIsbn(isbnField.getText().trim());
                currentMedia.setTitle(titleField.getText().trim());
                currentMedia.setOriginalTitle(originalTitleField.getText().trim());
                currentMedia.setCoverFileName(imageFileName);
                currentMedia.setDescription(descriptionArea.getText().trim());
                currentMedia.setRating((int) mediaRating.getRating());
                currentMedia.setReleaseDate(releaseDateField.getValue());
                currentMedia.setPublisher(publisherComboBox.getValue());
                currentMedia.setSeries(seriesComboBox.getValue());

                syncList(currentMedia.getGenres(), selectedGenres, currentMedia::removeGenre, currentMedia::addGenre);
                syncList(currentMedia.getCredits(), getSelectedMediaArtists(), currentMedia::removeCredit, currentMedia::addCredit);
                syncList(currentMedia.getLanguages(), selectedLanguages, currentMedia::removeLanguage, currentMedia::addLanguage);

                mediaDAO.save(currentMedia);
            }
        }catch(Exception e){
            System.err.println("Failed to save Media: " + e.getMessage());
        }
    }

    @FXML
    private void handleImageUpload(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(LanguageManager.getString("ui.selectCoverImage"));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        Window stage = imageDropZone.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if(file != null){
            selectedCoverImage = file;
            this.coverImage.setImage(new Image(file.toURI().toString()));
        }
    }


    // Helper
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

    private void addMediaArtistRow(MediaArtist selectedCredit){
        HBox row = new HBox(10);

        SearchableComboBox<Artist> artistComboBox = new SearchableComboBox<>();
        if(allArtists != null){
            artistComboBox.getItems().addAll(allArtists);
        }
        artistComboBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(artistComboBox, Priority.ALWAYS);

        Button addArtistButton = new Button(LanguageManager.getString("ui.addArtist"));
        addArtistButton.setOnAction(e -> handleAddNewArtist(artistComboBox));

        SearchableComboBox<ArtistRole> artistRoleComboBox = new SearchableComboBox<>();
        if(allArtistRoles != null){
            artistRoleComboBox.getItems().addAll(allArtistRoles);
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

    private <T> void addDynamicDropdownRow(VBox container, List<T> items, T selectedItem, Runnable onAddAction){
        HBox row = new HBox(10);

        SearchableComboBox<T> comboBox = new SearchableComboBox<>();
        if(items != null){
            comboBox.getItems().addAll(items);
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

    private <T> void syncList(Collection<T> currentItems, Collection<T> uiItems, Consumer<T> remover, Consumer<T> adder){
        new ArrayList<>(currentItems).forEach(item -> {
            if(!uiItems.contains(item)){remover.accept(item);}
        });
        uiItems.forEach(item -> {
            if(!currentItems.contains(item)){adder.accept(item);}
        });
    }
}
