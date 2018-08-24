package com.jlanka.tripplanner;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.stetho.Stetho;
import com.jlanka.tripplanner.Fragments.BluetoothFragment;
import com.jlanka.tripplanner.Fragments.EvStatFragment;
import com.jlanka.tripplanner.Fragments.HistoryFragment;
import com.jlanka.tripplanner.Fragments.MapFragment;
import com.jlanka.tripplanner.Fragments.PaymentFragment;
import com.jlanka.tripplanner.Fragments.ProfileFragment;
import com.jlanka.tripplanner.Fragments.SettingsFragment;
import com.jlanka.tripplanner.Fragments.VehicleFragment;
import com.jlanka.tripplanner.Server.OnErrorListner;
import com.jlanka.tripplanner.Server.OnResponseListner;
import com.jlanka.tripplanner.Server.ServerConnector;
import com.jlanka.tripplanner.UserActivity.LoginActivity;
import com.jlanka.tripplanner.UserActivity.SessionManager;

import org.eclipse.paho.android.service.MqttService;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cat.ereza.customactivityoncrash.config.CaocConfig;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    SessionManager session;
    FragmentManager fragmentManager = getFragmentManager();
    MapFragment mContent;
    String user_credit;
    TextView creditView;
    String vehicles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isLocationEnabled();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Stetho.initializeWithDefaults(this);
        checkLocationPermission();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // only for gingerbread and newer versions
            CaocConfig.Builder.create()
                    .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
                    .enabled(true) //default: true
                    .showErrorDetails(false) //default: truefalse
                    .showRestartButton(true) //default: true
                    .logErrorOnRestart(false) //default: truefalse
                    .trackActivities(true) //default: false
                    .minTimeBetweenCrashesMs(2000) //default: 3000
                    .errorDrawable(R.mipmap.ic_launcher) //default: bug image
                    .restartActivity(MainActivity.class) //default: null (your app's launch activity)
                    .apply();
        }

        // Session class instance
        session = new SessionManager(getApplicationContext());
        //This will redirect user to LoginActivity is he is not logged in
        session.checkLogin();
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();

        String fname = user.get(SessionManager.user_fname);
        String lname = user.get(SessionManager.user_lname);
        String user_password = user.get(SessionManager.pass_word);
        user_credit = user.get(SessionManager.user_credit);
        String user_mobile = user.get(SessionManager.user_mobile);
        vehicles = user.get(SessionManager.electric_vehicles);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                setCredit();
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        TextView nameView = (TextView) header.findViewById(R.id.user_name);
        creditView = (TextView) header.findViewById(R.id.user_credit);
        TextView userMob = (TextView) header.findViewById(R.id.user_mobile);

        nameView.setText(fname + " " + lname);
        setUser_credit(user_credit);
        userMob.setText(user_mobile);

        if (session.isFirstTimeLaunch()) {
            startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
            finish();
        } else {
            checkVehicles();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processExtraData();
    }

    protected void processExtraData() {
        Bundle extras = getIntent().getExtras();

        // process the extra here.
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            fragmentManager.beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            checkVehicles();
        } else if (id == R.id.nav_history) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new HistoryFragment()).addToBackStack(HistoryFragment.class.getName()).commit();
        } else if (id == R.id.nav_payment) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new PaymentFragment()).addToBackStack(PaymentFragment.class.getName()).commit();
        } else if (id == R.id.nav_profile) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new ProfileFragment()).addToBackStack(ProfileFragment.class.getName()).commit();
        } else if (id == R.id.nav_settings) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new SettingsFragment()).addToBackStack(SettingsFragment.class.getName()).commit();
        } else if (id == R.id.nav_bluetooth) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new BluetoothFragment()).addToBackStack(BluetoothFragment.class.getName()).commit();
        } else if (id == R.id.nav_evstat) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new EvStatFragment()).addToBackStack(EvStatFragment.class.getName()).commit();
        } else if (id == R.id.nav_logout) {
            ServerConnector.getInstance(getApplicationContext()).cancelRequest("Logout");
            ServerConnector.getInstance(getApplicationContext()).getRequest(ServerConnector.SERVER_ADDRESS+"ev_owners/logout/",
            new OnResponseListner() {
                @Override
                public void onResponse(String response) {
                    try{
                        if(new JSONObject(response).getString("status").contains("successful")){
                            session.logoutUser();
                        }
                        else{
                            session.logoutUser();
                        }
                    }
                    catch(JSONException e){
                        e.printStackTrace();
                    }
                }
            },

            new OnErrorListner() {
                @Override
                public void onError(String error, JSONObject obj) {
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle("Sorry");
                    alertDialog.setMessage(error);
                    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(i);
                        }
                    });
                    alertDialog.show();
                }
            },"Logout");
            session.logoutUser();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem myItem = menu.findItem(R.id.action_sync);
        myItem.setVisible(false);
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    public void setUser_credit(String user_credit) {
        this.user_credit = user_credit;
        creditView.setText("Credit : Rs. "+ this.user_credit);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, MqttService.class));

    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals( state ) ) {
            return true;
        }
        return false;
    }

    //--------------------------------Thiwanka-------------------------------------
    private void checkVehicles(){
        vehicles=session.getVehicles();
        if(vehicles==null || vehicles.equals("[]")){
            fragmentManager.beginTransaction().replace(R.id.content_frame, new VehicleFragment()).commit();
        }else{
            if (mContent==null) {
                mContent = new MapFragment();
            }

            if (fragmentManager.getBackStackEntryCount()<1)
                fragmentManager.beginTransaction().replace(R.id.content_frame,mContent).commit();
            else
                fragmentManager.beginTransaction().replace(R.id.content_frame,mContent).addToBackStack(MapFragment.class.getName()).commit();
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.support.v7.app.AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
                        checkVehicles();
                    }
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        checkLocationPermission();
                    }
                }
                return;
            }
        }
    }

    @Override
    public void onResume() {
        isLocationEnabled();
        super.onResume();
    }

    private void isLocationEnabled(){
        LocationManager lm = (LocationManager)this.getSystemService(this.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Please enable location services to use this app");
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    //get gps
                }
            });
            dialog.show();
        }
    }

    private void setCredit(){
        String id = session.getUserID();
        ProgressBar pb = findViewById(R.id.nav_credit_progress);
        pb.setVisibility(View.VISIBLE);

        ServerConnector.getInstance(this).cancelRequest("GetUserCredit");
        ServerConnector.getInstance(this).getRequest(ServerConnector.SERVER_ADDRESS + "ev_owners/" + id + "/",
                new OnResponseListner() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            String credit = new JSONObject(response).getString("balance");
                            setUser_credit(credit);
                            session.setUser_credit(credit);
                            pb.setVisibility(View.INVISIBLE);
                        }
                        catch (Exception e){
                            pb.setVisibility(View.INVISIBLE);
                        }

                    }
                },
                new OnErrorListner() {
                    @Override
                    public void onError(String error, JSONObject obj) {
                        pb.setVisibility(View.INVISIBLE);
                    }
                },
                "GetUserCredit"
        );
    }
}
