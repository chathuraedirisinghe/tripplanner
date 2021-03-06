package com.jlanka.tripplanner.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

import com.jlanka.tripplanner.GeofenceMonitor;
import com.jlanka.tripplanner.GoogleAnalyticsService;
import com.jlanka.tripplanner.Helpers.RouteParseHelper;
import com.jlanka.tripplanner.LocationMonitor;
import com.jlanka.tripplanner.MQTT.MQTTHelper;
import com.jlanka.tripplanner.MQTT.MQTTPublisher;
import com.jlanka.tripplanner.MainActivity;
import com.jlanka.tripplanner.Model.Charger;
import com.jlanka.tripplanner.Model.FoundSuggestion;
import com.jlanka.tripplanner.Model.GPSPoint;
import com.jlanka.tripplanner.Model.Route;
import com.jlanka.tripplanner.Model.Workable;
import com.jlanka.tripplanner.R;
import com.jlanka.tripplanner.Server.OnErrorListner;
import com.jlanka.tripplanner.Server.OnResponseListner;
import com.jlanka.tripplanner.Server.ServerConnector;
import com.jlanka.tripplanner.UI.ChargerDetailsDialog;
import com.jlanka.tripplanner.UI.LegendDialog;
import com.jlanka.tripplanner.UI.PlugInDialog;
import com.jlanka.tripplanner.UI.ReceiptDialog;
import com.jlanka.tripplanner.UI.RouteDialog;
import com.jlanka.tripplanner.UI.SelectVehicleDialog;
import com.jlanka.tripplanner.Helpers.UIHelper;
import com.jlanka.tripplanner.UserActivity.SessionManager;

import pl.droidsonroids.gif.GifImageView;

@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class MapFragment extends Fragment implements
        OnMapReadyCallback,
        FloatingSearchView.OnSearchListener,
        FloatingSearchView.OnQueryChangeListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnCameraIdleListener {

    MQTTHelper mqttHelper;
    SessionManager session;

    GoogleMap mGoogleMap;
    MapView mMapView;
    Location mLastLocation;
    Marker mCurrLocationMarker, chargingStation;
    String selectedMarker;

    //--------------Thiwanka--------------
    public static boolean routed;
    private HashMap<String, Marker> markers;
    private static Marker destinatonMarker;
    private HashMap<String, Charger> chargingStations;

    View mView;
    double currentLatitude, currentLongitude, mylatitude, mylongitude;
    LatLng latLng;
    private long chargingDuration;
    private boolean firstLoad = true;
    public static boolean isCharging = false, destinationSet = false;
    private AlertDialog ad;
    ProgressBar app_progress;

    String user_mobile, user_pin;
    HashMap<String, String> user, chargingSessions = new HashMap();

    View bottomSheet;
    BottomSheetBehavior behavior;

    //------------------------------------Thiwanka----------------------------------------------------------------
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place/autocomplete/json";
    private static final String MAPS_API_BASE = "https://maps.googleapis.com/maps/api/geocode/json";
    private static final String ROUTING_STRING = "https://maps.googleapis.com/maps/api/directions/json?";
    private static String charging_session_id;
    private ArrayList<Route> routes;
    private ArrayList<Circle> circles;
    private ArrayList<Polyline> routeLines;
    private ArrayList<Marker> infoMarkers, chargers;
    private ReceiptDialog rd;

    //------------------------------------------------------------------------------------------------------------
    ProgressDialog progressDialog;

    @BindView(R.id.btnLL)LinearLayout btn_layout;
    @BindView(R.id.stop)LinearLayout stop_layout;
    @BindView(R.id.details_progress_layout) LinearLayout details_progress_layout;
    @BindView(R.id.details_layout) LinearLayout details_layout;
    @BindView(R.id.soc_perc)ProgressBar soc_perc;
    @BindView(R.id.soc_val)TextView soc_val;
    @BindView(R.id.till_80)TextView till_80;
    @BindView(R.id.till_100)TextView till_100;
    @BindView(R.id.units)TextView units;
    @BindView(R.id.tot_price)TextView tot_price;

    @BindView(R.id.charger_id)TextView _charger_id;
    @BindView(R.id.charger_availability)TextView _charger_availability;
    @BindView(R.id.charger_type) TextView _charger_type;
    @BindView(R.id.charging_station_name) TextView _charging_station;
    @BindView(R.id.charge_now_btn) Button _chargenow_btn;
    @BindView(R.id.get_direction) Button _get_direction;
    @BindView(R.id.stop_now_btn) Button _stop_charge;
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
    @BindView(R.id.bd_refresh) ImageButton refreshButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mView = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, mView);
        container = (ViewGroup) getActivity().findViewById(R.id.cordinator_layout);
        app_progress=getActivity().findViewById(R.id.app_bar_progress);

        bottomSheet = mView.findViewById(R.id.design_bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomSheet);
        session = new SessionManager(getActivity());
        user = session.getUserDetails();
        user_mobile = user.get(SessionManager.user_mobile);
        user_pin = user.get(SessionManager.pass_word);

        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        setMargins(1.0f);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        setMargins(0.0f);
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                setMargins(slideOffset);
            }
        });

        //----------------------Thiwanka--------------------------------------
        GoogleAnalyticsService.getInstance().setScreenName(this.getClass().getSimpleName());
        chargers = new ArrayList<>();

        fabLegend.setAlpha(0.75f);
        fabLegend.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                LegendDialog ld = new LegendDialog();
                ld.show(getFragmentManager(), "Legend");
            }
        });

        fabRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentLocation(false);
                if (mLastLocation != null) {
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

        mMapView = mView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
        return mView;
    }

    private void setMargins(float slideOffset){
        UIHelper helper = UIHelper.getInstance(getActivity());

        int max =  helper.pxToDp(bottomSheet.getHeight())-20;

        int messageMargin = helper.getMarginInDp((int) Math.round(max * slideOffset));
        int bottomMargin = helper.getMarginInDp((int) Math.round(max * slideOffset) + 10);
        int endBottomMargin = helper.getMarginInDp((int) Math.round(max * slideOffset) + 70);

        helper.setMargins(fabRoute, 0, 0, 8, bottomMargin);
        helper.setMargins(fabNav, 0, 0, 8, bottomMargin);
        helper.setMargins(fabEnd, 0, 0, 8, endBottomMargin);
    }

    private void requestRoute() {
        RouteDialog rd = new RouteDialog();
        rd.init(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String vin = null;
                try {
                    if (rd.isValid()) {
                        vin = new JSONArray(session.getVehicles()).getJSONObject(0).getString("vin");
                        checkRoute(vin, new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), destinatonMarker.getPosition(), "30", rd.getRange());
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getActivity(), "Invalid range entered", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    System.out.println(e.getMessage());
                }
            }
        });
        rd.show(getFragmentManager(), "Route");
    }

    public void getCurrentLocation(boolean zoomToLocation) {
        LocationMonitor.instance(getActivity()).onChange(new Workable<GPSPoint>() {
            @Override
            public void work(GPSPoint gpsPoint) {
                mLastLocation = new Location("");
                mLastLocation.setLatitude(Double.parseDouble(gpsPoint.getLatitude().toString()));
                mLastLocation.setLongitude(Double.parseDouble(gpsPoint.getLongitude().toString()));

                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }

                if (zoomToLocation == true)
                    zoomToLocation(gpsPoint.getLatLng(), 15, 0);

                latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                if ((Math.abs(currentLatitude - mLastLocation.getLatitude()) > 0.001) && (Math.abs(currentLongitude - mLastLocation.getLongitude()) > 0.0001)) {
                    currentLatitude = Math.floor(mLastLocation.getLatitude() * 100) / 100;
                    currentLongitude = Math.floor(mLastLocation.getLongitude() * 10000) / 10000;
                    getResponse(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                } else {
                }
                LocationMonitor.instance(getActivity()).stop();
            }
        });
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionLayout.setVisibility(View.VISIBLE);
            return false;
        } else {
            permissionLayout.setVisibility(View.GONE);
            return true;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        selectedMarker = marker.getTitle();
        zoomToLocation(marker.getPosition(), 15, 0);
        if (chargingStation != null && isCharging && marker != chargingStation) {
            setCharging();
            onMarkerClick(chargingStation);
            return true;
        }

        if (marker.getTag().equals("Destination_Marker"))
            marker.showInfoWindow();
        else if (!marker.getTag().equals("Polyline_InfoWindow")) {
            updateBottomSheet(chargingStations.get(marker.getTag()));
        }

        chargingStation = marker;
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMarkerClick(marker);
            }
        });

        _chargenow_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                float[] distance = new float[2];
                if (mLastLocation != null) {
                    Location.distanceBetween(marker.getPosition().latitude, marker.getPosition().longitude,
                            mLastLocation.getLatitude(), mLastLocation.getLongitude(), distance);
                    if (distance[0] > 200)
                        notifyOnError("You need to be within 200m of the charging station");
                    else {
                        _chargenow_btn.setEnabled(false);
                        Handler h = new Handler();
                        h.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                _chargenow_btn.setEnabled(true);
                            }
                        },2000);
                        chargeNow(_charger_id.getTag().toString());
                    }
                } else {
                    notifyOnError("Unable to retrieve current location");
                    getCurrentLocation(true);
                }
            }
        });
        return false;
    }

    private void getChargerInfoWindow(Charger charger) {
        ChargerDetailsDialog cdd = new ChargerDetailsDialog();
        cdd.init(charger, new OnErrorListner() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onError(String error, JSONObject obj) {
                cdd.dismiss();
                showDialog("Unable retrieve data", error, "getChargerInfoWindow", new Object[]{charger}, false);
            }
        });
        cdd.show(getFragmentManager(), "Charger_Details_Dialog");
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnCameraIdleListener(this);
        googleMap.setOnCameraMoveStartedListener(this);
        googleMap.setOnCameraMoveListener(this);
        googleMap.setOnCameraMoveCanceledListener(this);


        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        mGoogleMap.setLatLngBoundsForCameraTarget(new LatLngBounds(new LatLng(5.774857, 79.579479), new LatLng(9.876957, 81.767971)));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(7.3371683, 80.8390386), 7f));
        mGoogleMap.setMinZoomPreference(7f);
        fabLegend.setVisibility(View.VISIBLE);
        fsv.setVisibility(View.VISIBLE);

        UIHelper helper = UIHelper.getInstance(getActivity());
        mGoogleMap.setPadding(0, helper.getMarginInDp(55), helper.getMarginInDp(2), 0);

        if (checkLocationPermission()) {
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            mLastLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            mGoogleMap.setMyLocationEnabled(true);
            getCurrentLocation(true);
        }

        if (mLastLocation != null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 14.0f));
        }

        if (markers != null) {
            markers = new HashMap<>();
            addMarkers(chargingStations);
        }

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                if (!isCharging)
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                else
                    return;
            }
        });

        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (!isCharging)
                    setDestinationOnClick(latLng);
                else
                    return;
            }
        });

    }

    @Override
    public void onCameraIdle() {
        double nextlatitude = mGoogleMap.getProjection().getVisibleRegion().latLngBounds.getCenter().latitude;
        double nextlongitude = mGoogleMap.getProjection().getVisibleRegion().latLngBounds.getCenter().longitude;

        Location my = new Location("point A");

        my.setLatitude(mylatitude);
        my.setLongitude(mylongitude);

        Location remote = new Location("point B");

        remote.setLatitude(nextlatitude);
        remote.setLongitude(nextlongitude);

        float distance = my.distanceTo(remote);

        if (distance > 5000.0) {
            getResponse(nextlatitude, nextlongitude);
        } else {
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

    public void getResponse(final double lat, final double lng) {

        app_progress.setVisibility(View.VISIBLE);
        String charging_address = "charging_stations/";

        ServerConnector.getInstance(getActivity()).cancelRequest("ChargingStations");
        ServerConnector.getInstance(getActivity()).getRequest(ServerConnector.SERVER_ADDRESS + charging_address,
                new OnResponseListner() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response.equals("[]")) {
                            } else {
                                stationUpdater(new JSONArray(response));
                            }

                        } catch (JSONException e) {
                            trackError(e);
                            e.printStackTrace();
                        }

                    }
                }
                , new OnErrorListner() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onError(String error, JSONObject obj) {
                        showDialog("Timeout", "Check your internet connection", "getResponse", new Double[]{lat, lng}, true);
                    }
                }, "ChargingStations");

    }

    private void addMarkers(HashMap<String, Charger> chargersToDraw) {
        if (markers == null)
            markers = new HashMap<>();

        if (chargersToDraw.size() < 1) {
            app_progress.setVisibility(View.GONE);
        }

        for (String key : chargersToDraw.keySet()) {        //loop 5-6 - constant time
            Charger c = chargersToDraw.get(key);
            Marker chargerMarker = mGoogleMap.addMarker(c.getMarkerOptions());
            chargerMarker.setIcon(BitmapDescriptorFactory.fromBitmap(UIHelper.getInstance(getActivity()).getMarkerIcon(c.getType(), c.getState())));
            chargerMarker.setTag(c.getDevice_id());
            addStatusListener(c.getDevice_id());
            markers.put(c.getDevice_id(), chargerMarker);
        }

        if (firstLoad) {
            addSessionListener();
            firstLoad = false;
        }
    }

    private void updateBottomSheet(Charger c) {
        _charger_id.setText(c.getAlias());
        _charger_id.setTag(c.getDevice_id());

        if (c.getType().equals("AC"))
            _charger_type.setText("Standard");
        else
            _charger_type.setText("Rapid");

        _charger_availability.setText(" " + c.getState());
        _charger_icon.setImageBitmap(UIHelper.getInstance(getActivity()).getMarkerIcon(c.getType(), c.getState()));

        if (c.getState().equals("Available")) {
            _chargenow_btn.setEnabled(true);
            _chargenow_btn.setAlpha(1f);
        } else {
            _chargenow_btn.setEnabled(false);
            _chargenow_btn.setAlpha(0.75f);
        }

        info_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getChargerInfoWindow(c);
            }
        });

        if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        _get_direction.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (mLastLocation != null)
                    sendRouteToGoogleApp(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), c.getPosition(), null);
                else
                    showDialog("Location", "Unable to retrieve location", "getCurrentLocation", new Object[]{false}, false);
            }
        });

        _stop_charge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _stop_charge.setEnabled(false);
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        _stop_charge.setEnabled(true);
                    }
                },2000);
                stopCharge(c.getDevice_id());
            }
        });
    }

    public void stationUpdater(JSONArray stations) {
        try {
            HashMap<String, Charger> chargersToDraw = new HashMap<>();
            for (int i = 0; i < stations.length(); i++) {        //loop 5-6 - nested loop

                if (chargingStations == null)
                    chargingStations = new HashMap<>();

                JSONObject charger = stations.getJSONObject(i);

                boolean found = chargingStations.containsKey(charger.getString("device_id"));

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

                    chargingStations.put(charger.getString("device_id"), c);
                    chargersToDraw.put(charger.getString("device_id"), c);
                }
            }
            addMarkers(chargersToDraw);
        } catch (JSONException e) {
            trackError(e);
            e.printStackTrace();
        }
    }

    private void updateItemArray(String charger_id, String charger_current_status) {
        if (chargingStations != null) {
            Charger c = chargingStations.get(charger_id);
            if (c != null && !c.getState().equals(charger_current_status)) {
                c.setState(charger_current_status);
                Marker m = markers.get(charger_id);

                if (m != null) {
                    m.setIcon(BitmapDescriptorFactory.fromBitmap(UIHelper.getInstance(getActivity()).getMarkerIcon(c.getType(), c.getState())));
                    app_progress.setVisibility(View.GONE);

                    if (selectedMarker != null && selectedMarker.equals(c.getDevice_id())) {
                        _charger_availability.setText(" " + c.getState());
                        _charger_icon.setImageBitmap(UIHelper.getInstance(getActivity()).getMarkerIcon(c.getType(), c.getState()));
                        updateBottomSheet(c);
                    }
                }
            }
        }
    }

    private void setFree() {
        isCharging = false;
        hideProgress();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        _chargenow_btn.setEnabled(true);
        _chargenow_btn.setAlpha(1f);
        _charging_station.setVisibility(View.GONE);
        btn_layout.setVisibility(View.VISIBLE);
        stop_layout.setVisibility(View.GONE);
        setMargins(0.0f);
    }

    private void notifyOnError(String s) {
        hideProgress();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        _chargenow_btn.setEnabled(true);
        _chargenow_btn.setAlpha(1f);
        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
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
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        btn_layout.setVisibility(View.GONE);
        stop_layout.setVisibility(View.VISIBLE);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        setMargins(1.0f);
    }

    public void chargeNow(String selectedMarker) {
        SelectVehicleDialog scd = new SelectVehicleDialog();
        scd.init(selectedMarker, session.getVehicles(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!scd.getSelectedVID().equals("")) {
                    Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(1000);
                    showProgress("Charger Initialising");

                    Map<String, String> params = new HashMap<>();
                    // the POST parameters:
                    params.put("charging_station_id", selectedMarker);
                    params.put("electric_vehicle_id", scd.getSelectedVID());

                    ServerConnector.getInstance(getActivity()).cancelRequest("Initialise_Charger");
                    ServerConnector.getInstance(getActivity()).sendRequest(ServerConnector.SERVER_ADDRESS + "charging_stations/init_charge/", params, Request.Method.POST,
                            new OnResponseListner() {
                                @RequiresApi(api = Build.VERSION_CODES.M)
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        hideProgress();
                                        String status = new JSONObject(response).getString("status");

                                        switch (status) {
                                            case "Charging Initialized":
                                                chargingSessions.put(selectedMarker, scd.getSelectedVID());
                                                break;
                                            default:
                                                showDialog(status, "Please try again later", "chargerNow", new Object[]{selectedMarker}, false);
                                                break;
                                        }
                                    } catch (Exception e) {
                                        System.out.println(e.getMessage());
                                    }
                                }
                            },
                            new OnErrorListner() {
                                @RequiresApi(api = Build.VERSION_CODES.M)
                                @Override
                                public void onError(String error, JSONObject obj) {
                                    hideProgress();
                                    showDialog(error, "Please try again later", "chargeNow", new Object[]{selectedMarker}, false);
                                }
                            }, "Initialise_Charger");
                } else {
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    Toast.makeText(getActivity(), "No vehicles found, please add a vehicle !", Toast.LENGTH_LONG).show();
                }
            }
        });
        scd.show(getFragmentManager(), "Select_Vehicle_Dialog");
    }

    public void stopCharge(String station_id) {
        showProgress("Ending charging session");

        Map<String, String> params = new HashMap<>();
        params.put("charging_station_id", station_id);
        params.put("electric_vehicle_id", chargingSessions.get(station_id));

        ServerConnector.getInstance(getActivity()).cancelRequest("Stop_Charger");
        ServerConnector.getInstance(getActivity()).sendRequest(ServerConnector.SERVER_ADDRESS + "charging_stations/stop_charge/", params, Request.Method.POST,
                new OnResponseListner() {
                    @Override
                    public void onResponse(String response) {
                        hideProgress();
                    }
                },
                new OnErrorListner() {
                    @Override
                    public void onError(String error, JSONObject obj) {
                        hideProgress();
                    }
                }, "Stop_Charger");
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
        onSearchTextChanged(null, currentQuery);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onSearchTextChanged(String oldQuery, String newQuery) {
        getCurrentLocation(false);
        if (mLastLocation != null)
            getAddressSuggestions(newQuery);
        else if (isCharging) {
            Toast.makeText(getActivity(), "Charging currently in progress", Toast.LENGTH_SHORT).show();
        } else
            showDialog("Location Error", "Unable to retrieve current location", "currentLocation", null, true);

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
                    if (failedMethod != null) {
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
                            case "generateReceipt":
                                generateReceipt((String) passedQuery[0], (String) passedQuery[1]);
                                break;
                            case "getChargerInfoWindow":
                                getChargerInfoWindow((Charger) passedQuery[0]);
                                break;
                            case "chargeNow":
                                chargeNow(passedQuery[0].toString());
                                break;
                        }
                    }
                    ad.dismiss();
                    ad = null;
                }
            });

            if (!required) {
                dlgAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (failedMethod != null) {
                            switch (failedMethod) {
                                case "route":
                                    removeDestinationMarker();
                                    break;
                                case "checkRoute":
                                    removeDestinationMarker();
                                    break;
                            }
                            ad.dismiss();
                            ad = null;
                        }
                    }
                });
            }

            dlgAlert.setCancelable(!required);
            if (ad == null) {
                ad = dlgAlert.create();
                ad.setIcon(R.mipmap.ic_launcher);
                ad.show();
                Button b = ad.getButton(DialogInterface.BUTTON_NEGATIVE);
                if (b != null) {
                    b.setTextColor(Color.BLACK);
                }
            }
            dlgAlert.setIcon(R.drawable.logo);


        } catch (Exception e) {
            GoogleAnalyticsService.getInstance().trackException(getActivity(), e);
        }
    }

    private void getAddressSuggestions(final String query) {
        //Cancel previous requests
        ServerConnector.getInstance(getActivity()).cancelRequest("Address_Suggestion_Requests");

        fsv.showProgress();
        StringBuilder url = new StringBuilder(PLACES_API_BASE);
        try {
            url.append("?input=" + URLEncoder.encode(query, "utf8"));
            url.append("&location=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());
            url.append("&radius=500&key=" + URLEncoder.encode(MAP_API_KEY, "utf8") + "&components=country:lk");
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
                                jsonResults = new JSONObject(response);
                                // Create a JSON object hierarchy from the results
                                JSONObject jsonObj = new JSONObject(jsonResults.toString());
                                JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

                                // Extract the Place descriptions from the results
                                if (predsJsonArray != null) {
                                    resultList = new ArrayList<FoundSuggestion>(predsJsonArray.length());
                                    for (int i = 0; i < predsJsonArray.length(); i++) {     //loop 5-10 contant time
                                        FoundSuggestion fs = new FoundSuggestion(predsJsonArray.getJSONObject(i).getString("place_id"), predsJsonArray.getJSONObject(i).getString("description").replace(", Sri Lanka", ""));
                                        resultList.add(fs);
                                    }
                                }
                                fsv.hideProgress();
                                fsv.swapSuggestions(resultList);
                            } catch (JSONException e) {
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
                        showDialog("Timeout", "Check Your internet connection...", "getAddressSuggestions", new Object[]{query}, false);
                    }
                }, "Address_Suggestion_Requests");
    }

    private void searchLocationByName(final String query) {
        showProgress(null);
        removeDestinationMarker();
        removeGeofences();

        OnResponseListner orl = new OnResponseListner() {
            @Override
            public void onResponse(String response) {

            }
        };


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

        ServerConnector.getInstance(getActivity()).sendRequest(url.toString(), null, Request.Method.POST,
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
                        showDialog("Timeout", "Check Your internet connection..", "searchLocationByName", new Object[]{query}, false);
                    }
                }, "Location_Search_Requests");
    }

    private void zoomToLocation(LatLng location, int zoom, float bearing) {
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 2000));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)      // Sets the center of the map to location user
                .zoom(zoom)                   // Sets the zoom
                .bearing(bearing)                // Sets the orientation of the camera to east
                .tilt(20)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder

        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void addDestinationMarker(LatLng location, String name) {
        routed = true;
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        destinationSet = true;
        destinatonMarker = mGoogleMap.addMarker(new MarkerOptions()
                .title("Destination")
                .snippet(name)
                .position(location));

        destinatonMarker.setTag("Destination_Marker");
        destinatonMarker.showInfoWindow();
    }

    private void showDestinationSet() {
        Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.cordinator_layout), "Destination Set", Snackbar.LENGTH_LONG);
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

    private void checkRoute(final String vin, final LatLng start, final LatLng end, final String capacity, final String perc) {
        showProgress("Getting directions...");
        GoogleAnalyticsService.getInstance().setAction("Map Functions", "Routing", "Routing Request");

        final String origin = String.valueOf(start.latitude) + "," + String.valueOf(start.longitude);
        final String destination = String.valueOf(end.latitude) + "," + String.valueOf(end.longitude);

        Map<String, String> params = new HashMap<>();
        // the POST parameters:
        params.put("vin", vin);
        params.put("origin", origin);
        params.put("destination", destination);
        params.put("battery_capacity", capacity);
        params.put("battery_remaining", perc);
        //params.put("electric_vehicles","[]");

        ServerConnector.getInstance(getActivity()).cancelRequest("CheckRoute");
        ServerConnector.getInstance(getActivity()).sendRequest(ServerConnector.SERVER_ADDRESS + "feasible_directions", params, Request.Method.POST,
                new OnResponseListner() {
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

                                    for (int i = 0; i < chargers.length(); i++) {       //loop 1-2 - nested loop
                                        String chargerLocation = chargers.get(i).toString();
                                        if (chargerLocation != null && !chargerLocation.equals("[]")) {
                                            String latLng[] = chargerLocation.split(",");

                                            chargerLocations.add(new LatLng(Double.parseDouble(latLng[0].replaceAll("[^0-9.]", "")),
                                                    Double.parseDouble(latLng[1].replaceAll("[^0-9.]", ""))));
                                        }
                                    }

                                    route(start, end, chargerLocations);
                                } else {
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
                        showDialog("Routing Error", error, "checkRoute",
                                new Object[]{vin, start, end, capacity, perc}, false);
                    }
                }, "CheckRoute");
    }

    private void route(final LatLng start, final LatLng end, final ArrayList<LatLng> chargerLocations) {
        //Cancel previous requests
        ServerConnector.getInstance(getActivity()).cancelRequest("Route");
        removeGeofences();

        final String origin = String.valueOf(start.latitude) + "," + String.valueOf(start.longitude);
        final String destination = String.valueOf(end.latitude) + "," + String.valueOf(end.longitude);
        String url = "";
        StringBuilder sb = new StringBuilder(ROUTING_STRING);
        try {
            sb.append("origin=" + origin);
            //sb.append("?input=" + URLEncoder.encode(query, "utf8"));
            //sb.append("&types=(regions)");
            sb.append("&destination=" + destination);

            for (int i = 0; i < chargerLocations.size(); i++) {        //loop 1-2 constant time
                if (i == 0)
                    sb.append("&waypoints=");

                sb.append(+chargerLocations.get(i).latitude + "," + chargerLocations.get(i).longitude);

                if ((chargerLocations.size() - i) > 1)
                    sb.append("|");
            }
            sb.append("&mode=driving&avoid=ferries&alternatives=true");
            sb.append("&key=" + URLEncoder.encode(MAP_API_KEY, "utf8") + "&components=country:lk");
            url = sb.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ServerConnector.getInstance(getActivity()).sendRequest(url, null, Request.Method.POST,
                new OnResponseListner() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("[]")) {
                        } else {
                            try {
                                JSONArray jsonLegs;
                                routes = new ArrayList<>();
                                routeLines = new ArrayList<>();
                                circles = new ArrayList<>();
                                // Create a JSON object hierarchy from the results
                                JSONObject responseObject = new JSONObject(response);
                                JSONArray routesArray = responseObject.getJSONArray("routes");
                                for (int i = 0; i < routesArray.length(); i++) {        //loop abt 50 (legs) constant time
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

                                                for (int i = 0; i < size; i++) {        //loop abt 1-2 - nested loop
                                                    List<List<HashMap<String, String>>> result = RouteParseHelper.init().parse(routes.get(i).getRoute());
                                                    ArrayList<LatLng> points;
                                                    PolylineOptions lineOptions = null;

                                                    // Traversing through all the routes
                                                    for (int p = 0; p < result.size(); p++) {       //loop 10-50
                                                        points = new ArrayList<LatLng>();
                                                        lineOptions = new PolylineOptions();

                                                        // Fetching i-th route
                                                        List<HashMap<String, String>> path = result.get(p);

                                                        // Fetching all the points in i-th route
                                                        for (int j = 0; j < path.size(); j++) {     //loop 4-8
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

                                                    TextView text = new TextView(getActivity());
                                                    IconGenerator generator = new IconGenerator(getActivity());

                                                    if (i == 0) {
                                                        lineOptions.color(Color.argb(250, 69, 151, 255));
                                                        lineOptions.zIndex(2l);

                                                        text.setTextColor(Color.WHITE);
                                                        generator.setBackground(getActivity().getDrawable(R.drawable.ideal_route_info));

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
                                    for (LatLng charger : chargerLocations)     //loop 0-2 - constant time
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
                        showDialog("Timeout", "Check Your internet connection...", "route", null, false);
                    }
                }, "Route");
    }

    private void removeDestinationMarker() {
        if (routeLines != null) {
            for (Polyline pl : routeLines) {        //loop 0-2 - constant time
                pl.remove();
            }
            routeLines.clear();
        }

        removeGeofences();

        if (infoMarkers != null) {
            for (Marker m : infoMarkers) {      //loop 0-1 - constant time
                m.remove();
            }
            infoMarkers.clear();
        }

        if (destinatonMarker != null)
            destinatonMarker.remove();

        destinatonMarker = null;
    }

    public void setDestinationOnClick(final LatLng location) {
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
                        showDialog("Timeout", "Check Your internet connection...", "setDestinationOnClick", new Object[]{location}, false);
                    }
                }, "Address_From_Location");
    }

    private void showProgress(String text) {
        if (text != null)
            progress_bar_text.setText(text);
        else
            progress_bar_text.setText("Loading...");

        progress_bar_text.setGravity(Gravity.CENTER);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static Intent makeNotificationMainIntent(Context context, String msg) {
        final Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("NOTIFICATION MSG", msg);
        return intent;
    }

    public static Intent makeNotificationFeedbackIntent(Context context, String msg) {
        final Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("NOTIFICATION MSG", msg);
        return intent;
    }

    private void createGeofence(LatLng location, String uId) {
        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(location.latitude, location.longitude))
                .radius(50)
                .fillColor(Color.argb(50, 135, 206, 235))
                .strokeColor(Color.argb(150, 135, 206, 235))
                .strokeWidth(2);
        circles.add(mGoogleMap.addCircle(circleOptions));

        GeofenceMonitor geofenceSingleton = GeofenceMonitor.getInstance(getActivity());
        Location geofence = new Location("");
        geofence.setLatitude(location.latitude);
        geofence.setLongitude(location.longitude);
        geofenceSingleton.addGeofence(geofence, uId);
        geofenceSingleton.startGeofencing();
    }

    private void removeGeofences() {
        if (circles != null) {
            for (Circle c : circles) {      //loop 1-3 - constant time
                c.remove();
            }
            circles.clear();
        }

        GeofenceMonitor geofenceSingleton = GeofenceMonitor.getInstance(getActivity());
        geofenceSingleton.removeGeofences();
    }

    private void sendRouteToGoogleApp(LatLng origin, LatLng dest, ArrayList<LatLng> chargers) {
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
            for (int i = 0; i < chargers.size(); i++) {        //looop 0-2 - constant time
                if (i == 0)
                    sBuf.append("&waypoints=");

                sBuf.append(+chargers.get(i).latitude + "," + chargers.get(i).longitude);

                if ((chargers.size() - i) > 1)
                    sBuf.append("|");
            }
        }

        GoogleAnalyticsService.getInstance().setAction("Map Functions", "Navigate", "Navigate to Destination");

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(sBuf.toString()));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);
    }

    private void trackError(Exception e) {
        GoogleAnalyticsService.getInstance().trackException(getActivity(), e);
    }

    private void generateReceipt(String charging_session_id, String charger_id) {
        rd = new ReceiptDialog();
        if (!rd.isAdded() && !rd.isVisible()) {
            rd.init(Integer.parseInt(charging_session_id), chargingStations.get(charger_id), session.getVehicles(), new OnErrorListner() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onError(String error, JSONObject obj) {
                    rd.dismiss();
                    showDialog("Routing Error", error, "generateReceipt", new Object[]{charging_session_id, charger_id}, true);
                }
            });
            rd.show(getFragmentManager(), "Receipt");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        if (checkLocationPermission()) {
            if (mGoogleMap != null) {
                mGoogleMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        if (checkLocationPermission()) {
            LocationMonitor.instance(getActivity()).stop();
        }
    }

    private void addSessionDetailsListener(String sessionId){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference sessionRef = database.child("charging_session_details").child(sessionId);
        sessionRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @SuppressLint("MissingPermission")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.getValue() != null) {
                        JSONObject obj = new JSONObject(dataSnapshot.getValue().toString());
                        switch (obj.getString("status")){
                            case "Charging":
                                details_progress_layout.setVisibility(View.GONE);

                                soc_perc.setProgress(Integer.parseInt(obj.getString("soc")));
                                soc_val.setText(obj.getString("soc"));
                                till_80.setText(UIHelper.getInstance(getActivity()).getDuration(obj.getInt("remaining_time_80")));
                                till_100.setText(UIHelper.getInstance(getActivity()).getDuration(obj.getInt("remaining_time_100")));
                                units.setText(obj.getString("units"));
                                tot_price.setText(obj.getString("current_fee"));

                                details_layout.setVisibility(View.VISIBLE);
                                setMargins(1);
                            break;
                            case "Done":
                                sessionRef.removeValue();
                                details_progress_layout.setVisibility(View.VISIBLE);
                                details_layout.setVisibility(View.GONE);
                                setMargins(1);
                            break;
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addSessionListener() {
        PlugInDialog pid = new PlugInDialog();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference sessionRef = database.child("charging_sessions").child(session.getUserID());

        sessionRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @SuppressLint("MissingPermission")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (pid != null && pid.isAdded())
                    pid.dismiss();

                hideProgress();
                System.out.println(dataSnapshot.getValue());
                if (dataSnapshot.getValue() != null) {
                    try {
                        HashMap<String, JSONObject> vehicles = new HashMap();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {

                            vehicles.put(ds.getKey(), new JSONObject(ds.getValue().toString()));
                        }

                        JSONObject vehicle = vehicles.get(vehicles.keySet().toArray()[0]);

                        if (chargingSessions.size() < 1)
                            chargingSessions.put(vehicle.getString("station"), dataSnapshot.getKey().toString());

                        setFree();
                        switch (vehicle.getString("status")) {
                            case "PluggedIn":
                                showProgress("Charger turning on");
                                break;
                            case "InitializationFailed":
                                sessionRef.child(vehicles.keySet().toArray()[0].toString()).removeValue();
                                showDialog("Initialising Failed", "Please try again later", "chargeNow",
                                        new Object[]{vehicle.getString("station")}, false);
                                break;
                            case "Charging":
                                onMarkerClick(markers.get(vehicle.getString("station")));
                                addSessionDetailsListener(vehicle.getString("session"));
                                setCharging();
                                break;
                            case "Done":
                                sessionRef.child(vehicles.keySet().toArray()[0].toString()).removeValue();
                                generateReceipt(vehicle.getString("session"), vehicle.getString("station"));
                                break;
                            case "ChargingFailed":
                                sessionRef.child(vehicles.keySet().toArray()[0].toString()).removeValue();
                                showDialog("Charging Failed", "Please try again later", "chargeNow",
                                        new Object[]{vehicle.getString("station")}, false);
                                break;
                            case "ChargingInitialized":
                                if (pid != null && !pid.isAdded())
                                    pid.show(getFragmentManager(), "Plug_In_Dialog");

                                break;
                        }
                    } catch (Exception e) {
                        hideProgress();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addStatusListener(String id) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference chargerStateRef = database.child("charging_stations").child(id);
        chargerStateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null)
                    updateItemArray(dataSnapshot.getKey(), dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}