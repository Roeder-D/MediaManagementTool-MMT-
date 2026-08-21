package de.srh_dr.mediamanagementtoolmmt.controller;

import de.srh_dr.mediamanagementtoolmmt.model.Media;
import de.srh_dr.mediamanagementtoolmmt.model.MediaArtist;
import de.srh_dr.mediamanagementtoolmmt.services.ImageManager;
import de.srh_dr.mediamanagementtoolmmt.services.MediaService;
import de.srh_dr.mediamanagementtoolmmt.util.AlertManager;
import de.srh_dr.mediamanagementtoolmmt.util.ConfigManager;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.controlsfx.control.Rating;

import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MediaDetailController implements MainControllerAware {
    private static final Logger LOGGER  = Logger.getLogger(MediaDetailController.class.getName());

    @FXML private Label titleLabel;
    @FXML private Label isbnLabel;
    @FXML private Label isbnField;
    @FXML private FlowPane genreContainer;
    @FXML private FlowPane tagContainer;
    @FXML private VBox artistContainer;
    @FXML private HBox ratingBox;
    @FXML private Label seriesField;
    @FXML private Label publisherField;
    @FXML private Label releaseDateField;
    @FXML private Label descriptionField;
    @FXML private Label franchiseLabel;
    @FXML private FlowPane franchiseContainer;
    @FXML private Label seriesLabel;
    @FXML private Label seriesOrderField;
    @FXML private FlowPane languageContainer;
    @FXML private Label tagLabel;
    @FXML private ImageView coverImage;
    @FXML private ScrollPane descriptionPane;


    private MainController mainController;
    private Media currentMedia;
    private final MediaService mediaService = MediaService.getInstance();
    private final ImageManager imageManager = ImageManager.getInstance();


    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public MainController getMainController() {
        return mainController;
    }

    @FXML
    private void handleDeleteMedia(){
        if(currentMedia == null)return;

        boolean confirmDelete = AlertManager.requestConfirmation(
                LanguageManager.getString("ui.warning"),
                LanguageManager.getString("warning.confirmDeleteMedia"),
                getWindow()
        );

        if(confirmDelete){
            try{
                mediaService.deleteMedia(currentMedia.getId());

                if(mainController != null){
                    mainController.showDefaultView();
                }
            }catch(Exception e){
                LOGGER.log(Level.SEVERE, "Error while deleting media : " + e.getMessage(), e);
                AlertManager.showAlert(
                        Alert.AlertType.ERROR,
                        LanguageManager.getString("ui.error"),
                        e.getMessage(),
                        getWindow());
            }
        }
    }

    @FXML
    private void handleEditMedia(){
        if(currentMedia != null && mainController != null){
            mainController.showMediaFormView(currentMedia.getId());
        }
    }

    @FXML
    private void handleLendMedia(){
        if(currentMedia != null && mainController != null){
            mainController.showLendingView(currentMedia.getId(), true);
        }
    }

    public void loadMediaDetails(int mediaId){
      if(mediaId == 0) return;

      try{
          Media media = mediaService.getMediaById(mediaId);
          if(media == null) return;

          this.currentMedia = media;

          //populate image
          displayImage();

          // populate title field
          titleLabel.setText(media.getTitle());

          // toggle ISBN field visible
          boolean showISBN = true;
          try {
              List<String> isbnTypes = ConfigManager.getISBNEnabledMediaTypes();
              String currentMediaType = currentMedia.getMediatype().getTypeName();
              showISBN = isbnTypes.stream().anyMatch(mt -> mt.equalsIgnoreCase(currentMediaType));
          }catch(Exception e){
              LOGGER.log(Level.SEVERE, "Error while getting isbn types : " + e.getMessage(), e);
              AlertManager.showAlert(
                      Alert.AlertType.ERROR,
                      LanguageManager.getString("ui.error"),
                      LanguageManager.getString("error.failedToLoad") + e.getMessage(),
                      getWindow()
              );
          }
          isbnLabel.setVisible(showISBN);
          isbnField.setVisible(showISBN);
          isbnLabel.setManaged(showISBN);
          isbnField.setManaged(showISBN);

          if(showISBN){
              isbnField.setText(media.getIsbn() != null ? media.getIsbn() : "");
          }
          if(isbnField.getText().isEmpty()){
              isbnLabel.setVisible(false);
              isbnLabel.setManaged(false);
          }

          // populate genres
          genreContainer.getChildren().clear();
          media.getGenres().forEach(genre -> {
              if(genre.getGenreName() != null && !genre.getGenreName().trim().isEmpty()){
                  Label badge = new Label(genre.getGenreName());
                  badge.getStyleClass().addAll("badge", "genre-badge");

                  genreContainer.getChildren().add(badge);
              }
          });

          //populate franchises
          if(media.getFranchises().isEmpty()){
              franchiseLabel.setVisible(false);
              franchiseLabel.setManaged(false);
              franchiseContainer.setVisible(false);
              franchiseContainer.setManaged(false);
          }else {
              franchiseContainer.getChildren().clear();
              media.getFranchises().forEach(franchise -> {
                  if (franchise.getName() != null && !franchise.getName().trim().isEmpty()) {
                      Label badge = new Label(franchise.getName());
                      badge.getStyleClass().addAll("badge", "franchise-badge");

                      franchiseContainer.getChildren().add(badge);
                  }
              });
          }

          // populate tags
          if(media.getTags().isEmpty()){
              tagLabel.setVisible(false);
              tagLabel.setManaged(false);
              tagContainer.setVisible(false);
              tagContainer.setManaged(false);
          }else {
              tagContainer.getChildren().clear();
              media.getTags().forEach(tag -> {
                  if (tag != null && !tag.getName().trim().isEmpty()) {
                      Label badge = new Label(tag.getName());
                      badge.getStyleClass().addAll("badge", "tag-badge");

                      tagContainer.getChildren().add(badge);
                  }
              });
          }

          //populate languages
          languageContainer.getChildren().clear();
          media.getLanguages().forEach(language -> {
              if(language != null && !language.getLanguage().trim().isEmpty()){
                  Label badge = new Label(language.getLanguage());
                  badge.getStyleClass().addAll("badge", "language-badge");

                  languageContainer.getChildren().add(badge);
              }
          });

          // populate artists
          artistContainer.getChildren().clear();
          media.getCredits().forEach(credit -> {
              if(credit != null){
                  HBox row = new HBox(10);
                  row.getStyleClass().add("credit-row");
                  Label artist = getLabel(credit);

                  Label artistRole =  new Label(credit.getArtistRole().getRole());
                  artistRole.getStyleClass().add("artist-role-label");
                  row.getChildren().addAll(artist, artistRole);
                  artistContainer.getChildren().add(row);
              }
          });

          // populate series order
          if(media.getSeriesOrder() != 0) {
              if(media.getSeries().getNumberOfTitles() == 0) {
                  seriesOrderField.setText("Volume " + media.getSeriesOrder());
              }else {
                  seriesOrderField.setText("Volume " + media.getSeriesOrder() + "/" + media.getSeries().getNumberOfTitles());
              }
          }else{
              seriesOrderField.setVisible(false);
              seriesOrderField.setManaged(false);
          }

          // implement rating system
          Rating starRating = new Rating(5);
          starRating.setPartialRating(false);
          starRating.setRating(media.getRating());
          ratingBox.getChildren().setAll(starRating);

          starRating.ratingProperty().addListener((obs, oldRating, newRating) -> {
              if(currentMedia != null){
                  try{
                      currentMedia.setRating(newRating.intValue());
                      mediaService.saveMedia(currentMedia);

                      System.out.println("Database auto-updated! New rating stored: " + newRating.intValue());
                  }catch(Exception e){
                      LOGGER.log(Level.SEVERE, "Error while saving new rating!", e);
                      AlertManager.showAlert(
                              Alert.AlertType.ERROR,
                              LanguageManager.getString("error.failedToSave"),
                              e.getMessage(),
                              getWindow());
                  }
              }
          });

          if(media.getSeries() != null){
              seriesField.setText(media.getSeries().getName());
          }else{
              seriesField.setVisible(false);
              seriesField.setManaged(false);
              seriesLabel.setVisible(false);
              seriesLabel.setManaged(false);
          }

          if(media.getPublisher() != null){
              publisherField.setText(media.getPublisher().getPublisherName());
          }else{
              publisherField.setText("/");
          }

          if(media.getReleaseDate() != null){
              releaseDateField.setText(media.getReleaseDate().toString());
          }else{
              releaseDateField.setText("/");
          }

          if(media.getDescription() != null){
              descriptionField.setText(media.getDescription());
          }else{
              descriptionField.setText("/");
          }

          descriptionPane.prefHeightProperty().bind(descriptionField.heightProperty().add(20));
          descriptionPane.setMaxHeight(200);


      }catch(Exception e){
          LOGGER.log(Level.SEVERE, "Error while loading media: " + e.getMessage(), e);
          AlertManager.showAlert(
                  Alert.AlertType.ERROR,
                  LanguageManager.getString("error.failedToLoad"),
                  e.getMessage(),
                  getWindow());
      }
    }

    private static Label getLabel(MediaArtist credit) {
        Label artist = new Label();
        String name = "";
        if(credit.getArtist().getFirstName() != null && !credit.getArtist().getFirstName().trim().isEmpty()){
            name = credit.getArtist().getFirstName();
        }
        if(name.isEmpty()){
            name = credit.getArtist().getLastName();
        }else{
            name += " " + credit.getArtist().getLastName();
        }
        artist.setText(name);
        return artist;
    }

    private void displayImage(){
        coverImage.setImage(null);
        if(currentMedia != null && currentMedia.getCoverFileName() != null && !currentMedia.getCoverFileName().isEmpty()){
            String fullServerURL = imageManager.getFullImageUrl(currentMedia.getCoverFileName());
            Image serverImage = new Image(fullServerURL, true); //true for background loading
            coverImage.setImage(serverImage);
        }else {
            String defaultImagePath = "/de/srh_dr/mediamanagementtoolmmt/Images/1920px-No-Image-Placeholder.svg.png";
            URL defaultImageURL = getClass().getResource(defaultImagePath);
            if(defaultImageURL != null){
                Image defaultImage = new Image(defaultImageURL.toExternalForm());
                coverImage.setImage(defaultImage);
            }
        }
    }
}
