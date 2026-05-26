package de.srh_dr.mediamanagementtoolmmt.model;

import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

public class MediaOverview {
    private final int mediaId;
    private final StringProperty title;
    private final StringProperty type;
    private final StringProperty releaseDate;
    private final StringProperty publisher;
    private final StringProperty status;

    public MediaOverview(int mediaId, String type, String title, String releaseDate, String publisher, String status){
        this.mediaId = mediaId;
        this.type = new SimpleStringProperty(type);
        this.title = new SimpleStringProperty(title);
        this.releaseDate = new SimpleStringProperty(releaseDate);
        this.publisher = new SimpleStringProperty(publisher);
        this.status = new SimpleStringProperty(status);
    }

    public int getMediaId(){
        return mediaId;
    }

    public String getType(){
        return type.get();
    }
    public  StringProperty typeProperty(){
        return type;
    }

    public String getTitle(){
        return title.get();
    }
    public StringProperty titleProperty(){
        return title;
    }

    public String getReleaseDate(){
        return releaseDate.get();
    }
    public StringProperty releaseDateProperty(){
        return releaseDate;
    }

    public String getPublisher(){
        return publisher.get();
    }
    public StringProperty publisherProperty(){
        return publisher;
    }

    public String getStatus(){
        return status.get();
    }
    public StringProperty statusProperty(){
        return status;
    }
}
