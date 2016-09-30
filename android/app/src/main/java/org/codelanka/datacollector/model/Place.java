package org.codelanka.datacollector.model;

/**
 * Created by dinush on 9/30/16.
 */

public class Place {

    public String idx;
    public double lat;
    public double lng;
    public String sitename;

    public Place(String idx, double lat, double lng, String sitename) {
        this.idx = idx;
        this.lat = lat;
        this.lng = lng;
        this.sitename = sitename;
    }
}
