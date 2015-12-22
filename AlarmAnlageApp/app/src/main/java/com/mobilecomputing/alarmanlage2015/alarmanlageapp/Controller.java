package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import java.util.Set;
import java.util.UUID;

import fllog.Log;

/**
 * Created by Jan Urbansky on 19.12.2015.
 *
 *  Übernommen aus dem RFCOMM-Server Projekt und unserem Projekt angepasst.
 *
 */
public class Controller extends StateMachine{

    private static final String TAG = "fhflController";
    private OnControllerInteractionListener mUiListener = null;
    private Activity mActivity = null;

    private AcceptThread mAcceptThread;
    private ConnectedThread mConnectedThread;

    BluetoothAdapter mBluetoothAdapter;
    // Hier (0x1101 => Serial Port Profile + Base_UUID)
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String mServiceName = "SerialPort";    //"KT-Service";

    protected static final int REQUEST_ENABLE_BT = 1;



    //TODO: UI_States entfernen und neue hinzufügen.
    public enum SmMessage {
        UI_START_SERVER, UI_STOP_SERVER, UI_SEND,       // from UI
        CO_INIT,                                        // to Controller
        AT_MANAGE_CONNECTED_SOCKET, AT_DEBUG,           // from AcceptThread
        CT_RECEIVED, CT_CONNECTION_CLOSED, CT_DEBUG     // from ConnectedThread
    }

    private enum State {
        START, IDLE, WAIT_FOR_CONNECT, CONNECTED
    }

    private State state = State.START;        // the state variable

    public static SmMessage[] messageIndex = SmMessage.values();

    public Controller() {
        Log.d(TAG, "Controller()");
    }

    public void init(Activity a, Fragment frag) {
        Log.d(TAG, "init()");

        mActivity = a;

        // init InterfaceListener
        try {
            mUiListener = (OnControllerInteractionListener) frag;
        } catch (ClassCastException e) {
            throw new ClassCastException(frag.toString()
                    + " must implement OnFragmentInteractionListener !!!!!!! ");
        }

        // send message for start transition
        sendSmMessage(SmMessage.CO_INIT.ordinal(), 0, 0, null);
    }

    /**
     * the statemachine
     *
     *   call it only via sendSmMessage()
     *
     * @param message
     */
    @Override
    void theBrain(android.os.Message message){
        SmMessage inputSmMessage = messageIndex[message.what];

        // erstmal ohne SM-Logging die Debug-Meldungen der Threads verarbeiten
        if ( inputSmMessage == SmMessage.AT_DEBUG ) {
            Log.d("AcceptThread: ", (String) message.obj);
            return;
        }

        if ( inputSmMessage == SmMessage.CT_DEBUG ) {
            Log.d("ConnectThread: ", (String) message.obj);
            return;
        }

        // jetzt gehts erst richtig los
        Log.i(TAG, "SM: state: " + state + ", input message: " +
                inputSmMessage.toString() + ", arg1: " +
                message.arg1 + ", arg2: " + message.arg2);
         if (message.obj != null){
             Log.i(TAG, "SM: data: " + message.obj.toString());
         }

        // der Rest
        switch ( state ) {
            case START:
                switch (inputSmMessage) {
                    case CO_INIT:
                        Log.v(TAG, "in Init");

                        mUiListener.onControllerConnectInfo("IDLE");

                        state = State.IDLE;
                        break;
                    default:
                        Log.v(TAG, "SM: not a valid input in this state !!!!!!");
                        break;
                }
                break;
            case IDLE:
                switch (inputSmMessage) {
                    case UI_START_SERVER:

                        Log.d(TAG, "Init Bluetooth");
                        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (mBluetoothAdapter == null) {
                            Log.d(TAG, "Error: Device does not support Bluetooth !!!");
                            mUiListener.onControllerServerInfo(false);

                            state = State.IDLE;
                            break;
                        }

                        if ( !mBluetoothAdapter.isEnabled() ) {
                            Log.d(TAG, "Try to enable Bluetooth.");
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            mActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        }
                        // ##todo:  evaluate returnvalue s. Foliensatz

                        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                        Log.d(TAG, "paired devices:");
                        if (pairedDevices.size() > 0) { //? Ist diese Anweisung nicht unnötig? Würde BluetoothDevice device : pairedDevices nicht reichen?
                            // Loop through paired devices
                            for (BluetoothDevice device : pairedDevices) {
                                Log.d(TAG, "   " + device.getName() + "  " + device.getAddress());
                            }
                        }
                        Log.d(TAG, "instanziere AcceptThread");

                        //AcceptThread startet
                        mAcceptThread = new AcceptThread(mBluetoothAdapter, this, MY_UUID, mServiceName);
                        mAcceptThread.start();

                        mUiListener.onControllerServerInfo(true);
                        mUiListener.onControllerConnectInfo("Wait for connect\nattempt");
                        state = State.WAIT_FOR_CONNECT;
                        break;
                    default:
                        Log.v(TAG, "SM: not a valid input in this state !!!!!!");
                        break;
                }
                break;

            case WAIT_FOR_CONNECT:
                switch (inputSmMessage) {

                    case UI_STOP_SERVER:
                        mAcceptThread.cancel();

                        mUiListener.onControllerServerInfo(false);
                        mUiListener.onControllerConnectInfo("IDLE");
                        state = State.IDLE;
                        break;

                    case AT_MANAGE_CONNECTED_SOCKET:

                        //Accept thread wird abbgebroche und ein neuer ConnectThread startet
                        //An dieser Stelle muss ein neuer AcceptThread gestartet werden. Es kann nicht
                        //mehr als 7 Geräte gleichzeitig gestartet werden (Bluetooth Standard)
                        //TODO Neuer State
                        mAcceptThread.cancel();
                        mConnectedThread = new ConnectedThread((BluetoothSocket)message.obj, this);
                        mConnectedThread.start();

                        mUiListener.onControllerServerInfo(true);
                        mUiListener.onControllerConnectInfo("Connected");
                        state = State.CONNECTED;
                        break;

                    default:
                        Log.v(TAG, "SM: not a valid input in this state !!!!!!");
                        break;
                }
                break;

            case CONNECTED:
                switch (inputSmMessage) {

                    case UI_SEND:
                        mConnectedThread.write(((String) message.obj).getBytes());
                        break;

                    //bei uns gibt es mehrere CT. Hier muss gefiltert werden, ob die Nachricht an mich
                    //gesendet werden soll.
                    //TODO
                    case CT_RECEIVED:
                        String str = new String((byte[]) message.obj, 0, message.arg1);
                        mUiListener.onControllerReceived( str );
                        break;

                    //Das verbundene Gerät aus dem Geräte speicher löschen.
                    case CT_CONNECTION_CLOSED:
                    case UI_STOP_SERVER:
                        mConnectedThread.cancel();

                        mUiListener.onControllerServerInfo(false);
                        mUiListener.onControllerConnectInfo("IDLE");
                        state = State.IDLE;
                        break;

                    default:
                        Log.v(TAG, "SM: not a valid input in this state !!!!!!");
                        break;
                }
        }
         Log.i(TAG, "SM: new State: " + state);
     }

    public interface OnControllerInteractionListener {
        public void onControllerReceived(String str);
        public void onControllerConnectInfo(String strState);
        public void onControllerServerInfo(Boolean serverInfo);
    }

}

