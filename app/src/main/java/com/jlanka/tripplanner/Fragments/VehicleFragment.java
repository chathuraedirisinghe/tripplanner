package com.jlanka.tripplanner.Fragments;


import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import com.jlanka.tripplanner.Adapters.Vehicle;
import com.jlanka.tripplanner.Adapters.VehicleAdapter;
import com.jlanka.tripplanner.GoogleAnalyticsService;
import com.jlanka.tripplanner.R;
import com.jlanka.tripplanner.Server.OnErrorListner;
import com.jlanka.tripplanner.Server.OnResponseListner;
import com.jlanka.tripplanner.Server.ServerConnector;
import com.jlanka.tripplanner.UserActivity.SessionManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class VehicleFragment extends Fragment {

    View mView;
    private RecyclerView recyclerView;
    private VehicleAdapter vehicleAdapter;
    private List<Vehicle> vehicleList = new ArrayList<>();
    SessionManager session;
    String user_id;

    FragmentManager fragmentManager;

    private TextView tv_message;
    private AlertDialog dialog;
    ImageButton addVehicleButton;
    Button done;

    public VehicleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView=inflater.inflate(R.layout.fragment_vehicle,container,false);
        addVehicleButton = (ImageButton) mView.findViewById(R.id.addBtn);
        done = (Button) mView.findViewById(R.id.doneBtn);
        recyclerView = (RecyclerView) mView.findViewById(R.id.recycler_view);
        ButterKnife.bind(this,mView);

        session = new SessionManager(getActivity());
        fragmentManager  = getFragmentManager();
        //This will redirect user to LoginActivity is he is not logged in
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();

        user_id = user.get(SessionManager.user_id);

        vehicleAdapter = new VehicleAdapter(vehicleList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getBaseContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(vehicleAdapter);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDetails(user_id);
            }
        });

        addVehicleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(getActivity().getBaseContext());
                View promptsView = li.inflate(R.layout.addvehicle, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(promptsView);
                builder.setTitle("Add Vehicle");

                final EditText regNo = (EditText)promptsView.findViewById(R.id.reg_number);
                //final EditText vin = (EditText)promptsView.findViewById(R.id.vehicle_vin);
                final EditText model = (EditText)promptsView.findViewById(R.id.vehicle_model);
                final EditText year = (EditText)promptsView.findViewById(R.id.vehicle_year);
                tv_message = (TextView) promptsView.findViewById(R.id.vehicle_tv_message);

                builder.setPositiveButton("Add Vehicle", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog = builder.create();
                dialog.show();
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String v_reg = String.valueOf(regNo.getText());
                        String v_in = "1234";//String.valueOf(vin.getText());
                        String v_model = String.valueOf(model.getText());
                        String v_year = String.valueOf(year.getText());
                        if (!v_reg.isEmpty()){
                            if(!v_in.isEmpty()){
                                if (!v_model.isEmpty()){
                                    if (!v_year.isEmpty()){
                                        sendData(new Vehicle(user_id,v_reg,v_in,v_model,v_year));
                                    }
                                    else{
                                        tv_message.setVisibility(View.VISIBLE);
                                        tv_message.setText("Please enter a Year");
                                    }
                                }
                                else{
                                    tv_message.setVisibility(View.VISIBLE);
                                    tv_message.setText("Please enter a Model name");
                                }
                            }
                            else{
                                tv_message.setVisibility(View.VISIBLE);
                                tv_message.setText("Please enter a valid VIN number");
                            }
                        }
                        else {
                            tv_message.setVisibility(View.VISIBLE);
                            tv_message.setText("Please enter a valid Registration Number");
                        }
                    }
                });
            }
        });

        //----------------------------------Thiwanka----------------------------------
        GoogleAnalyticsService.getInstance().setScreenName(this.getClass().getSimpleName());

        return mView;
    }

    private void getDetails(String user_id) {
        ProgressDialog pd = ProgressDialog.show(getActivity(), "", "Please Wait...", true);

        ServerConnector.getInstance(getActivity()).cancelRequest("GetUser");
        ServerConnector.getInstance(getActivity()).getRequest(ServerConnector.SERVER_ADDRESS+"ev_owners/"+user_id+"/",

                new OnResponseListner() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            pd.dismiss();
                            JSONObject user_data = new JSONObject(response);
                            JSONArray vehicles = user_data.getJSONArray("electric_vehicles");
                            if (vehicles.length()>0) {
                                session.setVehicles(vehicles);
                                fragmentManager.beginTransaction().replace(R.id.content_frame, new MapFragment()).commit();
                            }
                            else
                                Toast.makeText(getActivity(), "Please add a vehicle to proceed", Toast.LENGTH_SHORT).show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },

                new OnErrorListner() {
                    @Override
                    public void onError(String error, JSONObject obj) {
                        Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                    }
                },
                "GetUser");
    }

    private void sendData(Vehicle vehicle) {
        String id = vehicle.getUser_id();
        String regNo = vehicle.getRegNo();
        String vin = vehicle.getVin();
        final String modal = vehicle.getModel();
        final String year = vehicle.getYear();

        Map<String, String> params = new HashMap<>();
        // the POST parameters:
        params.put("owner", id);
        params.put("reg_no", regNo);
        params.put("vin", vin);
        params.put("model", modal);
        params.put("year", year);

        ServerConnector.getInstance(getActivity()).cancelRequest("CreateVehicle");
        ServerConnector.getInstance(getActivity()).sendRequest(ServerConnector.SERVER_ADDRESS+"electric_vehicles/",params, Request.Method.POST,
        new OnResponseListner() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject user_data = new JSONObject(response);
                    JSONObject VehicleDetails = new JSONObject(response);
                    JSONArray _vehicles = new JSONArray(session.getVehicles());
                    _vehicles.put(VehicleDetails);
                    session.setVehicles(_vehicles);
                    GoogleAnalyticsService.getInstance().setAction("Vehicle","Add Vehicle",modal+year+"");
                    dialog.dismiss();
                    String reg_no = user_data.getString("reg_no");
                    String vin = user_data.getString("vin");
                    String model = user_data.getString("model");
                    String year = user_data.getString("year");
                    vehicleAdapter.addVehicle(new Vehicle(user_id,reg_no,vin,model,year));
                    GoogleAnalyticsService.getInstance().setAction("Vehicle","Add Vehicle",modal+year+"");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        },

        new OnErrorListner() {
            @Override
            public void onError(String error, JSONObject obj) {
                JSONArray reg=null;
                JSONArray vin=null;
                String message=null;
                try {
                    if (obj.has("reg_no")) {
                        reg = obj.getJSONArray("reg_no");
                        message = "Reg no : "+reg.get(0).toString().substring(0, 1).toUpperCase() + reg.get(0).toString().substring(1);
                    }
                    else if (obj.has("vin")) {
                        vin = obj.getJSONArray("vin");
                        message = "Vin : "+vin.get(0).toString().substring(0, 1).toUpperCase() + vin.get(0).toString().substring(1);
                    }
                    else
                        message=error;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                tv_message.setText(message);
                tv_message.setVisibility(View.VISIBLE);
            }
        },"CreateVehicle");

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
