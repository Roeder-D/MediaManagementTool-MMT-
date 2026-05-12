package de.srh_dr.mediamanagementtoolmmt.model;

import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;

import java.util.Objects;

public class ArtistRole {
    private int id;
    private String role;
    private boolean isNewItem;
    private boolean isDirty;

    public ArtistRole(int id,  String role, boolean isNewItem) {
        this.id = id;
        this.role = role;
        this.isNewItem = isNewItem;
    }

    public int getId() {
        return id;
    }
    public String getRole() {
        return role;
    }
    public boolean isNewItem() {
        return isNewItem;
    }
    public boolean isDirty(){
        return isDirty;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setRole(String role) {
        if(role !=  null) {
            if(!role.equals(this.role)){
                this.role = role;
                this.isDirty = true;
            }
        }else{
            throw new IllegalArgumentException(LanguageManager.getString("error.not_null="));
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
        ArtistRole artistRole = (ArtistRole) o;
        // Focus only on the ID for database identity
        return this.id == artistRole.id;
    }

    @Override
    public int hashCode() {
        // Only use the ID to generate the hash
        return Objects.hash(id);
    }
}
