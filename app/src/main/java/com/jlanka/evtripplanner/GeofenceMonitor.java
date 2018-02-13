package com.jlanka.evtripplanner;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

/**
 * Created by User on 1/16/2018.
 */

public class GeofenceMonitor implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private final String TAG = this.getClass().toString();
    private GoogleApiClient googleApiClient;
    private static GeofenceMonitor _singleInstance;
    private static Context appContext;
    private PendingIntent mGeofencePendingIntent;
    private static ArrayList<Geofence> geofences;

    private GeofenceMonitor(Context applicationContext) {
        appContext=applicationContext;

        this.googleApiClient = new GoogleApiClient.Builder(this.appContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        this.googleApiClient.connect();
    }

    public static GeofenceMonitor getInstance(Context applicationContext) {
        if (_singleInstance == null)
            synchronized (GeofenceMonitor.class) {
                if (_singleInstance == null) {
                    _singleInstance = new GeofenceMonitor(applicationContext);
                    geofences=new ArrayList<>();
                }
            }
        return _singleInstance;
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void addGeofence(Location location, String uid) {
        geofences.add(new Geofence.Builder()
                .setRequestId(uid)
                .setCircularRegion(
                        location.getLatitude(),
                        location.getLongitude(),
                        50
                )
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build());
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofences);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(appContext, GeofenceMonitorService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @SuppressLint("MissingPermission")
    public void startGeofencing() throws IllegalStateException{
        if (!googleApiClient.isConnected()) {
            Log.d(TAG,"API not connected");
            return;
        }

        LocationServices.GeofencingApi.addGeofences(
                googleApiClient,
                // The GeofenceRequest object.
                getGeofencingRequest(),
                // A pending intent that that is reused when calling removeGeofences(). This
                // pending intent is used to generate an intent when a matched geofence
                // transition is observed.
                getGeofencePendingIntent()
        ).setResultCallback(this); // Result processed in onResult().
        Log.d(TAG,"Geofencing started");
    }


    public void removeGeofences() {
        if (googleApiClient.isConnected())
            LocationServices.getGeofencingClient(appContext).removeGeofences(getGeofencePendingIntent());

    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            Log.d(TAG,"Geofence created successfully");
        }
        else {
            Log.d(TAG,"Error creating Geofence");
        }
    }
}
