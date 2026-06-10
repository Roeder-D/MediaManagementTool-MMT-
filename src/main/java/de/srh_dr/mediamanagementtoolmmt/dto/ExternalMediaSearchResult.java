package de.srh_dr.mediamanagementtoolmmt.dto;

import de.srh_dr.mediamanagementtoolmmt.model.Artist;
import de.srh_dr.mediamanagementtoolmmt.model.Language;

import java.util.List;

public record ExternalMediaSearchResult(
        String title,
        String publisher,
        String releaseDate,
        String description,
        String imageUrl,
        List<Artist> artists,
        List<Language> languages,
        String remoteId
) {
}
