package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import android.telephony.SmsManager;

/**
 * Created by Donskelle-PC on 12.12.2015.
 */
public class Sms implements CommunicationInterface{
    private String phoneNumber;


    public Sms(String number) {
        phoneNumber = number;
    }

    @Override
    public boolean sendMessage(String messageText) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, messageText, null, null);
        return true;
    }
}
