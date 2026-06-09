package de.srh_dr.mediamanagementtoolmmt.model;

import javafx.beans.property.SimpleStringProperty;

import java.util.Objects;

public class Artist {
    private int id;
    private String firstName;
    private String lastName;
    private String alias;
    private String nationality;
    private boolean isNewItem;
    private boolean isDirty;

    private final SimpleStringProperty firstNameProperty = new SimpleStringProperty();
    private final SimpleStringProperty lastNameProperty = new SimpleStringProperty();
    private final SimpleStringProperty aliasProperty = new SimpleStringProperty();
    private final SimpleStringProperty nationalityProperty = new SimpleStringProperty();

    public Artist(int id, String firstName, String lastName, String alias, String nationality, boolean isNewItem) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.alias = alias;
        this.nationality = nationality;
        this.isNewItem = isNewItem;
        this.firstNameProperty.setValue(firstName);
        this.lastNameProperty.setValue(lastName);
        this.aliasProperty.setValue(alias);
        this.nationalityProperty.setValue(nationality);
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
    public String getAlias() {
        return alias;
    }
    public String getNationality() {
        return nationality;
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
    public void setFirstName(String firstName) {
        if(!Objects.equals(firstName, this.firstName)){
            this.firstName = firstName;
            this.isDirty = true;
        }
    }
    public void setLastName(String lastName) {
        if(!Objects.equals(lastName, this.lastName)){
            this.lastName = lastName;
            this.isDirty = true;
        }
    }
    public void setAlias(String alias) {
        if(!Objects.equals(alias, this.alias)){
            this.alias = alias;
            this.isDirty = true;
        }
    }
    public void setNationality(String nationality) {
        if(!Objects.equals(nationality, this.nationality)){
            this.nationality = nationality;
            this.isDirty = true;
        }
    }
    public void clearChangeTracking() {
        this.isNewItem = false;
        isDirty = false;
    }

    public SimpleStringProperty firstNameProperty() {
        return firstNameProperty;
    }
    public SimpleStringProperty lastNameProperty() {
        return lastNameProperty;
    }
    public SimpleStringProperty aliasProperty() {
        return aliasProperty;
    }
    public SimpleStringProperty nationalityProperty() {
        return nationalityProperty;
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

    @Override
    public String toString() {
        return this.alias + " " + this.firstName + " " + this.lastName;
    }
}
