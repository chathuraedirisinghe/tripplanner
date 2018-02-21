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
    private String device_id,alias,location,type;
    private LatLng position;
    private double power,price;
    private boolean available;
    private String state;

    public Charger(String device_id,String alias, int owner, String location, LatLng position, String type,double power,double price, boolean available) {
        this.owner = owner;
        this.alias=alias;
        this.device_id = device_id;
        this.location = location;
        this.type = type;
        this.position = position;
        this.power=power;
        this.price=price;
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

    public String getAlias() {
        return alias;
    }

    public LatLng getPosition() {
        return position;
    }

    public double getPower() {
        return power;
    }

    public double getPrice() {
        return price;
    }
}
