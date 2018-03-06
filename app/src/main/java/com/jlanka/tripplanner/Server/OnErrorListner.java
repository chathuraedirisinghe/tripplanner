package com.jlanka.tripplanner.Server;

import org.json.JSONObject;

/**
 * Created by chathura on 2/2/18.
 */

public interface OnErrorListner {
    public void onError(String error, JSONObject obj);
}
