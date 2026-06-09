package de.srh_dr.mediamanagementtoolmmt.model;

import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MediaTest {

    @Test
    void testIllegalISBN() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Media.Builder().isbn("12345");
        });
        assertEquals(LanguageManager.getString("error.media.isbn_invalid"), exception.getMessage());
    }

    @Test
    void testBuild_WithNullTitle() {
        Media.Builder builder = new Media.Builder()
                .isNewItem(true)
                .mediaType(new MediaType(1, "Book", true))
                .languages(List.of(new Language(1, "German", true)))
                .genres(List.of(new Genre(1, "Sci-Fi", true)));

        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertEquals(LanguageManager.getString("error.title_null"), exception.getMessage());
    }

    @Test
    void testBuild_WithEmptyGenres() {
        Media.Builder builder = new Media.Builder()
                .isNewItem(true)
                .mediaType(new MediaType(1, "Book", true))
                .languages(List.of(new Language(1, "German", true)))
                .genres(List.of())
                .title("Test Title");

        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertEquals(LanguageManager.getString("error.media.genre_unset"), exception.getMessage());
    }

    @Test
    void testRemoveTag_FromExistingItem_ShouldQueueForRemoval() {
        Tag tag1 = new Tag(1, "Java", false);
        Tag tag2 = new Tag(2, "UI", false);

        Media media = new Media.Builder()
                .isNewItem(false)
                .id(10)
                .title("Test Book")
                .mediaType(new MediaType(1, "Book", true))
                .languages(List.of(new Language(1, "English", true)))
                .genres(List.of(new Genre(1, "Tech", true)))
                .tags(new ArrayList<>(List.of(tag1, tag2)))
                .build();

        media.removeTag(tag1);

        assertFalse(media.getTags().contains(tag1), "Tag should be removed from main list.");
        assertTrue(media.getTagsToRemove().contains(tag1), "Tag should be queued in tagsToRemove for the database.");
        assertTrue(media.getDirtyFields().contains(MediaField.TAG), "The TAG field should be marked as dirty.");
    }

    @Test
    void testUndoTag_FromExistingItem() {
        Tag tag1 = new Tag(1, "Java", false);
        Tag tag2 = new Tag(2, "UI", false);

        Media media = new Media.Builder()
                .isNewItem(false)
                .id(10)
                .title("Test Book")
                .mediaType(new MediaType(1, "Book", true))
                .languages(List.of(new Language(1, "English", true)))
                .genres(List.of(new Genre(1, "Tech", true)))
                .tags(new ArrayList<>(List.of(tag1)))
                .build();

        media.addTag(tag2);
        media.removeTag(tag2);

        assertFalse(media.getTags().contains(tag2), "Tag should be removed from list.");
        assertFalse(media.getTagsToAdd().contains(tag2), "Tag shouldn't be queued in tagsToAdd for the database.");
        assertFalse(media.getTagsToRemove().contains(tag2), "Tag shouldn't be queued in tagsToRemove for the database.");
        assertFalse(media.getDirtyFields().contains(MediaField.TAG), "The TAG field should be marked as dirty.");
    }

    @Test
    void testGetTags_ReturnsUnmodifiableList(){
        Tag tag1 = new Tag(1, "Java", false);
        Tag tag2 = new Tag(2, "UI", false);

        Media media = new Media.Builder()
                .isNewItem(false)
                .id(10)
                .title("Test Book")
                .mediaType(new MediaType(1, "Book", true))
                .languages(List.of(new Language(1, "English", true)))
                .genres(List.of(new Genre(1, "Tech", true)))
                .tags(new ArrayList<>(List.of(tag1, tag2)))
                .build();

        List<Tag> tags = media.getTags();
        assertThrows(UnsupportedOperationException.class, () -> {
            tags.remove(tag1);
        });
    }

    @Test
    void testSetTitle_Behavior(){
        Media newMedia = new Media.Builder()
                .isNewItem(true)
                .id(10)
                .title("Test Book")
                .mediaType(new MediaType(1, "Book", true))
                .languages(List.of(new Language(1, "English", true)))
                .genres(List.of(new Genre(1, "Tech", true)))
                .build();

        Media oldMedia = new Media.Builder()
                .isNewItem(false)
                .id(11)
                .title("Test Book")
                .mediaType(new MediaType(1, "Book", true))
                .languages(List.of(new Language(1, "English", true)))
                .genres(List.of(new Genre(1, "Tech", true)))
                .build();

        newMedia.setTitle("Test Title");
        assertFalse(newMedia.getDirtyFields().contains(MediaField.TITLE));

        oldMedia.setTitle("Test Book");
        assertFalse(oldMedia.getDirtyFields().contains(MediaField.TITLE));

        oldMedia.setTitle("Test Title");
        assertTrue(oldMedia.getDirtyFields().contains(MediaField.TITLE));
    }

}