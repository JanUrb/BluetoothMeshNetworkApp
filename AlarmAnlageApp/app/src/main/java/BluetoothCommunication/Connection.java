package BluetoothCommunication;

import android.bluetooth.BluetoothDevice;

import java.io.IOException;

import fllog.Log;

/**
 * Wrapperklasse für einen ConnectedThread
 * <p/>
 * Speichert nebem dem ConnectedThread auch das verbunden BluetoothDevice.
 * <p/>
 * Created by Jan Urbansky on 02.01.2016.
 */
public class Connection {
    private static final String TAG = "fhflConnection";
    private ConnectedThread connectedThread;
    private BluetoothDevice bluetoothDevice;
    private long connectionID;

    public Connection(ConnectedThread ct, BluetoothDevice btDevice) {
        Log.d(TAG, "Connection(ConnectedThread ct, BluetoothDevice btDevice)");
        connectedThread = ct;
        bluetoothDevice = btDevice;
        connectionID = connectedThread.getId();
    }

    public void start() {
        Log.d(TAG, "start()");
        connectedThread.start();
    }

    public String getDeviceAddress() {
        Log.d(TAG, "getDeviceAddress");
        return bluetoothDevice.getAddress();
    }

    protected BluetoothDevice getBluetoothDevice(){
        return bluetoothDevice;
    }

    public void write(Message msg) throws IOException {
        Log.d(TAG, "write");
        connectedThread.write(msg.getBytes());
    }

    public long getConnectionID() {
        return connectionID;
    }
}

