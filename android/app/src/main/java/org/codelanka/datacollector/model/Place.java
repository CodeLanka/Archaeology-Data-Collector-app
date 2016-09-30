package org.codelanka.datacollector.model;

/**
 * Created by dinush on 9/30/16.
 */

public class Place {

    public String idx;
    public long lat;
    public long lng;
    public String sitename;

    public Place(String idx, long lat, long lng, String sitename) {
        this.idx = idx;
        this.lat = lat;
        this.lng = lng;
        this.sitename = sitename;
    }
}
