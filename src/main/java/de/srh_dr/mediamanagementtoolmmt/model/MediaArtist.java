package de.srh_dr.mediamanagementtoolmmt.model;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaArtist mediaArtist = (MediaArtist) o;
        // Focus only on the ID for database identity
        return this.id == mediaArtist.id;
    }

    @Override
    public int hashCode() {
        // Only use the ID to generate the hash
        return Objects.hash(id);
    }
}
