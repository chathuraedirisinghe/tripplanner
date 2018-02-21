package com.jlanka.jltripplanner.Model;

import android.os.IBinder;
import android.os.RemoteException;

import com.google.android.gms.dynamic.IObjectWrapper;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.internal.zzp;

/**
 * Created by Workstation on 2/6/2018.
 */

public class Charger {
    private int owner;
    private String device_id,location,type;
    private LatLng position;
    private boolean available;
    private String state;

    public Charger( String device_id, int owner, String location, LatLng position, String type, boolean available) {
        this.owner = owner;
        this.device_id = device_id;
        this.location = location;
        this.type = type;
        this.position = position;
        this.available = available;
        this.state="Pending...";
    }
    public void setState(String state) { this.state=state; }

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

    public boolean isAvailable() {
        return available;
    }

    public MarkerOptions getMarkerOptions(){
        return new MarkerOptions().title(device_id)
                .snippet(location)
                .position(position);
    }

    public String getState(){
        return state;
    }
}
