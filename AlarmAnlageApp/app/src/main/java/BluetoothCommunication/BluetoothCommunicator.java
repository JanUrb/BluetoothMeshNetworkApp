package BluetoothCommunication;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;

import com.mobilecomputing.alarmanlage2015.alarmanlageapp.Connection;

import java.util.List;

/**
 * Created by Jan Urbansky on 14.02.2016.
 */
public class BluetoothCommunicator {

    public void start(){

    }

    public void stop(){

    }

    public void setLogFragment(){

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

    public boolean disconnectConnection(BluetoothConnection btConnection){
        //TODO: returns true if the connection was there and was successfully disconected.
        return false;
    }

    public void sendToDevice(String uuid){

    }

    public void sendToDevice(BluetoothDevice btDevice){

    }

    public void sendToDevice(BluetoothConnection btConnection){

    }


}
