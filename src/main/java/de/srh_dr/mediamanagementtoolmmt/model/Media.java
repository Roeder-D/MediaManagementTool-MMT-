package de.srh_dr.mediamanagementtoolmmt.model;

import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;

import java.time.LocalDate;
import java.util.*;

public class Media {
    //current state
    private boolean isNewItem;
    private int id;
    private String isbn;
    private String title;
    private String originalTitle;
    private String coverFileName;
    private String description;
    private int rating;
    private LocalDate releaseDate;
    private final List<Tag> tags = new ArrayList<>();
    private final List<MediaArtist> credits = new ArrayList<>();
    private Publisher publisher;
    private Series series;
    private int seriesOrder;
    private final MediaType mediatype;
    private final List<Franchise> franchises = new ArrayList<>();
    private final List<Genre> genres = new ArrayList<>();
    private final List<Language> languages = new ArrayList<>();
    private MediaStatus status;

    // change tracking for DAO
    private final EnumSet<MediaField> dirtyFields = EnumSet.noneOf(MediaField.class);
    private final List<Tag> tagsToAdd = new ArrayList<>();
    private final List<Tag> tagsToRemove = new ArrayList<>();
    private final List<MediaArtist> creditsToAdd = new ArrayList<>();
    private final List<MediaArtist> creditsToRemove = new ArrayList<>();
    private final List<Genre> genresToAdd = new ArrayList<>();
    private final List<Genre> genresToRemove = new ArrayList<>();
    private final List<Language> languagesToAdd = new ArrayList<>();
    private final List<Language> languagesToRemove = new ArrayList<>();
    private final List<Franchise> franchisesToAdd = new ArrayList<>();
    private final List<Franchise> franchisesToRemove = new ArrayList<>();


    private Media(Builder builder) {
        this.isNewItem =  builder.isNewItem;
        this.id = builder.id;
        this.isbn = builder.isbn;
        this.title = builder.title;
        this.originalTitle = builder.originalTitle;
        this.coverFileName = builder.coverFileName;
        this.description = builder.description;
        this.rating = builder.rating;
        this.releaseDate = builder.releaseDate;
        this.tags.addAll(builder.tags);
        this.credits.addAll(builder.credits);
        this.publisher = builder.publisher;
        this.series = builder.series;
        this.seriesOrder = builder.seriesOrder;
        this.mediatype = builder.mediatype;
        this.franchises.addAll(builder.franchises);
        this.genres.addAll(builder.genres);
        this.languages.addAll(builder.languages);
        this.status = builder.status;
    }

    // getters
    public boolean isNewItem() {
        return isNewItem;
    }
    public int getId() {
        return id;
    }
    public String getIsbn() {
        return isbn;
    }
    public String getTitle() {
        return title;
    }
    public String getOriginalTitle() {
        return originalTitle;
    }
    public String getCoverFileName() {
        return coverFileName;
    }
    public String getDescription() {
        return description;
    }
    public int getRating() {
        return rating;
    }
    public LocalDate getReleaseDate() {
        return releaseDate;
    }
    public List<Tag> getTags() {
        return Collections.unmodifiableList(tags);
    }
    public List<MediaArtist> getCredits() {
        return Collections.unmodifiableList(credits);
    }
    public Publisher getPublisher() {
        return publisher;
    }
    public Series getSeries() {
        return series;
    }
    public int getSeriesOrder() {
        return seriesOrder;
    }
    public MediaType getMediatype() {
        return mediatype;
    }
    public List<Franchise> getFranchises() {
        return Collections.unmodifiableList(franchises);
    }
    public List<Genre> getGenres() {
        return Collections.unmodifiableList(genres);
    }
    public List<Language> getLanguages() {
        return Collections.unmodifiableList(languages);
    }
    public MediaStatus getStatus() {
        return status;
    }

    public EnumSet<MediaField> getDirtyFields(){
        return this.dirtyFields;
    }
    public List<Tag> getTagsToAdd() {
        return Collections.unmodifiableList(tagsToAdd);
    }
    public List<Tag> getTagsToRemove() {
        return Collections.unmodifiableList(tagsToRemove);
    }
    public List<MediaArtist> getCreditsToAdd() {
        return Collections.unmodifiableList(creditsToAdd);
    }
    public List<MediaArtist> getCreditsToRemove() {
        return Collections.unmodifiableList(creditsToRemove);
    }
    public List<Franchise> getFranchisesToAdd() {
        return Collections.unmodifiableList(franchisesToAdd);
    }
    public List<Franchise> getFranchisesToRemove() {
        return Collections.unmodifiableList(franchisesToRemove);
    }
    public List<Language> getLanguagesToAdd() {
        return Collections.unmodifiableList(languagesToAdd);
    }
    public List<Language> getLanguagesToRemove() {
        return Collections.unmodifiableList(languagesToRemove);
    }
    public List<Genre> getGenresToAdd() {
        return Collections.unmodifiableList(genresToAdd);
    }
    public List<Genre> getGenresToRemove() {
        return Collections.unmodifiableList(genresToRemove);
    }


    // setters
    public void setId(int id) {
        this.id = id;
    }
    public void setIsbn(String isbn) {
        if(!Objects.equals(isbn, this.isbn)){
            this.isbn = isbn;
            if(!isNewItem){
                this.dirtyFields.add(MediaField.ISBN);
            }
        }
    }
    public void setTitle(String title) {
        if(!Objects.equals(title, this.title)){
            this.title = title;
            if(!isNewItem){
                this.dirtyFields.add(MediaField.TITLE);
            }
        }
    }
    public void setOriginalTitle(String originalTitle) {
        if(!Objects.equals(originalTitle, this.originalTitle)){
            this.originalTitle = originalTitle;
            if(!isNewItem){
                this.dirtyFields.add(MediaField.ORIGINAL_TITLE);
            }
        }
    }
    public void setCoverFileName(String coverFileName) {
        if(!Objects.equals(coverFileName, this.coverFileName)){
            this.coverFileName = coverFileName;
            if(!isNewItem){
                this.dirtyFields.add(MediaField.COVER);
            }
        }
    }
    public void setDescription(String description) {
        if(!Objects.equals(description, this.description)){
            this.description = description;
            if(!isNewItem){
                this.dirtyFields.add(MediaField.DESCRIPTION);
            }
        }
    }
    public void setRating(int rating) {
        if(!Objects.equals(rating, this.rating)){
            this.rating = rating;
            if(!isNewItem){
                this.dirtyFields.add(MediaField.RATING);
            }
        }
    }
    public void setReleaseDate(LocalDate releaseDate) {
        if(!Objects.equals(releaseDate, this.releaseDate)){
            this.releaseDate = releaseDate;
            if(!isNewItem){
                this.dirtyFields.add(MediaField.RELEASE_DATE);
            }
        }
    }
    public void setPublisher(Publisher publisher) {
        if(!Objects.equals(publisher, this.publisher)){
            this.publisher = publisher;
            if(!isNewItem){
                this.dirtyFields.add(MediaField.PUBLISHER);
            }
        }
    }
    public void setSeries(Series series) {
        if(!Objects.equals(series, this.series)){
            this.series = series;
            if(!isNewItem){
                this.dirtyFields.add(MediaField.SERIES);
            }
        }
    }
    public void setSeriesOrder(int seriesOrder) {
        if(!Objects.equals(seriesOrder, this.seriesOrder)){
            this.seriesOrder = seriesOrder;
            if(!isNewItem){
                this.dirtyFields.add(MediaField.SERIES_ORDER);
            }
        }
    }
    public void setStatus(MediaStatus status) {
        if(!Objects.equals(status, this.status)){
            this.status = status;
            if(!isNewItem){
                this.dirtyFields.add(MediaField.STATUS);
            }
        }
    }

    // tracked List changes
    public void removeTag(Tag tag) {
        if(tag == null || tagsToRemove.contains(tag) || !this.tags.contains(tag)){
            return;
        }
        this.tags.remove(tag);
        if(!isNewItem){
            if(this.tagsToAdd.contains(tag)){
                this.tagsToAdd.remove(tag);
            }else{
                this.tagsToRemove.add(tag);
            }
            updateDirtyFlag(MediaField.TAG, !tagsToAdd.isEmpty() || !tagsToRemove.isEmpty());
        }
    }
    public void addTag(Tag tag) {
        if(tag == null || tagsToAdd.contains(tag)){
            return;
        }
        this.tags.add(tag);
        if(!isNewItem){
            if(tagsToRemove.contains(tag)){
                tagsToRemove.remove(tag);
            }else{
                tagsToAdd.add(tag);
            }
            updateDirtyFlag(MediaField.TAG, !tagsToAdd.isEmpty() || !tagsToRemove.isEmpty());
        }
    }

    public void removeCredit(MediaArtist credit) {
        if(credit == null || creditsToRemove.contains(credit) || !this.credits.contains(credit)){
            return;
        }
        this.credits.remove(credit);
        if(!isNewItem){
            if(this.creditsToAdd.contains(credit)){
                this.creditsToAdd.remove(credit);
            }else{
                this.creditsToRemove.add(credit);
            }
            updateDirtyFlag(MediaField.CREDITS, !creditsToAdd.isEmpty() || !creditsToRemove.isEmpty());
        }
    }
    public void addCredit(MediaArtist credit) {
        if(credit == null || creditsToAdd.contains(credit)){
            return;
        }
        this.credits.add(credit);
        if(!isNewItem){
            if(creditsToRemove.contains(credit)){
                creditsToRemove.remove(credit);
            }else{
                creditsToAdd.add(credit);
            }
            updateDirtyFlag(MediaField.CREDITS, !creditsToAdd.isEmpty() || !creditsToRemove.isEmpty());
        }
    }

    public void removeGenre(Genre genre) {
        if(genre == null || genresToRemove.contains(genre) || !this.genres.contains(genre)){
            return;
        }
        this.genres.remove(genre);
        if(!isNewItem){
            if(this.genresToAdd.contains(genre)){
                this.genresToAdd.remove(genre);
            }else{
                this.genresToRemove.add(genre);
            }
            updateDirtyFlag(MediaField.GENRES, !genresToAdd.isEmpty() || !genresToRemove.isEmpty());
        }
    }
    public void addGenre(Genre genre) {
        if(genre == null || genresToAdd.contains(genre)){
            return;
        }
        this.genres.add(genre);
        if(!isNewItem){
            if(genresToRemove.contains(genre)){
                genresToRemove.remove(genre);
            }else{
                genresToAdd.add(genre);
            }
            updateDirtyFlag(MediaField.GENRES, !genresToAdd.isEmpty() || !genresToRemove.isEmpty());
        }
    }

    public void removeLanguage(Language language) {
        if(language == null || languagesToRemove.contains(language) || !this.languages.contains(language)){
            return;
        }
        this.languages.remove(language);
        if(!isNewItem){
            if(this.languagesToAdd.contains(language)){
                this.languagesToAdd.remove(language);
            }else{
                this.languagesToRemove.add(language);
            }
            updateDirtyFlag(MediaField.LANGUAGE, !languagesToAdd.isEmpty() || !languagesToRemove.isEmpty());
        }
    }
    public void addLanguage(Language language) {
        if(language == null || languagesToAdd.contains(language)){
            return;
        }
        this.languages.add(language);
        if(!isNewItem){
            if(languagesToRemove.contains(language)){
                languagesToRemove.remove(language);
            }else{
                languagesToAdd.add(language);
            }
            updateDirtyFlag(MediaField.LANGUAGE, !languagesToAdd.isEmpty() || !languagesToRemove.isEmpty());
        }
    }

    public void removeFranchise(Franchise franchise) {
        if(franchise == null || franchisesToRemove.contains(franchise) || !this.franchises.contains(franchise)){
            return;
        }
        this.franchises.remove(franchise);
        if(!isNewItem){
            if(this.franchisesToAdd.contains(franchise)){
                this.franchisesToAdd.remove(franchise);
            }else{
                this.franchisesToRemove.add(franchise);
            }
            updateDirtyFlag(MediaField.FRANCHISE, !franchisesToAdd.isEmpty() || !franchisesToRemove.isEmpty());
        }
    }
    public void addFranchise(Franchise franchise) {
        if(franchise == null || franchisesToAdd.contains(franchise)){
            return;
        }
        this.franchises.add(franchise);
        if(!isNewItem){
            if(franchisesToRemove.contains(franchise)){
                franchisesToRemove.remove(franchise);
            }else{
                franchisesToAdd.add(franchise);
            }
            updateDirtyFlag(MediaField.FRANCHISE, !franchisesToAdd.isEmpty() || !franchisesToRemove.isEmpty());
        }
    }


    // builder pattern
    public static class Builder {
        private boolean isNewItem;
        private int id = 0;
        private String isbn;
        private String title;
        private String originalTitle;
        private String coverFileName;
        private String description;
        private int rating;
        private LocalDate releaseDate;
        private List<Tag> tags = new ArrayList<>();
        private List<MediaArtist> credits = new ArrayList<>();
        private Publisher publisher;
        private Series series;
        private int seriesOrder;
        private MediaType mediatype;
        private List<Franchise> franchises = new ArrayList<>();
        private List<Genre> genres = new ArrayList<>();
        private List<Language> languages = new ArrayList<>();
        private MediaStatus status = MediaStatus.AVAILABLE;

        public Builder isNewItem(boolean newItem) {
            this.isNewItem = newItem;
            return this;
        }
        public Builder id(int id) {
            this.id = id;
            return this;
        }
        public Builder isbn(String isbn) {
            if(isbn.length() == 10 ||  isbn.length() == 13){
                this.isbn = isbn;
                return this;
            }else {
                throw new IllegalArgumentException(LanguageManager.getString("error.media.isbn_invalid"));
            }
        }
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        public Builder originalTitle(String originalTitle) {
            this.originalTitle = originalTitle;
            return this;
        }
        public Builder coverFileName(String coverFileName) {
            this.coverFileName = coverFileName;
            return this;
        }
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        public Builder rating(int rating) {
            this.rating = rating;
            return this;
        }
        public Builder releaseDate(LocalDate releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }
        public Builder tags(List<Tag> tags) {
            this.tags = tags;
            return this;
        }
        public Builder credits(List<MediaArtist> credits) {
            this.credits = credits;
            return this;
        }
        public Builder publisher(Publisher publisher) {
            this.publisher = publisher;
            return this;
        }
        public Builder series(Series series) {
            this.series = series;
            return this;
        }
        public Builder seriesOrder(int seriesOrder) {
            this.seriesOrder = seriesOrder;
            return this;
        }
        public Builder genres(List<Genre> genres) {
            this.genres = genres;
            return this;
        }
        public Builder mediaType(MediaType mediatype) {
            this.mediatype = mediatype;
            return this;
        }
        public Builder franchises(List<Franchise> franchises) {
            this.franchises = franchises;
            return this;
        }
        public Builder languages(List<Language> languages) {
            this.languages = languages;
            return this;
        }
        public Builder status(MediaStatus status) {
            this.status = status;
            return this;
        }

        //Media constructor call
        public Media build() {
            if(id == 0 && !this.isNewItem){throw new IllegalStateException(LanguageManager.getString("error.id_unset"));}
            if(title == null){throw new IllegalStateException(LanguageManager.getString("error.title_null"));}
            if(mediatype == null){throw new IllegalStateException(LanguageManager.getString("error.media.type_unset"));}
            if(languages.isEmpty()){throw new IllegalStateException(LanguageManager.getString("error.media.language_unset"));}
            if(genres.isEmpty()){throw new IllegalStateException(LanguageManager.getString("error.media.genre_unset"));}

            return new Media(this);
        }
    }


    // helpers
    private void updateDirtyFlag(MediaField field, boolean isDirty) {
        if(isDirty){
            this.dirtyFields.add(field);
        }else{
            this.dirtyFields.remove(field);
        }
    }
    public void clearChangeTracking(){
        this.isNewItem = false;
        this.dirtyFields.clear();
        this.tagsToAdd.clear();
        this.tagsToRemove.clear();
        this.creditsToAdd.clear();
        this.creditsToRemove.clear();
        this.franchisesToAdd.clear();
        this.franchisesToRemove.clear();
        this.genresToAdd.clear();
        this.genresToRemove.clear();
        this.languagesToAdd.clear();
        this.languagesToRemove.clear();
        this.franchisesToAdd.clear();
        this.franchisesToRemove.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Media media = (Media) o;
        // Focus only on the ID for database identity
        return this.id == media.id;
    }

    @Override
    public int hashCode() {
        // Only use the ID to generate the hash
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return this.title;
    }
}
