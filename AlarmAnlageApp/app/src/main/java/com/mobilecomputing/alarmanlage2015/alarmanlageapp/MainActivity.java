package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import android.app.Activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;


import com.mobilecomputing.alarmanlage2015.alarmanlageapp.communication.CommunicationModel;

import fllog.Log;

public class MainActivity extends Activity {

    private static final String TAG = "fhflAlarmMainActivity";

    private Controller controller;
    private BluetoothModel bt_model;
    /**
     * Empfängt
     */
    private BroadcastReceiver mBroadCastReceiver;
    public static MainActivity instance = null; //?

    private CommunicationModel communicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.init(true, true);
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bt_model = new BluetoothModel();

        final MainActivityFragment mainFrag = new MainActivityFragment();
        mainFrag.setBt_model(bt_model);

        getFragmentManager().beginTransaction().replace(R.id.main_fragment_container, mainFrag).
                replace(R.id.log_fragment_container, Log.getFragment()).commit();

        controller = new Controller();

        //Die Bluetoothverbindung wird mit dem Starten der App aufgebaut. Es wird also keine
        //Benachrichtigung durch die UI benötigt.
        mainFrag.setController(controller);


        initBroadcastReceiver();

        controller.init(this, bt_model);


//        SharedPreferences sharedPref = getPreferences(this.MODE_PRIVATE);
//        int communicationType = sharedPref.getInt(getString(R.string.communicationtype), 0);
//        String addInfo = sharedPref.getString(getString(R.string.communicationaddinfo), null);
//
//
//        if(communicationType == 0) {
//            // Start Settings
//            Intent intentSettings = new Intent(this, SettingsActivity.class);
//            this.startActivity(intentSettings);
//        }
//        else {
//            // Init App
//            communicator = new CommunicationModel();
//            communicator.setType(communicationType, addInfo);
//            communicator.sendMessage("Hallo was geht");
//        }
    }

    /**
     * Initialisiert den Broadcast Receiver und registriert ihn für
     * BluetoothDevice.ACTION_FOUND, BluetoothAdapter.ACTION_DISCOVERY_STARTED(für Tests) und
     * BluetoothAdapter.ACTION_DISCOVERY_FINISHED
     * <p/>
     * Es darf pro Discovery Cycle nur mit einem Gerät eine Verbindung aufgebaut werden. Dazu wird
     * die validDeviceFound Variable genutzt.
     */
    public void initBroadcastReceiver() {
        mBroadCastReceiver = new BroadcastReceiver() {

            private long startTime;
            private boolean validDeviceFound = false;

            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    Log.d(TAG, "onReceive - Discovery Started");
                    startTime = System.currentTimeMillis();
                    validDeviceFound = false;

                }  else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.d(TAG, "onReceive - device name: " + device.getName() + " Device ID: " + device.getAddress());
                    //wenn die Grenze !validDeviceFound nicht eingebaut wird, können viele Threads vom
                    //Controller gestartet werden. Dies führt zu unberechenbarem Verhalten.
                    if (BluetoothModel.BANNED_DEVICE_ADDRESSES.contains(device.getAddress())) {
                        Log.d(TAG, "(Ignored device) found banned device: " + device.getName() + "MAC: " + device.getAddress());
                    } else if (!bt_model.isDeviceAlreadyConnected(device.getAddress()) && !validDeviceFound) {
                        validDeviceFound = true;
                        controller.startClientThread(device);
                    } else {
                        Log.d(TAG, "onReceive - device already connected or cycle full: " + device.getAddress()+"\nDeviceFound: "+ validDeviceFound);
                    }
                }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    Log.d(TAG, "onReceive - no Device Found - Discovery Finished");
                    Log.d(TAG, "onReceive - Discovery Duration: " + (System.currentTimeMillis() - startTime) + " ms");
                    //Wenn ein Gerät gefunden wurde übernimmt der ACTION_FOUND Zweig den Übergang in den nächsten State
                    //Wenn kein Gerät gefunden wurde, wird ein ServerThread gestartet.
                    if (!validDeviceFound) {
                        Log.d(TAG, "onReceive - start Server Routine");
                        controller.startServerThread();
                    }

                }
            }

        };
        IntentFilter actionFoundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter discoveryStartedFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        IntentFilter discoveryFinishedFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        //der Receiver handelt die actions.
        registerReceiver(mBroadCastReceiver, actionFoundFilter);

        registerReceiver(mBroadCastReceiver, discoveryStartedFilter);

        registerReceiver(mBroadCastReceiver, discoveryFinishedFilter);
    }


    /**
     * Dieser Handler fängt die im Controller gestarteten Intents ab und gibt diese entsprechend weiter.
     *
     * @param requestCode int
     * @param resultCode  int
     * @param data        Intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult()");
        switch (requestCode) {
            case Controller.REQUEST_ENABLE_BT:
                controller.bluetoothAdapterEnabled(); //es folgt discoverarbility
                break;

            case Controller.REQUEST_ENABLE_DISCO:
                controller.discoverabilityEnabled(); //es folgt read devices
                break;

            default:
                Log.v(TAG, "Unbekannter requestCode: " + requestCode);
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        instance = this;
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        instance = null;
    }
}