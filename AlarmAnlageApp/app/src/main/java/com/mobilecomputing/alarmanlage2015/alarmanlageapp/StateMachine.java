package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import android.os.Handler;

import fllog.Log;

/**
 *  Statemachine Klasse
 *
 *  wird im Projekt SmParkingMeter2 gepflegt.
 *
 *  History:
 *      07.12.15 tas Erstellung
 *
 *
 *  Eingebunden für das Projekt Alarmanlage!
 *
 */

public class StateMachine extends Handler {
    private static final String TAG = "fhflStateMachine";

    @Override
    public void handleMessage(android.os.Message message) {
        theBrain(message);
    }           //##0b

    /**
     * virtual method, must be overwritten in subclass
     * @param message
     */
    void theBrain(android.os.Message message){
    }

    protected void setTimer(int messageType, long durationMs){
        Log.d(TAG, "setTimer");
        android.os.Message msg = new android.os.Message();
        msg.what = messageType;
        msg.arg1 = 0;
        msg.arg2 = 0;
        sendMessageDelayed(msg, durationMs);                                                //##4a
    }

    protected void stopTimer(int messageType){
        Log.d(TAG, "stopTimer");
        removeMessages(messageType);                                                        //##4b
    }

    public void sendSmMessage(int messageType, int arg1, int arg2, Object obj){
        Log.d(TAG, "sendSmMessage");
        // PrintData d = new PrintData(3);                                                   //##3a
        android.os.Message msg = new android.os.Message();                                   //##0a
        msg.what = messageType;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        if ( obj != null ) {
            msg.obj = obj;
        }
        this.sendMessage(msg);
    }

}