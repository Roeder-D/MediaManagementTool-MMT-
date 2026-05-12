package de.srh_dr.mediamanagementtoolmmt.model;

import java.util.Objects;

public record AltTitle(int id, String title) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AltTitle altTitle = (AltTitle) o;
        // Focus only on the ID for database identity
        return this.id == altTitle.id;
    }

    @Override
    public int hashCode() {
        // Only use the ID to generate the hash
        return Objects.hash(id);
    }
}
