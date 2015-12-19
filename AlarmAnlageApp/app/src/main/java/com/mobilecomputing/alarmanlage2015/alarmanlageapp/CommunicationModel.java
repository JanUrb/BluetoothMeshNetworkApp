package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import fllog.Log;

/**
 * Created by Donskelle-PC on 15.12.2015.
 */
public class CommunicationModel {

    private static final String TAG = "fhflAlarmModel";

    private CommunicationInterface commuicator;

    public CommunicationModel() {
        Log.d(TAG, "CommunicationsModel()");
    }

    /**
     * SetType
     * Setzt den Type des verwendeten Kommunikationstyps
     * @param type
     * @param addidionalInfo
     */
    public void setType(int type, String addidionalInfo) {
        Log.d(TAG, "setType - Type: "+type);
        switch (type) {
            case 1:
                commuicator = new SmsSender(addidionalInfo);
                break;
            case 2:
                commuicator = new EmailSender(addidionalInfo);
            default:
                break;
        }
    }

    /**
     *
     * @param message
     */
    public boolean sendMessage(String message) {
        Log.d(TAG, "sendMessage");
        try {
            commuicator.sendMessage(message);
            return true;
        }
        catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
}