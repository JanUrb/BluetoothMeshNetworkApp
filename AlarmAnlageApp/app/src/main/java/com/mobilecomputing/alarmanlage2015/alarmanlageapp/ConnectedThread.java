package com.mobilecomputing.alarmanlage2015.alarmanlageapp;


import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {

    private Controller mController;
    private final BluetoothSocket mSocket;
    private final InputStream mInStream;
    private final OutputStream mOutStream;

    public ConnectedThread(BluetoothSocket socket, Controller controller) {
        mSocket = socket;
        mController = controller;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        debugOut("ConnectedThread()");

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            debugOut("ConnectedThread(): Error: IOException during get streams !!!");
        }

        mInStream = tmpIn;
        mOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()

        debugOut("run()");

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = mInStream.read(buffer);
                mController.obtainMessage(Controller.SmMessage.CT_RECEIVED.ordinal(), bytes, -1, buffer)
                        .sendToTarget(); // obtain...(): delivers 'empty' message-object from a pool
            } catch (IOException e) {
                debugOut("run(): IOException during read stream");
                mController.obtainMessage(Controller.SmMessage.CT_CONNECTION_CLOSED.ordinal(), -1, -1, null).sendToTarget();
                break;
            }
        }
        debugOut("run(): thread terminates");
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            String sendStr = new String(bytes);
            debugOut("write(" + sendStr + ")");

            mOutStream.write(bytes);
        } catch (IOException e) {
        }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        debugOut("cancel()");
        try {
            mSocket.close();
        } catch (IOException e) {
        }
    }

    private void debugOut(String str) {
        mController.obtainMessage(Controller.SmMessage.CT_DEBUG.ordinal(), -1, -1, str).sendToTarget();
    };
}
