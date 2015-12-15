package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import android.content.Intent;

/**
 * Created by Donskelle-PC on 15.12.2015.
 */
public class EmailSender implements CommunicationInterface {
    private String emailAddress;
    public EmailSender(String mail) {
        emailAddress = mail;
    }


    @Override
    public boolean sendMessage(String Message) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"donskelle@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "Alarmapp: Bewegung erkannt");
        i.putExtra(Intent.EXTRA_TEXT   , Message);
        try {
            if (MainActivity.instance != null)
            {
                MainActivity.instance.startActivity(Intent.createChooser(i, "Email senden."));
            }
            return true;
        } catch (android.content.ActivityNotFoundException ex) {
            return false;
        }
    }
}
