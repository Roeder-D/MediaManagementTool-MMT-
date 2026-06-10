package de.srh_dr.mediamanagementtoolmmt.services;

import de.srh_dr.mediamanagementtoolmmt.data.ArtistDAO;
import de.srh_dr.mediamanagementtoolmmt.data.LanguageDAO;
import de.srh_dr.mediamanagementtoolmmt.data.MediaDAO;
import de.srh_dr.mediamanagementtoolmmt.dto.ExternalMediaSearchResult;
import de.srh_dr.mediamanagementtoolmmt.model.Artist;
import de.srh_dr.mediamanagementtoolmmt.model.Language;
import de.srh_dr.mediamanagementtoolmmt.model.Media;
import de.srh_dr.mediamanagementtoolmmt.model.MediaArtist;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MediaIntegrationFacade {
    private final BookLookupService bookLookupService;
    private  final ArtistDAO artistDAO;
    private final LanguageDAO languageDAO;
    private final MediaDAO mediaDAO;

    public MediaIntegrationFacade() {
        this.bookLookupService = new GoogleBooksService();
        this.artistDAO = new ArtistDAO();
        this.languageDAO = new LanguageDAO();
        this.mediaDAO = new MediaDAO();
    }

    public ExternalMediaSearchResult fetchAndSyncBookIsbn(String isbn){
        ExternalMediaSearchResult rawResult = bookLookupService.searchByIsbn(isbn);
        if(rawResult == null){return null;}

        List<Artist> filteredArtists = new ArrayList<>();
        for(Artist transientArtist : rawResult.artists()){
            Artist dbArtist = artistDAO.findByFullName(transientArtist.getFirstName(), transientArtist.getLastName());
            filteredArtists.add(Objects.requireNonNullElse(dbArtist, transientArtist));
        }

        List<Language> filteredLanguages = new ArrayList<>();
        for(Language transientLanguage : rawResult.languages()){
            Language dbLanguage = languageDAO.findByName(transientLanguage.getLanguage());

            filteredLanguages.add(Objects.requireNonNullElse(dbLanguage, transientLanguage));
        }

        return new ExternalMediaSearchResult(
                rawResult.title(),
                rawResult.publisher(),
                rawResult.releaseDate(),
                rawResult.description(),
                rawResult.imageUrl(),
                filteredArtists,
                filteredLanguages,
                rawResult.remoteId()
        );
    }

    public void persistConfirmedBook(Media media){
        List<Language> languages = media.getLanguages();
        List<Artist> artists = new ArrayList<>();
        for(MediaArtist credit : media.getCredits()){
            artists.add(credit.getArtist());
        }

        //save new languages / artists first for referential integrity
        for(Language language : languages){
            if(language.isNewItem()){
                try {
                    languageDAO.save(language);
                }catch (SQLException e){
                    System.err.println("Failed to cascade save language " + language.getLanguage());
                    e.printStackTrace();
                }
            }
        }

        for(Artist artist : artists){
            if(artist.isNewItem()){
                artistDAO.save(artist);
            }
        }
        mediaDAO.save(media);
    }

}
