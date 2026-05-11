package de.srh_dr.mediamanagementtoolmmt.model;

import java.util.Objects;

public record Tag(int id, String name) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        // Focus only on the ID for database identity
        return this.id == tag.id;
    }

    @Override
    public int hashCode() {
        // Only use the ID to generate the hash
        return Objects.hash(id);
    }
}
