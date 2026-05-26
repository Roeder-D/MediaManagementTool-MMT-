package de.srh_dr.mediamanagementtoolmmt.controller;

import de.srh_dr.mediamanagementtoolmmt.data.MediaDAO;
import de.srh_dr.mediamanagementtoolmmt.model.Genre;
import de.srh_dr.mediamanagementtoolmmt.model.Media;
import de.srh_dr.mediamanagementtoolmmt.model.Tag;
import de.srh_dr.mediamanagementtoolmmt.util.AlertManager;
import de.srh_dr.mediamanagementtoolmmt.util.ConfigManager;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.Rating;

import java.util.List;
import java.util.stream.Collectors;

public class MediaDetailController implements MainControllerAware {
    @FXML private Label isbnLabel;
    @FXML private Label isbnField;
    @FXML private FlowPane genreContainer;
    @FXML private FlowPane tagsContainer;
    @FXML private VBox artistContainer;
    @FXML private HBox ratingBox;
    @FXML private Label seriesField;
    @FXML private Label publisherField;
    @FXML private Label releaseDateField;
    @FXML private Label descriptionField;


    MainController mainController;
    Media currentMedia;
    MediaDAO mediaDAO =  new MediaDAO();


    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleDeleteMedia(){
        if(currentMedia == null)return;

        boolean confirmDelete = AlertManager.requestConfirmation(
                LanguageManager.getString("ui.warning"),
                LanguageManager.getString("warning.confirmDeleteMedia")
        );

        if(confirmDelete){
            try{
                mediaDAO.deleteById(currentMedia.getId());

                if(mainController != null){
                    mainController.showDefaultView();
                }
            }catch(Exception e){
                AlertManager.showAlert(Alert.AlertType.ERROR, LanguageManager.getString("ui.error"), e.getMessage());
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
          Media media = mediaDAO.read(mediaId);
          if(media == null) return;

          this.currentMedia = media;

          // toggle ISBN field visible
          List<String> isbnTypes = ConfigManager.getISBNEnabledMediaTypes();
          String currentMediaType = currentMedia.getMediatype().getTypeName();
          boolean showISBN = isbnTypes.stream().anyMatch(mt -> mt.equalsIgnoreCase(currentMediaType));
          isbnLabel.setVisible(showISBN);
          isbnField.setVisible(showISBN);
          isbnLabel.setManaged(showISBN);
          isbnField.setManaged(showISBN);

          if(showISBN){
              isbnField.setText(media.getIsbn() != null ? media.getIsbn() : "");
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

          // populate tags
          tagsContainer.getChildren().clear();
          media.getTags().forEach(tag -> {
              if(tag != null && !tag.getName().trim().isEmpty()){
                  Label badge = new Label(tag.getName());
                  badge.getStyleClass().addAll("badge", "tag-badge");

                  tagsContainer.getChildren().add(badge);
              }
          });

          // populate artists
          artistContainer.getChildren().clear();
          media.getCredits().forEach(credit -> {
              if(credit != null){
                  HBox row = new HBox();
                  Label artist = new Label();
                  String name = "";
                  if(credit.getArtist().getFirstName() != null && !credit.getArtist().getFirstName().trim().isEmpty()){
                      name = credit.getArtist().getFirstName();
                  }
                  if(name.isEmpty()){
                      name = credit.getArtist().getLastName();
                  }else{
                      name += " " +credit.getArtist().getLastName();
                  }
                  artist.setText(name);

                  Label artistRole =  new Label(credit.getArtistRole().getRole());

                  row.getChildren().addAll(artist, artistRole);
                  artistContainer.getChildren().add(row);
              }
          });

          // implement rating system
          Rating starRating = new Rating(5);
          starRating.setPartialRating(false);
          starRating.setRating(media.getRating());
          ratingBox.getChildren().setAll(starRating);

          starRating.ratingProperty().addListener((obs, oldRating, newRating) -> {
              if(currentMedia != null){
                  try{
                      currentMedia.setRating(newRating.intValue());
                      mediaDAO.save(currentMedia);

                      System.out.println("Database auto-updated! New rating stored: " + newRating.intValue());
                  }catch(Exception e){
                        AlertManager.showAlert(Alert.AlertType.ERROR, LanguageManager.getString("error.failedToSave"),e.getMessage());
                  }
              }
          });

          if(media.getSeries() != null){
              seriesField.setText(media.getSeries().getName());
          }else{
              seriesField.setText("/");
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


      }catch(Exception e){
          AlertManager.showAlert(Alert.AlertType.ERROR, LanguageManager.getString("error.failedToLoad"), e.getMessage());
      }
    }
}
