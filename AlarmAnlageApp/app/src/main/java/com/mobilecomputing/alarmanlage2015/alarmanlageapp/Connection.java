package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Jan Urbansky on 02.01.2016.
 */
public class Connection {

    private ConnectedThread connectedThread;
    private BluetoothDevice bluetoothDevice;

    public Connection(ConnectedThread ct, BluetoothDevice btDevice) {
        connectedThread = ct;
        bluetoothDevice = btDevice;
    }
}
