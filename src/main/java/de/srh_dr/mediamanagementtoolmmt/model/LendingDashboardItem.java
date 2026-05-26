package de.srh_dr.mediamanagementtoolmmt.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class LendingDashboardItem {
    private final SimpleIntegerProperty lendingId;
    private final int mediaId;
    private final SimpleStringProperty mediaTitle;
    private final SimpleStringProperty lendeeInfo;
    private final SimpleStringProperty borrowedOn;
    private final SimpleStringProperty returnedOn;
    private final SimpleStringProperty status;

    public LendingDashboardItem(int lendingId, int mediaId, String mediaTitle, String lendeeInfo, String borrowedOn, String returnedOn, String status) {
        this.lendingId = new SimpleIntegerProperty(lendingId);
        this.mediaId = mediaId;
        this.mediaTitle = new SimpleStringProperty(mediaTitle);
        this.lendeeInfo = new SimpleStringProperty(lendeeInfo);
        this.borrowedOn = new SimpleStringProperty(borrowedOn);
        this.returnedOn = new SimpleStringProperty(returnedOn);
        this.status = new SimpleStringProperty(status);
    }

    public int getLendingId() {return lendingId.get();}
    public int getMediaId() {return mediaId;}
    public SimpleStringProperty mediaTitleProperty() {return mediaTitle;}
    public SimpleStringProperty lendeeInfoProperty() {return lendeeInfo;}
    public SimpleStringProperty borrowedOnProperty() {return borrowedOn;}
    public SimpleStringProperty returnedOnProperty() {return returnedOn;}
    public SimpleStringProperty statusProperty() {return status;}
}
