package com.jlanka.evtripplanner.Fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Request;
import com.jlanka.evtripplanner.GoogleAnalyticsService;
import com.jlanka.evtripplanner.R;
import com.jlanka.evtripplanner.Server.OnErrorListner;
import com.jlanka.evtripplanner.Server.OnResponseListner;
import com.jlanka.evtripplanner.Server.ServerConnector;
import com.jlanka.evtripplanner.UserActivity.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class FeedbackFragment extends Fragment {

    View myView;

    ProgressDialog progress;

    SessionManager session;

    String user_mobile,user_id;

    public FeedbackFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_feedback, container, false);


        return myView;
    }

    public void getHistory(String user_id){
        String url = "ev_owners/charging_history/"+user_id+"/";

        ServerConnector serverConnector= new ServerConnector(ServerConnector.SERVER_ADDRESS+url,null,Request.Method.GET,getActivity());
        serverConnector.setOnReponseListner(new OnResponseListner() {
            @Override
            public void onResponse(String response) {
                Log.w("User Details : ", String.valueOf(response));
                try{
                    JSONArray jsonResponse = new JSONArray(response);
                    System.out.println("History Data : "+jsonResponse);
                    updateLastExpenses(jsonResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progress.dismiss();
            }
        });

        serverConnector.setOnErrorListner(new OnErrorListner() {
            @Override
            public void onError(String error, JSONObject obj) {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("Sorry");
                alertDialog.setMessage(error);
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent i = new Intent(getActivity(), FeedbackFragment.class);
                        startActivity(i);
                    }
                });
                alertDialog.show();
            }
        });
        serverConnector.sendRequest();

        //initialize the progress dialog and show it
        progress = new ProgressDialog(getActivity());
        progress.setMessage("Requesting charging history data...");
        progress.show();
    }

    public void updateLastExpenses(JSONArray jsonResponse){
        TableLayout lastExpensesTable = (TableLayout)myView.findViewById(R.id.lastExpensesTable);
        lastExpensesTable.setStretchAllColumns(true);

        int i = 0;

            try{
                for ( i = 0 ; i < jsonResponse.length() ; i++ ){
                    JSONObject history_row = jsonResponse.getJSONObject(i);
                    final String date = history_row.getString("start_datetime").substring(0,10);
                    final String duration = history_row.getString("duration");
                    final String cost = history_row.getString("cost");
                    final String station_id = history_row.getString("charging_station");


                    TableRow tr = new TableRow(getActivity());
                    tr.setPadding(5,0,5,0);

                    if(i%2==0){
                        tr.setBackgroundColor(Color.rgb(207, 211, 214));
                    }else{

                    }

                    TextView c1 = new TextView(getActivity());
                    c1.setText(date);

                    TextView c2 = new TextView(getActivity());
                    c2.setText(duration);

                    TextView c3 = new TextView(getActivity());
                    c3.setText(cost);

                    TextView c4 = new TextView(getActivity());
                    c4.setText(station_id);

                    tr.addView(c1);
                    tr.addView(c2);
                    tr.addView(c3);
                    tr.addView(c4);
                    lastExpensesTable.addView(tr);
                }

            }catch (JSONException e){

            }

    }

}