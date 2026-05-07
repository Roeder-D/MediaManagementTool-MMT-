package de.srh_dr.mediamanagementtoolmmt.model;

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
}
