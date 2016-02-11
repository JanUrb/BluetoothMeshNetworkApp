package BluetoothCommunication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.mobilecomputing.alarmanlage2015.alarmanlageapp.Controller;

import java.io.IOException;


/**
 * Created by Jan Urbansky on 29.12.2015.
 * <p/>
 * Übernommen von
 * https://developer.android.com/guide/topics/connectivity/bluetooth.html#ConnectingAsAClient
 * <p/>
 * Debugging mit debugOut und android.util.Log
 */
public class ClientThread extends Thread {
    public final static String TAG = "fhflClientThread";
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private BluetoothAdapter mBluetoothAdapter;
    private Controller mController;


    public ClientThread(BluetoothDevice device, BluetoothAdapter bluetoothAdapter,
                        Controller controller) {

        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;
        mBluetoothAdapter = bluetoothAdapter;
        mController = controller;

        //debugOut braucht den Controller. Daher erst hier aufrufen!!
        debugOut("ClientThread()");


        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
//
            tmp = mmDevice.createRfcommSocketToServiceRecord(Controller.MY_UUID);
//            Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
//            tmp = (BluetoothSocket) m.invoke(device, 1);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Exception " + e.getMessage());
        }

        //tmp = mmDevice.createRfcommSocketToServiceRecord(Controller.MY_UUID);

        mmSocket = tmp;
    }

    /**
     * Versucht sich mit dem Device zu verbinden. Wenn dies fehlschlägt, wird in den ServerModus
     * übergegangen
     */
    public void run() {
        debugOut("ClientThread.run()");
        //verbindet sich mit dem anderen device und wird in die statemachine geschickt.
        // Cancel discovery because it will slow down the connection
        if (!mBluetoothAdapter.cancelDiscovery()) {
            debugOut("cancelDiscovery() failed!!");
            cancel();
        }

        try {
            //blockiert den Thread!
            //Der Vorgang wirft einen Service Discovery Failed Error, wenn ein Gerät gefunden wurde,
            // aber nicht der Service.
            // An dieser Stelle scheitert eins meiner Testgeräte mit API LVL 16.
            mmSocket.connect();
            mController.obtainMessage(Controller.SmMessage.AT_MANAGE_CONNECTED_SOCKET_AS_CLIENT.ordinal(),
                    -1, -1, mmSocket).sendToTarget();
        } catch (IOException connectException) {
            connectException.printStackTrace();

            /*
            an dieser Stelle erhalte ich den Fehler read failed, socket might closed or timeout, read ret: -1
            google hat ergeben: http://stackoverflow.com/a/25647197
            In der aktuellen Version wird einfach der Server-Modus aufgerufen und später nochmal versucht
            sich zu verbinden. Dies scheint in der Praxis gut zu funktionieren

            Versionen mit denen getestet wurde:
                4.1.2 (lvl 16) -> fehlgeschlagen
                5.1.1 (lvl 22) -> erfolgreich
            Mit entfernen und neu verkoppeln kann das Problem (manchmal) behoben werden!
            */


            debugOut("ClientThread.run " + connectException.getMessage());
            Log.d(TAG, "connectingError: " + connectException.getMessage());

            mController.obtainMessage(Controller.SmMessage.CONNECT_AS_SERVER.ordinal(),
                    -1, -1, mmSocket).sendToTarget();

            try {
                mmSocket.close();
            } catch (IOException closeException) {
                connectException.printStackTrace();
                debugOut("Schliessen des Sockets nicht erfolgreich!");
            }
            return;
        } catch (NullPointerException nullExe) {
            nullExe.printStackTrace();
            debugOut("ClientThread.run null exception");
        }
    }

    /**
     * Will cancel an in-progress connection, and close the socket
     */
    public void cancel() {
        debugOut("cancel()");
        try {
            mmSocket.close();
        } catch (IOException e) {
            debugOut("cancel() ClientThread: IOException during closing socket !!!");
        }
    }


    private void debugOut(String str) {
        try {
            if (mController == null) {
                throw new NullPointerException("Controller is null");
            }
            mController.obtainMessage(Controller.SmMessage.AT_DEBUG_CLIENT.ordinal(),
                    -1, -1, str).sendToTarget();
        } catch (NullPointerException e) {
            //mit loglvl Debug und Filter: ClientThread ergibt als Fehler null -> NullPointer
            e.printStackTrace();
            Log.d("ClientThread", "" + e.getMessage());
        }
    }
}
