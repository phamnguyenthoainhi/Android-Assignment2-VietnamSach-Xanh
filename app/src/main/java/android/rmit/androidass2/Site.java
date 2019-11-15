package android.rmit.androidass2;

import java.util.ArrayList;
import java.util.List;

public class Site {
    private String location;
    private String name;
    private String owner;
    private long dateTime;
    private List<String> volunteers = new ArrayList<>();
    private String id;

    public Site() {
    }

    public Site(String location, long dateTime, String name, String owner) {
        this.location = location;
        this.name = name;
        this.owner = owner;
        this.dateTime = dateTime;
    }


    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<String> getVolunteers() {
        return volunteers;
    }

    public void setVolunteers(List<String> volunteers) {
        this.volunteers = volunteers;
    }

    public Site(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Site{" +
                "location='" + location + '\'' +
                ", name='" + name + '\'' +
                ", owner='" + owner + '\'' +
                ", dateTime=" + dateTime +
                ", volunteers=" + volunteers +
                ", id='" + id + '\'' +
                '}';
    }
}
