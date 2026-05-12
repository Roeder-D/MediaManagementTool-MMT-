package de.srh_dr.mediamanagementtoolmmt.model;

import java.util.Objects;

public class Publisher{
    private final int id;
    private String publisherName;
    private boolean isNewItem;
    private boolean isDirty;

    public Publisher(int id, String publisherName,  boolean isNewItem){
        this.id = id;
        this.publisherName = publisherName;
        this.isNewItem = isNewItem;
    }

    public int getId(){
        return id;
    }
    public String getPublisherName(){
        return publisherName;
    }
    public boolean getIsNewItem(){
        return isNewItem;
    }
    public boolean getIsDirty(){
        return isDirty;
    }

    public void setPublisherName(String publisherName){
        if(!publisherName.equals(this.publisherName)){
            this.publisherName = publisherName;
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
        Publisher publisher = (Publisher) o;
        // Focus only on the ID for database identity
        return this.id == publisher.id;
    }

    @Override
    public int hashCode() {
        // Only use the ID to generate the hash
        return Objects.hash(id);
    }
}
