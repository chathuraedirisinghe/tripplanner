package com.jlanka.evtripplanner.UserActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.jlanka.evtripplanner.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ForgotPassword extends AppCompatActivity {

    @BindView(R.id.forgotPassword) WebView _forgotWebView;
    @BindView(R.id.link_toLogin) TextView _loginLink;
    private ProgressDialog progressBar;

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
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        _forgotWebView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i("WebView", "Processing webview url click...");
                view.loadUrl(url);
                return true;
            }

            public void onPageFinished(WebView view, String url) {
                Log.i("WebView", "Finished loading URL: " + url);
                if (progressBar.isShowing()) {
                    progressBar.dismiss();
                }
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                _forgotWebView.reload();
                Log.e("WebView", "Error: " + description);
                Toast.makeText(ForgotPassword.this, "Connection Error", Toast.LENGTH_SHORT).show();
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
