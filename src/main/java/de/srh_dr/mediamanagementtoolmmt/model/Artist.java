package de.srh_dr.mediamanagementtoolmmt.model;

public class Artist {
    private final int id;
    private String firstName;
    private String lastName;
    private String nationality;

    public Artist(int id, String firstName, String lastName, String nationality) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationality = nationality;
    }
}
