package com.jlanka.tripplanner.UI;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_plug_in, null);
        setCancelable(false);
        return view;
    }
}
