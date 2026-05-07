package de.srh_dr.mediamanagementtoolmmt.model;

public class MediaArtist {
    private final int id;
    private final Artist artist;
    private ArtistRole artistRole;

    public MediaArtist(int id,  Artist artist, ArtistRole artistRole) {
        this.id = id;
        this.artist = artist;
        this.artistRole = artistRole;
    }
    public int getId() {
        return id;
    }
    public Artist getArtist() {
        return artist;
    }
    public ArtistRole getArtistRole() {
        return artistRole;
    }
    public void setArtistRole(ArtistRole artistRole) {
        this.artistRole = artistRole;
    }

}
