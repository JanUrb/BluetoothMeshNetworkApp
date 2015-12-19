package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import fllog.Log;

public class MainActivity extends Activity {

    private static final String TAG = "fhflAlarmMainActivity";


    public static MainActivity instance = null; //?
    private CommunicationModel communicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.init(true, true); 
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init den Logger showTimestamp, showTag



        MainActivityFragment mainFrag = new MainActivityFragment();

        getFragmentManager().beginTransaction().replace(R.id.main_fragment_container, mainFrag).
                replace(R.id.log_fragment_container, Log.getFragment()).commit();


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