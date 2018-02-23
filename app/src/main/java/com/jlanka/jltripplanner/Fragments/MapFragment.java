package com.jlanka.jltripplanner.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextClock;
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
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

import com.jlanka.jltripplanner.GeofenceMonitor;
import com.jlanka.jltripplanner.GoogleAnalyticsService;
import com.jlanka.jltripplanner.LocationMonitor;
import com.jlanka.jltripplanner.MQTT.MQTTHelper;
import com.jlanka.jltripplanner.MQTT.MQTTPublisher;
import com.jlanka.jltripplanner.MainActivity;
import com.jlanka.jltripplanner.Model.Charger;
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

@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
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
    String selectedMarker;

    //--------------Thiwanka--------------
    public static boolean routed;
    private ArrayList<Marker> markers;
    private static Marker destinatonMarker;
    private ArrayList<Charger> chargingStations;

    public static Context currentContext;

    View mView;
    double currentLatitude,currentLongitude,mylatitude,mylongitude;
    LatLng latLng,selectedMarker_latLng;
    private long chargingDuration;
    private boolean firstLoad = true;

//    public JSONArray itemArray =new JSONArray();
    public JSONArray stations_object = new JSONArray();
    public static boolean isCharging=false,destinationSet=false;
    private AlertDialog ad;

    JSONObject charger_array;

    String user_mobile,user_pin;
    HashMap<String, String> user;

    View bottomSheet;
    BottomSheetBehavior behavior;

    //------------------------------------Thiwanka----------------------------------------------------------------
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place/autocomplete/json";
    private static final String MAPS_API_BASE= "https://maps.googleapis.com/maps/api/geocode/json";
    private static final String ROUTING_STRING="https://maps.googleapis.com/maps/api/directions/json?";
    private static String charging_session_id;
    private ArrayList<Route> routes;
    private ArrayList<Circle> circles;
    private ArrayList<Polyline> routeLines;
    private ArrayList<Marker> infoMarkers,chargers;

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
    @BindView(R.id.info_button) ImageButton info_button;
    @BindView(R.id.charger_loading_message) TextView chargerLoadingMessage;
    @BindView(R.id.bd_refresh) ImageButton refreshButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        mView=inflater.inflate(R.layout.fragment_map,container,false);
        ButterKnife.bind(this,mView);

        currentContext = getActivity().getApplicationContext();

        getCurrentLocation(true);
        container = (ViewGroup) getActivity().findViewById(R.id.cordinator_layout);


        bottomSheet = mView.findViewById(R.id.design_bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomSheet);
        session = new SessionManager(getActivity());
        user = session.getUserDetails();
        user_mobile = user.get(SessionManager.user_mobile);
        user_pin = user.get(SessionManager.pass_word);
//        getProfileData(user_mobile);
        //getCredit(user_mobile);
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
                int messageMargin=getMarginInDp((int)Math.round(180*slideOffset));
                int bottomMargin=getMarginInDp((int)Math.round(180*slideOffset)+10);
                int endBottomMargin=getMarginInDp((int)Math.round(180*slideOffset)+70);

                setMargins(chargerLoadingMessage,0,0,0,messageMargin);
                setMargins(fabRoute ,0,0,8,bottomMargin);
                setMargins(fabNav ,0,0,8,bottomMargin);
                setMargins(fabEnd ,0,0,8,endBottomMargin);
            }
        });

        //----------------------Thiwanka--------------------------------------
        GoogleAnalyticsService.getInstance().setScreenName(this.getClass().getSimpleName());
        chargers=new ArrayList<>();

        fabLegend.setAlpha(0.75f);
        fabLegend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater li = LayoutInflater.from(getActivity());
                View legend = li.inflate(R.layout.fragment_legend, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(legend);
                AlertDialog dialog = builder.create();
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //style id
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                legend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });

        fabRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentLocation(false);
                if (mLastLocation!=null) {
                    requestRoute();
                }
            }
        });

        fabNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentLocation(false);
                if (mLastLocation != null && routes != null)
                    sendRouteToGoogleApp(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), destinatonMarker.getPosition(), routes.get(0).getChargers());
            }
        });

        fabEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentLocation(false);
                removeDestinationMarker();
                fabEnd.hide();
                fabNav.hide();
                fabRoute.hide();
            }
        });

        fsv.setOnQueryChangeListener(this);
        fsv.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    fsv.hideProgress();
            }
        });
        fsv.setOnSearchListener(this);

        return mView;
    }

    private void requestRoute() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_route, null);
        EditText et = (EditText) view.findViewById(R.id.batt_cap);
        TextView tv = (TextView) view.findViewById(R.id.route_err_message);
        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        android.app.AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (et!=null && et.getText().length()>0){
                        if (Integer.parseInt(et.getText().toString())<101){
                            String vin = new JSONArray(session.getVehicles()).getJSONObject(0).getString("vin");
                            checkRoute(vin, new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), destinatonMarker.getPosition(), "30", et.getText().toString());
                            dialog.dismiss();
                        }
                        else{
                            if (tv!=null) {
                                tv.setText("Range on current charge cannot be greater than 150kms");
                                tv.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    else {
                        if (tv!=null) {
                            tv.setText("Range on current charge required");
                            tv.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (Exception e) {
                    if (tv!=null) {
                        tv.setText("Invalid input");
                        tv.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
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

    @Override
    public void onPause() {
        super.onPause();
        LocationMonitor.instance(getActivity().getApplicationContext()).stop();
    }

    public void getCurrentLocation(boolean zoomToLocation){
        LocationMonitor.instance(getActivity().getApplicationContext()).onChange(new Workable<GPSPoint>() {
            @Override
            public void work(GPSPoint gpsPoint) {
                mLastLocation=new Location("");
                mLastLocation.setLatitude(Double.parseDouble(gpsPoint.getLatitude().toString()));
                mLastLocation.setLongitude(Double.parseDouble(gpsPoint.getLongitude().toString()));

                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
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
            }
        });
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionLayout.setVisibility(View.VISIBLE);
            return false;
        }
        else {
            getCurrentLocation(false);
            permissionLayout.setVisibility(View.GONE);
            return true;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        selectedMarker = marker.getTitle();
        if (marker.getTag().equals("Destination_Marker"))
            marker.showInfoWindow();
        else if (!marker.getTag().equals("Polyline_InfoWindow")) {

            for (Charger c:chargingStations) {
                if (c.getDevice_id().equals(marker.getTag())) {
                    _charger_id.setText(c.getAlias());
                    _charger_id.setTag(c.getDevice_id());

                    if (c.getType().equals("AC"))
                        _charger_type.setText("Standard");
                    else
                        _charger_type.setText("Rapid");

                    _charger_availability.setText(" " + c.getState());
                    _charger_icon.setImageBitmap(getMarkerIcon(c.getType(), c.getState()));

                    float[] distance = new float[2];
                    Location.distanceBetween( marker.getPosition().latitude, marker.getPosition().longitude,
                            mLastLocation.getLatitude(), mLastLocation.getLongitude(), distance);

                    if (c.getState().equals("Available") && distance[0] < 50 ) {
                        _chargenow_btn.setEnabled(true);
                        _chargenow_btn.setAlpha(1f);
                    }
                    else{
                        _chargenow_btn.setEnabled(false);
                        _chargenow_btn.setAlpha(0.75f);
                    }

                    if (isCharging) {
                        setCharging();
                    }

                    info_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setInfoWindow(c);
                        }
                    });

                    if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                        expandBottomSheet();
                    }

                    refreshButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onMarkerClick(marker);
                        }
                    });

                    _chargenow_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            chargeNow(_charger_id.getTag().toString());
                        }
                    });

                    _get_direction.setOnClickListener(new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onClick(View v) {
                            if (mLastLocation!=null)
                                sendRouteToGoogleApp(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()),c.getPosition(),null);
                            else
                                showDialog("Location","Unable to retrieve location","getCurrentLocation",new Object[]{false},false);
                        }
                    });

                    _stop_charge.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            stopCharge(charging_session_id);
                        }
                    });
                    return false;
                }
            }
        }
        return false;
    }

    private void setInfoWindow(Charger charger) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_charger_details, null);
        ImageView icon=(ImageView) view.findViewById(R.id.dcd_icon);
        TextView alias = (TextView) view.findViewById(R.id.dcd_alias);
        TextView address = (TextView) view.findViewById(R.id.dcd_address);
        TextView id = (TextView) view.findViewById(R.id.dcd_id);
        TextView status = (TextView) view.findViewById(R.id.dcd_status);
        TextView type = (TextView) view.findViewById(R.id.dcd_type);
        TextView power = (TextView) view.findViewById(R.id.dcd_power);
        TextView owner = (TextView) view.findViewById(R.id.dcd_owner);
        TextView contact = (TextView) view.findViewById(R.id.dcd_contact);
        TextView price = (TextView) view.findViewById(R.id.dcd_price);
        TextView duration = (TextView) view.findViewById(R.id.dcd_duration);
        TextView cost = (TextView) view.findViewById(R.id.dcd_cost);

        LinearLayout ownerLayout = (LinearLayout) view.findViewById(R.id.dcd_owner_layout);
        LinearLayout contactLayout = (LinearLayout) view.findViewById(R.id.dcd_contact_layout);
        ProgressBar progress = (ProgressBar) view.findViewById(R.id.dcd_progress);

        icon.setImageBitmap(getMarkerIcon(charger.getType(), charger.getState()));
        alias.setText(charger.getAlias());
        address.setText(charger.getLocation());
        id.setText(charger.getDevice_id());
        status.setText(charger.getState());
        type.setText(charger.getType());
        power.setText(Math.round(charger.getPower())+" kW");

        price.setText("Rs."+Math.round(charger.getPrice())+" kWh\u207B\u00B9");

        if (charger.getType().equals("AC")) {
            duration.setText("3½ to 4 hrs");
            cost.setText("Rs.360 - Rs.450");
        }
        else{
            duration.setText("1 hr");
            cost.setText("Rs.750 - Rs.900");
        }

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        android.app.AlertDialog dialog = builder.create();
        dialog.setView(view);
        dialog.show();

        int owner_id=charger.getOwner();

        ServerConnector.getInstance(getActivity()).cancelRequest("GetOwner");
        ServerConnector.getInstance(getActivity()).sendRequest(ServerConnector.SERVER_ADDRESS+"profile/"+owner_id,null,Request.Method.GET,
                new OnResponseListner() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Create a JSON object hierarchy from the results
                            JSONObject responseObject = new JSONObject(response);
                            System.out.println(response);
                            if (responseObject.has("error")) {
                                showDialog("Error", responseObject.getString("error"), "GetOwner",
                                        new Object[]{owner_id}, false);
                            }
                            else {
                                progress.setVisibility(View.GONE);
                                ownerLayout.setVisibility(View.VISIBLE);
                                contactLayout.setVisibility(View.VISIBLE);
                                owner.setText(responseObject.getString("first_name")+" "+responseObject.getString("last_name"));
                                contact.setText("0"+responseObject.getString("contact_number"));

                                hideProgress();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new OnErrorListner() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onError(String error, JSONObject obj) {
                        showDialog("Error", error, "GetOwner",
                                new Object[]{owner_id}, false);
                    }
                },"GetOwner");
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getActivity());
        mGoogleMap=googleMap;

        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnCameraIdleListener(this);
        googleMap.setOnCameraMoveStartedListener(this);
        googleMap.setOnCameraMoveListener(this);
        googleMap.setOnCameraMoveCanceledListener(this);

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        mGoogleMap.setLatLngBoundsForCameraTarget(new LatLngBounds(new LatLng(5.774857, 79.579479),new LatLng(9.876957, 81.767971)));
        mGoogleMap.setMinZoomPreference(7f);
        fabLegend.setVisibility(View.VISIBLE);
        fsv.setVisibility(View.VISIBLE);
        mGoogleMap.setPadding(0,getMarginInDp(55),getMarginInDp(2),0);

        if (checkLocationPermission()) {
            LocationManager locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
            mLastLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            mGoogleMap.setMyLocationEnabled(true);

            if (mLastLocation!=null) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 14.0f));
            }

            if (markers!=null){
                markers=new ArrayList<>();
                addMarkers(chargingStations);
            }
        }

        //---------------Thiwanka-----------------
        if (firstLoad && mLastLocation!=null) {
            firstLoad = false;
        }

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                Log.d("Map","Map clicked");
//                if (behavior.getState()==BottomSheetBehavior.STATE_EXPANDED) {
                    collapseBottomSheet();
//                }
            }
        });

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
        chargerLoadingMessage.setVisibility(View.VISIBLE);
        String charging_address = "charging_stations/";

        ServerConnector.getInstance(getActivity()).cancelRequest("ChargingStations");
        ServerConnector.getInstance(getActivity()).sendRequest(ServerConnector.SERVER_ADDRESS+charging_address,null,Request.Method.GET,
        new OnResponseListner() {
            @Override
            public void onResponse(String response) {
                Log.w("Stations Details  : ", String.valueOf(response));
                try {
                            if(response.equals("[]")){
                                System.out.println("Charger Data : "+response);
                            }else{
                                stationUpdater(new JSONArray(response));
                            }

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            trackError(e);
                            e.printStackTrace();
                        }

            }
        }
        ,new OnErrorListner() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onError(String error, JSONObject obj) {
                System.out.println("SERVER RESPONSE : " + error);
                showDialog("Timeout","Check your internet connection","getResponse",new Double[]{lat,lng},true);
            }
        },"ChargingStations");
    }

    private void addMarkers(ArrayList<Charger> chargersToDraw){
        if (markers==null)
            markers=new ArrayList<>();

        if (chargersToDraw.size()<1)
            chargerLoadingMessage.setVisibility(View.GONE);

        for (Charger c:chargersToDraw) {
            Marker chargerMarker = mGoogleMap.addMarker(c.getMarkerOptions());
            chargerMarker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerIcon(c.getType(), c.getState())));
            chargerMarker.setTag(c.getDevice_id());
            startMqtt(c.getDevice_id());
            markers.add(chargerMarker);
        }
    }

    public void stationUpdater(JSONArray stations){
        Log.e("Station Updater","Started");
        try{
            ArrayList<Charger>chargersToDraw=new ArrayList<>();
            for(int i=0; i < stations.length();i++){
                if (chargingStations==null)
                    chargingStations=new ArrayList<>();

                JSONObject charger = stations.getJSONObject(i);

                boolean found=false;
                for (Charger cc:chargingStations){
                    if (cc.getDevice_id().equals(charger.getString("device_id")))
                        found=true;
                }

                if (!found) {
                    Charger c = new Charger(
                            charger.getInt("id"),
                            charger.getString("device_id"),
                            charger.getString("alias"),
                            charger.getInt("owner"),
                            charger.getString("location"),
                            new LatLng(Double.parseDouble(charger.getString("lat")), Double.parseDouble(charger.getString("lng"))),
                            charger.getString("charger_type"),
                            charger.getDouble("power"),
                            charger.getDouble("unit_price"),
                            charger.getBoolean("availability")
                    );

                    chargingStations.add(c);
                    chargersToDraw.add(c);
                    Log.e("Charger ID", c.getDevice_id());
                }
            }
            addMarkers(chargersToDraw);
        }catch (JSONException e){
            trackError(e);
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void startMqtt(final String charger_id) {
        MQTTHelper mqttHelper2 = new MQTTHelper(getActivity(),"tcp://development.enetlk.com:1887");
        mqttHelper2.subscriptionTopic=("server/"+charger_id.toLowerCase()+"/status");
        mqttHelper2.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
            }

            @Override
            public void connectionLost(Throwable throwable) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) {
                System.out.println(charger_id+","+mqttMessage.toString());
                updateItemArray(charger_id,mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });
    }

    private void updateItemArray(String charger_id, String charger_current_status){

        if (chargingStations!=null) {
            for (Charger c : chargingStations) {
                if (c.getDevice_id().equals(charger_id)) {
                    if (!c.getState().equals(charger_current_status)) {
                        c.setState(charger_current_status);
                        System.out.println(markers.size());
                        for (Marker m : markers) {
                            System.out.println(charger_current_status+"%"+c.getState());
                            if (m.getTag().equals(c.getDevice_id())) {
                                m.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerIcon(c.getType(), c.getState())));
                                chargerLoadingMessage.setVisibility(View.INVISIBLE);

                                if (selectedMarker.equals(c.getDevice_id())) {
                                    _charger_availability.setText(" " + c.getState());
                                    _charger_icon.setImageBitmap(getMarkerIcon(c.getType(), c.getState()));
                                    float[] distance = new float[2];
                                    Location.distanceBetween( c.getPosition().latitude, c.getPosition().longitude,
                                            mLastLocation.getLatitude(), mLastLocation.getLongitude(), distance);

                                    if (c.getState().equals("Available") && distance[0] < 100 ) {
                                        _chargenow_btn.setEnabled(true);
                                        _chargenow_btn.setAlpha(1f);
                                    }

                                }

                                return;
                            }
                        }
                    } else
                        return;
                }
            }
        }
        /*for(int i=0; i < stations_object.length();i++){
            charger_array = stations_object.getJSONObject(i);
            final String chargerid = charger_array.getString("device_id").toLowerCase();
            final String charger_previous_status = charger_array.getString("availability");

            if (chargerid.equals(charger_id) && (charger_previous_status!=charger_current_status)){
                stations_object.getJSONObject(i).put("availability",charger_current_status);

            }
        }*/
    }

    private void addStateSubscriber() {
        mqttHelper= new MQTTHelper(getActivity(),"tcp://development.enetlk.com:1887");
        mqttHelper.subscriptionTopic=("server/"+user_mobile+"/status");
        System.out.println("SUBS TOPIC : " + mqttHelper.subscriptionTopic);
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
            }
            @Override
            public void connectionLost(Throwable throwable) {
            }
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage)  {
                System.out.println("_________Is User : "+mqttMessage.toString());
                if(mqttMessage.toString().equals("charging")){
                    setCharging();
                }else if(mqttMessage.toString().contains("busy") || mqttMessage.toString().contains("Error!")){
                    notifyOnError(mqttMessage.toString().replace("Error! ",""));
                }else if(mqttMessage.toString().equals("not charging")){
                    if (isCharging)
                        getShowReceipt(charging_session_id);
                    setFree();
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });

        MQTTHelper mqttHelper3= new MQTTHelper(getActivity(),"tcp://development.enetlk.com:1887");
        mqttHelper3.subscriptionTopic=("server/"+user_mobile+"/session");
        mqttHelper3.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                charging_session_id=message.toString();

                System.out.println(message.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

    }

    private void setFree() {
        isCharging = false;
        hideProgress();
        if(progressDialog!=null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
//        session.user_charging_station(null);
        _chargenow_btn.setEnabled(true);
        _chargenow_btn.setAlpha(1f);
        _imageView.setVisibility(View.GONE);
        _charging_station.setVisibility(View.GONE);
        _chargenow_btn.setVisibility(View.VISIBLE);
        _get_direction.setVisibility(View.VISIBLE);
        _stop_charge.setVisibility(View.GONE);
    }

    private void notifyOnError(String s) {
        hideProgress();
        if(progressDialog!=null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }

        _chargenow_btn.setEnabled(true);
        _chargenow_btn.setAlpha(1f);
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

        hideProgress();
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

    public void chargeNow(String selectedMarker) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_select_vehicle, null);

        Spinner spinner = (Spinner) view.findViewById(R.id.vehicle_spinner);
        ArrayList<String> ar = new ArrayList<String>();
        ArrayList<String> vIds=new ArrayList<>();
        String reg_num;
        try {

            JSONArray obj = new JSONArray(user.get(SessionManager.electric_vehicles));

            for (int i = 0; i < obj.length(); i++) {
                JSONObject vehicle_object = obj.getJSONObject(i);
                final String model = vehicle_object.getString("model");
                final String reg_no = vehicle_object.getString("reg_no");
                vIds.add(vehicle_object.getString("id"));
                ar.add(reg_no +" "+ model);
            }
        } catch (Throwable t) {
        }

        String[] myVehicle = ar.toArray(new String[0]);
        ArrayAdapter<String> myVehicleGUIArray = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item, myVehicle);
        myVehicleGUIArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        myVehicleGUIArray.notifyDataSetChanged();
        spinner.setAdapter(myVehicleGUIArray);

        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startCharging(selectedMarker,vIds.get(spinner.getSelectedItemPosition()));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        android.app.AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    private void startCharging(String selectedMarker,String vehicle_id){
        showProgress("Starting Charging Session");
        charging_marker = selectedMarker;
        session.user_charging_station(charging_marker);

        Handler h=new Handler();
        h.postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void run() {
                if (isCharging!=true && progressBar.getVisibility()==View.VISIBLE) {
                    hideProgress();
                    showDialog("Connection Error", "Station server connection timeout!", "chargeNow", new Object[]{selectedMarker,vehicle_id}, false);
                }
            }
        },10000);

        JSONObject payload = new JSONObject();
        try {
            payload.put("cusid",user_mobile);
            payload.put("bysms", 0);
            payload.put("message", "ev chg "+ charging_marker +" "+user_pin+" "+vehicle_id);
            chargingDuration=System.currentTimeMillis();
            System.out.println(payload);
            GoogleAnalyticsService.getInstance().setAction("Charging","Starting Charging Session",selectedMarker);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            trackError(e);
            e.printStackTrace();

        }

        System.out.println("CHARGE NOW : " + payload.toString());

        MQTTPublisher mqttPublisher = new MQTTPublisher(getActivity().getApplicationContext(),"tcp://development.enetlk.com:1883");
        mqttPublisher.publishToTopic(payload.toString(),getActivity().getApplicationContext());
    }

    public void stopCharge(String session_id){
        showProgress("Stopping");

        Handler h=new Handler();
        h.postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void run() {
                if (isCharging==true && progressBar.getVisibility()==View.VISIBLE) {
                    hideProgress();
                    showDialog("Connection Error", "Station server connection timeout!", "stopCharge", null, false);
                }
            }
        },5000);

        JSONObject payload = new JSONObject();
        try {
            payload.put("cusid",user_mobile);
            payload.put("bysms", 0);
            payload.put("message", "ev stop "+ charging_marker +" "+user_pin+" "+session_id);
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
        onSearchTextChanged(null,currentQuery);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onSearchTextChanged(String oldQuery,String newQuery) {
        getCurrentLocation(false);
        if (mLastLocation!=null)
            getAddressSuggestions(newQuery);
        else
            showDialog("Location Error","Unable to retrieve current location","currentLocation",null,true);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showDialog(String title, String message, final String failedMethod, final Object[] passedQuery, boolean required) {
        try {
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(getActivity().getWindow().getContext());
                dlgAlert.setMessage(message);
                dlgAlert.setTitle(title);
                String positiveButtonText = "Retry";

                dlgAlert.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (failedMethod) {
                            case "getAddressSuggestions":
                                getAddressSuggestions(passedQuery[0].toString());
                                ad.dismiss();
                                ad=null;
                                break;
                            case "searchLocationByName":
                                searchLocationByName(passedQuery[0].toString());
                                ad.dismiss();
                                ad=null;
                                break;
                            case "setDestinationOnClick":
                                setDestinationOnClick((LatLng) passedQuery[0]);
                                ad.dismiss();
                                ad=null;
                                break;
                            case "checkRoute":
                                checkRoute(passedQuery[0].toString(), (LatLng) passedQuery[1], (LatLng) passedQuery[2],
                                        passedQuery[3].toString(), passedQuery[4].toString());
                                ad.dismiss();
                                ad=null;
                                break;
                            case "route":
                                setDestinationOnClick((LatLng) passedQuery[1]);
                                route((LatLng) passedQuery[0], (LatLng) passedQuery[1], (ArrayList) passedQuery[2]);
                                ad.dismiss();
                                ad=null;
                                break;
                            case "getResponse":
                                getResponse((Double) passedQuery[0], (Double) passedQuery[1]);
                                ad.dismiss();
                                ad=null;
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
                                    ad.dismiss();
                                    ad=null;
                                    break;
                                case "checkRoute":
                                    removeDestinationMarker();
                                    ad.dismiss();
                                    ad=null;
                                    break;
                            }
                        }
                    });
                }

                dlgAlert.setCancelable(!required);
                if (ad==null) {
                    ad = dlgAlert.create();
                    ad.setIcon(R.mipmap.ic_launcher);
                    ad.show();
                    Button b = ad.getButton(DialogInterface.BUTTON_NEGATIVE);
                    if (b != null) {
                        b.setTextColor(Color.BLACK);
                    }
                }
                dlgAlert.setIcon(R.drawable.logo);


        }
        catch(Exception e){
                GoogleAnalyticsService.getInstance().trackException(getActivity().getApplicationContext(), e);
            }
    }

    private void getAddressSuggestions(final String query){
        //Cancel previous requests
        ServerConnector.getInstance(getActivity()).cancelRequest("Address_Suggestion_Requests");

        fsv.showProgress();
        StringBuilder url = new StringBuilder(PLACES_API_BASE);
        try {
            url.append("?input=" + URLEncoder.encode(query, "utf8"));
            url.append("&location=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() );
            url.append("&radius=500&key=" + URLEncoder.encode(MAP_API_KEY, "utf8")  + "&components=country:lk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ServerConnector.getInstance(getActivity()).sendRequest(url.toString(), null, Request.Method.POST,
                new OnResponseListner() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonResults;
                        ArrayList<FoundSuggestion> resultList = null;
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
                                        FoundSuggestion fs = new FoundSuggestion(predsJsonArray.getJSONObject(i).getString("place_id"), predsJsonArray.getJSONObject(i).getString("description").replace(", Sri Lanka", ""));
                                        resultList.add(fs);
                                    }
                                }
                                fsv.hideProgress();
                                fsv.swapSuggestions(resultList);
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                fsv.hideProgress();
                                showDialog("Timeout", "Check Your internet connection...", "getAddressSuggestions", new Object[]{query}, false);
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new OnErrorListner() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onError(String error, JSONObject obj) {
                        fsv.hideProgress();
                        Log.d("Error   ", String.valueOf(error));
                        showDialog("Timeout","Check Your internet connection...","getAddressSuggestions",new Object[]{query},false);
                    }
                },"Address_Suggestion_Requests");
    }

    private void searchLocationByName(final String query){
        showProgress(null);
        removeDestinationMarker();
        removeGeofences();

        //Cancel previous requests
        ServerConnector.getInstance(getActivity()).cancelRequest("Location_Search_Requests");

        StringBuilder url = new StringBuilder(MAPS_API_BASE);
        try {
            url.append("?address=" + URLEncoder.encode(query, "utf8"));
            url.append("&location=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());
            url.append("&radius=500&key=" + URLEncoder.encode(MAP_API_KEY, "utf8") + "&components=country:lk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ServerConnector.getInstance(getActivity().getApplicationContext()).sendRequest(url.toString(), null, Request.Method.POST,
            new OnResponseListner() {
                @Override
                public void onResponse(String response) {
                    if (response.equals("[]")) {

                    } else {
                        try {
                            // Create a JSON object hierarchy from the results
                            JSONObject jsonObj = new JSONObject(response);
                            JSONArray geoResJsonArray = jsonObj.getJSONArray("results");
                            // Extract the LatLng from the results
                            if (geoResJsonArray != null) {
                                String address = geoResJsonArray.getJSONObject(0).getString("formatted_address").replace(", Sri Lanka", "");
                                JSONObject geo = geoResJsonArray.getJSONObject(0).getJSONObject("geometry");
                                JSONObject LatLngObj = geo.getJSONObject("location");
                                Double lat = LatLngObj.getDouble("lat");
                                Double lng = LatLngObj.getDouble("lng");
                                addDestinationMarker(new LatLng(Double.parseDouble(lat.toString()), Double.parseDouble(lng.toString())), address);
                                showDestinationSet();
                                zoomToLocation(destinatonMarker.getPosition(), 17, 0);
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
            new OnErrorListner() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onError(String error, JSONObject obj) {
                    hideProgress();
                    Log.d("Error   ", String.valueOf(error));
                    showDialog("Timeout","Check Your internet connection..","searchLocationByName",new Object[]{query},false);
                }
            },"Location_Search_Requests");
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

    private void addDestinationMarker(LatLng location,String name){
        routed=true;
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

        System.out.println(vin+","+start+","+end+","+capacity+","+perc);

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

        ServerConnector.getInstance(getActivity()).cancelRequest("CheckRoute");
        ServerConnector.getInstance(getActivity()).sendRequest(ServerConnector.SERVER_ADDRESS+"feasible_directions",params,Request.Method.POST,
        new OnResponseListner() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onResponse(String response) {
                try {
                    if (response.equals("[]")) {

                    }
                    else {
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
        },
        new OnErrorListner() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onError(String error, JSONObject obj) {
                System.out.println(obj);
                showDialog("Routing Error", error, "checkRoute",
                        new Object[]{vin, start, end, capacity, perc}, false);
            }
        },"CheckRoute");
    }

    private void route(final LatLng start, final LatLng end, final ArrayList<LatLng> chargerLocations){
        //Cancel previous requests
        ServerConnector.getInstance(getActivity()).cancelRequest("Route");
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
        ServerConnector.getInstance(getActivity()).sendRequest(url, null, Request.Method.POST,
                new OnResponseListner() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("[]")) {
                            System.out.println("ERROR");
                        } else {
                            try {
                                JSONArray jsonLegs;
                                routes = new ArrayList<>();
                                routeLines = new ArrayList<>();
                                circles = new ArrayList<>();
                                // Create a JSON object hierarchy from the results
                                JSONObject responseObject = new JSONObject(response);
                                JSONArray routesArray = responseObject.getJSONArray("routes");
                                for (int i = 0; i < routesArray.length(); i++) {
                                    jsonLegs = routesArray.getJSONObject(i).getJSONArray("legs");
                                    Route route = new Route(start, end, jsonLegs, chargerLocations);
                                    routes.add(route);
                                }

                                infoMarkers = new ArrayList<>();
                                final LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                builder.include(start);
                                getActivity().runOnUiThread(
                                        new Runnable() {
                                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                            @Override
                                            public void run() {
                                                int size = routes.size();
                                                if (size > 2)
                                                    size = 2;

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
                                    for (LatLng charger : chargerLocations)
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
                new OnErrorListner() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onError(String error, JSONObject obj) {
                        hideProgress();
                        System.out.println(error);
                        showDialog("Timeout","Check Your internet connection...","route",null,false);
                    }
                },"Route");
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
        ServerConnector.getInstance(getActivity()).cancelRequest("Address_From_Location");

        StringBuilder url = new StringBuilder(MAPS_API_BASE);
        try {
            url.append("?latlng=" + URLEncoder.encode(location.latitude + "," + location.longitude, "utf8"));
            url.append("&key=" + URLEncoder.encode(MAP_API_KEY, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ServerConnector.getInstance(getActivity()).sendRequest(url.toString(), null, Request.Method.POST,
                new OnResponseListner() {
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
                                if (goeResJsonArray != null && goeResJsonArray.length() > 0) {
                                    String address = goeResJsonArray.getJSONObject(0).getString("formatted_address").replace(", Sri Lanka", "");

                                    addDestinationMarker(location, address);
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
                                showDialog("Timeout", "Check Your internet connection...", "setDestinationOnClick", new Object[]{location}, false);
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new OnErrorListner() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onError(String error, JSONObject obj) {
                        hideProgress();
                        removeDestinationMarker();
                        showDialog("Timeout","Check Your internet connection...","setDestinationOnClick",new Object[]{location},false);
                    }
                },"Address_From_Location");
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
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra( "NOTIFICATION MSG", msg );
        return intent;
    }

    public static Intent makeNotificationFeedbackIntent(Context context, String msg) {
        final Intent intent = new Intent( context, MainActivity.class );
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
                case "Pending...":
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
                case "Pending...":
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

    private void expandBottomSheet(){
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        int leftMargin = getMarginInDp(8);
        int bottomMargin=getMarginInDp(180);
        int endBottomMargin=getMarginInDp(250);

        setMargins(fabRoute ,0,0,leftMargin,bottomMargin);
        setMargins(fabNav ,0,0,leftMargin,bottomMargin);
        setMargins(fabEnd ,0,0,leftMargin,endBottomMargin);
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
        collapseBottomSheet();
        checkLocationPermission();
        getCurrentLocation(true);
    }

    private void getShowReceipt(String charging_session_id){
        ServerConnector.getInstance(getActivity()).cancelRequest("GetReceipt");
        ServerConnector.getInstance(getActivity()).sendRequest(ServerConnector.SERVER_ADDRESS+"charging_sessions/"+charging_session_id,null,Request.Method.GET,
                new OnResponseListner() {
                    @TargetApi(Build.VERSION_CODES.O)
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response.equals("[]")) {
                            }
                            else {
                                System.out.println(response);
                                JSONObject obj=new JSONObject(response);

                                if (obj.getString("status").equals("DO")) {

                                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
                                    LayoutInflater inflater = getActivity().getLayoutInflater();
                                    View view = inflater.inflate(R.layout.dialog_receipt, null);
                                    TextView ch_id = view.findViewById(R.id.rec_charger_id);
                                    TextView ch_type = view.findViewById(R.id.rec_charger_type);
                                    TextView ch_power = view.findViewById(R.id.rec_charger_power);
                                    TextView rec_veh = view.findViewById(R.id.rec_veh);
                                    TextView rec_date = view.findViewById(R.id.rec_date);
                                    TextView rec_start = view.findViewById(R.id.rec_start);
                                    TextView rec_stop = view.findViewById(R.id.rec_stop);
                                    TextView rec_dur = view.findViewById(R.id.rec_dur);
                                    TextView rec_total = view.findViewById(R.id.rec_total);

                                    JSONArray vehicles = new JSONArray(session.getVehicles());
                                    String vehicle = "";
                                    for (int i = 0; i < vehicles.length(); i++) {
                                        JSONObject v = vehicles.getJSONObject(i);
                                        if (v.getString("id").equals(obj.getString("electric_vehicle"))) {
                                            vehicle=v.getString("reg_no") + " - " + v.getString("model");
                                        }
                                    }
                                    rec_veh.setText(vehicle);

                                    Charger ch=null;
                                    for (Charger c : chargingStations) {
                                        if (c.getId() == obj.getInt("charging_station")) {
                                            ch=c;
                                        }
                                    }
                                    ch_id.setText(ch.getDevice_id());
                                    ch_type.setText(ch.getType());
                                    ch_power.setText(ch.getPower() + " kWh");
                                    rec_total.setText("Rs."+Math.round(obj.getDouble("cost")));

                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                                    Date startDate = simpleDateFormat.parse(obj.getString("start_datetime"));

                                    SimpleDateFormat dateFormat=new SimpleDateFormat("dd-MM-yyyy");
                                    SimpleDateFormat timeFormat=new SimpleDateFormat("hh:mm:ss");

                                    rec_date.setText(dateFormat.format(startDate));
                                    rec_start.setText(timeFormat.format(startDate));

                                    Date stopDate = simpleDateFormat.parse(obj.getString("end_datetime"));

                                    rec_stop.setText(timeFormat.format(stopDate));

                                    int dur = obj.getInt("duration");
                                    if (dur > 59) {
                                        int hours = dur / 60; //since both are ints, you get an int
                                        int minutes = dur % 60;

                                        if (hours > 1)
                                            rec_dur.setText(hours + " hrs" + minutes + " mins");
                                        else
                                            rec_dur.setText(hours + " hr" + minutes + " mins");
                                    } else
                                        rec_dur.setText(dur + " mins");


                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    builder.setView(view);
                                    android.app.AlertDialog dialog = builder.create();
                                    dialog.show();
                                    takeScreenshot();
                                    clearSessionId();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new OnErrorListner() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onError(String error, JSONObject obj) {
                        System.out.println(obj);
                        showDialog("Routing Error", error, "checkRoute",
                                new Object[]{charging_session_id}, true);
                    }
                },"GetReceipt");
    }

    private void clearSessionId(){
        charging_session_id=null;
    }

    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = getActivity().getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            //openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }
}