package de.srh_dr.mediamanagementtoolmmt.model;

import java.util.Objects;

public class Lendee {
    private int id;
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
        if(!Objects.equals(this.firstName, firstName)){
            this.firstName = firstName;
            this.isDirty = true;
        }
    }
    public void setLastName(String lastName) {
        if(!Objects.equals(this.lastName, lastName)){
            this.lastName = lastName;
            this.isDirty =  true;
        }
    }
    public void setAlias(String alias) {
        if(!Objects.equals(this.alias, alias)){
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

    @Override
    public String toString() {
        String identifier = "";
        if(this.firstName != null && !this.firstName.trim().isEmpty()){
            identifier = this.firstName + " ";
        }
        if(this.lastName != null && !this.lastName.trim().isEmpty()){
            identifier += this.lastName + " ";
        }
        if(this.alias != null && !this.alias.trim().isEmpty()){
            identifier += "(" + this.alias + ")";
        }
        identifier = identifier.trim();
        return identifier;
    }
}
