package org.codelanka.datacollector.model;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by dinush on 9/12/16.
 */
@IgnoreExtraProperties
public class Site {
    public String username;
    public String email;
    public String sitename;
    public String category;
    public String province;
    public String district;
    public String dsDivision;
    public String gnDivision;
    public String nearestTown;
    public String nameOfOwner;
    public String nameOfUser;
    public String description;

    public Site(String username, String email, String sitename, String category, String province, String district,
                String dsDivision, String gnDivision, String nearestTown, String nameOfOwner,
                String nameOfUser, String description) {
        this.username = username;
        this.email = email;
        this.sitename = sitename;
        this.category = category;
        this.province = province;
        this.district = district;
        this.dsDivision = dsDivision;
        this.gnDivision = gnDivision;
        this.nearestTown = nearestTown;
        this.nameOfOwner = nameOfOwner;
        this.nameOfUser = nameOfUser;
        this.description = description;
    }
}
