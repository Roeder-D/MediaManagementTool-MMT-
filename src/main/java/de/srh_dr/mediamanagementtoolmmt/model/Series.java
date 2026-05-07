package de.srh_dr.mediamanagementtoolmmt.model;

import java.util.ArrayList;
import java.util.List;

public class Series {
    private final int id;
    private String name;
    private int numberOfTitles;
    private int startYear;
    private List<AltTitle> altTitles = new ArrayList<>();

    public Series(int id, String name, int numberOfTitles, int startYear, List<AltTitle> altTitles) {
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
    public void setName(String name) {
        this.name = name;
    }
    public void setNumberOfTitles(int numberOfTitles) {
        this.numberOfTitles = numberOfTitles;
    }
    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }
    public void addAltTitle(AltTitle altTitle) {
        this.altTitles.add(altTitle);
    }
    public void removeAltTitleById(int id) {
        //TODO: add method
    }
}
