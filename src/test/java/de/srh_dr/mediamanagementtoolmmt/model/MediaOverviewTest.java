package de.srh_dr.mediamanagementtoolmmt.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MediaOverviewTest {

    @Test
    void testConstructorAndStandardGetters() {
        MediaOverview overview = new MediaOverview(42, "Book", "Head First Java", "2022-05-10", "O Reilly", "AVAILABLE", "");

        assertEquals(42, overview.getMediaId());
        assertEquals("Book", overview.getType());
        assertEquals("Head First Java", overview.getTitle());
        assertEquals("2022-05-10", overview.getReleaseDate());
        assertEquals("O Reilly", overview.getPublisher());
        assertEquals("AVAILABLE", overview.getStatus());
    }

    @Test
    void testJavaFXProperties() {
        MediaOverview overview = new MediaOverview(1, "Book", "Title", "2026", "Pub", "AVAILABLE", "");

        assertNotNull(overview.titleProperty(), "JavaFX titleProperty should not be null.");
        assertEquals("Title", overview.titleProperty().get());
    }

    @Test
    void testTagParsingWithValidData() {
        String rawTags = "Programming,JavaFX,Learning";

        MediaOverview overview = new MediaOverview(1, "Book", "Title", "2026", "Pub", "AVAILABLE", rawTags);

        List<String> parsedTags = overview.getTags();

        assertEquals(3, parsedTags.size(), "Should parse exactly 3 separate tag elements.");
        assertTrue(parsedTags.contains("Programming"));
        assertTrue(parsedTags.contains("JavaFX"));
        assertTrue(parsedTags.contains("Learning"));
    }

    @Test
    void testTagParsingWithEmptyData() {
        String rawTags = "";

        MediaOverview overview = new MediaOverview(1, "Book", "Title", "2026", "Pub", "AVAILABLE", rawTags);

        List<String> parsedTags = overview.getTags();

        assertEquals(0, parsedTags.size(), "Should parse 0 tag elements.");
    }
}