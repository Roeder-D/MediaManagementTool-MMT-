package de.srh_dr.mediamanagementtoolmmt.model;

import java.util.Objects;

public class Lendee {
    private final int id;
    private String firstName;
    private String lastName;
    private String alias;

    public Lendee(int id, String firstName, String lastName, String alias) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.alias = alias;
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
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public void setAlias(String alias) {
        this.alias = alias;
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
