package de.srh_dr.mediamanagementtoolmmt.model;

import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Franchise {
    private int id;
    private String name;
    private final List<AltTitle> altTitles;
    private boolean isNewItem;
    private boolean isDirty;
    private boolean listChanged;
    private final List<AltTitle> altTitlesToAdd = new ArrayList<>();
    private final List<AltTitle> altTitlesToRemove = new ArrayList<>();

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
    public boolean listChanged() {
        return listChanged;
    }
    public List<AltTitle> getAltTitles(){
        return altTitles;
    }
    public List<AltTitle> getAltTitlesToAdd() {
        return Collections.unmodifiableList(altTitlesToAdd);
    }
    public List<AltTitle> getAltTitlesToRemove() {
        return Collections.unmodifiableList(altTitlesToRemove);
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
    public void addAltTitle(AltTitle altTitle) {
        if(!altTitles.contains(altTitle)) {
            this.altTitles.add(altTitle);
            if(this.altTitlesToRemove.contains(altTitle)) {
                this.altTitlesToRemove.remove(altTitle);
            }else{
                this.altTitlesToAdd.add(altTitle);
            }
            this.listChanged = true;
        }
    }
    public void removeAltTitle(AltTitle altTitle) {
        this.altTitles.remove(altTitle);
        if(this.altTitlesToAdd.contains(altTitle)) {
            this.altTitlesToAdd.remove(altTitle);
        }else{
            this.altTitlesToRemove.add(altTitle);
        }
        this.listChanged = true;
    }

    public void clearChangeTracking() {
        altTitlesToAdd.clear();
        altTitlesToRemove.clear();
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

    @Override
    public String toString() {
        return this.name;
    }
}
