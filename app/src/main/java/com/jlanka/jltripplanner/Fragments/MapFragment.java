package com.jlanka.jltripplanner.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.transition.ArcMotion;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

import com.jlanka.jltripplanner.DataLoader;
import com.jlanka.jltripplanner.GeofenceMonitor;
import com.jlanka.jltripplanner.GoogleAnalyticsService;
import com.jlanka.jltripplanner.LocationMonitor;
import com.jlanka.jltripplanner.MQTT.MQTTHelper;
import com.jlanka.jltripplanner.MQTT.MQTTPublisher;
import com.jlanka.jltripplanner.MainActivity;
import com.jlanka.jltripplanner.Model.FoundSuggestion;
import com.jlanka.jltripplanner.Model.GPSPoint;
import com.jlanka.jltripplanner.Model.Route;
import com.jlanka.jltripplanner.Model.Workable;
import com.jlanka.jltripplanner.R;
import com.jlanka.jltripplanner.Server.OnErrorListner;
import com.jlanka.jltripplanner.Server.OnResponseListner;
import com.jlanka.jltripplanner.Server.ServerConnector;
import com.jlanka.jltripplanner.UserActivity.SessionManager;

import pl.droidsonroids.gif.GifImageView;
import static android.content.ContentValues.TAG;

public class MapFragment extends Fragment implements
        OnMapReadyCallback,
        FloatingSearchView.OnSearchListener,
        FloatingSearchView.OnQueryChangeListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnCameraIdleListener{

    MQTTHelper mqttHelper;
    SessionManager session;

    GoogleMap mGoogleMap;
    MapView mMapView;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    Marker chargerMarker;
    String selectedMarker;

    //--------------Thiwanka--------------
    private static Marker destinatonMarker;
    private static MapFragment mapFragment;

    View mView;
    double currentLatitude,currentLongitude,mylatitude,mylongitude;
    LatLng latLng,selectedMarker_latLng;
    private long chargingDuration;
    private boolean firstLoad = true;

//    public JSONArray itemArray =new JSONArray();
    public JSONArray stations_object = new JSONArray();
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public static boolean isCharging,destinationSet=false;

    String user_mobile,user_pin;
    HashMap<String, String> user;

    View bottomSheet;
    BottomSheetBehavior behavior;

    //------------------------------------Thiwanka----------------------------------------------------------------
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place/autocomplete/json";
    private static final String MAPS_API_BASE= "https://maps.googleapis.com/maps/api/geocode/json";
    private static final String CHECK_ROUTING_STRING="http://yvk.rxn.mybluehost.me:8000/api/feasible_directions";
    private static final String ROUTING_STRING="https://maps.googleapis.com/maps/api/directions/json?";
    private ArrayList<Route> routes;
    private ArrayList<Circle> circles;
    private ArrayList<Polyline> routeLines;
    private ArrayList<Marker> infoMarkers,chargers;
    private ViewGroup container;

    //------------------------------------------------------------------------------------------------------------

    String charger_type, charger_address, charger_available;
    ProgressDialog progressDialog;
    public static String charging_marker;

    @BindView(R.id.charger_id) TextView _charger_id;
    @BindView(R.id.charger_availability) TextView _charger_availability;
    @BindView(R.id.charger_type) TextView _charger_type;
    @BindView(R.id.charging_station_name) TextView _charging_station;
    @BindView(R.id.charge_now_btn) Button _chargenow_btn;
    @BindView(R.id.get_direction) Button _get_direction;
    @BindView(R.id.stop_now_btn) Button _stop_charge;
    @BindView(R.id.charger_animation) GifImageView _imageView;

    //------------------------Thiwanka-----------------------------
    @BindView(R.id.permissionLayout) RelativeLayout permissionLayout;
    @BindView(R.id.charger_icon) ImageView _charger_icon;
    @BindView(R.id.progress_Bar_Layout) RelativeLayout progressBar;
    @BindView(R.id.textView) TextView progress_bar_text;
    @BindString(R.string.google_maps_key) String MAP_API_KEY;
    @BindView(R.id.fabLegend) FloatingActionButton fabLegend;
    @BindView(R.id.fabRoute) FloatingActionButton fabRoute;
    @BindView(R.id.fabNav) FloatingActionButton fabNav;
    @BindView(R.id.fabEnd) FloatingActionButton fabEnd;
    @BindView(R.id.floating_search_view) FloatingSearchView fsv;
    @BindView(R.id.legendView) ImageView legendView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        mView=inflater.inflate(R.layout.fragment_map,container,false);
        ButterKnife.bind(this,mView);

        getCurrentLocation();
        container = (ViewGroup) getActivity().findViewById(R.id.cordinator_layout);


        bottomSheet = mView.findViewById(R.id.design_bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomSheet);
        session = new SessionManager(getActivity());
        user = session.getUserDetails();
        user_mobile = user.get(SessionManager.user_mobile);
        user_pin = user.get(SessionManager.pass_word);
//        getProfileData(user_mobile);
        getCredit(user_mobile);
        if(session.isLoggedIn()) {
            addStateSubscriber();
        }
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_DRAGGING:
                        Log.d(TAG, "BottomSheetBehavior.STATE_DRAGGING");
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        Log.d(TAG, "BottomSheetBehavior.STATE_SETTLING");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        Log.d(TAG, "BottomSheetBehavior.STATE_EXPANDED");
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.d(TAG, "BottomSheetBehavior.STATE_COLLAPSED");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        Log.d(TAG, "BottomSheetBehavior.STATE_HIDDEN");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                int val14 = getMarginInDp((int)Math.round(6*slideOffset)+8);
                int val80=getMarginInDp((int)Math.round(70*slideOffset)+10);
                int val140=getMarginInDp((int)Math.round(75*slideOffset)+70);

                setMargins(fabRoute ,0,0,val14,val80);
                setMargins(fabNav ,0,0,val14,val80);
                setMargins(fabEnd ,0,0,val14,val140);
            }
        });


        _chargenow_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chargeNow(selectedMarker);
            }
        });

        _get_direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDirection(latLng.latitude,latLng.longitude);
            }
        });

        _stop_charge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopCharge();
            }
        });

        //----------------------Thiwanka--------------------------------------
        GoogleAnalyticsService.getInstance().setScreenName(this.getClass().getSimpleName());
        chargers=new ArrayList<>();
        fabLegend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentLocation();
                showLegend();
            }
        });

        fabRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentLocation();
                if (mLastLocation!=null)
                    checkRoute("1234", new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()), destinatonMarker.getPosition(), "30", "30");
            }
        });

        fabNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentLocation();
                if (mLastLocation != null && routes != null)
                    sendRouteToGoogleApp(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), destinatonMarker.getPosition(), routes.get(0).getChargers());
            }
        });

        fabEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentLocation();
                removeDestinationMarker();
                fabEnd.hide();
                fabNav.hide();
                fabRoute.hide();
                mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
            }
        });

        fsv.setOnQueryChangeListener(this);
        fsv.setOnSearchListener(this);

        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMapView = (MapView) mView.findViewById(R.id.map);
        if (mMapView != null) {
            mMapView.onCreate(savedInstanceState);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }

    public static MapFragment getInstance(){
        if (mapFragment==null)
            mapFragment=new MapFragment();

        return mapFragment;
    }

    @Override
    public void onPause() {
        super.onPause();
        LocationMonitor.instance(getActivity().getApplicationContext()).stop();
    }

    public void getCurrentLocation(){
        LocationMonitor.instance(getActivity().getApplicationContext()).onChange(new Workable<GPSPoint>() {
            @Override
            public void work(GPSPoint gpsPoint) {
                mLastLocation=new Location("");
                mLastLocation.setLatitude(Double.parseDouble(gpsPoint.getLatitude().toString()));
                mLastLocation.setLongitude(Double.parseDouble(gpsPoint.getLongitude().toString()));

                mylatitude=mLastLocation.getLatitude();
                mylongitude=mLastLocation.getLongitude();

                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }

                if (firstLoad) {
                    mGoogleMap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()) , 14.0f) );
                    firstLoad = false;
                }

                latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                if ((Math.abs(currentLatitude-mLastLocation.getLatitude())>0.001) && (Math.abs(currentLongitude-mLastLocation.getLongitude())>0.0001)){
                    currentLatitude = Math.floor(mLastLocation.getLatitude()*100)/100;
                    currentLongitude = Math.floor(mLastLocation.getLongitude()*10000)/10000;
//            Log.w("Mewwa ",latLng.toString());
//            Toast.makeText(getActivity(), "Location    " + currentLatitude+"     "+currentLongitude, Toast.LENGTH_SHORT).show();
                    getResponse(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                }else{
//            Toast.makeText(getActivity(), "OK", Toast.LENGTH_SHORT).show();
                }

//        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        });
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionLayout.setVisibility(View.VISIBLE);
            return false;
        }
        else {
            getCurrentLocation();
            permissionLayout.setVisibility(View.GONE);
            return true;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        hideLegend();
        if (marker.getTag().equals("Charger_Marker")) {
            selectedMarker = marker.getTitle();
            selectedMarker_latLng = marker.getPosition();
            Log.d("Selected Marker", String.valueOf(selectedMarker));
            Log.d("Stations Array Up : ", stations_object.toString());
            try {
                for (int i = 0; i < stations_object.length(); i++) {
                    JSONObject charger_array = stations_object.getJSONObject(i);
                    final String chargerid = charger_array.getString("device_id");
                    if (chargerid.equals(selectedMarker)) {
                        charger_type = charger_array.getString("charger_type");
                        charger_address = charger_array.getString("location");
                        charger_available = charger_array.getString("availability");
                    }
                }

                switch (charger_available){
                    case "Available":
                        _charger_icon.setImageBitmap(getMarkerIcon(charger_type, "Available"));
                        break;
                    case "Busy":
                        _chargenow_btn.setEnabled(false);
                        _charger_icon.setImageBitmap(getMarkerIcon(charger_type,"Busy" ));
                        break;
                    case "NA":
                        _chargenow_btn.setEnabled(false);
                        _charger_icon.setImageBitmap(getMarkerIcon(charger_type,"NA" ));
                        break;
                }

                if (isCharging) {
                    setCharging();
                }

                collapseBottomSheet();
                if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    expandBottomSheet();
                }
                _charger_id.setText(marker.getTitle());

                if (charger_type.equals("AC"))
                    _charger_type.setText("Standard");
                else
                    _charger_type.setText("Rapid");
//        _charger_address.setText("Address : "+charger_address);
                _charger_availability.setText(" " + charger_available);

            } catch (JSONException e) {
                trackError(e);
                e.printStackTrace();
            }
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getActivity());
        mGoogleMap=googleMap;

        if (checkLocationPermission())
            mGoogleMap.setMyLocationEnabled(true);

        //---------------Thiwanka-----------------
        fsv.setVisibility(View.VISIBLE);

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(true);

        googleMap.setOnMarkerClickListener(this);

        googleMap.setOnCameraIdleListener(this);
        googleMap.setOnCameraMoveStartedListener(this);
        googleMap.setOnCameraMoveListener(this);
        googleMap.setOnCameraMoveCanceledListener(this);

        //Initialize Google Play Services
        mGoogleMap.setMyLocationEnabled(true);


        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                Log.d("Map","Map clicked");
//                if (behavior.getState()==BottomSheetBehavior.STATE_EXPANDED) {
                    hideLegend();
                    collapseBottomSheet();
//                }
            }
        });

        //----------------------------Thiwanka---------------------------------------
        mGoogleMap.setPadding(0,getMarginInDp(55),getMarginInDp(2),0);

        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                setDestinationOnClick(latLng);
            }
        });
    }

    @Override
    public void onCameraIdle() {
        double nextlatitude = mGoogleMap.getProjection().getVisibleRegion().latLngBounds.getCenter().latitude;
        double nextlongitude = mGoogleMap.getProjection().getVisibleRegion().latLngBounds.getCenter().longitude;

        System.out.println("Location My : "+mylatitude+"  "+mylongitude);
        System.out.println("Location Next : "+nextlatitude+"  "+nextlongitude);

        Location my = new Location("point A");

        my.setLatitude(mylatitude);
        my.setLongitude(mylongitude);

        Location remote = new Location("point B");

        remote.setLatitude(nextlatitude);
        remote.setLongitude(nextlongitude);

        float distance = my.distanceTo(remote);

        System.out.println("Location Distance : "+ distance);

        if(distance>5000.0){
            getResponse(nextlatitude,nextlongitude);
        }else{
            //nothing
        }

    }

    @Override
    public void onCameraMoveCanceled() {
//        Toast.makeText(getActivity(), "Camera movement canceled.",
//                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraMove() {

    }

    @Override
    public void onCameraMoveStarted(int reason) {
//        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
//            Toast.makeText(getActivity(), "The user gestured on the map.",
//                    Toast.LENGTH_SHORT).show();
//        } else if (reason == GoogleMap.OnCameraMoveStartedListener
//                .REASON_API_ANIMATION) {
//            Toast.makeText(getActivity(), "The user tapped something on the map.",
//                    Toast.LENGTH_SHORT).show();
//        } else if (reason == GoogleMap.OnCameraMoveStartedListener
//                .REASON_DEVELOPER_ANIMATION) {
//            Toast.makeText(getActivity(), "The app moved the camera.",
//                    Toast.LENGTH_SHORT).show();
//        }
    }

    public void getResponse(final double lat, final double lng){
        String charging_address = "charging_stations/";

        ServerConnector serverConnector= new ServerConnector(ServerConnector.SERVER_ADDRESS+charging_address,null,Request.Method.GET,getActivity());
        serverConnector.setOnReponseListner(new OnResponseListner() {
            @Override
            public void onResponse(String response) {
                Log.w("Stations Details  : ", String.valueOf(response));
                try {
                            if(response.equals("[]")){
                                System.out.println("Charger Data : "+response);
                            }else{
                                stations_object = new JSONArray(response);
                                stationUpdater();
                            }

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            trackError(e);
                            e.printStackTrace();
                        }

            }
        });
        serverConnector.setOnErrorListner(new OnErrorListner() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onError(String error, JSONObject obj) {
                System.out.println("SERVER RESPONSE : " + error);
                showDialog("Timeout","Check your internet connection","getResponse",new Double[]{lat,lng},true);
            }
        });
        serverConnector.sendRequest();
    }

    public void addMarkers(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!destinationSet)
                        mGoogleMap.clear();

                    ArrayList<Marker> markers = new ArrayList<>();
                    for (int i = 0; i < stations_object.length(); i++) {
                        JSONObject charger = stations_object.getJSONObject(i);
                        System.out.println("Charger Status : "+charger.getString("availability")+" , Charger Type : "+charger.getString("charger_type"));

                        chargerMarker = mGoogleMap.addMarker(new MarkerOptions()
                                .title(charger.getString("device_id"))
                                .snippet(charger.getString("location"))
                                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerIcon(charger.getString("charger_type").toString(),
                                        charger.getString("availability").toString())))
                                .position(new LatLng(
                                        charger.getDouble("lat"),
                                        charger.getDouble("lng")
                                ))
                        );
                        //---------------------------Thiwanka----------------------------
                        chargerMarker.setTag("Charger_Marker");
                        markers.add(chargerMarker);
                    }

                    //---------------------------Thiwanka----------------------------
                    if(destinationSet) {
                        for (Marker m : chargers) {
                            m.remove();
                        }
                        chargers = markers;
                    }

                } catch (JSONException e) {
                    trackError(e);
                    e.printStackTrace();
                }
            }
        });
    }

    public void stationUpdater(){
        Log.e("Station Updater","Started");
        try{
            for(int i=0; i < stations_object.length();i++){
                JSONObject charger_array = stations_object.getJSONObject(i);
                final String charger_id = charger_array.getString("device_id").toLowerCase();
                Log.e("Charger ID",charger_id);
                startMqtt(charger_id);
            }
        }catch (JSONException e){
            trackError(e);
            e.printStackTrace();
        }
    }

    public void startMqtt(final String charger_id) {
        MQTTHelper mqttHelper2 = new MQTTHelper(getActivity(),"tcp://development.enetlk.com:1887");
        mqttHelper2.subscriptionTopic=("server/"+charger_id+"/status");
        mqttHelper2.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
            }

            @Override
            public void connectionLost(Throwable throwable) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Is User : ",charger_id.toString()+"      "+mqttMessage.toString());
                updateItemArray(charger_id,mqttMessage.toString());
//                System.out.println(itemArray.toString());
                addMarkers();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });
    }

    private void updateItemArray(String charger_id, String s) throws JSONException {
        try{
            for(int i=0; i < stations_object.length();i++){
                JSONObject charger_array = stations_object.getJSONObject(i);
                final String chargerid = charger_array.getString("device_id").toLowerCase();
                if (chargerid.equals(charger_id)){
                    stations_object.getJSONObject(i).put("availability",s);
                }
            }
        }catch (JSONException e){
            trackError(e);
            e.printStackTrace();
        }

    }

    private void addStateSubscriber() {
        mqttHelper = new MQTTHelper(getActivity().getApplicationContext(),"tcp://development.enetlk.com:1887");
        mqttHelper.subscriptionTopic=("server/"+user_mobile.substring(1)+"/status");
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
            }
            @Override
            public void connectionLost(Throwable throwable) {
            }
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Is User : ",mqttMessage.toString());
                if(mqttMessage.toString().equals("charging")){
                    setCharging();
                }else if(mqttMessage.toString().contains("busy") || mqttMessage.toString().contains("recharge")){
                    notifyOnError(mqttMessage.toString());
                }else if(mqttMessage.toString().equals("not charging")){
                    setFree();
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });

    }

    private void setFree() {
        isCharging = false;
        if(progressDialog!=null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
//        session.user_charging_station(null);
        _chargenow_btn.setEnabled(true);
        _imageView.setVisibility(View.GONE);
        _charging_station.setVisibility(View.GONE);
        _chargenow_btn.setVisibility(View.VISIBLE);
        _get_direction.setVisibility(View.VISIBLE);
        _stop_charge.setVisibility(View.GONE);
    }

    private void notifyOnError(String s) {
        if(progressDialog!=null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }

        _chargenow_btn.setEnabled(true);
        AlertDialog dialog =new AlertDialog.Builder(getActivity()).create();
        dialog.setTitle("Error...");
        dialog.setMessage(s.toString());
        dialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    private void setCharging() {
        isCharging = true;

        if(progressDialog!=null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        _imageView.setVisibility(View.VISIBLE);
        _charging_station.setVisibility(View.VISIBLE);
        _charging_station.setText(user.get(SessionManager.user_chargingStation));
        _chargenow_btn.setVisibility(View.GONE);
        _get_direction.setVisibility(View.GONE);
        _stop_charge.setVisibility(View.VISIBLE);

    }

    public void chargeNow(String selectedMarker){
        progressDialog= new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Start Charging...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        charging_marker =selectedMarker;
        session.user_charging_station(selectedMarker);

        JSONObject payload = new JSONObject();
        try {
            payload.put("cusid",user_mobile.substring(1));
            payload.put("bysms", 0);
            payload.put("message", "ev chg "+ charging_marker +" "+user_pin);
            chargingDuration=System.currentTimeMillis();
            GoogleAnalyticsService.getInstance().setAction("Charging","Start Charging","");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            trackError(e);
            e.printStackTrace();

        }

//        Log.d("CHARGE NOW : " , payload.toString());

        MQTTPublisher mqttPublisher = new MQTTPublisher(getActivity().getApplicationContext(),"tcp://development.enetlk.com:1883");
        mqttPublisher.publishToTopic(payload.toString(),getActivity().getApplicationContext());
        collapseBottomSheet();
    }

    public void stopCharge(){
        progressDialog= new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Stopping...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JSONObject payload = new JSONObject();
        try {
            payload.put("cusid",user_mobile.substring(1));
            payload.put("bysms", 0);
            payload.put("message", "ev stop "+ charging_marker +" "+user_pin);
            chargingDuration=System.currentTimeMillis()-chargingDuration;
            double hours = (chargingDuration / (1000*60*60)) % 24;
            GoogleAnalyticsService.getInstance().setAction("Charging","Duration",hours+"");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            trackError(e);
            e.printStackTrace();

        }
        MQTTPublisher mqttPublisher = new MQTTPublisher(getActivity().getApplicationContext(),"tcp://development.enetlk.com:1883");
        mqttPublisher.publishToTopic(payload.toString(),getActivity().getApplicationContext());
        collapseBottomSheet();
    }

    public void getDirection(double latitude, double longitude){
        String markerlatitude = String.valueOf(selectedMarker_latLng.latitude);
        String markerlongitude = String.valueOf(selectedMarker_latLng.longitude);

        GoogleAnalyticsService.getInstance().setAction("Map Functions","Navigate","Navigate to Charger");

        StringBuilder sb =new StringBuilder("http://www.google.lk/maps/dir/");
        sb.append(latitude+","+longitude);
        sb.append("/");
        sb.append(markerlatitude+","+markerlongitude);
        Uri uriUrl = Uri.parse(String.valueOf(sb));
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

    public void getCredit(final String user_mobile){
        RequestQueue rq = Volley.newRequestQueue(getActivity().getApplicationContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ServerConnector.SERVER_ADDRESS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject mydata = new JSONObject(response);
                            String credit = mydata.get("credit").toString();
//                            System.out.println("Credits : "+credit);
                            user.put(SessionManager.user_credit, credit);
                            ((MainActivity)getActivity()).setUser_credit(user.get(SessionManager.user_credit));
                        } catch (JSONException e) {
                            trackError(e);
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error   ", String.valueOf(error));
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("fnID", "3");
                params.put("mobNo", user_mobile);
                return params;
            }
        };
        rq.add(stringRequest);
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------Thiwanka----------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
        fsv.hideProgress();
        fsv.closeMenu(true);
        fsv.clearSearchFocus();
        searchLocationByName(searchSuggestion.getBody());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onSearchAction(String currentQuery) {
        hideLegend();
        onSearchTextChanged(null,currentQuery);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onSearchTextChanged(String oldQuery,String newQuery) {
        getCurrentLocation();
        if (mLastLocation!=null)
            getAddressSuggestions(newQuery);
        else
            showDialog("Location Error","Unable to retrieve current location","currentLocation",null,true);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showDialog(String title, String message, final String failedMethod, final Object[] passedQuery, boolean required) {
        try {
            final android.app.AlertDialog.Builder dlgAlert = new android.app.AlertDialog.Builder(getActivity().getWindow().getContext());
            dlgAlert.setMessage(message);
            dlgAlert.setTitle(title);
            String positiveButtonText = "Retry";

            dlgAlert.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (failedMethod) {
                        case "getAddressSuggestions":
                            getAddressSuggestions(passedQuery[0].toString());
                            break;
                        case "searchLocationByName":
                            searchLocationByName(passedQuery[0].toString());
                            break;
                        case "setDestinationOnClick":
                            setDestinationOnClick((LatLng) passedQuery[0]);
                            break;
                        case "checkRoute":
                            checkRoute(passedQuery[0].toString(), (LatLng) passedQuery[1], (LatLng) passedQuery[2],
                                    passedQuery[3].toString(), passedQuery[4].toString());
                            break;
                        case "route":
                            setDestinationOnClick((LatLng) passedQuery[1]);
                            route((LatLng) passedQuery[0], (LatLng) passedQuery[1], (ArrayList) passedQuery[2]);
                            break;
                        case "getResponse":
                            getResponse((Double) passedQuery[0], (Double) passedQuery[1]);
                            break;
                    }
                }
            });

            if (!required) {
                dlgAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (failedMethod) {
                            case "route":
                                removeDestinationMarker();
                                break;
                            case "checkRoute":
                                removeDestinationMarker();
                                break;
                        }
                    }
                });
            }

            dlgAlert.setCancelable(!required);
            android.app.AlertDialog ad=dlgAlert.create();
            ad.show();
            Button b = ad.getButton(DialogInterface.BUTTON_NEGATIVE);
            dlgAlert.setIcon(R.drawable.logo);

            if(b != null) {
                b.setTextColor(Color.BLACK);
            }
        }
        catch (Exception e){
            GoogleAnalyticsService.getInstance().trackException(getActivity().getApplicationContext(),e);
        }
    }

    private void getAddressSuggestions(final String query){
        //Cancel previous requests
        DataLoader.getInstance(getActivity().getApplicationContext()).cancelRequest("Address_Suggestion_Requests");

        fsv.showProgress();
        StringBuilder url = new StringBuilder(PLACES_API_BASE);
        try {
            url.append("?input=" + URLEncoder.encode(query, "utf8"));
            url.append("&location=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() );
            url.append("&radius=500&key=" + URLEncoder.encode(MAP_API_KEY, "utf8")  + "&components=country:lk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url.toString(),new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onResponse(String response) {
                JSONObject jsonResults;
                ArrayList<FoundSuggestion> resultList=null;
                if (response.equals("[]")) {

                } else {

                    try {
                        // Log.d(TAG, jsonResults.toString());
                        jsonResults = new JSONObject(response);
                        // Create a JSON object hierarchy from the results
                        JSONObject jsonObj = new JSONObject(jsonResults.toString());
                        JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

                        // Extract the Place descriptions from the results
                        if (predsJsonArray != null) {
                            resultList = new ArrayList<FoundSuggestion>(predsJsonArray.length());
                            for (int i = 0; i < predsJsonArray.length(); i++) {
                                FoundSuggestion fs = new FoundSuggestion(predsJsonArray.getJSONObject(i).getString("place_id"), predsJsonArray.getJSONObject(i).getString("description").replace(", Sri Lanka",""));
                                resultList.add(fs);
                            }
                        }
                        fsv.hideProgress();
                        fsv.swapSuggestions(resultList);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        fsv.hideProgress();
                        showDialog("Timeout","Check Your internet connection...","getAddressSuggestions",new Object[]{query},false);
                        e.printStackTrace();
                    }
                }
            }
        },
                new Response.ErrorListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        fsv.hideProgress();
                        Log.d("Error   ", String.valueOf(error));
                        DataLoader.getInstance(getActivity().getApplicationContext()).cancelRequest("Address_Suggestion_Requests");
                        showDialog("Timeout","Check Your internet connection...","getAddressSuggestions",new Object[]{query},false);
                    }
                });
        stringRequest.setTag("Address_Suggestion_Requests");
        DataLoader.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);
    }

    private void searchLocationByName(final String query){
        showProgress(null);
        removeDestinationMarker();
        removeGeofences();

        //Cancel previous requests
        DataLoader.getInstance(getActivity().getApplicationContext()).cancelRequest("Location_Search_Requests");

        StringBuilder url = new StringBuilder(MAPS_API_BASE);
        try {
            url.append("?address=" + URLEncoder.encode(query, "utf8"));
            url.append("&location=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());
            url.append("&radius=500&key=" + URLEncoder.encode(MAP_API_KEY, "utf8") + "&components=country:lk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url.toString(),new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equals("[]")) {

                } else {
                    try {
                        // Create a JSON object hierarchy from the results
                        JSONObject jsonObj = new JSONObject(response);
                        JSONArray geoResJsonArray = jsonObj.getJSONArray("results");
                        // Extract the LatLng from the results
                        if (geoResJsonArray!=null) {
                            String address = geoResJsonArray.getJSONObject(0).getString("formatted_address").replace(", Sri Lanka","");
                            JSONObject geo=geoResJsonArray.getJSONObject(0).getJSONObject("geometry");
                            JSONObject LatLngObj=geo.getJSONObject("location");
                            Double lat=LatLngObj.getDouble("lat");
                            Double lng=LatLngObj.getDouble("lng");
                            addDestinationMarker(new LatLng(Double.parseDouble(lat.toString()),Double.parseDouble(lng.toString())),address);
                            showDestinationSet();
                            zoomToLocation(destinatonMarker.getPosition(),17,0);
                            fabNav.hide();
                            fabEnd.hide();
                            fabRoute.show();
                        }
                        hideProgress();
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        hideProgress();
                        e.printStackTrace();
                    }
                }

            }
        },
                new Response.ErrorListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideProgress();
                        Log.d("Error   ", String.valueOf(error));
                        DataLoader.getInstance(getActivity().getApplicationContext()).cancelRequest("Location_Search_Requests");
                        showDialog("Timeout","Check Your internet connection..","searchLocationByName",new Object[]{query},false);
                    }
                });
        stringRequest.setTag("Location_Search_Requests");
        DataLoader.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);
    }

    private void zoomToLocation(LatLng location,int zoom,float bearing){
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 2000));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)      // Sets the center of the map to location user
                .zoom(zoom)                   // Sets the zoom
                .bearing(bearing)                // Sets the orientation of the camera to east
                .tilt(20)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder

        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void panCamera(LatLng[] includePoints){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for(LatLng l:includePoints) {
            builder.include(l);
        }

        LatLngBounds bounds = builder.build();

        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mGoogleMap.animateCamera(cu);
    }

    private void addDestinationMarker(LatLng location,String name){
        collapseBottomSheet();
        destinationSet=true;
        destinatonMarker=mGoogleMap.addMarker(new MarkerOptions()
                .title("Destination")
                .snippet(name)
                .position(location));

        destinatonMarker.setTag("Destination_Marker");
        destinatonMarker.showInfoWindow();
    }

    private void showDestinationSet(){
        Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.cordinator_layout),"Destination Set", Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(Color.RED);
        snackbar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabRoute.hide();
                removeDestinationMarker();
            }
        });
        snackbar.show();
    }

    private void checkRoute(final String vin, final LatLng start, final LatLng end, final String capacity, final String perc){
        showProgress("Getting directions...");
        GoogleAnalyticsService.getInstance().setAction("Map Functions","Routing","Routing Request");

        final String origin= String.valueOf(start.latitude)+","+String.valueOf(start.longitude);
        final String destination= String.valueOf(end.latitude)+","+String.valueOf(end.longitude);

        Map<String, String>  params = new HashMap<>();
        // the POST parameters:
        params.put("vin", vin);
        params.put("origin", origin);
        params.put("destination", destination);
        params.put("battery_capacity", capacity);
        params.put("battery_remaining", perc);
        //params.put("electric_vehicles","[]");

        ServerConnector serverConnector= new ServerConnector(ServerConnector.SERVER_ADDRESS+"feasible_directions",params,Request.Method.POST,getActivity().getApplicationContext());
        serverConnector.setOnReponseListner(new OnResponseListner() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onResponse(String response) {
                try {
                    if (response.equals("[]")) {

                    } else {
                            // Create a JSON object hierarchy from the results
                            JSONObject responseObject = new JSONObject(response);
                            int errorCode = responseObject.getInt("error_code");
                            if (errorCode == 0) {
                                JSONArray chargers = responseObject.getJSONArray("waypoints");
                                JSONArray ids = responseObject.getJSONArray("charging_station_ids");
                                ArrayList<LatLng> chargerLocations = new ArrayList<>();

                                for (int i = 0; i < chargers.length(); i++) {
                                    String chargerLocation = chargers.get(i).toString();
                                    if (chargerLocation != null && !chargerLocation.equals("[]")) {
                                        String latLng[] = chargerLocation.split(",");

                                        chargerLocations.add(new LatLng(Double.parseDouble(latLng[0].replaceAll("[^0-9.]", "")),
                                                Double.parseDouble(latLng[1].replaceAll("[^0-9.]", ""))));
                                    }
                                }

                                route(start, end, chargerLocations);
                            }
                            else {
                                String message = responseObject.getString("error");
                                hideProgress();
                                removeDestinationMarker();
                                showDialog("Routing Error", message, "checkRoute",
                                        new Object[]{vin, start, end, capacity, perc}, false);
                            }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        serverConnector.setOnErrorListner(new OnErrorListner() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onError(String error, JSONObject obj) {
                showDialog("Routing Error", error, "checkRoute",
                        new Object[]{vin, start, end, capacity, perc}, false);
            }
        });
        serverConnector.sendRequest();
    }

    private void route(final LatLng start, final LatLng end, final ArrayList<LatLng> chargerLocations){
        //Cancel previous requests
        DataLoader.getInstance(getActivity().getApplicationContext()).cancelRequest("Route");
        removeGeofences();

        System.out.println(start+","+end +chargerLocations);
        final String origin= String.valueOf(start.latitude)+","+String.valueOf(start.longitude);
        final String destination= String.valueOf(end.latitude)+","+String.valueOf(end.longitude);
        String url="";
        StringBuilder sb = new StringBuilder(ROUTING_STRING);
        try {
            sb.append("origin="+origin);
            //sb.append("?input=" + URLEncoder.encode(query, "utf8"));
            //sb.append("&types=(regions)");
            sb.append("&destination=" + destination);

            for(int i=0;i<chargerLocations.size();i++) {
                if (i==0)
                    sb.append("&waypoints=");

                sb.append(+chargerLocations.get(i).latitude+","+chargerLocations.get(i).longitude);

                if ((chargerLocations.size()-i)>1)
                    sb.append("|");
            }
            sb.append("&mode=driving&avoid=ferries&alternatives=true");
            sb.append("&key=" +  URLEncoder.encode(MAP_API_KEY, "utf8")+ "&components=country:lk");
            url=sb.toString();
            System.out.println(url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onResponse(final String response) {
                if (response.equals("[]")) {
                    System.out.println("ERROR");
                } else {
                    try {
                        JSONArray jsonLegs;
                        routes=new ArrayList<>();
                        routeLines = new ArrayList<>();
                        circles=new ArrayList<>();
                        // Create a JSON object hierarchy from the results
                        JSONObject responseObject = new JSONObject(response);
                        JSONArray routesArray=responseObject.getJSONArray("routes");
                        for(int i=0;i<routesArray.length();i++){
                            jsonLegs = routesArray.getJSONObject(i).getJSONArray("legs");
                            Route route=new Route(start,end,jsonLegs,chargerLocations);
                            routes.add(route);
                        }

                        infoMarkers=new ArrayList<>();
                        final LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(start);
                        getActivity().runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        int size=routes.size();
                                        if (size>2)
                                            size=2;

                                        for (int i = 0; i < size; i++) {
                                            List<List<HashMap<String, String>>> result = parse(routes.get(i).getRoute());
                                            ArrayList<LatLng> points;
                                            PolylineOptions lineOptions = null;

                                            // Traversing through all the routes
                                            for (int p = 0; p < result.size(); p++) {
                                                points = new ArrayList<LatLng>();
                                                lineOptions = new PolylineOptions();

                                                // Fetching i-th route
                                                List<HashMap<String, String>> path = result.get(p);

                                                // Fetching all the points in i-th route
                                                for (int j = 0; j < path.size(); j++) {
                                                    HashMap<String, String> point = path.get(j);

                                                    double lat = Double.parseDouble(point.get("lat"));
                                                    double lng = Double.parseDouble(point.get("lng"));
                                                    LatLng position = new LatLng(lat, lng);
                                                    points.add(position);
                                                    builder.include(position);
                                                }

                                                // Adding all the points in the route to LineOptions
                                                lineOptions.addAll(points);
                                                lineOptions.width(15);
                                            }

                                            TextView text = new TextView(getActivity().getApplicationContext());
                                            IconGenerator generator = new IconGenerator(getActivity().getApplicationContext());

                                            if (i == 0) {
                                                lineOptions.color(Color.argb(250, 69, 151, 255));
                                                lineOptions.zIndex(2l);

                                                text.setTextColor(Color.WHITE);
                                                generator.setBackground(getActivity().getApplicationContext().getDrawable(R.drawable.ideal_route_info));

                                                int mid = lineOptions.getPoints().size() / 2;
                                                LatLng latLng = lineOptions.getPoints().get(mid);

                                                text.setText(routes.get(i).getDurationString());

                                                generator.setContentView(text);
                                                Bitmap icon = generator.makeIcon();

                                                MarkerOptions tp = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(icon));
                                                Marker m = mGoogleMap.addMarker(tp);
                                                m.setTag("Polyline_InfoWindow");
                                                infoMarkers.add(m);
                                            } else {
                                                lineOptions.color(Color.argb(250, 176, 189, 197));
                                                // Drawing polyline in the Google Map for the i-th route
                                                lineOptions.zIndex(1l);
                                            }
                                            // Drawing polyline in the Google Map for the i-th route
                                            routeLines.add(mGoogleMap.addPolyline(lineOptions));
                                        }
                                    }
                                });


                        //adding geofence
                        if (!chargerLocations.isEmpty()) {
                            for(LatLng charger:chargerLocations)
                                createGeofence(charger, "Charger_Location");
                        }
                        //createGeofence(end,"Destination");

                        LatLngBounds bounds = builder.build();
                        int padding = 100; // offset from edges of the map in pixels
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                        mGoogleMap.animateCamera(cu);

                        fabNav.show();
                        fabEnd.show();
                        fabRoute.hide();
                        hideProgress();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        hideProgress();
                        e.printStackTrace();
                    }
                }
            }
        },
                new Response.ErrorListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideProgress();
                        error.printStackTrace();
                        DataLoader.getInstance(getActivity().getApplicationContext()).cancelRequest("Route");
                        showDialog("Timeout","Check Your internet connection...","route",null,false);
                    }
                });
        stringRequest.setTag("Route");
        DataLoader.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);
    }

    private void removeDestinationMarker(){
        if (routeLines!=null) {
            for(Polyline pl:routeLines){
                pl.remove();
            }
            routeLines.clear();
        }

        removeGeofences();

        if(infoMarkers!=null) {
            for (Marker m : infoMarkers) {
                m.remove();
            }
            infoMarkers.clear();
        }

        if (destinatonMarker!=null)
            destinatonMarker.remove();

        destinatonMarker=null;
    }

    public void setDestinationOnClick(final LatLng location){
        showProgress("Setting destination");
        removeDestinationMarker();
        removeGeofences();

        //Cancel previous requests
        DataLoader.getInstance(getActivity().getApplicationContext()).cancelRequest("Address_From_Location");

        StringBuilder url = new StringBuilder(MAPS_API_BASE);
        try {
            url.append("?latlng=" + URLEncoder.encode(location.latitude + "," + location.longitude, "utf8"));
            url.append("&key=" + URLEncoder.encode(MAP_API_KEY, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url.toString(),new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onResponse(String response) {
                JSONObject jsonResults;
                if (response.equals("[]")) {

                } else {
                    try {
                        // Create a JSON object hierarchy from the results
                        JSONObject jsonObj = new JSONObject(response);
                        JSONArray goeResJsonArray = jsonObj.getJSONArray("results");

                        // Extract the LatLng from the results
                        if (goeResJsonArray!=null && goeResJsonArray.length()>0) {
                            String address = goeResJsonArray.getJSONObject(0).getString("formatted_address").replace(", Sri Lanka","");

                            addDestinationMarker(location,address);
                            destinatonMarker.showInfoWindow();
                            showDestinationSet();
                            fabNav.hide();
                            fabEnd.hide();
                            fabRoute.show();
                        }
                        hideProgress();
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        hideProgress();
                        showDialog("Timeout","Check Your internet connection...","setDestinationOnClick",new Object[]{location},false);
                        e.printStackTrace();
                    }
                }
            }
        },
                new Response.ErrorListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideProgress();
                        error.printStackTrace();
                        removeDestinationMarker();
                        DataLoader.getInstance(getActivity().getApplicationContext()).cancelRequest("Address_From_Location");
                        showDialog("Timeout","Check Your internet connection...","setDestinationOnClick",new Object[]{location},false);
                    }
                });
        stringRequest.setTag("Address_From_Location");
        DataLoader.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);
    }

    private void showProgress(String text){
        if (text!=null)
            progress_bar_text.setText(text);
        else
            progress_bar_text.setText("Loading...");

        progress_bar_text.setGravity(Gravity.CENTER);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress(){
        progressBar.setVisibility(View.GONE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static Intent makeNotificationMainIntent(Context context, String msg){
        final Intent intent = new Intent(context , MainActivity.class );
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //intent.putExtra( NOTIFICATION_MSG, msg );
        intent.putExtra( "NOTIFICATION MSG", msg );
        return intent;
    }

    public static Intent makeNotificationFeedbackIntent(Context context, String msg) {
        final Intent intent = new Intent( context, MainActivity.class );
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.setPackage(null);
        //intent.putExtra( NOTIFICATION_MSG, msg );
        intent.putExtra( "NOTIFICATION MSG", msg );
        return intent;
    }

    private void createGeofence(LatLng location,String uId){
        CircleOptions circleOptions = new CircleOptions()
                .center( new LatLng(location.latitude, location.longitude) )
                .radius(50)
                .fillColor(Color.argb(50,135,206,235))
                .strokeColor(Color.argb(150,135,206,235))
                .strokeWidth(2);
        circles.add(mGoogleMap.addCircle(circleOptions));

        GeofenceMonitor geofenceSingleton = GeofenceMonitor.getInstance(getActivity().getApplicationContext());
        Location geofence = new Location("");
        geofence.setLatitude(location.latitude);
        geofence.setLongitude(location.longitude);
        geofenceSingleton.addGeofence(geofence, uId);
        geofenceSingleton.startGeofencing();
    }

    private void removeGeofences(){
        if (circles!=null) {
            for(Circle c:circles){
                c.remove();
            }
            circles.clear();
        }

        GeofenceMonitor geofenceSingleton = GeofenceMonitor.getInstance(getActivity().getApplicationContext());
        geofenceSingleton.removeGeofences();
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    private void sendRouteToGoogleApp(LatLng origin,LatLng dest,ArrayList<LatLng> chargers){
        String jsonURL = "https://www.google.com/maps/dir/?api=1&";
        final StringBuffer sBuf = new StringBuffer(jsonURL);
        sBuf.append("origin=");
        sBuf.append(origin.latitude);
        sBuf.append(',');
        sBuf.append(origin.longitude);

        sBuf.append("&destination=");
        sBuf.append(dest.latitude);
        sBuf.append(',');
        sBuf.append(dest.longitude);
        sBuf.append("&travelmode=driving&dir_action=navigate");

        if (chargers != null) {
            for(int i=0;i<chargers.size();i++) {
                if (i==0)
                    sBuf.append("&waypoints=");

                sBuf.append(+chargers.get(i).latitude+","+chargers.get(i).longitude);

                if ((chargers.size()-i)>1)
                    sBuf.append("|");
            }
        }

        GoogleAnalyticsService.getInstance().setAction("Map Functions","Navigate","Navigate to Destination");

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(sBuf.toString()));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);
    }

    private Bitmap getMarkerIcon(String type,String status){
        Bitmap image=null;
        if (type.equals("AC") ){
            switch (status){
                case "Available":
                    image=(resizeMapIcons("l2a",100,120));
                    break;
                case "Busy":
                    image=(resizeMapIcons("l2b",100,120));
                    break;
                case "NA":
                    image=(resizeMapIcons("l2u",100,120));
                    break;
            }
        }
        else {
            switch (status){
                case "Available":
                    image=(resizeMapIcons("dcfa",100,120));
                    break;
                case "Busy":
                    image=(resizeMapIcons("dcfb",100,120));
                    break;
                case "NA":
                    image=(resizeMapIcons("dcfu",100,120));
                    break;
            }
        }
        return image;
    }

    private Bitmap resizeMapIcons(String iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getActivity().getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    private List<List<HashMap<String, String>>> parse(JSONArray jLegs) {

        List<List<HashMap<String, String>>> routes = new ArrayList<>();
        //JSONArray jLegs = null;
        JSONArray jSteps = null;
        JSONObject duration = null;
        JSONObject distance = null;
        List path = new ArrayList<HashMap<String, String>>();

        try {

            /** Traversing all routes
             for (int i = 0; i < jRoutes.length(); i++) {
             jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");*/


            /** Traversing all legs */
            for (int j = 0; j < jLegs.length(); j++) {
                distance = (JSONObject) ((JSONObject) jLegs.get(j)).get("distance");
                duration = (JSONObject) ((JSONObject) jLegs.get(j)).get("duration");
                jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                /** Traversing all steps */
                for (int k = 0; k < jSteps.length(); k++) {
                    String polyline = "";
                    polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                    List<LatLng> list = decodePoly(polyline);

                    /** Traversing all points */
                    for (int l = 0; l < list.size(); l++) {
                        HashMap<String, String> hm = new HashMap<String, String>();
                        hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                        hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
                        path.add(hm);
                    }
                }
                routes.add(path);
            }
            //}
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return routes;
    }

    private void showLegend(){
        fabLegend.hide();
        legendView.setVisibility(View.VISIBLE);
    }

    private void hideLegend(){
        fabLegend.show();
        legendView.setVisibility(View.INVISIBLE);
    }

    private void expandBottomSheet(){
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        int val14 = getMarginInDp(14);
        int val80=getMarginInDp(80);
        int val145=getMarginInDp(145);

        setMargins(fabRoute ,0,0,val14,val80);
        setMargins(fabNav ,0,0,val14,val80);
        setMargins(fabEnd ,0,0,val14,val145);
    }

    private void collapseBottomSheet(){
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        int val8=getMarginInDp(8);
        int val10 = getMarginInDp(10);

        int val70 = getMarginInDp(70);

        setMargins(fabRoute, 0, 0, val8, val10);
        setMargins(fabNav, 0, 0, val8, val10);
        setMargins(fabEnd, 0, 0, val8, val70);
    }

    private void setMargins (View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    private int getMarginInDp(int sizeInDP) {
        int marginInDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, sizeInDP, getResources()
                        .getDisplayMetrics());

        return marginInDp;
    }

    private void trackError(Exception e){
        GoogleAnalyticsService.getInstance().trackException(getActivity().getApplicationContext(),e);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLocationPermission();
    }
}