package de.srh_dr.mediamanagementtoolmmt.model;

import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MediaOverview {
    private final int mediaId;
    private final StringProperty title;
    private final StringProperty type;
    private final StringProperty releaseDate;
    private final StringProperty publisher;
    private final StringProperty status;
    private final List<String> tags = new ArrayList<>();

    public MediaOverview(int mediaId, String type, String title, String releaseDate, String publisher, String status, String tags){
        this.mediaId = mediaId;
        this.type = new SimpleStringProperty(type);
        this.title = new SimpleStringProperty(title);
        this.releaseDate = new SimpleStringProperty(releaseDate);
        this.publisher = new SimpleStringProperty(publisher);
        this.status = new SimpleStringProperty(status);

        if(!tags.isEmpty()){
            this.tags.addAll(Arrays.asList(tags.split(",")));
        }
    }

    public List<String> getTags() {
        return tags;
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
