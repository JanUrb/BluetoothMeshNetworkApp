package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

/*
* MobileComputing Projekt 2015/2016
* Alarmanlage: Bluetooth-Netzwerk
*
* Diese App ist das Bluetooth-Netzwerk der Gruppe Alarmanlage.
* Das Ziel der App ist, dass Android-Geräte autonom ein Mesh-Netzwerk aufbauen und verwalten.
*
* Diese App wurde alleine und ohne Hilfe von Jan Urbansky geschaffen.
*
*
*
* Starten der App:
*
* Bei modernen Geräten muss die App nur gestartet werden. Bei alten Geräten kann es sein, dass die App
* abstürzt, wenn kein Bluetooth aktiviert ist.
*
*
* */


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import BluetoothCommunication.BluetoothCommunicator;
import BluetoothCommunication.Controller;
import fllog.Log;

public class MainActivity extends Activity {

    private static final String TAG = "fhflAlarmMainActivity";
    private BluetoothCommunicator mBluetoothCommunicator;

    private AppController mAppController;
    private AppModel mAppModel;
    /**
     * Empfängt die Intents von FIND_DEVICE
     */

    public static MainActivity instance = null; //?


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.init(true, true);
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBluetoothCommunicator = new BluetoothCommunicator(this);
        mAppController = new AppController(mBluetoothCommunicator);
        mAppModel = new AppModel(mBluetoothCommunicator);


        final MainActivityFragment mainFrag = new MainActivityFragment();
        mainFrag.setAppModel(mAppModel);

        getFragmentManager().beginTransaction().replace(R.id.main_fragment_container, mainFrag).
                replace(R.id.log_fragment_container, Log.getFragment()).commit();


        mBluetoothCommunicator.init();

        //Die Bluetoothverbindung wird mit dem Starten der App aufgebaut. Es wird also keine
        //Benachrichtigung durch die UI benötigt.
        mainFrag.setController(mAppController);

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
                mAppController.bluetoothAdapterEnabled(); //es folgt discoverarbility
                break;

            case Controller.REQUEST_ENABLE_DISCO:
                mAppController.discoverabilityEnabled(); //es folgt read devices
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