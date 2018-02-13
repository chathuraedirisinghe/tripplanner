package com.jlanka.evtripplanner;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.jlanka.evtripplanner.Model.GPSPoint;
import com.jlanka.evtripplanner.Model.Workable;

/**
 * Created by Workstation on 2/13/2018.
 */

public class LocationMonitor extends Application {

    private static LocationMonitor instance;

    private static final String TAG = LocationMonitor.class.getSimpleName();
    private static Context context;

    private static FusedLocationProviderClient mFusedLocationClient;
    private static LocationCallback locationCallback;
    private static LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;

    private Workable<GPSPoint> workable;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 60000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 30000;

    @Override
    public void onCreate(){
        super.onCreate();
    }
    private LocationMonitor(Context c){
        context=c;
        instance=this;
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location currentLocation = locationResult.getLastLocation();

                GPSPoint gpsPoint = new GPSPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                if (null != workable)
                    workable.work(gpsPoint);
            }
        };

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        init();
    }

    public void init(){
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    public LocationRequest getRequestObject(){
        return locationRequest;
    }

    public static LocationMonitor instance(Context ctxt) {
        if (instance==null) {
            instance = new LocationMonitor(ctxt);
        }

        instance.init();
        return instance;
    }

    public void onChange(Workable<GPSPoint> workable) {
        this.workable = workable;
    }

    public LocationSettingsRequest getLocationSettingsRequest() {
        return this.locationSettingsRequest;
    }

    public void stop() {
        mFusedLocationClient.removeLocationUpdates(this.locationCallback);
    }
}
