package com.jlanka.jltripplanner.UserActivity;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.jlanka.jltripplanner.MainActivity;
import com.jlanka.jltripplanner.NetworkUtil;
import com.jlanka.jltripplanner.R;
import com.jlanka.jltripplanner.Server.OnErrorListner;
import com.jlanka.jltripplanner.Server.OnResponseListner;
import com.jlanka.jltripplanner.Server.ServerConnector;
import com.jlanka.jltripplanner.UI.ModalFragment;

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
        checkNetwork();

        // Session Manager
        session = new SessionManager(getApplicationContext());

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

                // TODO: Implement successful signup logic here
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
        Log.d(TAG, "Login");

        if (!validate()) {
//            onLoginFailed();
            _loginButton.setEnabled(true);
            return;
        }

        _loginButton.setEnabled(false);

        String username = _usernameText.getText().toString();
        String password = _passwordText.getText().toString();

        // TODO: Implement--> Get Data from Database.

        getServerResponse(username,password);
        System.out.println("USERNAME : " +username+ "    Password : "+password);

    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        // Staring MainActivity
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
    }

    public void onLoginFailed() {
        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
        alertDialog.setTitle("Invalid Credentials");
        alertDialog.setMessage("Please check your mobile number & PIN number again...");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();

//        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        _passwordText.setText("");
        _loginButton.setEnabled(true);
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

        if (password.isEmpty() || password.length() < 3 || password.length() > 10) {
            _passwordText.setError("Pin number should between 3 and 6 numeric characters");
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

        final ServerConnector serverConnector= new ServerConnector(ServerConnector.SERVER_ADDRESS+"ev_owners/login/",params,Request.Method.POST,this);
        serverConnector.setOnReponseListner(new OnResponseListner() {
            @Override
            public void onResponse(String response) {

                if(pd.isShowing()){
                    pd.dismiss();
                }

                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    Log.w("Login Response   ", String.valueOf(jsonResponse));
                    String error = jsonResponse.getString("error_code");
                    String status = jsonResponse.getString("status");
                    String id = jsonResponse.getString("id");

                    if(error.equals("0")){
                        getUserDetails(id, password);

                    } else if(error.equals("1") && (status.contains("verify"))){
                        final AlertDialog ad = new AlertDialog.Builder(LoginActivity.this).create();
                        ad.setTitle("Verify Account");
                        ad.setMessage("Please check your email and verify account.");
                        ad.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(ad.isShowing()) {
                                            dialog.dismiss();
                                            send_verification_email();
                                        }
                                    }
                                });
                        ad.show();

//                      not registered setMobilenumber(mobNo); modal.show(manager, "My Cart");
                    }else if(error.equals("1") && (status.contains("incorrect"))) {
                        onLoginFailed();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        serverConnector.setOnErrorListner(new OnErrorListner() {
            @Override
            public void onError(String error, JSONObject obj) {
                String message=error;
                if (obj!=null) {
                    System.out.println("SERVER RESPONSE : " + error + "OBJ : " + obj.toString());
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
        });
        serverConnector.sendRequest();
    }

    private void getUserDetails(String id, final String password) {
        ServerConnector serverConnector= new ServerConnector(ServerConnector.SERVER_ADDRESS+"ev_owners/"+id+"/",null,Request.Method.GET,this);
        serverConnector.setOnReponseListner(new OnResponseListner() {
            @Override
            public void onResponse(String response) {
                Log.w("User Details : ", String.valueOf(response));
                try{
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
        });
        serverConnector.sendRequest();
    }

    private void send_verification_email() {
    }

    public static String getMobilenumber() {
        return mobilenumber;
    }

    public static void setMobilenumber(String mobilenumber) {
        LoginActivity.mobilenumber = mobilenumber;
    }

    public void checkNetwork(){
        String ss = NetworkUtil.getConnectivityStatusString(this);

        if(ss.contains("Not connected")){
            AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
            alertDialog.setTitle("No Internet");
            alertDialog.setMessage("Please check your internet Connection & try again...");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Exit",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                            System.exit(0);
                        }
                    });
            alertDialog.show();
        }
    }
}
