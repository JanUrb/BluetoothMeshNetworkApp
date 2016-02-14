package BluetoothCommunication;

import java.util.UUID;

import fllog.Log;

/**
 * Speichert die Nachricht mit einer Timestamp.
 * <p/>
 * Die Timestamp wird von der Klasse MessageStorage genutzt, um festzustellen ob eine Nachricht in einem
 * Timeframe schon einmal erhalten wurde.
 * <p/>
 * Created by Jan Urbansky on 06.01.2016.
 */
public class MessageStoreContainer {
    private static final String TAG = "fhflMessageStoreCont";
    private Message message;
    private long timestamp;

    public MessageStoreContainer(Message message, long timestamp) {
        Log.d(TAG, "MessageStoreContainer()");
        this.message = message;
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        Log.d(TAG, "getTimestamp()");
        return timestamp;
    }

    public UUID getMessageId() {
        Log.d(TAG, "getMessageId()");
        return message.getMessageId();
    }
}
