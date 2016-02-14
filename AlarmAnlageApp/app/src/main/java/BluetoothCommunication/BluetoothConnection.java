package BluetoothCommunication;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Jan Urbansky on 14.02.2016.
 */
public final class BluetoothConnection {
    /*TODO: Create class. It contains:
    *       The Bluetooth Device
    *       Some Meta Info about the connection.
    *       Connection as private field !!
    * */


    private Connection mActiveConnection;



    /**
     * Hidden Constructor
     */
    protected BluetoothConnection(Connection connection){
        mActiveConnection = connection;
    }

    /**
     * Facing to the public
     * @return
     */
    public BluetoothDevice getDevice() {
        return mActiveConnection.getBluetoothDevice();
    }

    /**
     * Internal
     * @return
     */
    protected Connection getActiveConnection(){
        return mActiveConnection;
    }
}