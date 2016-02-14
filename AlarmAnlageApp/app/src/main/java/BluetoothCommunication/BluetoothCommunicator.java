package BluetoothCommunication;

import android.bluetooth.BluetoothDevice;

import java.util.List;

/**
 * Created by Jan Urbansky on 14.02.2016.
 */
public class BluetoothCommunicator {


    /**
     * Enables Bluetooth and Discoverability.
     */
    public void init(){

    }

    /**
     * The search for connections starts.
     */
    public void start(){

    }

    public void stop(){

    }

    public void setLogFragment(ILogFragment logFragment){

    }

    public void setCommunicationEventListener(ICommunicationEvents communicationEventListener){

    }

    /**
     *
     * @return
     */
    public List<BluetoothConnection> getDirectConnections(){
        return null;
    }

    public void disconnectConnection(BluetoothConnection btConnection){

    }

    public void sendToDevice(String deviceMAC){

    }

    public void sendToDevice(BluetoothDevice btDevice){
        sendToDevice(btDevice.getAddress());
    }

    public void sendToDevice(BluetoothConnection btConnection){
        sendToDevice(btConnection.getDevice());
    }


}
