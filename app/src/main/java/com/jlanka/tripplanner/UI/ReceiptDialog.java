package com.jlanka.tripplanner.UI;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.jlanka.tripplanner.Helpers.UIHelper;
import com.jlanka.tripplanner.Model.Charger;
import com.jlanka.tripplanner.R;
import com.jlanka.tripplanner.Server.OnErrorListner;
import com.jlanka.tripplanner.Server.OnResponseListner;
import com.jlanka.tripplanner.Server.ServerConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Workstation on 2/24/2018.
 */

public class ReceiptDialog extends DialogFragment {

    private int id;
    private View view;
    private AlertDialog dialog;
    private Charger charger;
    private String vehiclesString="";
    private OnErrorListner errorListener;

    public void init(int id,Charger charger, String vehiclesString,OnErrorListner errorListner){
        this.id=id;
        this.charger=charger;
        this.vehiclesString=vehiclesString;
        this.errorListener=errorListner;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        LayoutInflater li = LayoutInflater.from(getActivity().getBaseContext());
        view = li.inflate(R.layout.dialog_receipt, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNeutralButton("Take Screenshot", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        getResponse(view,dialog);
        dialog.show();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.neutralColor));
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setEnabled(false);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeScreenshot(view);
            }
        });
        return dialog;
    }

    private void getResponse(View view,android.app.AlertDialog dialog){
        ServerConnector.getInstance(getActivity()).cancelRequest("GetReceipt");
        ServerConnector.getInstance(getActivity()).getRequest(ServerConnector.SERVER_ADDRESS+"charging_sessions/"+id,
            new OnResponseListner() {
                @TargetApi(Build.VERSION_CODES.O)
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onResponse(String response) {
                    try {
                        if (!response.equals("[]")) {
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

                            JSONArray vehicles = new JSONArray(vehiclesString);
                            for (int i = 0; i < vehicles.length(); i++) {
                                JSONObject v = vehicles.getJSONObject(i);
                                if (v.getString("id").equals(obj.getString("electric_vehicle"))) {
                                    rec_veh.setText(v.getString("reg_no") + " - " + v.getString("model"));
                                }
                            }

                            ch_id.setText(charger.getDevice_id());
                            ch_type.setText(charger.getType());
                            ch_power.setText(charger.getPower() + " kW");
                            rec_icon.setImageBitmap(UIHelper.getInstance(getActivity()).getMarkerIcon(charger.getType(), charger.getState()));

                            rec_total.setText("Rs."+obj.getString("cost"));

                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                            Date startDate = simpleDateFormat.parse(obj.getString("start_datetime"));

                            SimpleDateFormat dateFormat=new SimpleDateFormat("dd-MM-yyyy");
                            SimpleDateFormat timeFormat=new SimpleDateFormat("hh:mm:ss a");

                            rec_date.setText(dateFormat.format(startDate));
                            rec_start.setText(timeFormat.format(startDate));

                            Date stopDate = simpleDateFormat.parse(obj.getString("end_datetime"));

                            rec_stop.setText(timeFormat.format(stopDate));

                            int dur = obj.getInt("duration");
                            rec_dur.setText(UIHelper.getInstance(getActivity()).getDurationInTimeFormat(dur));

                            progressLayout.setVisibility(View.GONE);
                            detailsLayout.setVisibility(View.VISIBLE);
                            dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setEnabled(true);
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        }
                    }
                    catch (Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            },
            errorListener
            ,"GetReceipt");

    }

    private void takeScreenshot(View view) {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            checkPermissions();
            // image naming and path  to include sd card  appending name you choose for file
            File f = new File(Environment.getExternalStorageDirectory().toString()+"/JL Trip Planner");
            if (!f.exists())
                f.mkdir();

            String mPath = f.getPath() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = view;
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

    private boolean checkPermissions(){
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return false;
        }
        else
            return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takeScreenshot(view);

                } else {

                    Toast.makeText(getActivity(), "App does not have permission to save files", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
}
