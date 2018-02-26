package com.jlanka.jltripplanner.UI;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.jlanka.jltripplanner.Helpers.UIHelper;
import com.jlanka.jltripplanner.Model.Charger;
import com.jlanka.jltripplanner.R;
import com.jlanka.jltripplanner.Server.OnErrorListner;
import com.jlanka.jltripplanner.Server.OnResponseListner;
import com.jlanka.jltripplanner.Server.ServerConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Workstation on 2/24/2018.
 */

public class ReceiptDialog extends DialogFragment {

    private int id;
    private ArrayList<Charger> chargers;
    private String vehiclesString="";
    private OnErrorListner errorListener;

    public void init(int id, ArrayList<Charger> chargers, String vehiclesString,OnErrorListner errorListner){
        this.id=id;
        this.chargers=chargers;
        this.vehiclesString=vehiclesString;
        this.errorListener=errorListner;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_receipt, null);
        builder.setCancelable(false);
        builder.setView(view);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setNeutralButton("Take Screenshot", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                takeScreenshot(dialog);
            }
        });

        android.app.AlertDialog dialog = builder.create();
        getResponse(view,dialog);
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setEnabled(false);
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(R.color.neutralColor);
        return dialog;
    }

    private void getResponse(View view,android.app.AlertDialog dialog){
        ServerConnector.getInstance(getActivity()).cancelRequest("GetReceipt");
        ServerConnector.getInstance(getActivity()).sendRequest(ServerConnector.SERVER_ADDRESS+"charging_sessions/"+id,null, Request.Method.GET,
                new OnResponseListner() {
                    @TargetApi(Build.VERSION_CODES.O)
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response.equals("[]")) {
                            }
                            else {
                                LinearLayout detailsLayout = view.findViewById(R.id.rec_details_layout);
                                LinearLayout progressLayout = view.findViewById(R.id.rec_progress_layout);
                                TextView ch_id = view.findViewById(R.id.rec_charger_id);
                                TextView ch_type = view.findViewById(R.id.rec_charger_type);
                                TextView ch_power = view.findViewById(R.id.rec_charger_power);
                                TextView rec_veh = view.findViewById(R.id.rec_veh);
                                TextView rec_date = view.findViewById(R.id.rec_date);
                                TextView rec_start = view.findViewById(R.id.rec_start);
                                TextView rec_stop = view.findViewById(R.id.rec_stop);
                                TextView rec_dur = view.findViewById(R.id.rec_dur);
                                TextView rec_total = view.findViewById(R.id.rec_total);
                                ImageView rec_icon = view.findViewById(R.id.rec_icon);

                                JSONObject obj=new JSONObject(response);

                                if (obj.getString("status").equals("DO")) {
                                    JSONArray vehicles = new JSONArray(vehiclesString);
                                    for (int i = 0; i < vehicles.length(); i++) {
                                        JSONObject v = vehicles.getJSONObject(i);
                                        if (v.getString("id").equals(obj.getString("electric_vehicle"))) {
                                            rec_veh.setText(v.getString("reg_no") + " - " + v.getString("model"));
                                        }
                                    }

                                    for (Charger c : chargers) {
                                        if (c.getId() == obj.getInt("charging_station")) {
                                            ch_id.setText(c.getDevice_id());
                                            ch_type.setText(c.getType());
                                            ch_power.setText(c.getPower() + " kWh");
                                            rec_icon.setImageBitmap(UIHelper.getInstance(getActivity()).getMarkerIcon(c.getType(), c.getState()));
                                        }
                                    }

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
                                    progressLayout.setVisibility(View.GONE);
                                    detailsLayout.setVisibility(View.VISIBLE);
                                    dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setEnabled(true);
                                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                },
                errorListener
                ,"GetReceipt");

    }

    private void takeScreenshot(DialogInterface dialog) {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";
            System.out.println(mPath);

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
            Toast.makeText(getActivity(),"Screenshot Taken",Toast.LENGTH_LONG).show();
            //openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            Toast.makeText(getActivity(),"Unable to take screenshot",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
