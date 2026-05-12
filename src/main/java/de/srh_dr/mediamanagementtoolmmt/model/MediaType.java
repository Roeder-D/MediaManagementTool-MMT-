package de.srh_dr.mediamanagementtoolmmt.model;

import java.util.Objects;

public record MediaType(int id, String typeName) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaType mediaType = (MediaType) o;
        // Focus only on the ID for database identity
        return this.id == mediaType.id;
    }

    @Override
    public int hashCode() {
        // Only use the ID to generate the hash
        return Objects.hash(id);
    }
}
