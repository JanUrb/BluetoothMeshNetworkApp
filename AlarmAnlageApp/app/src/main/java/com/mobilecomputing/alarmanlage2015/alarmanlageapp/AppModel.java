package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import BluetoothCommunication.BluetoothCommunicator;
import BluetoothCommunication.BluetoothConnection;
import BluetoothCommunication.Message;

/**
 * Created by Jan Urbansky on 22.02.2016.
 */
public class AppModel extends Observable {



    private String myBTADDR;
    private BluetoothCommunicator mBluetoothCommunicator;
    private Message currentMessage;

    public AppModel(BluetoothCommunicator mBluetoothCommunicator) {
        this.mBluetoothCommunicator = mBluetoothCommunicator;
        currentMessage = new Message("0000000", "111111", null);
    }

    public String getMyBTADDR() {
        return mBluetoothCommunicator.getMyBtAddr();
    }



    public Message getCurrentMessage() {
        return currentMessage;
    }





    public List<BluetoothConnection> getBluetoothConnections(){
       return mBluetoothCommunicator.getDirectConnections();
    }



    @Override
    public void addObserver(Observer observer) {
        super.addObserver(observer);
    }

    @Override
    public boolean hasChanged() {
        return super.hasChanged();
    }

    @Override
    public void notifyObservers() {
        super.notifyObservers();
    }
}
