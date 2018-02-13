package com.jlanka.jltripplanner.Fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.stetho.Stetho;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.jlanka.jltripplanner.GoogleAnalyticsService;
import com.jlanka.jltripplanner.R;
import com.jlanka.jltripplanner.Server.ServerConnector;
import com.jlanka.jltripplanner.UserActivity.SessionManager;

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
//    @BindView(R.id.profile_address)TextView _address;
//    @BindView(R.id.profile_cartype)TextView _cartype;

    View myView;

    ProgressDialog progress;
    String user_mobile;
    SessionManager session;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.content_profile_scrolling, container, false);
        Stetho.initializeWithDefaults(getActivity());
        ButterKnife.bind(this,myView);

        session = new SessionManager(getActivity());
        HashMap<String, String> user = session.getUserDetails();
        user_mobile = user.get(SessionManager.user_mobile);

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
            Log.e("My App", "Could not parse malformed JSON: \"" + t + "\"");
        }

        String[] myArray = ar.toArray(new String[0]);

        Spinner spinner = (Spinner)myView.findViewById(R.id.spinner);;
        ArrayAdapter<String> gameKindArray= new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, myArray);
        gameKindArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(gameKindArray);


//        getProfileData(user_mobile);
        setProfile();

        //----------------------------------Thiwanka----------------------------------
        GoogleAnalyticsService.getInstance().setScreenName(this.getClass().getSimpleName());

        return myView;
    }

    private void getProfileData(final String user_mobile) {
        RequestQueue rq = Volley.newRequestQueue(getActivity().getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ServerConnector.SERVER_ADDRESS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject jsonResponse = new JSONObject(response);
                            System.out.println("User Data : "+jsonResponse);
                            String credit = jsonResponse.getString("credit");
                            String fname = jsonResponse.getString("FName");
                            String lname = jsonResponse.getString("LName");
                            String title = jsonResponse.getString("title");
                            String occupation= jsonResponse.getString("occupation");
                            String email= jsonResponse.getString("email");
                            String nic= jsonResponse.getString("nic");
                            String altmobno= jsonResponse.getString("AltMblNo");
                            String birthday= jsonResponse.getString("Birthday");
                            String address= jsonResponse.getString("Adress");
                            String cartype= jsonResponse.getString("carType");

//                            session.createProfileData(credit,fname,lname,title,occupation,email,nic,altmobno,birthday,address,cartype);

//                            updateProfileWindow();

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        progress.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progress.dismiss();
                        Log.d("Error   ", String.valueOf(error));
                        Toast.makeText(getActivity().getApplicationContext(), "EROOORRRRRRRRRRRR"+error, Toast.LENGTH_SHORT).show();
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
        //initialize the progress dialog and show it
        progress = new ProgressDialog(getActivity());
        progress.setMessage("Requesting profile data...");
        progress.setCancelable(false);
        progress.show();

    }

    private void setProfile() {
        HashMap<String, String> user = session.getUserDetails();
//        _title.setText(user.get(SessionManager.user_title));
        _fname.setText(user.get(SessionManager.user_fname));
        _lname.setText(user.get(SessionManager.user_lname));
//        _occupation.setText(user.get(SessionManager.user_occupation));
        _email.setText(user.get(SessionManager.user_email));
//        _nic.setText(user.get(SessionManager.user_nic));
        _mobile.setText(user.get(SessionManager.user_mobile));
//        _address.setText(user.get(SessionManager.user_address));
//        _cartype.setText(user.get(SessionManager.electric_vehicles));

    }

}
