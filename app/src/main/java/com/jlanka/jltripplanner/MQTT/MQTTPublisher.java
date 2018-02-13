package com.jlanka.jltripplanner.MQTT;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Random;

/**
 * Created by chathura on 11/30/17.
 */

public class MQTTPublisher {

    public MqttAndroidClient mqttClient;

    public String serverUri;
    public String clientId = mqttRandomUser();

    public MQTTPublisher(Context context, String serverUri){
        this.serverUri=serverUri;
        mqttClient = new MqttAndroidClient(context, this.serverUri, clientId);
        mqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("mqtt", s);
            }
            @Override
            public void connectionLost(Throwable throwable) {
            }
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Mqtt", mqttMessage.toString());
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });
        connect();
    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttClient.setCallback(callback);
    }

    private void connect(){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(false);
        mqttConnectOptions.setCleanSession(true);
        try {
            mqttClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttClient.setBufferOpts(disconnectedBufferOptions);
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Failed to connect to: " + serverUri + exception.toString());
                }
            });
        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    public void publishToTopic(final String message, final Context ctx) {
        mqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                try {
                    mqttClient.publish("goev/server/charge",message.getBytes(),1,false);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void connectionLost(Throwable cause) {
            }
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
    }

    public String mqttRandomUser(int n){
        String SALTCHARS = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() <n ) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }
    public String mqttRandomUser(){
        String SALTCHARS = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() <62 ) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }
}