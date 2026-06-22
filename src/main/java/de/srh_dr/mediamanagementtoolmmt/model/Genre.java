package de.srh_dr.mediamanagementtoolmmt.model;

import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;

import java.util.Objects;

public class Genre implements Identifiable {
    private int id;
    private String genreName;
    private boolean isNewItem;
    private boolean isDirty;

    public Genre(int id,  String genreName,  boolean isNewItem) {
        this.id = id;
        this.genreName = genreName;
        this.isNewItem = isNewItem;
    }

    public int getId() {
        return id;
    }
    public String getGenreName() {
        return genreName;
    }
    public boolean isNewItem() {
        return isNewItem;
    }
    public boolean isDirty(){
        return isDirty;
    }


    public void setId(int id) {
        this.id = id;
    }
    public void setGenreName(String genreName) {
        if(genreName != null) {
            if(!genreName.equals(this.genreName)) {
                this.genreName = genreName;
                this.isDirty = true;
            }
        }else{
            throw new NullPointerException(LanguageManager.getString("error.title_null"));
        }
    }

    public void clearChangeTracking() {
        this.isNewItem = false;
        isDirty = false;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Genre genre = (Genre) o;
        // Focus only on the ID for database identity
        return this.id == genre.id;
    }

    @Override
    public int hashCode() {
        // Only use the ID to generate the hash
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return this.genreName;
    }
}
