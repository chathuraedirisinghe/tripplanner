package com.jlanka.jltripplanner.UserActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.jlanka.jltripplanner.R;

import com.jlanka.jltripplanner.GoogleAnalyticsService;
import com.jlanka.jltripplanner.Server.OnErrorListner;
import com.jlanka.jltripplanner.Server.OnResponseListner;
import com.jlanka.jltripplanner.Server.ServerConnector;

public class SignupActivity extends Activity {
    private static final String TAG = "SignupActivity";
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private ProfileTracker mProfileTracker;
    @BindView(R.id.sign_form) LinearLayout form;
    @BindView(R.id.singup_buttons) LinearLayout buttonsLayout;
    @BindView(R.id.input_username) EditText _username;
    @BindView(R.id.input_fname) EditText _fname;
    @BindView(R.id.input_lname) EditText _lname;
    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_mobile) EditText _mobileText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.btn_signup) Button _signupButton;
    @BindView(R.id.signup_manual) Button manualSignup;
    @BindView(R.id.link_login) TextView _loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _signupButton.setEnabled(false);
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(
                 "email","public_profile"));
        callbackManager = CallbackManager.Factory.create();

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                Profile profile = Profile.getCurrentProfile();

                // Facebook Email address
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object,GraphResponse response) {

                                Log.v("LoginActivity Response ", response.toString());

                                try {

                                    if (object.has("email")) {
                                        _emailText.setText(object.getString("email"));
                                    }
                                    if (object.has("first_name")) {
                                        _username.setText(object.getString("first_name"));
                                        _fname.setText(object.getString("first_name"));
                                    }
                                    if (object.has("last_name")) {
                                        _lname.setText(object.getString("last_name"));
                                    }

                                    buttonsLayout.setVisibility(View.GONE);
                                    form.setVisibility(View.VISIBLE);
                                    LoginManager.getInstance().logOut();
                                    //Toast.makeText(getApplicationContext(), "Name " + Name, Toast.LENGTH_LONG).show();


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "email,first_name,last_name");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                LoginManager.getInstance().logOut();
            }

            @Override
            public void onError(FacebookException e) {
                AlertDialog alertDialog = new AlertDialog.Builder(SignupActivity.this).create();
                alertDialog.setTitle("Sorry");
                alertDialog.setMessage(e.toString());
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });

        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                // Fetch user details from New Profile
            }
        };

        manualSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonsLayout.setVisibility(View.GONE);
                form.setVisibility(View.VISIBLE);
            }
        });
    }

    public void signup() {

        if (!validate()) {
            onSignupFailed();
            _signupButton.setEnabled(true);
            return;
        }

        /*final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this, R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();*/

        String username = _username.getText().toString();
        String fname = _fname.getText().toString();
        String lname = _lname.getText().toString();
        String mobile = _mobileText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        // TODO: Send Server Request-->

        getServerRequest(username,fname,lname,mobile,email,password);

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success
//                        onSignupSuccess();
                        // onSignupFailed();
                        //progressDialog.dismiss();
                    }
                }, 3000);
    }

    private void getServerRequest(String username, String fname, String lname, String mobile, String email, String password) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setIndeterminate(true);
        pd.setCancelable(false);
        pd.setMessage("Creating Account...");
        pd.show();

        Map<String, String>  params = new HashMap<>();
        // the POST parameters:
        params.put("username", username);
        params.put("first_name",fname);
        params.put("last_name",lname);
        params.put("contact_number", mobile);
        params.put("email",email);
        params.put("password", password);
        //params.put("electric_vehicles","[]");

        ServerConnector.getInstance(getApplicationContext()).cancelRequest("SignUp");
        ServerConnector.getInstance(getApplicationContext()).sendRequest(ServerConnector.SERVER_ADDRESS+"ev_owners/",params,Request.Method.POST,
                new OnResponseListner() {
                    @Override
                    public void onResponse(String response) {

                        if(pd.isShowing()){
                            pd.dismiss();
                        }
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            Log.w("Register Response   ", String.valueOf(jsonResponse));
                            if (jsonResponse.getString("id").isEmpty()){
                                accountCreateFailed();
                            }else{
                                //-----------------------Thiwanka----------------------------
                                GoogleAnalyticsService.getInstance().setAction("User","Signup",username);

                                activateAccount();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

        ,new OnErrorListner() {
            @Override
            public void onError(String error, JSONObject obj) {
                String message=error;
                try {
                    if (obj.has("username"))
                        message="Username : "+obj.getJSONArray("username").get(0).toString();
                    else if (obj.has("email"))
                        message="Email : "+obj.getJSONArray("email").get(0).toString();
                    else if (obj.has("contact_number"))
                        message="Contact no : "+obj.getJSONArray("contact_number").get(0).toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                AlertDialog alertDialog = new AlertDialog.Builder(SignupActivity.this).create();
                alertDialog.setTitle("Sorry");
                alertDialog.setMessage(message);
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        pd.hide();
                        _signupButton.setEnabled(true);
                    }
                });
                alertDialog.show();
            }
        },"SignUp");
    }

    private void smsFailedAlert() {
        //Account Creating Failed
        final AlertDialog ad = new AlertDialog.Builder(SignupActivity.this).create();
        ad.setTitle("Verification code sms failed..");
        ad.setMessage("Please Try again later..");
        ad.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(ad!=null && ad.isShowing()){
                            ad.dismiss();
                            _signupButton.setEnabled(true);
                        }
                    }
                });
        ad.show();

    }

    private void accountCreateFailed() {
        //Account Creating Failed
        final AlertDialog dialog1 = new AlertDialog.Builder(SignupActivity.this).create();
        dialog1.setTitle("Registration Failed");
        dialog1.setMessage("Please Try again later..");
        dialog1.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(dialog1!=null && dialog1.isShowing()){
                            dialog1.dismiss();
                        }
                    }
                });
        dialog1.show();
        _signupButton.setEnabled(true);
    }

    private void activateAccount() {
        final AlertDialog dialog2 = new AlertDialog.Builder(SignupActivity.this).create();
        dialog2.setTitle("Registration Successful");
        dialog2.setMessage("You will receive an activation email shortly..");
        dialog2.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(dialog2!=null && dialog2.isShowing()){
                            dialog2.dismiss();
                            Intent myIntent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(myIntent);
                        }
                    }
                });
        dialog2.show();
    }

    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {

        final AlertDialog dialog3 = new AlertDialog.Builder(SignupActivity.this).create();
        dialog3.setTitle("Registration Failed");
        dialog3.setMessage("Please try again later...");
        dialog3.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(dialog3!=null && dialog3.isShowing()){
                            dialog3.dismiss();
                            _signupButton.setEnabled(true);
                        }
                    }
                });
        dialog3.show();
    }

    public boolean validate() {
        boolean valid = true;
        Pattern sPattern = Pattern.compile("07[\\d]{8}");
        String username = _username.getText().toString();
        String first_name = _fname.getText().toString();
        String last_name = _lname.getText().toString();
        String user_mobile = _mobileText.getText().toString();
        String user_email = _emailText.getText().toString();
        String user_password = _passwordText.getText().toString();

        if (username.isEmpty() || username.length() < 2 || username.length()>20) {
            _username.setError("2 to 20 characters required. Letters, digits and @/./+/-/_ only.");
            valid = false;
        } else {
            _username.setError(null);
        }

        if (first_name.isEmpty()) {
            _fname.setError("First Name cannot be empty. And only letters allowed");
            valid=false;
        } else {
            _fname.setError(null);
        }

        if (last_name.isEmpty()) {
            _fname.setError("First Name cannot be empty. And only letters allowed");
            valid = false;
        } else {
            _lname.setError(null);
        }

        if (!sPattern.matcher(user_mobile).matches()){
            _mobileText.setError("Not a valid phone number");
            valid = false;
        }
        else if(user_mobile.isEmpty() || !Patterns.PHONE.matcher(user_mobile).matches() || user_mobile.length()>10 || user_mobile.length()<10 ){
            _mobileText.setError("Length should be 10 digits");
            valid = false;
        }else{
            _mobileText.setError(null);
        }

        if (user_email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(user_email).matches()) {
            _emailText.setError("Enter a valid email address.");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (user_password.isEmpty() || user_password.length()<4 || user_password.length()>6) {
            _passwordText.setError("Pin length should be between 4 - 6 digits.");
            valid = false;
        } else {
            _passwordText.setError(null);
        }
        return valid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mProfileTracker.stopTracking();
    }
}
