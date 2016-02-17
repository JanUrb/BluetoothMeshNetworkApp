package BluetoothCommunication;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import BluetoothCommunication.BluetoothConnection;
import BluetoothCommunication.Controller;
import BluetoothCommunication.Message;
import fllog.Log;

/**
 * Created by Jan Urbansky on 23.12.2015.
 */
public class BluetoothModel extends Observable {

    private final static String TAG = "fhflBluetoothModel";

    /**
     * Die BANNED_DEVICE_ADDRESSES sind bekannte(!) Geräte, die den Testvorgang stören.
     * ZB. Smartphones ohne die App: Diese haben nicht den richtigen Service mit der UUID und beim
     * clientSocket.connect() führt es zu Service Discovery Failed.
     */
    public static Set<String> BANNED_DEVICE_ADDRESSES = new HashSet<String>(Arrays.asList(new String[]{"18:CF:5E:3D:D5:9B"}));


    private String myBT_ADDR = "";

    /**
     * Das HashSet wird von BluetoothAdapter.getBondedDevices() verwendet.
     */
    private Set<BluetoothConnection> bluetoothConnections = new HashSet<BluetoothConnection>(Controller.MAX_NUMBER_OF_DEVICES);

    /**
     * Speichert die eingegangenen Nachrichten.
     */
    private Message currentMessage = null;

    public BluetoothModel() {
        Log.d(TAG, "BluetoothModel()");
    }


    //Setter
    public void setMyBT_ADDR(String bt_addr) {
        Log.d(TAG, "setMyBT_ADDR");
        myBT_ADDR = bt_addr;
        notifyObservers();
    }

    public void setCurrentMessage(Message currentMessage) {
        Log.d(TAG, "setMessageReceivedFrom");
        this.currentMessage = currentMessage;
        notifyObservers();
    }

    //getter
    public String getMyBT_ADDR() {
        Log.d(TAG, "getMyBT_ADDR");
        return myBT_ADDR;
    }

    public Message getCurrentMessage() {
        Log.d(TAG, "getCurrentMessage()");
        return currentMessage;
    }

    public int getNumberOfConnections() {
        Log.d(TAG, "getNumberOfConnections: " + bluetoothConnections.size());
        return bluetoothConnections.size();
    }

    public void addConnection(BluetoothConnection bluetoothConnection) {
        Log.d(TAG, "addConnection");
        bluetoothConnections.add(bluetoothConnection);
        notifyObservers();
    }

    /**
     * Überprüft, ob schon eine Verbindung zu einem Gerät besteht.
     *
     * @param deciveMac String Device Address
     * @return
     */
    public boolean isDeviceAlreadyConnected(String deciveMac) {
        Log.d(TAG, "isDeviceAlreadyConnected: " + deciveMac + "...");
        //Kein Gerät verbunden -> Gerät darf sich verbinden.
        if (bluetoothConnections.isEmpty()) {
            Log.d(TAG, "..device not connected");
            return false;
        }
        for (BluetoothConnection c : bluetoothConnections) {
            if (c.getDeviceAddress().equals(deciveMac)) {
                Log.d(TAG, "..device connected");
                return true;
            }
        }
        Log.d(TAG, "..device not connected");
        return false;
    }

    public Set<BluetoothConnection> getBluetoothConnections() {
        Log.d(TAG, "getBluetoothConnections");
        return bluetoothConnections;
    }

    /**
     * Entfernt die Verbindung mit der zugehörigen Connectin ID.
     * Die Funktion geht davon aus, das nur eine Verbindung pro Gerät besteht.
     *
     * @param connectionID long BluetoothConnection Id der geschlossenen Verbindung.
     * @return
     */
    public boolean removeConnection(long connectionID) {
        Log.d(TAG, "removeConnection");
        if (bluetoothConnections.isEmpty()) {
            Log.d(TAG, "connection set is empty!");
            return true;
        }
        //find the connection with the thread id and update the observer.
        for (BluetoothConnection c : bluetoothConnections) {
            if (c.getConnectionID() == connectionID) {
                Log.d(TAG, "connectionID " + connectionID + " removed");
                bluetoothConnections.remove(c);
                notifyObservers();
                return true;
            }
        }


        return false;
    }

    /**
     * Sonst müsste man in jedem Setter setChanged und notifyObservers
     * getrennt aufrufen. 3 Zeilen gespart!!!
     */
    @Override
    public void notifyObservers() {
        Log.v(TAG, "notifyObservers");
        super.setChanged();
        super.notifyObservers();
    }
}
