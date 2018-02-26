package com.jlanka.jltripplanner.UI;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.jlanka.jltripplanner.Fragments.MapFragment;
import com.jlanka.jltripplanner.R;
import com.jlanka.jltripplanner.UserActivity.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Observable;

/**
 * Created by Workstation on 2/24/2018.
 */

public class StartChargingDialog extends DialogFragment {

    private String selectedMarker,vehicles;
    private DialogInterface.OnClickListener positiveButtonListener;
    private ArrayList<String> vIds=new ArrayList<>();
    Spinner spinner;

    public void init(String selectedMarker, String vehicles, DialogInterface.OnClickListener positiveButtonListener){
        this.selectedMarker=selectedMarker;
        this.vehicles=vehicles;
        this.positiveButtonListener=positiveButtonListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_select_vehicle, null);

        spinner = (Spinner) view.findViewById(R.id.vehicle_spinner);
        ArrayList<String> ar = new ArrayList<String>();

        String reg_num;
        try {

            JSONArray obj = new JSONArray(vehicles);

            if (obj.length()>0) {
                for (int i = 0; i < obj.length(); i++) {
                    JSONObject vehicle_object = obj.getJSONObject(i);
                    final String model = vehicle_object.getString("model");
                    final String reg_no = vehicle_object.getString("reg_no");
                    vIds.add(vehicle_object.getString("id"));
                    ar.add(reg_no + " " + model);
                }
            }
            else {
                getDialog().dismiss();
                Toast.makeText(getActivity(), "No vehicles found, please add a vehicle !", Toast.LENGTH_LONG).show();
            }
        } catch (Throwable t) {
        }

        String[] myVehicle = ar.toArray(new String[0]);
        ArrayAdapter<String> myVehicleGUIArray = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item, myVehicle);
        myVehicleGUIArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        myVehicleGUIArray.notifyDataSetChanged();
        spinner.setAdapter(myVehicleGUIArray);

        builder.setView(view);
        builder.setPositiveButton("OK", positiveButtonListener);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        android.app.AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        return dialog;
    }

    public String getSelectedVID(){
        if (vIds.size()>0)
            return vIds.get(spinner.getSelectedItemPosition());
        else
            return "";
    }
}
