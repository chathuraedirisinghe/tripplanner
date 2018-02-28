package com.jlanka.jltripplanner.UI;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.jlanka.jltripplanner.R;
import com.jlanka.jltripplanner.Server.ServerConnector;
import com.jlanka.jltripplanner.UserActivity.LoginActivity;

/**
 * Created by chathura on 11/22/17.
 */

public class ModalFragment extends DialogFragment{

    View mView;
    Button submit,login;
    @BindView(R.id.txt_your_pin) EditText _pin;
//    @Nullable @BindView(R.id.input_phone) EditText _phone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView=inflater.inflate(R.layout.modal_account_confirm, null);
        ButterKnife.bind(this,mView);

        login = (Button)getActivity().findViewById(R.id.btn_login);

        submit =(Button)mView.findViewById(R.id.activate);

        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pin = _pin.getText().toString();
                String mobile = ((LoginActivity)getActivity()).getMobilenumber();
                activateAccount(mobile,pin);
            }
        });

    }

    private void activateAccount(final String mobile, final String pin) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, ServerConnector.SERVER_ADDRESS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);

                            String title = jsonResponse.getString("activated");
                            if(title.equals("0")){
                                //not registered
                                activationFailed();

                            }else if(title.equals("1")){
                                //Activated
                                activationSuccess();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        errorNetwork();

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("fnID", "12");
                params.put("mobNo", mobile);
                params.put("code", pin);
                return params;
            }
        };
        Volley.newRequestQueue(getActivity()).add(postRequest);
    }

    private void activationSuccess() {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Activation Successful");
        alertDialog.setMessage("Please Login...");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        login.setEnabled(true);
                        getDialog().dismiss();

                    }
                });
        alertDialog.show();
    }

    private void activationFailed() {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Activation Failed");
        alertDialog.setMessage("Invalid PIN number. Please check your SMS and Try again.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        login.setEnabled(true);
                        getDialog().dismiss();
                    }
                });
        alertDialog.show();
    }

    private void errorNetwork() {
        AlertDialog errorNet = new AlertDialog.Builder(getActivity()).create();
        errorNet.setTitle("Activation Failed");
        errorNet.setMessage("Pleae try again later.");
        errorNet.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        login.setEnabled(true);
                        getDialog().dismiss();
                    }
                });
        errorNet.show();
    }
}
