package BluetoothCommunication;

import java.util.HashSet;
import java.util.Set;

import fllog.Log;

/**
 * Speichert eine Nachricht mit einer Timestamp.
 * <p/>
 * Wird benutzt, um sicher zu stellen, dass eine Nachricht nicht von Gerät zu Gerät geschickt wird und nie
 * verworfen wird. Zb. Eine Nachricht wird an eine MAC-Adresse gesendet, die nicht im Netzwerk ist.
 * <p/>
 * Created by Jan Urbansky
 */
public class MessageStorage {
    private static final String TAG = "fhflMessageStorage";
    private Set<MessageStoreContainer> storeContainerSet = new HashSet<MessageStoreContainer>();
    //10 secs
    private static final long TIME_LIMIT = 10000;

    public MessageStorage() {
        Log.d(TAG, "MessageStorage");
    }

    /**
     * Überprüft ob eine Nachricht in der nahen Vergangenheit schon einmal eingegangen ist und fügt
     * diese hinzu, wenn dies nicht der Fall ist.
     *
     * @param message
     * @return Gibt true zurück, wenn die Nachricht schon erhalten wurde.
     */
    public boolean checkMessage(Message message) {
        Log.d(TAG, "checkMessage");
        //entferne alte Nachrichten -> älter als 10secs
        removeOldMessages();
        //überprüfen ob eine Nachricht schon erhalten wurde.
        boolean msgReceived = checkMessageIdInHistory(message);
        if (!msgReceived) {
            addMessageWithTimestamp(message);
        }
        return msgReceived;
    }

    private void addMessageWithTimestamp(Message message) {
        Log.d(TAG, "addMessageWithTimestamp");
        storeContainerSet.add(new MessageStoreContainer(message, System.currentTimeMillis()));
    }


    private void removeOldMessages() {
        Log.d(TAG, "removeOldMessages");
        long currentTimestamp = System.currentTimeMillis();
        for (MessageStoreContainer m : storeContainerSet) {
            if (m.getTimestamp() < currentTimestamp - TIME_LIMIT) {
                storeContainerSet.remove(m);
            }
        }
    }

    /**
     * @param message
     * @return Gibt true zurück, wenn die Nachricht in der History ist.
     */
    private boolean checkMessageIdInHistory(Message message) {
        Log.d(TAG, "checkMessageId");
        boolean messageAlreadyReceived = false;
        for (MessageStoreContainer m : storeContainerSet) {
            if (m.getMessageId().equals(message.getMessageId())) {
                messageAlreadyReceived = true;
                break;
            }
        }
        return messageAlreadyReceived;
    }
}
