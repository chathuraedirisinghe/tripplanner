package com.jlanka.jltripplanner.Helpers;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.android.volley.Request;
import com.google.android.gms.maps.model.LatLng;
import com.jlanka.jltripplanner.Server.ServerConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Workstation on 2/24/2018.
 */

public class MapHelper {
    static MapHelper mh;

    public MapHelper init(){
        if (mh==null)
            mh=new MapHelper();

        return mh;
    }

    public void getLocation(String name) {
    }
}
