package de.srh_dr.mediamanagementtoolmmt.model;

import java.util.Objects;

public class MediaArtist {
    private int id;
    private final Artist artist;
    private ArtistRole artistRole;
    private boolean isNewItem;
    private boolean isDirty;

    public MediaArtist(int id,  Artist artist, ArtistRole artistRole, boolean isNewItem) {
        this.id = id;
        this.artist = artist;
        this.artistRole = artistRole;
        this.isNewItem = isNewItem;
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
    public boolean isNewItem() {
        return isNewItem;
    }
    public boolean isDirty(){
        return isDirty;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setArtistRole(ArtistRole artistRole) {
        if(!artistRole.equals(this.artistRole)){
            this.artistRole = artistRole;
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
