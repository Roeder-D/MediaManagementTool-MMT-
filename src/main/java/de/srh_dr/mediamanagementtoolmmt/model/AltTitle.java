package de.srh_dr.mediamanagementtoolmmt.model;

import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;

import java.util.Objects;

public class AltTitle{
    private final int id;
    private String title;
    private boolean isNewItem;
    private boolean isDirty;

    public AltTitle(int id, String title,  boolean isNew) {
        this.id = id;
        this.title = title;
        this.isNewItem = isNew;
    }

    public int getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public boolean getIsNewItem() {
        return isNewItem;
    }
    public boolean getIsDirty(){
        return this.isDirty;
    }

    public void setTitle(String title) {
        if(title != null){
            if(!title.equals(this.title)){
                this.title = title;
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
        AltTitle altTitle = (AltTitle) o;
        // Focus only on the ID for database identity
        return this.id == altTitle.id;
    }

    @Override
    public int hashCode() {
        // Only use the ID to generate the hash
        return Objects.hash(id);
    }
}
