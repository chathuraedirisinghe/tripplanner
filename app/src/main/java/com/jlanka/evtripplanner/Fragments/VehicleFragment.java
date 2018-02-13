package com.jlanka.evtripplanner.Fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import com.jlanka.evtripplanner.Adapters.Vehicle;
import com.jlanka.evtripplanner.Adapters.VehicleAdapter;
import com.jlanka.evtripplanner.GoogleAnalyticsService;
import com.jlanka.evtripplanner.MainActivity;
import com.jlanka.evtripplanner.R;
import com.jlanka.evtripplanner.Server.OnErrorListner;
import com.jlanka.evtripplanner.Server.OnResponseListner;
import com.jlanka.evtripplanner.Server.ServerConnector;
import com.jlanka.evtripplanner.UserActivity.SessionManager;

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
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setView(promptsView);

                final EditText regNo = (EditText)promptsView.findViewById(R.id.reg_number);
                final EditText vin = (EditText)promptsView.findViewById(R.id.vehicle_vin);
                final EditText model = (EditText)promptsView.findViewById(R.id.vehicle_model);
                final EditText year = (EditText)promptsView.findViewById(R.id.vehicle_year);

                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        String v_reg = String.valueOf(regNo.getText());
                                        String v_in = String.valueOf(vin.getText());
                                        String v_model = String.valueOf(model.getText());
                                        String v_year = String.valueOf(year.getText());
                                        sendData(new Vehicle(user_id,v_reg,v_in,v_model,v_year));
                                        vehicleAdapter.addVehicle(new Vehicle(user_id,v_reg,v_in,v_model,v_year));
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
                alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            }
        });

        //----------------------------------Thiwanka----------------------------------
        GoogleAnalyticsService.getInstance().setScreenName(this.getClass().getSimpleName());

        return mView;
    }

    private void getDetails(String user_id) {
        ServerConnector serverConnector= new ServerConnector(ServerConnector.SERVER_ADDRESS+"ev_owners/"+user_id+"/",null,Request.Method.GET,getActivity());
        serverConnector.setOnReponseListner(new OnResponseListner() {
            @Override
            public void onResponse(String response) {
                Log.w("User Details : ", String.valueOf(response));
                try{
                    JSONObject user_data = new JSONObject(response);
                    JSONArray vehicles = user_data.getJSONArray("electric_vehicles");

                    session.setVehicles(vehicles);
                    Intent i = new Intent(getActivity(), MainActivity.class);
                    startActivity(i);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        serverConnector.sendRequest();
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

        ServerConnector serverConnector= new ServerConnector(ServerConnector.SERVER_ADDRESS+"electric_vehicles/",params, Request.Method.POST,getActivity());
        serverConnector.setOnReponseListner(new OnResponseListner() {
            @Override
            public void onResponse(String response) {
                Log.w("User Details : ", String.valueOf(response));
                try{
                    JSONObject user_data = new JSONObject(response);
                    GoogleAnalyticsService.getInstance().setAction("Vehicle","Add Vehicle",modal+year+"");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        serverConnector.setOnErrorListner(new OnErrorListner() {
            @Override
            public void onError(String error, JSONObject obj) {
                System.out.println("SERVER RESPONSE : " + error + "OBJ : "+obj.toString());

                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("Sorry");
                alertDialog.setMessage(error);
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });
        serverConnector.sendRequest();

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
