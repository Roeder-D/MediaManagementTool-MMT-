package de.srh_dr.mediamanagementtoolmmt.dto;

import de.srh_dr.mediamanagementtoolmmt.model.Media;

import java.time.LocalDate;

public record MediaEntity(int id, String isbn, String title, String originalTitle, String coverFileName, String description, int rating, LocalDate releaseDate, int seriesOrder, int seriesId, int mediaTypeId, int publisherId,
                          Media.MediaStatus status){};
