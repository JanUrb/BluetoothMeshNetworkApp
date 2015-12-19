package com.mobilecomputing.alarmanlage2015.alarmanlageapp.communication;

import android.content.Intent;

import com.mobilecomputing.alarmanlage2015.alarmanlageapp.MainActivity;

import fllog.Log;

/**
 * Created by Donskelle-PC on 15.12.2015.
 */
public class EmailSender implements CommunicationInterface {

    private static final String TAG = "fhflEmailSender";
    private String emailAddress;

    public EmailSender(String mail) {
        Log.d(TAG, "EmailSender(mail)");
        emailAddress = mail;
    }


    @Override
    public boolean sendMessage(String Message) {
        Log.d(TAG, "sendMessage");
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"donskelle@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "Alarmapp: Bewegung erkannt");
        i.putExtra(Intent.EXTRA_TEXT, Message);
        try {
            if (MainActivity.instance != null) {
                MainActivity.instance.startActivity(Intent.createChooser(i, "Email senden."));
            }
            return true;
        } catch (android.content.ActivityNotFoundException ex) {
            return false;
        }
    }
}
