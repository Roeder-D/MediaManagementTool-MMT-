package de.srh_dr.mediamanagementtoolmmt.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Franchise {
    private final int id;
    private String name;
    private final List<AltTitle> altTitles;

    public Franchise(int id, String name, List<AltTitle> altTitles) {
        this.id = id;
        this.name = name;
        this.altTitles = Objects.requireNonNullElseGet(altTitles, ArrayList::new);
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<AltTitle> getAltTitles(){
        return altTitles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Franchise franchise = (Franchise) o;
        // Focus only on the ID for database identity
        return this.id == franchise.id;
    }

    @Override
    public int hashCode() {
        // Only use the ID to generate the hash
        return Objects.hash(id);
    }
}
