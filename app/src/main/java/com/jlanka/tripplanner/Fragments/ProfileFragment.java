package com.jlanka.tripplanner.Fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.facebook.stetho.Stetho;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.jlanka.tripplanner.Adapters.Vehicle;
import com.jlanka.tripplanner.GoogleAnalyticsService;
import com.jlanka.tripplanner.R;
import com.jlanka.tripplanner.Server.OnErrorListner;
import com.jlanka.tripplanner.Server.OnResponseListner;
import com.jlanka.tripplanner.Server.ServerConnector;
import com.jlanka.tripplanner.UI.EditProfileDialog;
import com.jlanka.tripplanner.UserActivity.SessionManager;

/**
 * Created by chathura on 11/23/17.
 */

public class ProfileFragment extends Fragment {

//    @BindView(R.id.profile_title)TextView _title;
    @BindView(R.id.profile_fname)TextView _fname;
    @BindView(R.id.profile_lname)TextView _lname;
//    @BindView(R.id.profile_occupation)TextView _occupation;
    @BindView(R.id.profile_email)TextView _email;
//    @BindView(R.id.profile_nic)TextView _nic;
    @BindView(R.id.profile_mobile)TextView _mobile;
    @BindView(R.id.addVehiclebtn) ImageButton _addVehicle;
    @BindView(R.id.remVehiclebtn) ImageButton _remVehicle;
    @BindView(R.id.prof_edit) ImageButton _editProfile;
//    @BindView(R.id.profile_address)TextView _address;
//    @BindView(R.id.profile_cartype)TextView _cartype;

    View myView;

    String user_id;

    ProgressDialog progress;
    String user_mobile;
    SessionManager session;
    private TextView tv_message;
    private AlertDialog dialog;
    FragmentManager fragmentManager = getFragmentManager();

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.content_profile_scrolling, container, false);
//        Stetho.initializeWithDefaults(getActivity());
        session = new SessionManager(getActivity());
        //This will redirect user to LoginActivity is he is not logged in
        // get user data from session

        ButterKnife.bind(this,myView);

        session = new SessionManager(getActivity());
        HashMap<String, String> user = session.getUserDetails();
        user_id = user.get(SessionManager.user_id);
        user_mobile = user.get(SessionManager.user_mobile);

        addToSpinner(user);


//        getProfileData(user_mobile);
        setProfile();

        _editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditProfileDialog dpf=new EditProfileDialog();
                dpf.init(user.get(SessionManager.user_id),_fname.getText().toString(),_lname.getText().toString(),_mobile.getText().toString(),user.get(SessionManager.user_email),
                        user.get(SessionManager.user_name),user.get(SessionManager.pass_word),new OnResponseListner() {
                    @Override
                    public void onResponse(String response) {
                        dpf.dismiss();
                        Toast.makeText(getActivity(), "Profile edit successful", Toast.LENGTH_SHORT).show();
                        reloadSession(user_id,user.get(SessionManager.pass_word));
                    }
                });
                dpf.show(getFragmentManager(),"ProfileEditDialog");
            }
        });

        //----------------------------------Thiwanka----------------------------------
        GoogleAnalyticsService.getInstance().setScreenName(this.getClass().getSimpleName());

        _addVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(getActivity().getBaseContext());
                View promptsView = li.inflate(R.layout.addvehicle, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(promptsView);
                builder.setTitle("Add Vehicle");

                final EditText regNo = (EditText)promptsView.findViewById(R.id.reg_number);
                final EditText vin = (EditText)promptsView.findViewById(R.id.vehicle_vin);
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
                        String v_in = String.valueOf(vin.getText());
                        String v_model = String.valueOf(model.getText());
                        String v_year = String.valueOf(year.getText());
                        if (!v_reg.isEmpty()){
                            if(!v_in.isEmpty()){
                                if (!v_model.isEmpty()){
                                    if (!v_year.isEmpty() || v_year.length()!=4){
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

        _remVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Spinner spinner = (Spinner) myView.findViewById(R.id.spinner);
                    spinner.getSelectedItemPosition();
                    JSONArray obj = new JSONArray(user.get(SessionManager.electric_vehicles));
                    deleteVehicle(obj.getJSONObject(spinner.getSelectedItemPosition()).getString("id").toString());
                }
                catch (Exception e){

                }
            }
        });
        return myView;
    }

    private void addToSpinner(HashMap<String, String> user) {
        ArrayList<String> ar = new ArrayList<String>();
        try {

            JSONArray obj = new JSONArray(user.get(SessionManager.electric_vehicles));
            for (int i = 0; i < obj.length(); i++){
                JSONObject vehicle_object = obj.getJSONObject(i);
                final String model = vehicle_object.getString("model");
                final String reg_no = vehicle_object.getString("reg_no");
                ar.add(model+"   "+reg_no);
            }
        } catch (Throwable t) {

        }

        String[] myVehicle = ar.toArray(new String[0]);

        Spinner spinner = (Spinner)myView.findViewById(R.id.spinner);;
        ArrayAdapter<String> myVehicleGUIArray= new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, myVehicle);
        myVehicleGUIArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        myVehicleGUIArray.notifyDataSetChanged();
        spinner.setAdapter(myVehicleGUIArray);
    }

    private void setProfile() {
        HashMap<String, String> user = session.getUserDetails();
        _fname.setText(user.get(SessionManager.user_fname));
        _lname.setText(user.get(SessionManager.user_lname));
        _email.setText(user.get(SessionManager.user_email));
        _mobile.setText(user.get(SessionManager.user_mobile));

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

        ServerConnector.getInstance(getActivity()).cancelRequest("SendVehicleData");
        ServerConnector.getInstance(getActivity()).sendRequest(ServerConnector.SERVER_ADDRESS+"electric_vehicles/",params, Request.Method.POST,
        new OnResponseListner() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject VehicleDetails = new JSONObject(response);
                    JSONArray _vehicles = new JSONArray(session.getVehicles());
                    _vehicles.put(VehicleDetails);
                    HashMap<String, String> user = session.getUserDetails();
                    reloadSession(user_id,user.get(SessionManager.pass_word));
                    GoogleAnalyticsService.getInstance().setAction("Vehicle","Add Vehicle",modal+year+"");
                    addToSpinner(session.getUserDetails());
                    Toast.makeText(getActivity(), "Vehicle Added", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        },

        new OnErrorListner() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onError(String error, JSONObject obj) {
                JSONArray reg = null;
                JSONArray vin = null;
                String message = null;
                try {
                    if (obj.has("reg_no")) {
                        reg = obj.getJSONArray("reg_no");
                        message = "Reg no : " + reg.get(0).toString();
                    } else if (obj.has("vin")) {
                        vin = obj.getJSONArray("vin");
                        message = "Vin : " + vin.get(0).toString();
                    } else
                        message = error;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                tv_message.setText(message);
                tv_message.setVisibility(View.VISIBLE);
            }
        },"SendVehicleData");
    }

    private void deleteVehicle(String vehicleID){
        ServerConnector.getInstance(getActivity()).cancelRequest("DeleteVehicle");
        ServerConnector.getInstance(getActivity()).sendRequest(ServerConnector.SERVER_ADDRESS+"electric_vehicles/"+vehicleID+"/",null, Request.Method.DELETE,
                new OnResponseListner() {
                    @Override
                    public void onResponse(String response) {
                        HashMap<String, String> user = session.getUserDetails();
                        reloadSession(user_id,user.get(SessionManager.pass_word));
                        Toast.makeText(getActivity(), "Vehicle removed", Toast.LENGTH_SHORT).show();
                    }
                },

                new OnErrorListner() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onError(String error, JSONObject obj) {
                        System.out.println(error+","+obj);
                        Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                    }
                },"SendVehicleData");
    }

    private void reloadSession(String id, final String password) {
        ServerConnector.getInstance(getActivity()).cancelRequest("getUserDetails");
        ServerConnector.getInstance(getActivity()).getRequest(ServerConnector.SERVER_ADDRESS + "ev_owners/" + id + "/",
                new OnResponseListner() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject user_data = new JSONObject(response);
                            String id = user_data.getString("id");
                            String username = user_data.getString("username");
                            String credits = user_data.getString("balance");
                            String fname = user_data.getString("first_name");
                            String lname = user_data.getString("last_name");
                            String email = user_data.getString("email");
                            String mob = user_data.getString("contact_number");
                            JSONArray vehicles = user_data.getJSONArray("electric_vehicles");

                            NavigationView navigationView = (NavigationView) getActivity().getWindow().findViewById(R.id.nav_view);
                            View header = navigationView.getHeaderView(0);
                            TextView nameView = (TextView)header.findViewById(R.id.user_name);
                            TextView creditView = (TextView)header.findViewById(R.id.user_credit);
                            TextView userMob = (TextView)header.findViewById(R.id.user_mobile);

                            nameView.setText(fname+" "+lname);
                            userMob.setText(mob);

                            session.createLoginSession(id, username, password, fname, lname, email, mob, credits, vehicles);
                            addToSpinner(session.getUserDetails());
                            getFragmentManager().beginTransaction().remove(ProfileFragment.this);
                            getFragmentManager().beginTransaction().commit();
                            getFragmentManager().beginTransaction().replace(R.id.content_frame, new ProfileFragment()).addToBackStack(HistoryFragment.class.getName()).commit();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new OnErrorListner() {
                    @Override
                    public void onError(String error, JSONObject obj) {
                        Toast.makeText(getActivity(),error,Toast.LENGTH_LONG).show();
                    }
                },
                "getUserDetails");
    }
}
