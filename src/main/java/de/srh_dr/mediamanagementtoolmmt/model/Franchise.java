package de.srh_dr.mediamanagementtoolmmt.model;

import java.util.ArrayList;
import java.util.List;

public class Franchise {
    private final int id;
    private String name;
    private List<AltTitle> altTitles = new ArrayList<>();

    public Franchise(int id, String name, List<AltTitle> altTitles) {
        this.id = id;
        this.name = name;
        this.altTitles = altTitles;
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
}
