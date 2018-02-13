package com.jlanka.jltripplanner.Adapters;

/**
 * Created by chathura on 2/7/18.
 */

public class Vehicle {
    private String user_id, regNo, vin, model, year;

    public Vehicle(){

    }

    public Vehicle(String user_id, String regNo, String vin, String model, String year) {
        this.user_id = user_id;
        this.regNo = regNo;
        this.vin = vin;
        this.model = model;
        this.year = year;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
