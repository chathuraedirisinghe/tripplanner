package com.jlanka.evtripplanner;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;


/**
 * Created by Workstation on 2/7/2018.
 */

public class GoogleAnalyticsService extends Application {

    private static GoogleAnalyticsService service;
    private static GoogleAnalytics sAnalytics;
    private static Tracker sTracker;

    @Override
    public void onCreate() {
        super.onCreate();

        sAnalytics = GoogleAnalytics.getInstance(this);
        getDefaultTracker();
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    public synchronized Tracker getDefaultTracker() {
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        if (sTracker == null) {
            sTracker = sAnalytics.newTracker(R.xml.global_tracker);
        }

        return sTracker;
    }

    public static GoogleAnalyticsService getInstance(){
        if (service==null) {
            service = new GoogleAnalyticsService();
        }

        return service;
    }

    public void trackException(Context c, Exception e) {
        if (e != null) {
            sTracker.send(new HitBuilders.ExceptionBuilder()
                    .setDescription(new StandardExceptionParser(c, null)
                            .getDescription(Thread.currentThread().getName(), e))
                    .setFatal(false)
                    .build());
        }
    }

    public void setScreenName(String screenName){
        sTracker.setScreenName(screenName);
        sTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void setUser(String uId,String action){
        sTracker.set("&uid", uId);
        sTracker.send(new HitBuilders.EventBuilder()
                .setCategory("UX")
                .setAction(action)
                .build());
    }

    public void setAction(String category,String action,String label){
        sTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .build());
    }
}

