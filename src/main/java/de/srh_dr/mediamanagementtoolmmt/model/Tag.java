package de.srh_dr.mediamanagementtoolmmt.model;

import java.util.Objects;

public class Tag{
    private int id;
    private String name;
    private boolean isNewItem;
    private boolean isDirty;

    public Tag(int id,String name, boolean isNewItem){
        this.id = id;
        this.name = name;
        this.isNewItem = isNewItem;
    }

    public int getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public boolean isNewItem(){
        return isNewItem;
    }
    public boolean isDirty() {
        return isDirty;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name){
        if(!name.equals(this.name)){
            this.name = name;
            this.isDirty =  true;
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
        Tag tag = (Tag) o;
        // Focus only on the ID for database identity
        return this.id == tag.id;
    }

    @Override
    public int hashCode() {
        // Only use the ID to generate the hash
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
