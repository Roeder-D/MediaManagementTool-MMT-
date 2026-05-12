package de.srh_dr.mediamanagementtoolmmt.model;

import java.util.*;

public class Series {
    private final int id;
    private String name;
    private int numberOfTitles;
    private int startYear;
    private List<AltTitle> altTitles = new ArrayList<>();

    // change tracking for DAO
    private boolean isNewItem;
    private boolean isDirty;
    private boolean listChanged;
    private final List<AltTitle> altTitlesToAdd = new ArrayList<>();
    private final List<AltTitle> altTitlesToRemove = new ArrayList<>();


    public Series(boolean isNewItem, int id, String name, int numberOfTitles, int startYear, List<AltTitle> altTitles) {
        this.isNewItem = isNewItem;
        this.id = id;
        this.name = name;
        this.numberOfTitles = numberOfTitles;
        this.startYear = startYear;
        this.altTitles = altTitles;
    }



    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public int getNumberOfTitles() {
        return numberOfTitles;
    }
    public int getStartYear() {
        return startYear;
    }
    public List<AltTitle> getAltTitles() {
        return altTitles;
    }
    public boolean isNewItem() {
        return isNewItem;
    }
    public boolean listChanged() {
        return listChanged;
    }
    public List<AltTitle> getAltTitlesToAdd() {
        return Collections.unmodifiableList(altTitlesToAdd);
    }
    public List<AltTitle> getAltTitlesToRemove() {
        return Collections.unmodifiableList(altTitlesToRemove);
    }

    public void setName(String name) {
        if(!Objects.equals(this.name, name)){
            this.name = name;
            this.isDirty =  true;
        }
    }
    public void setNumberOfTitles(int numberOfTitles) {
        if(!Objects.equals(this.numberOfTitles, numberOfTitles)){
            this.numberOfTitles = numberOfTitles;
            this.isDirty =  true;
        }
    }
    public void setStartYear(int startYear) {
        if(!Objects.equals(this.startYear, startYear)){
            this.startYear = startYear;
            this.isDirty =  true;
        }
    }
    public void addAltTitle(AltTitle altTitle) {
        if(!altTitles.contains(altTitle)){
            this.altTitles.add(altTitle);
            if(this.altTitlesToRemove.contains(altTitle)){
                this.altTitlesToRemove.remove(altTitle);
            }else{
                this.altTitlesToAdd.add(altTitle);
            }
            this.listChanged =  true;
        }
    }
    public void removeAltTitleById(int id) {
        AltTitle toRemove = altTitles.stream()
                .filter(t -> t.id() == id)
                .findFirst()
                .orElse(null);

        if (toRemove != null) {
            this.altTitles.remove(toRemove);

            if (!isNewItem) {
                if (altTitlesToAdd.contains(toRemove)) {
                    altTitlesToAdd.remove(toRemove);
                } else {
                    altTitlesToRemove.add(toRemove);
                }
                this.listChanged = true;
            }
        }
    }

    public void clearChangeTracking(){
        this.listChanged = false;
        this.isNewItem = false;
        this.isDirty = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Series series = (Series) o;
        // Focus only on the ID for database identity
        return this.id == series.id;
    }

    @Override
    public int hashCode() {
        // Only use the ID to generate the hash
        return Objects.hash(id);
    }
}
