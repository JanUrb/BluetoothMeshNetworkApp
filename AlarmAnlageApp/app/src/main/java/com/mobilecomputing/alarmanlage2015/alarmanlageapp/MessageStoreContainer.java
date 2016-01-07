package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import java.util.UUID;

/**
 * Created by Jan Urbansky on 06.01.2016.
 */
public class MessageStoreContainer {
    private Message message;
    private long timestamp;

    public MessageStoreContainer(Message message, long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public UUID getMessageId(){
        return message.getMessageId();
    }
}
