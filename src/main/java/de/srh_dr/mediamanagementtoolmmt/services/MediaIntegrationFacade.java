package de.srh_dr.mediamanagementtoolmmt.services;

import de.srh_dr.mediamanagementtoolmmt.data.ArtistDAO;
import de.srh_dr.mediamanagementtoolmmt.data.LanguageDAO;
import de.srh_dr.mediamanagementtoolmmt.data.PublisherDAO;
import de.srh_dr.mediamanagementtoolmmt.dto.ApiSource;
import de.srh_dr.mediamanagementtoolmmt.dto.ExternalMediaSearchResult;
import de.srh_dr.mediamanagementtoolmmt.model.*;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

//Separation-layer between APIs, UI and DAOs
public class MediaIntegrationFacade {
    private static final Logger LOGGER = Logger.getLogger(MediaIntegrationFacade.class.getName());
    private static final MediaIntegrationFacade INSTANCE = new MediaIntegrationFacade();

    public static MediaIntegrationFacade getInstance(){
        return INSTANCE;
    }

    private final Map<ApiSource, ExternalMediaService> lookupServices;

    private  final ArtistDAO artistDAO;
    private final LanguageDAO languageDAO;
    private final PublisherDAO publisherDAO;
    private final MediaService mediaService;

    private MediaIntegrationFacade() {
        this.artistDAO = ArtistDAO.getInstance();
        this.languageDAO = LanguageDAO.getInstance();
        this.mediaService = MediaService.getInstance();
        this.publisherDAO = PublisherDAO.getInstance();

        this.lookupServices = new HashMap<>();
        this.lookupServices.put(ApiSource.GOOGLE_BOOKS, new GoogleBooksService());
        this.lookupServices.put(ApiSource.TMDB, new TMDBService());
    }

    //fetch data via API
    public ExternalMediaSearchResult fetchAndSyncBookIsbn(String isbn, ApiSource apiSource) {
        ExternalMediaSearchResult rawResult = lookupServices.get(apiSource).searchByIsbn(isbn);
        return syncWithDatabase(rawResult, apiSource);
    }

    public List<ExternalMediaSearchResult> fetchAndSyncByTitle(String title, ApiSource apiSource){
        ExternalMediaService targetService = lookupServices.get(apiSource);

        if(targetService == null){
            System.err.println(apiSource + " not found");
            return new ArrayList<>();
        }

        List<ExternalMediaSearchResult> rawResults = targetService.searchByTitle(title);
        List<ExternalMediaSearchResult> syncedResults = new ArrayList<>();

        if (rawResults != null) {
            for (ExternalMediaSearchResult rawResult : rawResults){
                syncedResults.add(syncWithDatabase(rawResult, apiSource));
            }
        }
        return syncedResults;
    }

    public ExternalMediaSearchResult fetchAndSyncDetails(String remoteId, ApiSource apiSource){
        ExternalMediaService targetService = lookupServices.get(apiSource);
        if(targetService == null){return null;}

        ExternalMediaSearchResult rawResult = targetService.fetchDetails(remoteId);
        return syncWithDatabase(rawResult, apiSource);
    }

    //check db for duplicates
    private ExternalMediaSearchResult syncWithDatabase(ExternalMediaSearchResult rawResult, ApiSource apiSource){
        if(rawResult == null){return null;}

        Publisher dbPublisher = null;
        if(rawResult.publisher() != null){
            dbPublisher = publisherDAO.findByName(rawResult.publisher().getPublisherName());
        }
        Publisher filteredPublisher = Objects.requireNonNullElse(dbPublisher, rawResult.publisher());

        List<Artist> filteredArtists = new ArrayList<>();
        if(rawResult.artists() != null){
            for(Artist transientArtist : rawResult.artists()){
                Artist dbArtist = artistDAO.findByFullName(transientArtist.getFirstName(), transientArtist.getLastName());
                filteredArtists.add(Objects.requireNonNullElse(dbArtist, transientArtist));
            }
        }

        List<Language> filteredLanguages = new ArrayList<>();
        if(rawResult.languages() != null){
            for(Language transientLanguage : rawResult.languages()){
                Language dbLanguage = languageDAO.findByName(transientLanguage.getLanguage());
                filteredLanguages.add(Objects.requireNonNullElse(dbLanguage, transientLanguage));
            }
        }

        return new ExternalMediaSearchResult(
                rawResult.title(),
                filteredPublisher,
                rawResult.releaseDate(),
                rawResult.description(),
                rawResult.imageUrl(),
                filteredArtists,
                filteredLanguages,
                rawResult.remoteId(),
                apiSource
        );
    }

    //saves new Artist and creates missing relational entities
    public void persistConfirmedMedia(Media media) throws SQLException{
        List<Language> languages = media.getLanguages();
        List<Artist> artists = new ArrayList<>();
        Publisher publisher = media.getPublisher();
        for(MediaArtist credit : media.getCredits()){
            artists.add(credit.getArtist());
        }

        //Save new publisher
        if(publisher != null && publisher.isNewItem()){
            try {
                publisherDAO.save(publisher);
            }catch (Exception e){
                LOGGER.log(Level.SEVERE,"Failed to cascade save publisher " + publisher.getPublisherName(), e);
            }
        }

        //save new languages / artists first for referential integrity
        if(languages != null) {
            for (Language language : languages) {
                if (language.isNewItem()) {
                    try {
                        languageDAO.save(language);
                    } catch (SQLException e) {
                        LOGGER.log(Level.SEVERE, "Failed to cascade save language " + language.getLanguage(), e);
                    }
                }
            }
        }

        for (Artist artist : artists) {
            if (artist.isNewItem()) {
               try {
                   artistDAO.save(artist);
               }catch (Exception e){
                   LOGGER.log(Level.WARNING, "Failed to save artist.", e);
               }
            }
        }
        mediaService.saveMedia(media);
    }
}
