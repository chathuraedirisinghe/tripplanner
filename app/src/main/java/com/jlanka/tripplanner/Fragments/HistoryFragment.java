package com.jlanka.tripplanner.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.jlanka.tripplanner.GoogleAnalyticsService;
import com.jlanka.tripplanner.R;
import com.jlanka.tripplanner.Server.OnErrorListner;
import com.jlanka.tripplanner.Server.OnResponseListner;
import com.jlanka.tripplanner.Server.ServerConnector;
import com.jlanka.tripplanner.UserActivity.SessionManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {

    View myView;

    ProgressDialog progress;

    SessionManager session;

    String user_mobile,user_id;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_history, container, false);

        session = new SessionManager(getActivity());
        HashMap<String, String> user = session.getUserDetails();

        user_mobile = user.get(SessionManager.user_mobile);
        user_id = user.get(SessionManager.user_id);
        getHistory(user_id);

        //----------------------------------Thiwanka----------------------------------
        GoogleAnalyticsService.getInstance().setScreenName(this.getClass().getSimpleName());

        return myView;
    }

    public void getHistory(String user_id){
        String url = "ev_owners/charging_history/"+user_id+"/";

        ServerConnector.getInstance(getActivity()).cancelRequest("GetChargingHistory");
        ServerConnector.getInstance(getActivity()).sendRequest(ServerConnector.SERVER_ADDRESS+url,null,Request.Method.GET,
        new OnResponseListner() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonResponse = new JSONArray(response);
                    updateLastExpenses(jsonResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progress.dismiss();
            }
        },

        new OnErrorListner() {
            @Override
            public void onError(String error, JSONObject obj) {
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
        },"GetChargingHistory");

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
                    String date = history_row.getString("start_datetime").substring(0,10);
                    int duration = history_row.getInt("duration");
                    double cost = history_row.getDouble("cost");
                    String station_id = history_row.getString("charging_station");

                    TableRow tr = new TableRow(getActivity());
                    tr.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    if(i%2==0){
                        tr.setBackgroundColor(Color.rgb(207, 211, 214));
                    }

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                    Date startDate = simpleDateFormat.parse(history_row.getString("start_datetime"));
                    SimpleDateFormat dateFormat=new SimpleDateFormat("dd MMM - hh:mm a");

                    TextView c1 = new TextView(getActivity());
                    c1.setTextSize(10);
                    c1.setText(dateFormat.format(startDate));

                    TextView c2 = new TextView(getActivity());
                    c2.setTextSize(12);
                    c2.setPadding(0,0,5,0);
                    c2.setGravity(Gravity.RIGHT);
                    if (duration > 59) {
                        int hours = duration / 60; //since both are ints, you get an int
                        int minutes = duration % 60;

                        if (hours > 1)
                            c2.setText(hours + " hrs " + minutes + " mins");
                        else
                            c2.setText(hours + " hr " + minutes + " mins");
                    } else
                        c2.setText(duration + " mins");


                    TextView c3 = new TextView(getActivity());
                    c3.setText(Math.round(cost)+"    ");
                    c3.setPadding(0,0,5,0);
                    c3.setGravity(Gravity.END);
                    c3.setTextSize(12);

                    TextView c4 = new TextView(getActivity());
                    c4.setText(station_id);
                    c4.setTextSize(10);

                    tr.addView(c1);
                    tr.addView(c2);
                    tr.addView(c3);
                    tr.addView(c4);
                    lastExpensesTable.addView(tr);
                }
            }catch (JSONException e){

            } catch (ParseException e) {
                e.printStackTrace();
            }

    }

}