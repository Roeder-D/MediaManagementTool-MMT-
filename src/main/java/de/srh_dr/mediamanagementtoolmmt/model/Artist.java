package de.srh_dr.mediamanagementtoolmmt.model;

import java.util.Objects;

public class Artist {
    private final int id;
    private String firstName;
    private String lastName;
    private String nationality;
    private boolean isNewItem;
    private boolean isDirty;

    public Artist(int id, String firstName, String lastName, String nationality, boolean isNewItem) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationality = nationality;
        this.isNewItem = isNewItem;
    }

    public int getId() {
        return id;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getNationality() {
        return nationality;
    }
    public boolean getIsNewItem() {
        return isNewItem;
    }
    public boolean getIsDirty(){
        return isDirty;
    }

    public void setFirstName(String firstName) {
        if(!firstName.equals(this.firstName)){
            this.firstName = firstName;
            this.isDirty = true;
        }
    }
    public void setLastName(String lastName) {
        if(!lastName.equals(this.lastName)){
            this.lastName = lastName;
            this.isDirty = true;
        }
    }
    public void setNationality(String nationality) {
        if(!nationality.equals(this.nationality)){
            this.nationality = nationality;
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
        Artist artist = (Artist) o;
        // Focus only on the ID for database identity
        return this.id == artist.id;
    }

    @Override
    public int hashCode() {
        // Only use the ID to generate the hash
        return Objects.hash(id);
    }
}
