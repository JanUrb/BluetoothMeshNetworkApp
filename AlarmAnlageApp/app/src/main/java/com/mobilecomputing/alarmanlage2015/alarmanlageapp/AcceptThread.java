package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;


public class AcceptThread extends Thread {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothServerSocket mServerSocket;
    private Controller mController;
    private UUID mUUID;
    private String mServiceName;

    public AcceptThread(BluetoothAdapter btAdapter, Controller controller, UUID uuID, String serviceName) {

        mBluetoothAdapter = btAdapter;
        mController = controller;
        mUUID = uuID;
        mServiceName = serviceName;

        debugOut("AcceptThread()");	// handler must be already initialized !!!


        if (mBluetoothAdapter == null)
        {
            debugOut("AcceptThread(): Error: mBluetoothAdapter == null");
            return;
        }
        else
        {
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                debugOut("AcceptThread(): create service-record");

                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(mServiceName, mUUID);
            }
            catch (IOException e) {
                debugOut("AcceptThread(): Error: IOException during get server-socket !!!");
                return;
            };

            mServerSocket = tmp;
        }

    }

    public void run() {
        BluetoothSocket socket = null;

        debugOut("AcceptThread.run()");

        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                debugOut("run(): listen");

                socket = mServerSocket.accept();
            } catch (IOException e) {
                debugOut("run(): Error: IOException during listen !!!");

                break;
            }

            debugOut("run(): connection accepted");

            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
                mController.obtainMessage(Controller.SmMessage.AT_MANAGE_CONNECTED_SOCKET.ordinal(),
                        -1, -1, socket).sendToTarget();

                try {
                    mServerSocket.close();

                    debugOut("run(): accept server-socket closed");
                } catch (IOException e) {
                    debugOut("run(): Error: IOException during closing socket !!!");
                }

                break;
            }
        }
        debugOut("run(): thread terminates");
    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        debugOut("cancel()");
        try {
            mServerSocket.close();
        } catch (IOException e) {
            debugOut("cancel(): IOException during closing socket !!!");
        }
    }

    private void debugOut(String str) {
        mController.obtainMessage(Controller.SmMessage.AT_DEBUG.ordinal(),
                -1, -1, str).sendToTarget();
    };

}
