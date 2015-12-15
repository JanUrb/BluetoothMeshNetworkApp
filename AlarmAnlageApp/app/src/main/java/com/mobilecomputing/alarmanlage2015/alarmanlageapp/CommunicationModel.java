package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

/**
 * Created by Donskelle-PC on 15.12.2015.
 */
public class CommunicationModel {
    private CommunicationInterface commuicator;

    public CommunicationModel() {

    }

    /**
     * SetType
     * Setzt den Type des verwendeten Kommunikationstyps
     * @param type
     * @param addidionalInfo
     */
    public void setType(int type, String addidionalInfo) {
        switch (type) {
            case 1:
                commuicator = new Sms(addidionalInfo);
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