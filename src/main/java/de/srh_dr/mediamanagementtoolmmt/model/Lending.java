package de.srh_dr.mediamanagementtoolmmt.model;

import java.time.LocalDate;
import java.time.ZoneId;


public class Lending {
    private final int id;
    private final Media media;
    private final Lendee lendee;
    private String note;
    private final LocalDate borrowDate;
    private LocalDate returnDate;

    public Lending(int id, Media media, Lendee lendee, java.util.Date borrowDate, java.util.Date returnDate) {
        this.id = id;
        this.media = media;
        this.lendee = lendee;

        if(borrowDate == null) {
            throw new IllegalArgumentException("borrowDate is null");
            //TODO: add i18n
        }
        this.borrowDate = borrowDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if(returnDate != null) {
            setReturnDate(returnDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }else{
            this.returnDate = null;
        }
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
    public void setNote(String note) {
        this.note = note;
    }
    public void setReturnDate(LocalDate returnDate) {
        if(returnDate != null){
            if(returnDate.isBefore(borrowDate)) {
                throw new IllegalArgumentException("returnDate is before borrowDate");
                //TODO: add i18n
            }
            this.returnDate = returnDate;
        }else{
            this.returnDate = null;
        }
    }
}
