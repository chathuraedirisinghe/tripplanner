package com.jlanka.tripplanner.Fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.jlanka.tripplanner.Database.DatabaseHandler;
import com.jlanka.tripplanner.Database.Vehicle;
import com.jlanka.tripplanner.R;
import com.jlanka.tripplanner.UserActivity.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EvStatFragment extends Fragment {
    @BindView(R.id.vehicle_spinner)Spinner _vehicleList;
    @BindView(R.id.data_spinner)Spinner _evDataList;

    View myView;
    SessionManager session;
    DatabaseHandler databaseHandler;
    LineChart chart;
    private static final String TAG = "Statistics";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_ev_stat, container, false);
        ButterKnife.bind(this,myView);
        databaseHandler=new DatabaseHandler(getActivity());

        session = new SessionManager(getActivity());
        HashMap<String, String> user = session.getUserDetails();

        chart = myView.findViewById(R.id.chart);

        setData(user); //to Vehicle Spinner

        _evDataList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                List<Entry> entries = new ArrayList<Entry>();
                JSONObject vehicleData = databaseHandler.getAllContacts(item.toString());
                drawChart(vehicleData,item.toString());
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        _vehicleList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Vehicle vehicle = (Vehicle) parent.getSelectedItem();
                Toast.makeText(getActivity(), "Vehicle VIN: "+vehicle.getVin()+",  Vehicle REG : "+vehicle.getReg_no() +",  Vehicle Modal : "+vehicle.getModal(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return myView;
    }

    private void drawChart(JSONObject vehicleData, String topic) {
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<Entry> vals1 = new ArrayList<Entry>();

        JSONArray keys = vehicleData.names();

        try {
            for (int i = 0; i < keys.length(); i++) {
                Long key = Long.valueOf(keys.getString(i));
                Date d = new Date((long)key*1000);
                String date = DateFormat.format("dd-MM-yyyy hh:mm:ss", d).toString();
                float value = Float.parseFloat(vehicleData.getString(key.toString())); // Here's your value
                xVals.add(date);
                vals1.add(new Entry(value, i));
//                System.out.println("EV DATA : " + date + "    " + value);
            }
        }catch (Exception e){

        }

        LineDataSet set1 = new LineDataSet(vals1, topic);
        set1.setCubicIntensity(0.2f);
        set1.setDrawCircles(false);
        set1.setLineWidth(1.8f);
        set1.setCircleColor(Color.BLACK);
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setColor(Color.BLACK);
        set1.setFillColor(Color.BLACK);
        set1.setFillAlpha(100);

        LineData data = new LineData(xVals, set1);
        data.setValueTextSize(9f);
        data.setDrawValues(false);
        chart.setData(data);
        chart.invalidate();
    }


    private void setData(HashMap<String, String> user) {
        ArrayList<Vehicle> vehicleList = new ArrayList<>();
        try{
            JSONArray obj = new JSONArray(user.get(SessionManager.electric_vehicles));
            for (int i = 0; i < obj.length(); i++){
                JSONObject vehicle_object = obj.getJSONObject(i);
                final String model = vehicle_object.getString("model");
                final String reg_no = vehicle_object.getString("reg_no");
                final String vin = vehicle_object.getString("vin");
                vehicleList.add(new Vehicle(vin, reg_no,model));
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayAdapter<Vehicle> adapter = new ArrayAdapter<Vehicle>(getActivity(), android.R.layout.simple_spinner_item, vehicleList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.notifyDataSetChanged();
        _vehicleList.setAdapter(adapter);

    }

    private void addToSpinner(HashMap<String, String> user) {
        ArrayList<String> ar = new ArrayList<String>();
        try {
            JSONArray obj = new JSONArray(user.get(SessionManager.electric_vehicles));
            for (int i = 0; i < obj.length(); i++){
                JSONObject vehicle_object = obj.getJSONObject(i);
                System.out.println(vehicle_object.toString());
                final String model = vehicle_object.getString("model");
                final String reg_no = vehicle_object.getString("reg_no");
                final String vin = vehicle_object.getString("vin");
                ar.add(model+"   "+reg_no);
            }
        } catch (Throwable t) {
        }
        String[] myVehicle = ar.toArray(new String[0]);
        ArrayAdapter<String> myVehicleGUIArray= new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, myVehicle);
        myVehicleGUIArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        myVehicleGUIArray.notifyDataSetChanged();
        _vehicleList.setAdapter(myVehicleGUIArray);
    }


}
