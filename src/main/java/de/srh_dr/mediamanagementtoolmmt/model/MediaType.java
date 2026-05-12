package de.srh_dr.mediamanagementtoolmmt.model;

import java.util.Objects;

public class MediaType{
    private int id;
    private String typeName;
    private boolean isNewItem;
    private boolean isDirty;

    public MediaType(int id, String typeName,  boolean isNewItem) {
        this.id = id;
        this.typeName = typeName;
        this.isNewItem = isNewItem;
    }

    public int getId() {
        return id;
    }
    public String getTypeName() {
        return typeName;
    }
    public boolean isNewItem() {
        return isNewItem;
    }
    public boolean isDirty() {
        return isDirty;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setTypeName(String typeName) {
        if(!typeName.equals(this.typeName)){
            this.typeName = typeName;
            this.isDirty = true;
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
        MediaType mediaType = (MediaType) o;
        // Focus only on the ID for database identity
        return this.id == mediaType.id;
    }

    @Override
    public int hashCode() {
        // Only use the ID to generate the hash
        return Objects.hash(id);
    }
}
