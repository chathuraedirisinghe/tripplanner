package com.jlanka.tripplanner.UI;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.jlanka.tripplanner.R;

/**
 * Created by Workstation on 2/24/2018.
 */

public class RouteDialog extends DialogFragment{
    private DialogInterface.OnClickListener clickListener;
    private AlertDialog dialog;
    private TextView tv;
    EditText et;

    public void init(DialogInterface.OnClickListener clickListener){
        this.clickListener=clickListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_route, null);
        et = (EditText) view.findViewById(R.id.batt_cap);
        tv = (TextView) view.findViewById(R.id.route_err_message);
        builder.setView(view);
        builder.setPositiveButton("OK", clickListener);
        android.app.AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        return dialog;
    }

    public boolean isValid(){
        boolean valid=false;
        try {
            if (et!=null && et.getText().length()>0){
                if (Integer.parseInt(et.getText().toString())<101){
                    valid=true;
                }
                else{
                    if (tv!=null) {
                        tv.setText("Range on current charge cannot be greater than 150kms");
                        tv.setVisibility(View.VISIBLE);
                    }
                }
            }
            else {
                if (tv!=null) {
                    tv.setText("Range on current charge required");
                    tv.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            if (tv!=null) {
                tv.setText("Invalid input");
                tv.setVisibility(View.VISIBLE);
            }
        }
        return valid;
    }

    public String getRange(){
        return et.getText().toString();
    }
}
