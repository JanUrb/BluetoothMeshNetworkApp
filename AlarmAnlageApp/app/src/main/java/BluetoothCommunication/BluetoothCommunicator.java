package BluetoothCommunication;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;

import java.util.List;

/**
 * Created by Jan Urbansky on 14.02.2016.
 *
 *
 * TODO: Improvement Ideas
 *
 * Enable a mode that only reacts on messages directed to the client -> lowers the info overhead on
 * the client device!
 *
 *
 */
public class BluetoothCommunicator {

    private Controller controller;
    private BluetoothModel bt_model;
    private ILogFragment logFragment;
    private Activity mainActivity = null;


    public BluetoothCommunicator(Activity mainActivity) {
        controller = new Controller();
        bt_model = new BluetoothModel();
        this.mainActivity = mainActivity;

    }

    /**
     * Enables Bluetooth and Discoverability.
     * TODO: Im controller trennen
     */
    public void init(){
        controller.init(mainActivity, bt_model);
    }

    /**
     * The search for connections starts.
     */
    public void start(){
        controller.startBluetoothCycle();
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

    public String getMyBtAddr(){
        return "";
    }

    public void sendToDevice(BluetoothDevice btDevice){
        sendToDevice(btDevice.getAddress());
    }

    public void sendToDevice(BluetoothConnection btConnection){
        sendToDevice(btConnection.getBluetoothDevice());
    }


}
