package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Jan Urbansky on 29.12.2015.
 * <p/>
 * Übernommen von
 * https://developer.android.com/guide/topics/connectivity/bluetooth.html#ConnectingAsAClient
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


        this.mBluetoothAdapter = mBluetoothAdapter;
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = mmDevice.createRfcommSocketToServiceRecord(Controller.MY_UUID);
        } catch (IOException e) {
            Log.d(TAG, "rfcommExcecption: " + e.getLocalizedMessage());
            debugOut("Rfcomm Socket nicht erstellt:  " + e.getMessage());
        }
        mmSocket = tmp;
    }

    public void run() {
        debugOut("ClientThread.run()");
        //verbindet sich mit dem anderen device und wird in die statemachine geschickt.
        // Cancel discovery because it will slow down the connection
        if (!mBluetoothAdapter.cancelDiscovery()) {
            debugOut("cancelDiscovery() failed!!");
        }

        try {
            //blockiert den Thread!
            //Der Vorgang wirft einen Service Discovery Failed Error, wenn ein Gerät gefunden wurde,
            // aber nicht der Service.

            mmSocket.connect();
            mController.obtainMessage(Controller.SmMessage.AT_MANAGE_CONNECTED_SOCKET_AS_CLIENT.ordinal(),
                    -1, -1, mmSocket).sendToTarget();
        } catch (IOException connectException) {
            connectException.printStackTrace();
            //an dieser Stelle erhalte ich den Fehler read failed, socket might closed or timeout, read ret: -1
            //google hat ergeben: http://stackoverflow.com/a/25647197
            //Versionen mit denen getestet wurde: 4.1.2 (lvl 16) -> fehlgeschlagen
            //                                    5.1.1 (lvl 22) -> erfolgreich
            debugOut("ClientThread.run " + connectException.getMessage());
            Log.d(TAG, "connectingError: " + connectException.getMessage());

            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                connectException.printStackTrace();
                debugOut("Schliessen des Sockets nicht erfolgreich!");
            }
            return;
        } catch (NullPointerException nullExe){
            nullExe.printStackTrace();
            debugOut("ClientThread.run");
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
