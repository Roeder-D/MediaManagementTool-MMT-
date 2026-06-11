package de.srh_dr.mediamanagementtoolmmt.dto;

import de.srh_dr.mediamanagementtoolmmt.model.Artist;
import de.srh_dr.mediamanagementtoolmmt.model.Language;
import de.srh_dr.mediamanagementtoolmmt.model.Publisher;

import java.util.List;

public record ExternalMediaSearchResult(
        String title,
        Publisher publisher,
        String releaseDate,
        String description,
        String imageUrl,
        List<Artist> artists,
        List<Language> languages,
        String remoteId
) {
}
