package de.srh_dr.mediamanagementtoolmmt.model;

import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;

import java.util.Objects;

public class Language{
    private final int id;
    private String language;
    private boolean isNewItem;
    private boolean isDirty;

    public Language(int id, String language, boolean isNewItem) {
        this.id = id;
        this.language = language;
        this.isNewItem = isNewItem;
    }

    public int getId() {
        return id;
    }
    public String getLanguage() {
        return language;
    }
    public boolean getIsNewItem() {
        return isNewItem;
    }
    public boolean getIsDirty(){
        return isDirty;
    }

    public void setLanguage(String language){
        if(language != null){
            if(!language.equals(this.language)){
                this.language = language;
                this.isDirty = true;
            }
        }else{
            throw new NullPointerException(LanguageManager.getString("error.not_null"));
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
        Language language = (Language) o;
        // Focus only on the ID for database identity
        return this.id == language.id;
    }

    @Override
    public int hashCode() {
        // Only use the ID to generate the hash
        return Objects.hash(id);
    }
}
