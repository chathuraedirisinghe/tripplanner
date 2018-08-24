package com.jlanka.tripplanner.Database;

public class VehicleData {
    private int id;
    private long time;
    private String data;

    public VehicleData(){}

    public VehicleData(long time, String data){
        this.time = time;
        this.data = data;
    }

    public VehicleData(int id, long time, String data) {
        this.id = id;
        this.time = time;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
