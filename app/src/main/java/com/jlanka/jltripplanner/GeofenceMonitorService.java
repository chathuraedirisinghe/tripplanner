package com.jlanka.jltripplanner;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import com.jlanka.jltripplanner.Fragments.MapFragment;


/**
 * Created by delaroy on 4/18/17.
 */
public class GeofenceMonitorService extends IntentService {

    private static final String TAG = GeofenceMonitorService.class.getSimpleName();

    public static final int GEOFENCE_NOTIFICATION_ID = 0;

    public GeofenceMonitorService() {
        super(TAG);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        // Handling errors
        if ( geofencingEvent.hasError() ) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode() );
            Log.e( TAG, errorMsg );
            return;
        }

        int geoFenceTransition = geofencingEvent.getGeofenceTransition();
        // Check if the transition type is of interest
        if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ){
               //|| geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ) {

            // Get the geofence that were triggered
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            //String geofenceTransitionDetails = getGeofenceTransitionDetails(geoFenceTransition, triggeringGeofences );
            String title="";
            String message="";

            switch (triggeringGeofences.get(0).getRequestId()){
                case "Charger_Location":
                    title="Reached charging station";
                    message="Start charging from the app";
                    break;
                case "Destination":
                    title="Reached destination";
                    message="Thank you for using JL Trip Planner";
                    break;
            }
            // Send notification details as a String
            sendNotification(triggeringGeofences.get(0).getRequestId(),title,message );
        }
    }


    private String getGeofenceTransitionDetails(int geoFenceTransition, List<Geofence> triggeringGeofences) {
        // get the ID of each geofence triggered
        ArrayList<String> triggeringGeofencesList = new ArrayList<>();
        for ( Geofence geofence : triggeringGeofences ) {
            triggeringGeofencesList.add( geofence.getRequestId() );
        }

        String status = null;
        if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER )
            status = "Reached ";
        else if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT )
            status = "Left ";
        return status + TextUtils.join( ", ", triggeringGeofencesList);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sendNotification(String uId,String title,String message ) {
        Log.i(TAG, "sendNotification: " + title + message );

        Intent notificationIntent;
        // Intent to start the main Activity
        if (uId.equals("Charger_Location")) {
            notificationIntent = MapFragment.makeNotificationMainIntent(
                    this, title
            );
        }
        else{
            notificationIntent = MapFragment.makeNotificationFeedbackIntent(
                    this, title
            );
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0,  PendingIntent.FLAG_ONE_SHOT);

        // Creating and sending Notification
        NotificationManager notificatioMng =
                (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        notificatioMng.notify(
                GEOFENCE_NOTIFICATION_ID,
                createNotification(title,message, notificationPendingIntent));
    }

    // Create notification
    @RequiresApi(api = Build.VERSION_CODES.N)
    private Notification createNotification(String title,String message, PendingIntent notificationPendingIntent) {

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);

        notificationBuilder
                .setSmallIcon(R.drawable.location_icon)
                .setColor(Color.RED)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher_round))
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setAutoCancel(true);

        Notification n= notificationBuilder.build();
        return n;
    }


    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}