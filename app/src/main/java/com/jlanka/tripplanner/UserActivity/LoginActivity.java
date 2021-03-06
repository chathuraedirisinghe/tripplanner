package com.jlanka.tripplanner.UserActivity;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.android.volley.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.jlanka.tripplanner.GoogleAnalyticsService;
import com.jlanka.tripplanner.MainActivity;
import com.jlanka.tripplanner.NetworkUtil;
import com.jlanka.tripplanner.R;
import com.jlanka.tripplanner.Server.OnErrorListner;
import com.jlanka.tripplanner.Server.OnResponseListner;
import com.jlanka.tripplanner.Server.ServerConnector;
import com.jlanka.tripplanner.UI.ModalFragment;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    public static String mobilenumber;
    @BindView(R.id.input_username) EditText _usernameText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.btn_login) Button _loginButton;
    @BindView(R.id.link_signup) TextView _signupLink;
    @BindView(R.id.link_forgotPassword) TextView _forgotPassword;

    SessionManager session;
    FragmentManager manager = getFragmentManager();
    ModalFragment modal = new ModalFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);
        // Session Manager
        session = new SessionManager(this);

        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });

        _forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ForgotPassword.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void login() {
        if (!validate()) {
//            onLoginFailed();
            _loginButton.setEnabled(true);
            return;
        }

        _loginButton.setEnabled(false);

        String username = _usernameText.getText().toString();
        String password = _passwordText.getText().toString();

        getServerResponse(username,password);

    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        // Staring MainActivity
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    public boolean validate() {
        boolean valid = true;

        String username = _usernameText.getText().toString();
        String password = _passwordText.getText().toString();

        if (username.isEmpty()) {
            _usernameText.setError("Username cannot be empty");
            valid = false;
        } else {
            _usernameText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 6) {
            _passwordText.setError("Pin number should between 4 and 6 numeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    private void getServerResponse(final String username, final String password) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setIndeterminate(true);
        pd.setMessage("Authenticating...");
        pd.show();

        Map<String, String>  params = new HashMap<>();
        // the POST parameters:
        params.put("username", username);
        params.put("password", password);

        ServerConnector.getInstance(getApplicationContext()).cancelRequest("CheckUser");
        ServerConnector.getInstance(getApplicationContext()).login(params,
        new OnResponseListner() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String status = jsonResponse.getString("status");
                    String id = jsonResponse.getString("id");
                    String token = jsonResponse.getString("token");

                    if(status.equals("Login success")){
                        GoogleAnalyticsService.getInstance().setUser(username,"Login");
                        getUserDetails(id, password);
                        session.setToken(token);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        },

        new OnErrorListner() {
            @Override
            public void onError(String error, JSONObject obj) {
                String message=error;
                if (obj!=null) {
                    try {
                        message = obj.getString("status");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                alertDialog.setTitle("Sorry");
                alertDialog.setMessage(message);
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(i);
                    }
                });
                alertDialog.show();
            }
        },"CheckUser");
    }

    private void getUserDetails(String id, final String password) {
        ServerConnector.getInstance(getApplicationContext()).cancelRequest("getUserDetails");
        ServerConnector.getInstance(getApplicationContext()).getRequest(ServerConnector.SERVER_ADDRESS + "ev_owners/" + id + "/",
                new OnResponseListner() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject user_data = new JSONObject(response);
                            String id = user_data.getString("id");
                            String username = user_data.getString("username");
                            String credits = user_data.getString("balance");
                            String fname = user_data.getString("first_name");
                            String lname = user_data.getString("last_name");
                            String email = user_data.getString("email");
                            String mob = user_data.getString("contact_number");
                            JSONArray vehicles = user_data.getJSONArray("electric_vehicles");

//                    if(vehicle.getString(0).equals(null)){
//                        vehicle = "default";
//                    }

                            session.createLoginSession(id, username, password, fname, lname, email, mob, credits, vehicles);
                            onLoginSuccess();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new OnErrorListner() {
                    @Override
                    public void onError(String error, JSONObject obj) {
                        String message=error;
                        if (obj!=null) {
                            try {
                                message = obj.getString("status");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                        alertDialog.setTitle("Sorry");
                        alertDialog.setMessage(message);
                        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(i);
                            }
                        });
                        alertDialog.show();
                    }
                },
                "getUserDetails");
    }

    public static String getMobilenumber() {
        return mobilenumber;
    }
}
