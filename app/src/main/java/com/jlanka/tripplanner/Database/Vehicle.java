package com.jlanka.tripplanner.Database;

import org.json.JSONException;
import org.json.JSONObject;

public class Vehicle {
    private String vin;
    private String reg_no;
    private String modal;

    public Vehicle(String vin, String reg_no, String modal) {
        this.vin = vin;
        this.reg_no = reg_no;
        this.modal = modal;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getReg_no() {
        return reg_no;
    }

    public void setReg_no(String reg_no) {
        this.reg_no = reg_no;
    }

    public String getModal() {
        return modal;
    }

    public void setModal(String modal) {
        this.modal = modal;
    }

    //to display object as a string in spinner
    @Override
    public String toString() {
        return reg_no;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Vehicle){
            Vehicle v = (Vehicle )obj;
            if(v.getReg_no().equals(reg_no) && v.getVin()==vin ) return true;
        }

        return false;
    }

    public String toJSON(){

        JSONObject jsonObject= new JSONObject();
        try {
            jsonObject.put("vin", getVin());
            jsonObject.put("reg_no", getReg_no());
            jsonObject.put("modal", getModal());

            return jsonObject.toString();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }

    }
}
