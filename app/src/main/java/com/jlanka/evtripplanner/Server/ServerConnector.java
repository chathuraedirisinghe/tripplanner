package com.jlanka.evtripplanner.Server;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.stetho.server.http.HttpStatus;
import com.jlanka.evtripplanner.GoogleAnalyticsService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by chathura on 11/22/17.
 */

public class ServerConnector {
    public static final String SERVER_ADDRESS = "http://yvk.rxn.mybluehost.me:8000/api/";
    Map<String, String> params ;//= new HashMap<>();
    private int requestMethod;
    private String serverAddress;
    private Context context;

    private OnResponseListner onResponseListner;
    private OnErrorListner onErrorListner;

    public ServerConnector(String serverAddress,Map<String, String> params ,int requestMethod,Context context){
        this.serverAddress=serverAddress;
        this.params=params;
        this.requestMethod=requestMethod;
        this.context=context;
    }
    public void sendRequest(){
        StringRequest postRequest = new StringRequest(requestMethod/* Request.Method.POST */, serverAddress,
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
                                mess=obj.toString().split(":");
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
                            if (mess==null)
                                message = "Cannot connect to Internet...Please check your connection!";
                            else
                                message = mess[1].replaceAll("[^a-zA-Z-.' ]","");
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
                parameater.putAll(params);
                return parameater;
            }
        };
        Volley.newRequestQueue(context).add(postRequest);
    }
    public void setOnReponseListner(OnResponseListner onResponseListner){
        if(onResponseListner!=null)
            this.onResponseListner=onResponseListner;
    }

    public void setOnErrorListner(OnErrorListner onErrorListner){
        if(onErrorListner!=null)
            this.onErrorListner=onErrorListner;
    }
}
