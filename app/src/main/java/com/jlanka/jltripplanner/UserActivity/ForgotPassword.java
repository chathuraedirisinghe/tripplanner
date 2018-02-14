package com.jlanka.jltripplanner.UserActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.jlanka.jltripplanner.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ForgotPassword extends AppCompatActivity {

    @BindView(R.id.forgotPassword) WebView _forgotWebView;
    @BindView(R.id.link_toLogin) TextView _loginLink;
    private ProgressDialog progressBar;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ButterKnife.bind(this);

        progressBar = ProgressDialog.show(ForgotPassword.this, "Loading", "Please wait...");
        _forgotWebView.loadUrl("http://yvk.rxn.mybluehost.me:8000/reset_password/");
        _forgotWebView.clearCache(true);
        _forgotWebView.clearHistory();
        _forgotWebView.getSettings().setJavaScriptEnabled(true);
        _forgotWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.acceptThirdPartyCookies(_forgotWebView);

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();


        _forgotWebView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            public void onPageFinished(WebView view, String url) {
                if (progressBar.isShowing()) {
                    progressBar.dismiss();
                }
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                _forgotWebView.reload();
                Toast.makeText(ForgotPassword.this, "Connection Error", Toast.LENGTH_LONG).show();
            }
        });
        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
