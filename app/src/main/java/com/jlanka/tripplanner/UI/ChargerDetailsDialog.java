package com.jlanka.tripplanner.UI;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.jlanka.tripplanner.Helpers.UIHelper;
import com.jlanka.tripplanner.Model.Charger;
import com.jlanka.tripplanner.R;
import com.jlanka.tripplanner.Server.OnErrorListner;
import com.jlanka.tripplanner.Server.OnResponseListner;
import com.jlanka.tripplanner.Server.ServerConnector;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Workstation on 2/24/2018.
 */

public class ChargerDetailsDialog extends DialogFragment{
    private Charger charger;
    private OnErrorListner errorListener;
    private android.app.AlertDialog dialog;

    public void init(Charger charger,OnErrorListner errorListner){
        this.charger=charger;
        this.errorListener=errorListner;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_charger_details, null);
        ImageView icon=(ImageView) view.findViewById(R.id.dcd_icon);
        TextView alias = (TextView) view.findViewById(R.id.dcd_alias);
        TextView address = (TextView) view.findViewById(R.id.dcd_address);
        TextView id = (TextView) view.findViewById(R.id.dcd_id);
        TextView status = (TextView) view.findViewById(R.id.dcd_status);
        TextView type = (TextView) view.findViewById(R.id.dcd_type);
        TextView power = (TextView) view.findViewById(R.id.dcd_power);
        TextView owner = (TextView) view.findViewById(R.id.dcd_owner);
        TextView contact = (TextView) view.findViewById(R.id.dcd_contact);
        TextView price = (TextView) view.findViewById(R.id.dcd_price);
        TextView duration = (TextView) view.findViewById(R.id.dcd_duration);
        TextView cost = (TextView) view.findViewById(R.id.dcd_cost);

        LinearLayout ownerLayout = (LinearLayout) view.findViewById(R.id.dcd_owner_layout);
        LinearLayout contactLayout = (LinearLayout) view.findViewById(R.id.dcd_contact_layout);
        ProgressBar progress = (ProgressBar) view.findViewById(R.id.dcd_progress);

        icon.setImageBitmap(UIHelper.getInstance(getActivity()).getMarkerIcon(charger.getType(), charger.getState()));
        alias.setText(charger.getAlias());
        address.setText(charger.getLocation());
        id.setText(charger.getDevice_id());
        status.setText(charger.getState());
        type.setText(charger.getType());
        power.setText(Math.round(charger.getPower())+" kW");

        price.setText("Rs."+Math.round(charger.getPrice())+" /kWh");

        if (charger.getType().equals("AC")) {
            duration.setText("3½ to 4 hrs");
            cost.setText("Rs.360 - Rs.450");
        }
        else{
            duration.setText("1 hr");
            cost.setText("Rs.750 - Rs.900");
        }

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog = builder.create();
        dialog.setView(view);

        getResponse(charger.getOwner(),progress,ownerLayout,contactLayout,owner,contact);
        return dialog;
    }

    private void getResponse(int owner_id,ProgressBar progress,LinearLayout ownerLayout,LinearLayout contactLayout,TextView owner,TextView contact){
        ServerConnector.getInstance(getActivity()).cancelRequest("GetOwner");
        ServerConnector.getInstance(getActivity()).sendRequest(ServerConnector.SERVER_ADDRESS+"profile/"+owner_id,null, Request.Method.GET,
                new OnResponseListner() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Create a JSON object hierarchy from the results
                            JSONObject responseObject = new JSONObject(response);
                            if (responseObject.has("error")) {
//                                showDialog("Error", responseObject.getString("error"), "GetOwner",
//                                        new Object[]{owner_id}, false);
                            }
                            else {
                                progress.setVisibility(View.GONE);
                                ownerLayout.setVisibility(View.VISIBLE);
                                contactLayout.setVisibility(View.VISIBLE);
                                owner.setText(responseObject.getString("first_name")+" "+responseObject.getString("last_name"));
                                contact.setText("0"+responseObject.getString("contact_number"));

//                                hideProgress();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                errorListener
                ,"GetOwner");
    }
}
