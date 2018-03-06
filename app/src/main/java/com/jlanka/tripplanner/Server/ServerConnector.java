package com.jlanka.tripplanner.Server;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jlanka.tripplanner.GoogleAnalyticsService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Created by chathura on 11/22/17.
 */

public class ServerConnector {
    private static ServerConnector serverConnector;
    public static final String SERVER_ADDRESS = "http://yvk.rxn.mybluehost.me:8000/api/";
    private static RequestQueue mRequestQueue;
    private Context context;

    private ServerConnector(Context context){
        this.context=context;
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public static synchronized ServerConnector getInstance(Context context){
        if (serverConnector==null)
            serverConnector=new ServerConnector(context);

        return serverConnector;
    }

    public synchronized void sendRequest(String serverAddress,Map<String, String> params ,int requestMethod,OnResponseListner onResponseListner,OnErrorListner onErrorListner,String tag){
        StringRequest postRequest = new StringRequest(requestMethod, serverAddress,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
//                            JSONObject jsonResponse = new JSONObject(response);
                            onResponseListner.onResponse(response);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message = null;
                        String mess[]=null;
                        JSONObject obj = null;

                        NetworkResponse response = error.networkResponse;
                        error.printStackTrace();
                        if (error instanceof ServerError && response != null) {
                            try {
                                String res = new String(response.data,
                                        HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                // Now you can use any deserializer to make sense of data
                                obj = new JSONObject(res);
                            } catch (UnsupportedEncodingException e1) {
                                // Couldn't properly decode data to string
                                e1.printStackTrace();
                            } catch (JSONException e2) {
                                // returned data is not JSONObject?
                                e2.printStackTrace();
                            }
                        }

                        if (error instanceof NetworkError) {
                            message = "Cannot connect to Internet...Please check your connection!";
                        } else if (error instanceof ServerError) {
                            message = "Cannot connect to Internet...Please check your connection!";
                            GoogleAnalyticsService.getInstance().trackException(context,error);
                        } else if (error instanceof AuthFailureError) {
                            message = "Incorrect username or password!";
                        } else if (error instanceof ParseError) {
                            GoogleAnalyticsService.getInstance().trackException(context,error);
                            message = "Parsing error! Please try again after some time!!";
                        } else if (error instanceof NoConnectionError) {
                            message = "Cannot connect to Internet...Please check your connection!";
                        } else if (error instanceof TimeoutError) {
                            message = "Connection TimeOut! Please check your internet connection.";
                        }
                        onErrorListner.onError(message,obj);
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String>  parameater = new HashMap<>();
                if (params!=null)
                    parameater.putAll(params);
                return parameater;
            }
        };
        postRequest.setTag(tag);
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(8),
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(postRequest);
    }

    public void cancelRequest(String tag){
        mRequestQueue.cancelAll(tag);
    }
}
