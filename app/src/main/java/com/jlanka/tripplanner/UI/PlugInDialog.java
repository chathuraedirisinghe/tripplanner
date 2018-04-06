package com.jlanka.tripplanner.UI;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.jlanka.tripplanner.R;
import com.jlanka.tripplanner.Server.OnResponseListner;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Workstation on 2/24/2018.
 */

public class PlugInDialog extends DialogFragment {

    private String selectedMarker,vehicles;
    private ArrayList<String> vIds=new ArrayList<>();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_plug_in, null);
        builder.setView(view);
        android.app.AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }
}
