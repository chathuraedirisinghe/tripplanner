package com.jlanka.tripplanner.Model;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by User on 29-Jan-18.
 */

public class Route {
    private int duration,distance;
    private LatLng origin,dest;
    private ArrayList<LatLng> chargerLocations;
    private JSONArray route;

    public Route(LatLng origin,LatLng dest,JSONArray route,ArrayList<LatLng> chargerLocation)throws Exception{
        this.route = route;
        this.origin=origin;
        this.dest=dest;
        this.chargerLocations=chargerLocation;
        for(int i=0;i<route.length();i++) {
            distance += route.getJSONObject(i).getJSONObject("distance").getInt("value");
            duration += route.getJSONObject(i).getJSONObject("duration").getInt("value");
        }
    }

    public ArrayList<LatLng> getChargers(){
        return chargerLocations;
    }

    public JSONArray getRoute(){
        return route;
    }

    public int getDurationValue() {
        return duration;
    }

    public String getDurationString() {
        return duration/60 + " min";
    }
}
