package com.jlanka.jltripplanner.Fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
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
import com.jlanka.jltripplanner.GoogleAnalyticsService;
import com.jlanka.jltripplanner.R;
import com.jlanka.jltripplanner.Server.ServerConnector;
import com.jlanka.jltripplanner.UserActivity.ForgotPassword;
import com.jlanka.jltripplanner.UserActivity.SessionManager;

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
    String user_fname, user_lname,user_title,user_credit,user_mobile;

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

        user_fname = user.get(SessionManager.user_fname);
        user_lname = user.get(SessionManager.user_lname);
        user_title = user.get(SessionManager.user_title);
        user_credit = user.get(SessionManager.user_credit);
        user_mobile = user.get(SessionManager.user_mobile);

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
                    pd.dismiss();
                    _credit_amount.setEnabled(true);
                    _btn_proceed.setEnabled(true);
                    return;
                }else {
                    String url = "https://www.goev.lk/pay/au.com.gateway.IT/pay-mob.php";
                    final String credit_amount = _credit_amount.getText().toString();
                    String postData = null;
                    String fullname = user_fname + " " + user_lname;
                    try {
                        postData = "mobNo=" + URLEncoder.encode(user_mobile, "UTF-8")
                                + "&noOfCredit=" + URLEncoder.encode("10", "UTF-8")
                                + "&title=" + URLEncoder.encode("", "UTF-8")
                                + "&FName=" + URLEncoder.encode(fullname, "UTF-8")
                                + "&credit=" + URLEncoder.encode(user_credit, "UTF-8");
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
                    mWebView.postUrl(url, postData.getBytes());

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

                                    Calendar c = Calendar.getInstance();
                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    String formattedDate = df.format(c.getTime());

                                    //process JSON
                                    if (responseCode.equals("00")) {
                                        GoogleAnalyticsService.getInstance().setAction("Payment", "Success", session.getUserID() + "," + credit_amount);
                                        sendServerRequest(session.getUserID(), credit_amount, formattedDate);
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
        if (credit_amount.isEmpty() || Integer.valueOf(credit_amount)<49 ) {
            _credit_amount.setError("The minimum amount is Rs. 50");
            GoogleAnalyticsService.getInstance().setAction("Payment","Preferred Amount",credit_amount);
            valid = false;
        } else {
            _credit_amount.setError(null);
        }
        return valid;
    }

    private void sendServerRequest(final String userId, final String amount, final String dateTime) {
        final ProgressDialog pd = ProgressDialog.show(getActivity(), "", "Please Wait...", true);
        pd.setCancelable(false);
        _credit_amount.setEnabled(false);
        _btn_proceed.setEnabled(false);
        RequestQueue rq = Volley.newRequestQueue(getActivity());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ServerConnector.SERVER_ADDRESS+"ev_owners/account/recharge/",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (pd != null && pd.isShowing()) {
                            pd.dismiss();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", userId);
                params.put("amount", amount);
                params.put("date_time", dateTime);
                return params;
            }
        };
        rq.add(stringRequest);
    }
}


