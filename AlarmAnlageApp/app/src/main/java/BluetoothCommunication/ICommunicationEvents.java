package BluetoothCommunication;

/**
 * Created by Jan Urbansky on 14.02.2016.
 */
public interface ICommunicationEvents {

    /**
     * Triggers only on messages directed to the client.
     */
    public void onDirectMessageReceived();

    /**
     * Triggers on every message received. Directed to the client or not.
     */
    public void onMessageReceived();

    /**
     * Triggers if a connection with a device is established.
     */
    public void onConnectedEstablished();

    /**
     * Triggers if the connection to a device is disconnected.
     */
    public void onConnectionDisconnected();
}
