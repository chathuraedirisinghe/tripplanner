package com.jlanka.jltripplanner.Model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Workstation on 2/6/2018.
 */

public class Charger {
    private int id,owner;
    private String device_id,location,type;
    private LatLng position;
    private boolean available;

    public Charger(int id, String device_id, int owner, String location, LatLng position, String type, boolean available) {
        this.id = id;
        this.owner = owner;
        this.device_id = device_id;
        this.location = location;
        this.type = type;
        this.position = position;
        this.available = available;
    }

    public int getId() {
        return id;
    }

    public int getOwner() {
        return owner;
    }

    public String getDevice_id() {
        return device_id;
    }

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public LatLng getPosition() {
        return position;
    }

    public boolean isAvailable() {
        return available;
    }

}
