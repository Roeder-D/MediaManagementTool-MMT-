package de.srh_dr.mediamanagementtoolmmt.model;

import java.util.Objects;

public record ArtistRole(int id, String role) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtistRole artistRole = (ArtistRole) o;
        // Focus only on the ID for database identity
        return this.id == artistRole.id;
    }

    @Override
    public int hashCode() {
        // Only use the ID to generate the hash
        return Objects.hash(id);
    }
}
