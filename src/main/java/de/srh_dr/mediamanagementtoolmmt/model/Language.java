package de.srh_dr.mediamanagementtoolmmt.model;

import java.util.Objects;

public record Language(int id, String language) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Language language = (Language) o;
        // Focus only on the ID for database identity
        return this.id == language.id;
    }

    @Override
    public int hashCode() {
        // Only use the ID to generate the hash
        return Objects.hash(id);
    }
}
