package de.srh_dr.mediamanagementtoolmmt.services;

import de.srh_dr.mediamanagementtoolmmt.dto.ExternalMediaSearchResult;

public interface BookLookupService extends ExternalMediaService {
    ExternalMediaSearchResult searchByIsbn(String isbn);
}
