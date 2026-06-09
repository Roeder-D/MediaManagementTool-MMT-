package de.srh_dr.mediamanagementtoolmmt.model;

import java.util.Objects;

public class
MediaArtist {
    private final Artist artist;
    private ArtistRole artistRole;
    private boolean isNewItem;
    private boolean isDirty;

    public MediaArtist(Artist artist, ArtistRole artistRole, boolean isNewItem) {
        this.artist = artist;
        this.artistRole = artistRole;
        this.isNewItem = isNewItem;
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
        MediaArtist that = (MediaArtist) o;
        return artist.getId() == that.artist.getId() &&
                artistRole.getId() == that.artistRole.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(artist.getId(), artistRole.getId());
    }

    @Override
    public String toString() {
        return this.artist.toString() + " " + this.artistRole;
    }
}
