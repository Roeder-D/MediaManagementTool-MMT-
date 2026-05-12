package de.srh_dr.mediamanagementtoolmmt.model;

import java.util.Objects;

public class Lendee {
    private final int id;
    private String firstName;
    private String lastName;
    private String alias;
    private boolean isNewItem;
    private boolean isDirty;

    public Lendee(int id, String firstName, String lastName, String alias,  boolean isNewItem) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.alias = alias;
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
    public String getAlias() {
        return alias;
    }
    public boolean getIsNewItem() {
        return isNewItem;
    }
    public void setFirstName(String firstName) {
        if(!this.firstName.equals(firstName)){
            this.firstName = firstName;
            this.isDirty = true;
        }
    }
    public void setLastName(String lastName) {
        if(!this.lastName.equals(lastName)){
            this.lastName = lastName;
            this.isDirty =  true;
        }
    }
    public void setAlias(String alias) {
        if(!this.alias.equals(alias)){
            this.alias = alias;
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
        Lendee lendee = (Lendee) o;
        // Focus only on the ID for database identity
        return this.id == lendee.id;
    }

    @Override
    public int hashCode() {
        // Only use the ID to generate the hash
        return Objects.hash(id);
    }
}
