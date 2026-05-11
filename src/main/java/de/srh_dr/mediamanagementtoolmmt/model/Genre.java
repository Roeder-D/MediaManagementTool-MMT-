package de.srh_dr.mediamanagementtoolmmt.model;

import java.util.Objects;

public record Genre(int id, String genreName) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Genre genre = (Genre) o;
        // Focus only on the ID for database identity
        return this.id == genre.id;
    }

    @Override
    public int hashCode() {
        // Only use the ID to generate the hash
        return Objects.hash(id);
    }
}
