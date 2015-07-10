package sakethkaparthi.silenceplease;

/**
 * Created by Saketh on 27-03-2015.
 */
public class Location {
    private long id;
    private String name;
    private String lat;
    private String lon;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }

    public void setName(String id) {
        this.name = id;
    }

    public String getLat() {
        return this.lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return this.lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

}
