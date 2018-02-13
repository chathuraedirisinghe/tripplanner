package com.jlanka.evtripplanner.Fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.jlanka.evtripplanner.GoogleAnalyticsService;
import com.jlanka.evtripplanner.R;
import com.jlanka.evtripplanner.Server.ServerConnector;
import com.jlanka.evtripplanner.UserActivity.SessionManager;

import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment {

    @BindView(R.id.tv_name) TextView tv_name;
    @BindView(R.id.tv_mobile) TextView tv_mobile;
    @BindView(R.id.btn_logout) Button _btn_logout;
    @BindView(R.id.btn_chg_password) Button _btn_chg_password;

    SessionManager session;
    String user_fname, user_lname, user_title, user_mail, user_mobile, user_passwd;

    private EditText et_old_password,et_new_password;
    private TextView tv_message;
    private AlertDialog dialog;
    private ProgressBar progress;

    View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, mView);

        _btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.logoutUser();
            }
        });

        _btn_chg_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        //----------------------------------Thiwanka----------------------------------
        GoogleAnalyticsService.getInstance().setScreenName(this.getClass().getSimpleName());

        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        session = new SessionManager(getActivity().getApplicationContext());

        //This will redirect user to LoginActivity is he is not logged in
        session.checkLogin();

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();

        user_fname = user.get(SessionManager.user_fname);
        user_lname = user.get(SessionManager.user_lname);
        user_title = user.get(SessionManager.user_title);
        user_mobile = user.get(SessionManager.user_mobile);
        user_passwd = user.get(SessionManager.user_pin);

        tv_name.setText(user_fname+" "+user_lname);
        tv_mobile.setText(user_mobile);

    }

    private void showDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_change_password, null);
        et_old_password = (EditText) view.findViewById(R.id.et_old_password);
        et_new_password = (EditText) view.findViewById(R.id.et_new_password);
        tv_message = (TextView) view.findViewById(R.id.tv_message);
        progress = (ProgressBar) view.findViewById(R.id.progress);
        builder.setView(view);
        builder.setTitle("Change PIN");
        builder.setPositiveButton("Change PIN", new DialogInterface.OnClickListener() {
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
        dialog = builder.create();
        dialog.show();
        dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String old_password = et_old_password.getText().toString();
                String new_password = et_new_password.getText().toString();
                if ((!old_password.isEmpty() && !new_password.isEmpty()) && (old_password.equals(user_passwd))) {
                    progress.setVisibility(View.VISIBLE);
                    changePasswordProcess(user_mobile, old_password, new_password);

                } else if(old_password.isEmpty() && new_password.isEmpty()) {
                    tv_message.setVisibility(View.VISIBLE);
                    tv_message.setText("Fields are empty");
                }else if(!old_password.equals(user_passwd)){
                    tv_message.setVisibility(View.VISIBLE);
                    tv_message.setText("Current PIN is Wrong");
                }else if(new_password.length()<4){
                    tv_message.setVisibility(View.VISIBLE);
                    tv_message.setText("PIN length should greater than 4 digit");
                }
            }
        });
    }

    private void changePasswordProcess(final String user_mobile, final String old_password, final String new_password) {

        RequestQueue rq = Volley.newRequestQueue(getActivity());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ServerConnector.SERVER_ADDRESS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("Server Respond : "+response);
                        if (response.contains("1")) {
                            progress.setVisibility(View.GONE);
                            tv_message.setVisibility(View.GONE);
                            dialog.dismiss();
                            Snackbar.make(getView(), "PIN Changed Successfully.", Snackbar.LENGTH_LONG).show();

                            AlertDialog.Builder db = new AlertDialog.Builder(getActivity());

                            db.setTitle("Successfully Changed!")
                                    .setMessage("You will automatically redirect to Login page")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            session.logoutUser();
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        } else {
                            progress.setVisibility(View.GONE);
                            tv_message.setVisibility(View.VISIBLE);
                            tv_message.setText("Unable to Change PIN.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progress.setVisibility(View.GONE);
                        tv_message.setVisibility(View.VISIBLE);
                        tv_message.setText(error.getLocalizedMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("fnID", "8");
                params.put("mobNo", user_mobile);
                params.put("passwd", old_password);
                params.put("newPasswd", new_password);
                return params;
            }
        };
        rq.add(stringRequest);
    }
}
