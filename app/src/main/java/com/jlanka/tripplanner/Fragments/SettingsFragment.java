package com.jlanka.tripplanner.Fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.jlanka.tripplanner.GoogleAnalyticsService;
import com.jlanka.tripplanner.R;
import com.jlanka.tripplanner.Server.OnErrorListner;
import com.jlanka.tripplanner.Server.OnResponseListner;
import com.jlanka.tripplanner.Server.ServerConnector;
import com.jlanka.tripplanner.UserActivity.SessionManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment {

    @BindView(R.id.tv_name) TextView tv_name;
    @BindView(R.id.tv_mobile) TextView tv_mobile;
    @BindView(R.id.btn_logout) Button _btn_logout;
    @BindView(R.id.btn_chg_password) Button _btn_chg_password;

    SessionManager session;
    String user_name,user_fname, user_lname, user_title, user_mail, user_mobile, user_passwd;

    private EditText et_old_password,et_new_password;
    private TextView tv_message;
    private AlertDialog dialog;
    ProgressDialog pd;

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
        user_name = user.get(SessionManager.user_name);
        user_fname = user.get(SessionManager.user_fname);
        user_lname = user.get(SessionManager.user_lname);
        user_title = user.get(SessionManager.user_title);
        user_mobile = user.get(SessionManager.user_mobile);
        user_passwd = user.get(SessionManager.pass_word);

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

                if(!old_password.isEmpty() || !new_password.isEmpty()) {
                    if (new_password.length() > 3 && new_password.length() < 7) {
                        if (!old_password.equals(user_passwd)) {
                            tv_message.setVisibility(View.VISIBLE);
                            tv_message.setText("Current PIN is Wrong");
                        }
                        else {
                            changePasswordProcess(user_name, old_password, new_password);
                        }
                    }
                    else{
                        tv_message.setVisibility(View.VISIBLE);
                        tv_message.setText("PIN length should be between 4 - 6 digit");
                    }
                }
                else{
                    tv_message.setVisibility(View.VISIBLE);
                    tv_message.setText("All Fields are required");
                }
            }
        });
    }

    private void changePasswordProcess(final String user_name, final String old_password, final String new_password) {
        pd = ProgressDialog.show(getActivity(), "", "Please Wait...", true);

        Map<String, String> params = new HashMap<String, String>();
        params.put("username",user_name );
        params.put("password", old_password);
        params.put("new_password", new_password);

        ServerConnector.getInstance(getActivity()).cancelRequest("ChangePassword");
        ServerConnector.getInstance(getActivity()).sendRequest(ServerConnector.SERVER_ADDRESS+"ev_owners/change_password/",params, Request.Method.POST,
        new OnResponseListner() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (obj.getString("status").equals("Password changed!")) {
                        pd.dismiss();
                        tv_message.setVisibility(View.GONE);
                        dialog.dismiss();
                        AlertDialog.Builder db = new AlertDialog.Builder(getActivity());

                        db.setTitle("Successfully Changed!")
                                .setMessage("You will automatically redirect to Login page")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        session.logoutUser();
                                    }
                                })
                                .show();
                    } else {
                        pd.dismiss();
                        tv_message.setVisibility(View.VISIBLE);
                        tv_message.setText("Unable to Change PIN.");
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },

        new OnErrorListner() {
            @Override
            public void onError(String error, JSONObject obj) {
                if (obj!=null)

                pd.dismiss();
                tv_message.setVisibility(View.VISIBLE);
                tv_message.setText(error);
            }
        },"ChangePassword");
    }
}
