package com.jlanka.jltripplanner.UI;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.jlanka.jltripplanner.Adapters.Vehicle;
import com.jlanka.jltripplanner.Helpers.UIHelper;
import com.jlanka.jltripplanner.R;
import com.jlanka.jltripplanner.Server.OnErrorListner;
import com.jlanka.jltripplanner.Server.OnResponseListner;
import com.jlanka.jltripplanner.Server.ServerConnector;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Workstation on 2/26/2018.
 */

public class EditProfileDialog extends DialogFragment {

    ProgressBar progress;
    TextView errorMessage;
    private AlertDialog dialog;
    private OnResponseListner responseListner;
    private String userId,fName,lName,contactNo,email,username,password;

    public void init(String id,String fName,String lName,String contactNo,String email,String username,String password,OnResponseListner responseListner){
        userId=id;
        this.fName=fName;
        this.lName=lName;
        this.contactNo=contactNo;
        this.email=email;
        this.username=username;
        this.password=password;
        this.responseListner=responseListner;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater li = LayoutInflater.from(getActivity().getBaseContext());
        View view = li.inflate(R.layout.dialog_profile_edit, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        EditText fname=view.findViewById(R.id.prof_edit_fname);
        EditText lname=view.findViewById(R.id.prof_edit_lname);
        EditText contact=view.findViewById(R.id.prof_edit_contact);
        progress = (ProgressBar) view.findViewById(R.id.prof_edit_progress);
        errorMessage=view.findViewById(R.id.prof_edit_error);

        builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        fname.setText(fName);
        lname.setText(lName);
        contact.setText(contactNo);

        dialog = builder.create();
        dialog.show();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValid(fname.getText().toString(),lname.getText().toString(),contact.getText().toString())){
                    progress.setVisibility(View.VISIBLE);
                    sendNewData(fname.getText().toString(),lname.getText().toString(),contact.getText().toString(),email);
                }


            }
        });
        return dialog;
    }

    private void sendNewData(String fname, String lname, String contact,String email){

        Map<String, String>  params = new HashMap<>();
        params.put("first_name", fname);
        params.put("last_name", lname);
        params.put("contact_number", contact);
        params.put("email",email);
        params.put("username",username);
        params.put("password",password);

        int id=Integer.parseInt(userId);

        ServerConnector.getInstance(getActivity()).cancelRequest("GetOwner");
        ServerConnector.getInstance(getActivity()).sendRequest(ServerConnector.SERVER_ADDRESS + "ev_owners/"+id+"/", params, Request.Method.PUT,
                responseListner,
                new OnErrorListner() {
                    @Override
                    public void onError(String error, JSONObject obj) {
                        progress.setVisibility(View.GONE);
                        String message=error;
                        try {
                            if (obj != null) {
                                if (obj.has("contact_number"))
                                    message = "Contact no : " + obj.getJSONArray("contact_number").get(0).toString();
                                else if (obj.has("email"))
                                    message = "Email : " + obj.getJSONArray("email").get(0).toString();
                            }
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        errorMessage.setText(message);
                        errorMessage.setVisibility(View.VISIBLE);
                    }
                }
                , "GetOwner");
    }

    private boolean isValid(String fname,String lname,String contact){
        boolean valid=true;
        Pattern sPattern = Pattern.compile("07[\\d]{8}");

        if (fname.isEmpty()) {
            errorMessage.setText("First Name cannot be empty. And only letters allowed");
            errorMessage.setVisibility(View.VISIBLE);
            valid=false;
        } else {
            errorMessage.setText("");
            errorMessage.setVisibility(View.INVISIBLE);
        }

        if (lname.isEmpty()) {
            errorMessage.setText("First Name cannot be empty. And only letters allowed");
            errorMessage.setVisibility(View.VISIBLE);
            valid = false;
        } else {
            errorMessage.setText("");
            errorMessage.setVisibility(View.INVISIBLE);
        }

        if (!sPattern.matcher(contact).matches()){
            errorMessage.setText("Not a valid phone number");
            errorMessage.setVisibility(View.VISIBLE);
            valid = false;
        }
        else if(contact.isEmpty() || !Patterns.PHONE.matcher(contact).matches() || contact.length()>10 || contact.length()<10 ){
            errorMessage.setText("Not a valid phone number");
            errorMessage.setVisibility(View.VISIBLE);
            valid = false;
        }else{
            errorMessage.setText("");
            errorMessage.setVisibility(View.INVISIBLE);
        }
        return valid;
    }
}
