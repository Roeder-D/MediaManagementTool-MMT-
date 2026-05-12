package de.srh_dr.mediamanagementtoolmmt.model;

import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Franchise {
    private int id;
    private String name;
    private final List<AltTitle> altTitles;
    private boolean isNewItem;
    private boolean isDirty;

    public Franchise(int id, String name, List<AltTitle> altTitles,  boolean isNewItem) {
        this.id = id;
        this.name = name;
        this.altTitles = Objects.requireNonNullElseGet(altTitles, ArrayList::new);
        this.isNewItem = isNewItem;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public boolean isNewItem(){
        return isNewItem;
    }
    public boolean isDirty() {
        return isDirty;
    }
    public List<AltTitle> getAltTitles(){
        return altTitles;
    }


    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) {
        if(name != null) {
            if(!name.equals(this.name)) {
                this.name = name;
                this.isDirty = true;
            }
        }else{
            throw new NullPointerException(LanguageManager.getString("error.title_null"));
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
