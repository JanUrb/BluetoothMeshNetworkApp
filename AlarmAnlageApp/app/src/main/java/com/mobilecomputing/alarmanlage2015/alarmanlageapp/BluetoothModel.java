package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.Observable;
import java.util.Set;

import fllog.Log;

/**
 * Created by Jan Urbansky on 23.12.2015.
 */
public class BluetoothModel extends Observable {

    private final static String TAG = "fhflBluetoothModel";

    //pairedDevices sind nicht die eientlich verbunden geräte. es können auch geräte sein, die früher schon mal
    //verbunden waren.
    private Set<BluetoothDevice> pairedDevices = null;
    private String myBT_ADDR = "";
    /**
     * Die verbunden Geräte.
     */
    private Set<BluetoothDevice> connectedDevices = null;



    private String messageReceivedFrom = "";

    public BluetoothModel(){
        Log.d(TAG, "BluetoothModel()");
    }


    //Setter

    public void setPairedDevices(Set<BluetoothDevice> bt_devices){
        Log.d(TAG, "setPairedDevices()");
        pairedDevices = bt_devices;
        notifyObservers();
    }

    public void setMyBT_ADDR(String bt_addr){
        Log.d(TAG, "setMyBT_ADDR");
        myBT_ADDR = bt_addr;
        notifyObservers();
    }

    public void setMessageReceivedFrom(String messageReceivedFrom) {
        Log.d(TAG, "setMessageReceivedFrom");
        this.messageReceivedFrom = messageReceivedFrom;
        notifyObservers();
    }

    //Getter
    public Set<BluetoothDevice> getPairedDevices() {
        return pairedDevices;
    }

    public String getMyBT_ADDR() {
        return myBT_ADDR;
    }

    public String getMessageReceivedFrom() {
        return messageReceivedFrom;
    }

    public Set<BluetoothDevice> getConnectedDevices() {
        return connectedDevices;
    }

    public void setConnectedDevices(Set<BluetoothDevice> connectedDevices) {
        this.connectedDevices = connectedDevices;
    }

    /**
     * Sonst müsste man in jedem Setter setChanged und notifyObservers
     * getrennt aufrufen. 3 Zeilen gespart!!!
     */
    @Override
    public void notifyObservers() {
        Log.d(TAG, "notifyObservers");
        super.setChanged();
        super.notifyObservers();
    }
}
