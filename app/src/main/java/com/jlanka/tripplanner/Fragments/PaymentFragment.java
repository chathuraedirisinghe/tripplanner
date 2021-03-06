package com.jlanka.tripplanner.Fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jlanka.tripplanner.GoogleAnalyticsService;
import com.jlanka.tripplanner.R;
import com.jlanka.tripplanner.Server.ServerConnector;
import com.jlanka.tripplanner.UserActivity.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class PaymentFragment extends Fragment {

    View myView;
    WebView mWebView;
    SessionManager session;
    ProgressDialog pd;

    @BindView(R.id.credit_amount) EditText _credit_amount;
    @BindView(R.id.buy_credit) Button _btn_proceed;
    @BindView(R.id.payment_header) TextView _payment_header;

    public PaymentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_payment, container, false);
        mWebView = (WebView) myView.findViewById(R.id.webview);

        ButterKnife.bind(this,myView);

        _payment_header.setVisibility(View.VISIBLE);
        _credit_amount.setVisibility(View.VISIBLE);
        _btn_proceed.setVisibility(View.VISIBLE);
        mWebView.setVisibility(View.GONE);

        // Session class instance
        session = new SessionManager(getActivity().getApplicationContext());

        //This will redirect user to LoginActivity is he is not logged in
        session.checkLogin();

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        String userId=user.get(SessionManager.user_id);

        // Enable Javascript
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        _btn_proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                buyCredits(user_name,user_title,user_credit,user_mobile);
                _credit_amount.setEnabled(false);
                _btn_proceed.setEnabled(false);

                if (!validate()) {
                    _credit_amount.setEnabled(true);
                    _btn_proceed.setEnabled(true);
                    return;
                }else {
                    String url = "https://api.goev.lk/pay/au.com.gateway.IT/pay-mob.php";
                    final String credit_amount = _credit_amount.getText().toString();
                    String postData = null;
                    try {
                        postData = "user_id=" + URLEncoder.encode(userId, "UTF-8")
                                + "&noOfCredit=" + URLEncoder.encode(credit_amount, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        GoogleAnalyticsService.getInstance().trackException(getActivity().getApplicationContext(), e);
                    }
                    _payment_header.setVisibility(View.GONE);
                    _btn_proceed.setVisibility(View.GONE);
                    _credit_amount.setVisibility(View.GONE);
                    mWebView.setVisibility(View.VISIBLE);

                    // Force links and redirects to open in the WebView instead of in a browser
                    String finalPostData = postData;
                    mWebView.setWebViewClient(new WebViewClient() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                            super.onReceivedError(view, request, error);
                            final android.app.AlertDialog.Builder dlgAlert = new android.app.AlertDialog.Builder(getActivity().getWindow().getContext());
                            dlgAlert.setMessage(error.getDescription());
                            dlgAlert.setTitle("Sorry");
                            String positiveButtonText = "OK";

                            dlgAlert.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });

                            dlgAlert.setCancelable(false);
                            android.app.AlertDialog ad = dlgAlert.create();
                            ad.show();
                            dlgAlert.setIcon(R.drawable.logo);
                        }
                    });

                    mWebView.setWebChromeClient(new WebChromeClient() {
                        public void onProgressChanged(WebView view, int progress) {
                            if (progress == 100) {
                                if (pd.isShowing()) {
                                    pd.dismiss();
                                }
                            } else if (pd == null)
                                pd = ProgressDialog.show(getActivity(), "", "Please Wait...", true);
                            else if (!pd.isShowing())
                                pd = ProgressDialog.show(getActivity(), "", "Please Wait...", true);
                        }

                        public boolean onConsoleMessage(ConsoleMessage cmsg)
                        {
                            try {
                                if (cmsg.message().startsWith("Raw response")) {
                                    JSONObject obj = new JSONObject(cmsg.message().replace("Raw response :  ", ""));
                                    JSONObject responseData = obj.getJSONObject("responseData");
                                    String responseCode = responseData.getString("responseCode");

                                    //process JSON
                                    if (responseCode.equals("00")) {
                                        GoogleAnalyticsService.getInstance().setAction("Payment", "Success", session.getUserID() + "," + credit_amount);
                                    }
                                    else
                                        GoogleAnalyticsService.getInstance().setAction("Payment", "Failed", session.getUserID() + "," + credit_amount);
                                }
                            } catch(JSONException e){
                                e.printStackTrace();
                            }
                            return true;
                        }
                    });
                    mWebView.postUrl(url, postData.getBytes());
                }
            }
        });

        //----------------------------------Thiwanka----------------------------------
        GoogleAnalyticsService.getInstance().setScreenName(this.getClass().getSimpleName());

        return myView;
    }

    private void buyCredits(String name, String title, String credit, String mobile) {
        if (!validate()) {
            _credit_amount.setEnabled(true);
            _btn_proceed.setEnabled(true);
            return;
        }else {
            String url = "https://www.goev.lk/pay/au.com.gateway.IT/pay-mob.php";
            final String credit_amount = _credit_amount.getText().toString();
            String postData = null;
            try {

                postData = "mobNo=" + URLEncoder.encode(mobile, "UTF-8")
                        + "&noOfCredit=" + URLEncoder.encode(credit_amount, "UTF-8")
                        + "&title=" + URLEncoder.encode(title, "UTF-8")
                        + "&FName=" + URLEncoder.encode(name, "UTF-8")
                        + "&credit=" + URLEncoder.encode(credit, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            _payment_header.setVisibility(View.GONE);
            _btn_proceed.setVisibility(View.GONE);
            _credit_amount.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
            mWebView.postUrl(url, postData.getBytes());

            // Force links and redirects to open in the WebView instead of in a browser
            mWebView.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageFinished(WebView view,String url){
                    super.onPageFinished(view,url);
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    super.onReceivedError(view, request, error);
                }
            });
        }
    }

    private boolean validate() {
        boolean valid = true;
        String credit_amount = _credit_amount.getText().toString();
        if (credit_amount.isEmpty() || Integer.valueOf(credit_amount)<50 ) {
            _credit_amount.setError("The minimum amount is Rs. 50");
            GoogleAnalyticsService.getInstance().setAction("Payment","Preferred Amount",credit_amount);
            valid = false;
        } else {
            _credit_amount.setError(null);
        }
        return valid;
    }
}


