package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import android.bluetooth.BluetoothDevice;

import java.util.UUID;

import fllog.Log;

/**
 * Created by Jan Urbansky on 02.01.2016.
 */
public class Connection {
    private static final String TAG = "fhflConnection";
    private ConnectedThread connectedThread;
    private BluetoothDevice bluetoothDevice;

    public Connection(ConnectedThread ct, BluetoothDevice btDevice) {
        Log.d(TAG, "Connection(ConnectedThread ct, BluetoothDevice btDevice)");
        connectedThread = ct;
        bluetoothDevice = btDevice;
    }

    public void start(){
        Log.d(TAG, "start()");
        connectedThread.start();
    }

    public String getDeviceAddress(){
        return bluetoothDevice.getAddress();
    }
}
