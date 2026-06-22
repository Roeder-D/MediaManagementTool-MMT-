package de.srh_dr.mediamanagementtoolmmt.services;

import de.srh_dr.mediamanagementtoolmmt.data.*;
import de.srh_dr.mediamanagementtoolmmt.dto.MediaEntity;
import de.srh_dr.mediamanagementtoolmmt.model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MediaService {
    Logger LOGGER = Logger.getLogger(MediaService.class.getName());

    private final MediaDAO mediaDAO;
    private final PublisherDAO publisherDAO;
    private final SeriesDAO seriesDAO;
    private final TagDAO tagDAO;
    private final GenreDAO genreDAO;
    private final LanguageDAO languageDAO;
    private final ArtistDAO artistDAO;
    private final FranchiseDAO franchiseDAO;

    public MediaService(){
        this.mediaDAO = new MediaDAO();
        this.publisherDAO = new PublisherDAO();
        this.seriesDAO = new SeriesDAO();
        this.tagDAO = new TagDAO();
        this.genreDAO = new GenreDAO();
        this.languageDAO = new LanguageDAO();
        this.artistDAO = new ArtistDAO();
        this.franchiseDAO = new FranchiseDAO();
    }

    public Media getMediaById(int id){
        Connection conn = null;
        MediaEntity mediaCore = mediaDAO.readMediaEntity(id);

        if(mediaCore == null ||mediaCore.id() < 1){return null;}

        try {
            Publisher publisher = null;
            if(mediaCore.publisherId() != 0 ){
                publisher = publisherDAO.findById(mediaCore.publisherId());
            }

            Series series = null;
            if(mediaCore.seriesId() != 0 ){
                series = seriesDAO.findById(mediaCore.seriesId());
            }

            Media.Builder builder = new Media.Builder()
                    .isNewItem(false)
                    .id(mediaCore.id())
                    .title(mediaCore.title())
                    .originalTitle(mediaCore.originalTitle())
                    .coverFileName(mediaCore.coverFileName())
                    .description(mediaCore.description())
                    .rating(mediaCore.rating())
                    .releaseDate(mediaCore.releaseDate())
                    .publisher(publisher)
                    .series(series)
                    .status(mediaCore.status())
                    //Fetch lists (all todo)
                    .tags(tagDAO.fetchByMediaId(id))
                    .genres(genreDAO.fetchByMediaId(id))
                    .languages(languageDAO.fetchByMediaId(id))
                    .franchises(franchiseDAO.fetchByMediaId(id))
                    .credits(artistDAO.fetchByMediaId(id));

            return builder.build();
        }catch (Exception e){
            LOGGER.log(Level.WARNING,"Failed to load media: " + e.getMessage(), e);
        }
        return null;
    }

    public void saveMedia(Media media) throws SQLException {
        if(media == null){return;}

        try (Connection conn = DBConnection.getConnection()) {
            try {
                conn.setAutoCommit(false);

                if (media.isNewItem()) {
                    createMedia(media, conn);
                } else {
                    updateMedia(media, conn);
                }
                conn.commit();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE,"Save failed. Rolling back transaction: " + e.getMessage(), e);
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void updateMedia(Media media, Connection conn) throws SQLException {
        mediaDAO.update(media, conn);
        int mediaId = media.getId();

        if(media.getTagsToAdd() != null && !media.getTagsToAdd().isEmpty()){
            tagDAO.saveTagsForMedia(mediaId, media.getTagsToAdd(), conn);
        }
        if(media.getTagsToRemove() != null && !media.getTagsToRemove().isEmpty()){
            tagDAO.deleteTagsForMedia(mediaId, media.getTagsToRemove(), conn);
        }

        if(media.getGenresToAdd() != null && !media.getGenresToAdd().isEmpty()){
            genreDAO.saveGenresForMedia(mediaId, media.getGenresToAdd(), conn);
        }
        if(media.getGenresToRemove() != null && !media.getGenresToRemove().isEmpty()){
            genreDAO.deleteGenresForMedia(mediaId, media.getGenresToRemove(), conn);
        }

        if(media.getLanguagesToAdd() != null && !media.getLanguagesToAdd().isEmpty()){
            languageDAO.saveLanguagesForMedia(mediaId, media.getLanguagesToAdd(), conn);
        }
        if(media.getLanguagesToRemove() != null && !media.getLanguagesToRemove().isEmpty()){
            languageDAO.deleteLanguagesForMedia(mediaId, media.getLanguagesToRemove(), conn);
        }

        if(media.getFranchisesToAdd() != null && !media.getFranchisesToAdd().isEmpty()){
            franchiseDAO.saveFranchisesForMedia(mediaId, media.getFranchisesToAdd(), conn);
        }
        if(media.getFranchisesToRemove() != null && !media.getFranchisesToRemove().isEmpty()){
            franchiseDAO.deleteFranchisesForMedia(mediaId, media.getFranchisesToRemove(), conn);
        }

        if(media.getCreditsToAdd() != null && !media.getCreditsToAdd().isEmpty()){
            artistDAO.saveCreditsForMedia(mediaId, media.getCreditsToAdd(), conn);
        }
        if(media.getCreditsToRemove() != null && !media.getCreditsToRemove().isEmpty()){
            artistDAO.deleteCreditsForMedia(mediaId, media.getCreditsToRemove(), conn);
        }

    }

    private void createMedia(Media media, Connection conn) throws SQLException {
        int newMediaId = mediaDAO.create(media, conn);

        if(media.getTags() != null && !media.getTags().isEmpty()){
            tagDAO.saveTagsForMedia(newMediaId, media.getTags(), conn);
        }

        if(media.getGenres() != null && !media.getGenres().isEmpty()){
            genreDAO.saveGenresForMedia(newMediaId, media.getGenres(), conn);
        }

        if(media.getLanguages() != null && !media.getLanguages().isEmpty()){
            languageDAO.saveLanguagesForMedia(newMediaId, media.getLanguages(), conn);
        }

        if(media.getFranchises() != null && !media.getFranchises().isEmpty()){
            franchiseDAO.saveFranchisesForMedia(newMediaId, media.getFranchises(), conn);
        }

        if(media.getCredits() != null && !media.getCredits().isEmpty()){
            artistDAO.saveCreditsForMedia(newMediaId, media.getCredits(), conn);
        }
    }

    public void deleteMedia(int mediaId) throws SQLException {
        mediaDAO.deleteById(mediaId);
    }
}