package com.jlanka.evtripplanner.Model;

import com.google.android.gms.maps.model.LatLng;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;

/**
 *  * Created by alejandro.tkachuk
 *   */

public class GPSPoint {

    private BigDecimal lat, lon;
    private Date date;
    private String lastUpdate;

    public GPSPoint(BigDecimal latitude, BigDecimal longitude) {
        this.lat = latitude;
        this.lon = longitude;
        this.date = new Date();
        this.lastUpdate = DateFormat.getTimeInstance().format(this.date);
    }

    public GPSPoint(Double latitude, Double longitude) {
        this.lat = BigDecimal.valueOf(latitude);
        this.lon = BigDecimal.valueOf(longitude);
    }

    public BigDecimal getLatitude() {
        return lat;
    }

    public Date getDate() {
        return date;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public BigDecimal getLongitude() {
        return lon;
    }

    public LatLng getLatLng(){
        return new LatLng(Double.parseDouble(lat.toString()),Double.parseDouble(lon.toString()));
    }

    @Override
    public String toString() {
        return "(" + lat + ", " + lon + ")";
    }
}


