package com.jlanka.tripplanner.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jlanka.tripplanner.Database.DatabaseHandler;
import com.jlanka.tripplanner.Database.Decrypt;
import com.jlanka.tripplanner.Database.Vehicle;
import com.jlanka.tripplanner.Database.VehicleData;
import com.jlanka.tripplanner.MQTT.Constants;
import com.jlanka.tripplanner.MQTT.PahoMqttClient;
import com.jlanka.tripplanner.MainActivity;
import com.jlanka.tripplanner.R;
import com.jlanka.tripplanner.UserActivity.SessionManager;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

public class BluetoothFragment extends Fragment {
    @BindView(R.id.bluetoothStatus)TextView mBluetoothStatus;
    @BindView(R.id.readBuffer)TextView mReadBuffer;
    @BindView(R.id.vehicle_spinner)Spinner _selectEV;
    @BindView(R.id.switch1)Switch _bluetoothSwitch;
//    @BindView(R.id.exportDatabase)Button mExportData;
    @BindView(R.id.discover)Button _discoverDevices;
    @BindView(R.id.devicesListView)ListView mDevicesListView;

    View mView;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private final String TAG = MainActivity.class.getSimpleName();
    private Handler mHandler; // Our main handler that will receive callback notifications
    private Handler ui; // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    //User Variables
    SessionManager session;
    String user_name,user_fname, user_lname, user_title, user_mail, user_mobile, user_passwd;
    ProgressBar app_progress;
    private MqttAndroidClient client;
    private PahoMqttClient pahoMqttClient;
    DatabaseHandler databaseHandler;
    Vehicle vehicle;

    String key = "emobilityjlpande";
    SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");

    public BluetoothFragment() {
        // Required empty public constructor
    }

    @SuppressLint("HandlerLeak")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        ButterKnife.bind(this,mView);

        session = new SessionManager(getActivity());
        HashMap<String, String> user = session.getUserDetails();

        setSpinner(user);

        app_progress=getActivity().findViewById(R.id.app_bar_progress);
        databaseHandler=new DatabaseHandler(getActivity());

        mBTArrayAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);

        pahoMqttClient = new PahoMqttClient();
        client = pahoMqttClient.getMqttClient(getActivity(), Constants.MQTT_BROKER_URL, Constants.CLIENT_ID);

        // Ask for location permission if not already allowed
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        if(mBTAdapter.isEnabled()) {
            _bluetoothSwitch.setChecked(true);
        }else{
            _bluetoothSwitch.setChecked(false);
        }

        mHandler = new Handler(){
            public void handleMessage(Message msg){
                if(msg.what == MESSAGE_READ){
                    String readMessage = null;
                    String removeDummy = null;
                    String valid=null;
                    long unixTime = System.currentTimeMillis() / 1000L;
//                    String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                    Log.d("Bluetooth Data", "---------->Incoming");
                    readMessage = new String((byte[]) msg.obj);
                    removeDummy = readMessage.replaceAll("[^!-~\\u20000-\\uFE1F\\uFF00-\\uFFEF]", "");

//                    System.out.println("Raw Base 64 "+readMessage);
//                    System.out.println("Raw Base 64 Remove Dummy "+ removeDummy);

                    Pattern p = Pattern.compile("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$");
                    Matcher m = p.matcher(removeDummy);
                    if(m.find()){
                        valid = m.group(0);
                    }
                    app_progress.setVisibility(View.VISIBLE);
//                    mReadBuffer.setText(readMessage);

//                    Log.d("insert Values", unixTime+"    "+readMessage);
//                    mqttPublisher(readMessage,unixTime);
                    msgDecrypter(unixTime,valid);

//                    databaseHandler.addData(new VehicleData(unixTime, readMessage));
                } else {
                    app_progress.setVisibility(View.GONE);
                }

                if(msg.what == CONNECTING_STATUS){
                    if(msg.arg1 == 1) {
                        mBluetoothStatus.setText("Connected to Device: " + (String) (msg.obj));
                        mConnectedThread.write(vehicle.toJSON());
                    } else
                        mBluetoothStatus.setText("Connection Failed");
                }
            }
        };


        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            mBluetoothStatus.setText("Status: Bluetooth not found");
            Toast.makeText(getActivity(),"Bluetooth device not found!",Toast.LENGTH_SHORT).show();
        }
        else {
            _bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        bluetoothOn();
                    }else{
                        bluetoothOff();
                    }
                }
            });


            _discoverDevices.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    discover(v);
                }
            });

            //            mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v){
//                    listPairedDevices(v);
//                }
//            });
        }

        _selectEV.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                vehicle = (Vehicle) parent.getSelectedItem();
//                System.out.println("Vehicle Object : : : : "+vehicle.toJSON());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

//        mExportData.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                exportDb();
//            }
//        });
        return mView;
    }

    private void setSpinner(HashMap<String, String> user) {
        ArrayList<Vehicle> vehicleList = new ArrayList<>();
        try{
            JSONArray obj = new JSONArray(user.get(SessionManager.electric_vehicles));
            for (int i = 0; i < obj.length(); i++){
                JSONObject vehicle_object = obj.getJSONObject(i);
                final String model = vehicle_object.getString("model");
                final String reg_no = vehicle_object.getString("reg_no");
                final String vin = vehicle_object.getString("vin");
                vehicleList.add(new Vehicle(vin, reg_no,model));
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayAdapter<Vehicle> adapter = new ArrayAdapter<Vehicle>(getActivity(), android.R.layout.simple_spinner_item, vehicleList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.notifyDataSetChanged();
        _selectEV.setAdapter(adapter);
    }

    private void msgDecrypter(long unixTime, String readMessage) {

        String decrypted  = decryptData(readMessage);

        if(decrypted != null && !decrypted.isEmpty()) {
            System.out.println("Decrypted Valid : " + decrypted.replaceAll("[^!-~\\u20000-\\uFE1F\\uFF00-\\uFFEF]", ""));
        }


//        String message  = decrypted.replaceAll("[^!-~\\u20000-\\uFE1F\\uFF00-\\uFFEF]", "");
//        System.out.println("Message Regex: " + message);
//        Log.d(TAG, "msgDecrypter() returned: " + Arrays.toString(readMessage.toCharArray()));

//        String [] a = message.split("[}]");
//        for (String str : a) {
//            String block = str + '}';
//            JSONObject evData = null;
//            try {
//                evData = new JSONObject(block);
//                Log.d(TAG, "msgDecrypter: JSON " + evData);
//                if(evData.has("SOC")){
//                    databaseHandler.addData(new VehicleData(unixTime, evData.toString()));
//                    try {
//                        System.out.println("Static Data : SOC -> "+evData.get("SOC"));
//                        System.out.println("Static Data : Gids -> "+evData.get("Gids"));
//                        System.out.println("Static Data : TotalKWh ->"+evData.get("TotalKWh"));
//                        System.out.println("Static Data : AvlbKW ->"+evData.get("AvlbKW"));
//                        System.out.println("Static Data : PackV ->"+evData.get("PackV"));
//                        System.out.println("Static Data : PackAmp ->"+evData.get("PackAmp"));
//                        System.out.println("Static Data : KW ->"+evData.get("KW"));
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }else if(evData.has("Chip_ID")){
//                    try{
//                        System.out.println("CHIP ID ::: -> "+evData.get("Chip_ID"));
//                    }catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            } catch (JSONException e) {
//
//            }
//        }
    }

    private String decryptData(String data){
//        System.out.println("DATA Length 64 : "+data.length());
        byte[] decode = base64ToByteArray(data);
        try {
            Cipher cipher2 = Cipher.getInstance("AES/ECB/NoPadding","BC");
            cipher2.init(Cipher.DECRYPT_MODE, skeySpec);
            String de = new String(cipher2.doFinal(decode));
//            Log.w("Decrypted  Data : ",de);
            return de;
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return null;
    }

    private void mqttPublisher(String readMessage, long timestamp) {
        String Message = null;
        try {
            JSONObject DATA = new JSONObject(readMessage);
            JSONObject message =new JSONObject();
            message.put("timestamp",timestamp);

            JSONArray keys = DATA.names ();
            for (int i = 0; i < keys.length(); ++i) {
                String key = keys.getString(i); // Here's your key
                String value = DATA.getString(key); // Here's your value
                System.out.println("EV DATA : " + key+"    "+value);
                message.put(key,value);
            }
            databaseHandler.addData(new VehicleData(timestamp, message.toString()));
            pahoMqttClient.publishMessage(client, message.toString(), 1, Constants.PUBLISH_TOPIC);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
//        mqttPublisher.publishToTopic(Message,getActivity().getApplicationContext());

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        getUserDetails();
    }

    public void getUserDetails(){
        session = new SessionManager(getActivity().getApplicationContext());
        //This will redirect user to LoginActivity is he is not logged in
        session.checkLogin();
        HashMap<String, String> user = session.getUserDetails();
        user_name = user.get(SessionManager.user_name);
        user_fname = user.get(SessionManager.user_fname);
        user_lname = user.get(SessionManager.user_lname);
        user_title = user.get(SessionManager.user_title);
        user_mobile = user.get(SessionManager.user_mobile);
        user_passwd = user.get(SessionManager.pass_word);
    }

    private void bluetoothOn(){
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mBluetoothStatus.setText("Bluetooth enabled");
            Toast.makeText(getActivity(),"Bluetooth turned on",Toast.LENGTH_SHORT).show();
        }
        else{
            mBluetoothStatus.setText("Bluetooth enabled");
            Toast.makeText(getActivity(),"Bluetooth is already on", Toast.LENGTH_SHORT).show();
        }
    }

    private void bluetoothOff(){
        mBTAdapter.disable();
        mConnectedThread.cancel();// turn off
        mBluetoothStatus.setText("Bluetooth disabled");
        Toast.makeText(getActivity(),"Bluetooth turned Off", Toast.LENGTH_SHORT).show();
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent Data){
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                mBluetoothStatus.setText("Enabled");
            }
            else
                mBluetoothStatus.setText("Disabled");
        }
    }

    private void discover(View v){
        // Check if the device is already discovering
        if(mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
            mBTAdapter.startDiscovery();
            Toast.makeText(getActivity(),"Discovery Restarted",Toast.LENGTH_SHORT).show();
        }
        else{
            if(mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(getActivity(), "Discovery started", Toast.LENGTH_SHORT).show();
                Objects.requireNonNull(getActivity()).registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else{
                Toast.makeText(getActivity(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void exportDb() {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        File dataDirectory = Environment.getDataDirectory();

        FileChannel source = null;
        FileChannel destination = null;

        String currentDBPath = "/data/" + getActivity().getApplicationInfo().packageName + "/databases/EVDATA";
        String backupDBPath = "SampleDB.sqlite";
        File currentDB = new File(dataDirectory, currentDBPath);
        File backupDB = new File(externalStorageDirectory, backupDBPath);

        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());

            Toast.makeText(getActivity(), "DB Exported!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (source != null) source.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (destination != null) destination.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                try {
                    System.out.println("DEVICE ::::: " + device.getName() + "   " + device.getName().length());
                    mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    mBTArrayAdapter.notifyDataSetChanged();
                }catch (Exception e){
                    Log.e(TAG, "onReceive: ", e);
                }
            }
        }
    };

    private void listPairedDevices(View view){
        mPairedDevices = mBTAdapter.getBondedDevices();
        if(mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(getActivity(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getActivity(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            if(!mBTAdapter.isEnabled()) {
                Toast.makeText(getActivity(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }

            mBluetoothStatus.setText("Connecting...");
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0,info.length() - 17);

            // Spawn a new thread to avoid blocking the GUI one
            new Thread()
            {
                public void run() {
                    boolean fail = false;
                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);
                    try {
                        String decryptedName = decrypt(device.getName());
                        if((decryptedName.contains("EM"))){
                            mBTSocket = createBluetoothSocket(device);
                        }else{
                            fail = true;
                        }
                        try {
                            mBTSocket.connect();
                        } catch (IOException e) {
                            try {
                                fail = true;
                                mBTSocket.close();
                                mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                        .sendToTarget();
                            } catch (IOException e2) {
                                //insert code to deal with this
                                Toast.makeText(getActivity(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                            }
                        }

                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getActivity(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.

                    if(!fail) {
                        mConnectedThread = new ConnectedThread(mBTSocket);
                        mConnectedThread.start();

                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();
        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BTMODULEUUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection",e);
        }
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer;  // buffer store for the stream
//            buffer = null;
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();


                    if(bytes != 0) {
                        buffer = new byte[2048];
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private String decrypt(String name) {
        byte[] decode = base64ToByteArray(name);

        Log.w("Encrypted Device : ",name);
        Log.w("Device to Byte Array : " ,Arrays.toString(decode));

        try {
            Cipher cipher2 = Cipher.getInstance("AES/ECB/NoPadding","BC");
            cipher2.init(Cipher.DECRYPT_MODE, skeySpec);
            String de = new String(cipher2.doFinal(decode), "UTF-8");
            Log.w("Decrypted Device : ",de);
            return de;
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return null;
    }

    public static byte[] base64ToByteArray(String text){
//        System.out.println("base64toByte :: " + text);
        byte[] decodedString=null;
            try {
                decodedString=new byte[0];
                decodedString = Base64.decodeBase64(text.getBytes());
            } catch (Exception e) {
                Log.e("base64ToByteArray: ",e.getMessage() );
            }
        return decodedString;
    }
}
