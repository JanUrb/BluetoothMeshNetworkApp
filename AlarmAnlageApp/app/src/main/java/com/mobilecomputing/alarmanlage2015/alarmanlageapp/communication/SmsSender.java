package com.mobilecomputing.alarmanlage2015.alarmanlageapp.communication;

import android.telephony.SmsManager;
import fllog.Log;

/**
 * Created by Donskelle-PC on 12.12.2015.
 */
public class SmsSender implements CommunicationInterface{

    private static final String TAG = "fhflAlarmSmsSender";


    private String phoneNumber;


    public SmsSender(String number) {
        Log.d(TAG, "SmsSender()");
        phoneNumber = number;
    }

    @Override
    public boolean sendMessage(String messageText) {
        Log.d(TAG, "sendMessage");
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, messageText, null, null);
        return true;
    }
}
