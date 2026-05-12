package de.srh_dr.mediamanagementtoolmmt.model;

import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;


public class Lending {
    private final int id;
    private final Media media;
    private final Lendee lendee;
    private String note;
    private final LocalDate borrowDate;
    private LocalDate returnDate;
    private boolean isNewItem;
    private boolean isDirty;

    public Lending(int id, Media media, Lendee lendee, java.util.Date borrowDate, java.util.Date returnDate, boolean isNewItem) {
        this.id = id;
        this.media = media;
        this.lendee = lendee;

        if(borrowDate == null) {
            throw new IllegalArgumentException(LanguageManager.getString("error.lending.borrow_date_null"));
        }
        this.borrowDate = borrowDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if(returnDate != null) {
            setReturnDate(returnDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }else{
            this.returnDate = null;
        }
        this.isNewItem = isNewItem;
    }

    public int getId() {
        return id;
    }
    public Media getMedia() {
        return media;
    }
    public Lendee getLendee() {
        return lendee;
    }
    public String getNote() {
        return note;
    }
    public LocalDate getBorrowDate() {
        return borrowDate;
    }
    public LocalDate getReturnDate() {
        return returnDate;
    }
    public boolean getIsNewItem() {
        return isNewItem;
    }
    public boolean getIsDirty(){
        return isDirty;
    }

    public void setNote(String note) {
        if(!note.equals(this.note)) {
            this.note = note;
            this.isDirty =  true;
        }
    }
    public void setReturnDate(LocalDate returnDate) {
        if(returnDate != null){
            if(returnDate.isBefore(borrowDate)) {
                throw new IllegalArgumentException(LanguageManager.getString("error.lending.invalid_return_date"));
            }
            this.returnDate = returnDate;
        }else{
            this.returnDate = null;
        }
        this.isDirty = true;
    }

    public void clearChangeTracking() {
        this.isNewItem = false;
        isDirty = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lending lending = (Lending) o;
        // Focus only on the ID for database identity
        return this.id == lending.id;
    }

    @Override
    public int hashCode() {
        // Only use the ID to generate the hash
        return Objects.hash(id);
    }
}
