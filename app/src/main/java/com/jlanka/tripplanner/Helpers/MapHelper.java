package com.jlanka.tripplanner.Helpers;

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
