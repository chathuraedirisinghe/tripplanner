package com.jlanka.jltripplanner.UserActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.jlanka.jltripplanner.GoogleAnalyticsService;

import org.json.JSONArray;

import java.util.HashMap;

/**
 * Created by chathura on 11/22/17.
 */

public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    // Sharedpref file name
    private static final String PREF_NAME = "GoEVShared";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";
    private static final String IS_FIRST = "IsFirst";

    // Shared Variables Declaration
    public static final String user_id = "id";
    public static final String user_name = "username";
    public static final String pass_word = "password";
    public static final String user_title = "title";
    public static final String user_credit = "credit";
    public static final String user_fname = "fname";
    public static final String user_lname = "lname";
    public static final String user_email = "email";
    public static final String electric_vehicles = "ev";
    public static final String user_occupation = "occupation";
    public static final String user_nic = "nic";
    public static final String user_altmobno = "altmobno";
    public static final String user_chargingStation = "userstation";
    public static final String user_birthday = "birthday";
    public static final String user_address = "address";
    public static final String user_cartype = "cartype";

    public static final String user_mobile = "mobile";
    public static final String user_pin = "pin";

    // Constructor
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }


    /**
     * Create login session
     * */
    public void createLoginSession(String id, String username, String password, String fname, String lname, String email, String mob, String credits, JSONArray vehicle){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(user_id, id);
        editor.putString(user_name, username);
        editor.putString(pass_word, password);
        editor.putString(user_fname, fname);
        editor.putString(user_lname, lname);
        editor.putString(user_email, email);
        editor.putString(user_mobile, mob);
        editor.putString(user_credit, credits);
        editor.putString(electric_vehicles, String.valueOf(vehicle));
        editor.commit();
    }

    public void set_user_id(String userid){
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(user_id, userid);
        editor.commit();
    }
//    public void createProfileData(String credit, String fname, String lname, String title, String occupation, String email, String nic, String altmobno, String birthday, String address, String cartype){
//
//        editor.putString(user_credit, credit);
//        editor.putString(user_fname, fname);
//        editor.putString(user_lname, lname);
//        editor.putString(user_title, title);
//        editor.putString(user_occupation, occupation);
//        editor.putString(user_email, email);
//        editor.putString(user_nic, nic);
//        editor.putString(user_altmobno, altmobno);
//        editor.putString(user_birthday, birthday);
//        editor.putString(user_address, address);
//        editor.putString(user_cartype, cartype);
//        editor.commit();
//    }

    public void user_charging_station(String selectedStation){
        editor.putString(user_chargingStation,selectedStation);
        editor.commit();
    }

    public void checkLogin(){
        // Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }
    }

    public void isFirst(){
        // Check login status
        if(!this.isFirstLogin()){

        }
    }

    public String getUserID(){
        return pref.getString(user_id,null);
    }

    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();

        user.put(user_id, pref.getString(user_id,null));
        user.put(user_name, pref.getString(user_name,null));
        user.put(user_title, pref.getString(user_title, null));
        user.put(user_credit, pref.getString(user_credit, null));
        user.put(user_fname, pref.getString(user_fname, null));
        user.put(user_lname, pref.getString(user_lname, null));
        user.put(user_occupation, pref.getString(user_occupation, null));
        user.put(electric_vehicles,pref.getString(electric_vehicles,null));
        user.put(user_email, pref.getString(user_email, null));
        user.put(user_nic, pref.getString(user_nic, null));
        user.put(user_altmobno, pref.getString(user_altmobno, null));
        user.put(user_birthday, pref.getString(user_birthday, null));
        user.put(user_address, pref.getString(user_address, null));
        user.put(user_cartype, pref.getString(user_cartype, null));
        user.put(user_chargingStation,pref.getString(user_chargingStation,null));
        user.put(user_mobile, pref.getString(user_mobile, null));
        user.put(pass_word, pref.getString(pass_word, null));

        return user;
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, LoginActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }

    public void setFName(String fname){
        editor.putString(user_fname, fname);
    }

    public void setLName(String lname){
        editor.putString(user_lname, lname);
    }

    public void setMobile(String mob){
        editor.putString(user_mobile, mob);
    }



    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }

    public boolean isFirstLogin(){
        return pref.getBoolean(IS_FIRST, false);
    }

    public void setVehicles(JSONArray vehicles) {
        editor.putString(electric_vehicles, String.valueOf(vehicles));
        editor.commit();
    }

    public String getVehicles(){
        return pref.getString(electric_vehicles,null);
    }

    public String getUser_chargingStation(){
        return pref.getString(user_chargingStation,null);
    }
}
