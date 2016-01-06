package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.UUID;

import fllog.Log;

/**
 * Die Message-Klasse besteht aus der MAC-Adresse des Empfängers und aus einer ID.
 * Die ID wird beim Aufrufen des Konstruktors erstellt.
 *
 * Serializeable erlaubt das Umformen in ein byte[]
 *
 * <p/>
 * Created by Jan Urbansky on 04.01.2016.
 */
public class Message implements Serializable{
    private static final String TAG = "fhflMessage";
    private UUID messageId;
    private String messageTargetMac;
    private String messageSourceMac;


    public Message(String messageSourceMac, String messageTargetMac) {
        this.messageSourceMac = messageSourceMac;
        this.messageTargetMac = messageTargetMac;
        messageId = UUID.randomUUID();
    }

    public UUID getMessageId() {
        return messageId;
    }

    public String getMessageSourceMac() {
        Log.d(TAG, "getMessageSourceMac");
        return messageSourceMac;
    }

    public String getMessageTargetMac() {
        Log.d(TAG, "getMessageTargetMac");
        return messageTargetMac;
    }

    /**
     * Quelle: https://stackoverflow.com/questions/5837698/converting-any-object-to-a-byte-array-in-java
     * @return
     * @throws IOException
     */
    public byte[] getBytes() throws IOException {
        Log.d(TAG, "getBytes()");
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = null;
        o = new ObjectOutputStream(b);
        o.writeObject(this);
        return b.toByteArray();
    }
}
