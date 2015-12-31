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
    private BluetoothModel bt_model;

    /**
     * Serverthread
     */
    private ServerThread mAcceptThread;
    /**
     * Clientthread
     */
    private ClientThread mClientThread;

    /**
     * Verwaltet eine erfolgreiche Verbindung.
     */
    private ConnectedThread mConnectedThread;

    BluetoothAdapter mBluetoothAdapter;
    // Hier (0x1101 => Serial Port Profile + Base_UUID)
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String mServiceName = "SerialPort";    //"KT-Service";

    //wird in der Activity abgefangen
    public static final int REQUEST_ENABLE_BT = 1;
    //Enable discoverability
    public static final int REQUEST_ENABLE_DISCO = 2;



    //TODO: UI_States entfernen und neue hinzufügen.
    public enum SmMessage {
        UI_START_SERVER, UI_STOP_SERVER, UI_SEND,       // from UI
        ENABLE_BT, ENABLE_DISCOVERABILITY, WAIT_FOR_INTENT, CONNECT_TO_DEVICE, READ_PAIRED_DEVICES,
        // Bluetooth Initiation
        CO_INIT,                                        // to Controller
        //Try connecting
        FIND_DEVICE, CONNECT_AS_SERVER, CONNECT_AS_CLIENT,
        AT_MANAGE_CONNECTED_SOCKET_AS_SERVER, AT_MANAGE_CONNECTED_SOCKET_AS_CLIENT, AT_DEBUG_SERVER,           // from ServerThread
        CT_RECEIVED, CT_CONNECTION_CLOSED, AT_DEBUG_CLIENT, CT_DEBUG     // from ConnectedThread
    }

    private enum State {
        START, INIT_BT, WAIT_FOR_CONNECT, CONNECTED
    }

    private State state = State.START;        // the state variable

    public static SmMessage[] messageIndex = SmMessage.values();

    public Controller() {
        Log.d(TAG, "Controller()");
    }

    public void init(Activity a, Fragment frag, BluetoothModel bt_model) {
        Log.d(TAG, "init()");

        mActivity = a;
        this.bt_model = bt_model;

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

        /**
         * inputSmMessage ist nicht die Message selber. Nur der enum Wert.
         */
        SmMessage inputSmMessage = messageIndex[message.what];



        // erstmal ohne SM-Logging die Debug-Meldungen der Threads verarbeiten
        if ( inputSmMessage == SmMessage.AT_DEBUG_SERVER) {
            Log.d(ServerThread.TAG, (String) message.obj);
            return;
        }

        if ( inputSmMessage == SmMessage.AT_DEBUG_CLIENT) {
            Log.d(ClientThread.TAG, (String) message.obj);
            return;
        }


        if ( inputSmMessage == SmMessage.CT_DEBUG ) {
            Log.d(ConnectedThread.TAG, (String) message.obj);
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

                        mUiListener.onControllerConnectInfo("INIT_BT"); //kann raus

                        state = State.INIT_BT;
                        sendSmMessage(SmMessage.ENABLE_BT.ordinal(), 0, 0, null);
                        break;

                    default:
                        Log.v(TAG, "SM: not a valid input in this state !!!!!!");
                        break;
                }
                break;
            case INIT_BT:
                switch (inputSmMessage) {
                    case ENABLE_BT:

                        Log.d(TAG, "Init Bluetooth");
                        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (mBluetoothAdapter == null) {
                            Log.d(TAG, "Error: Device does not support Bluetooth !!!");
                            mUiListener.onControllerServerInfo(false);

                            state = State.INIT_BT; //fallback in init_bt state
                            break;
                        }

                        //die eigene Bluetoothadresse auslesen
                        bt_model.setMyBT_ADDR(mBluetoothAdapter.getAddress());


                        if ( !mBluetoothAdapter.isEnabled() ) {
                            Log.d(TAG, "Try to enable Bluetooth.");
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            mActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                            //warte auf den Intent
                            sendSmMessage(SmMessage.WAIT_FOR_INTENT.ordinal(), 0, 0, null);

                        }else{
                            sendSmMessage(SmMessage.ENABLE_DISCOVERABILITY.ordinal(), 0, 0, null);
                        }
                        break;


                    case ENABLE_DISCOVERABILITY:
                        //das Gerät sichtbar schalten
                        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0); //0 bedeutet, dass das Gerät immer sichtbar ist.
                        mActivity.startActivityForResult(discoverableIntent, REQUEST_ENABLE_DISCO);
                        sendSmMessage(SmMessage.WAIT_FOR_INTENT.ordinal(), 0, 0, null);

                        break;


                    //unnötig für die funktion der app... TODO DELETE
                    case READ_PAIRED_DEVICES:
                        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                        Log.d(TAG, "paired devices:");
                        if (pairedDevices.size() > 0) {
                            bt_model.setPairedDevices(pairedDevices);
                            for (BluetoothDevice device : pairedDevices) {
                                Log.d(TAG, "   " + device.getName() + "  " + device.getAddress());
                            }
                        }

                        state = State.WAIT_FOR_CONNECT;
                        sendSmMessage(SmMessage.FIND_DEVICE.ordinal(), 0, 0, null);

                        break;

                    //Dieser State wird benutzt, um die App so lange anzuhalten, bis ein Intent erfolgreich
                    //zurück geschrieben hat.
                    case WAIT_FOR_INTENT:
                        break;




                    default:
                        Log.v(TAG, "SM INIT_BT: not a valid input in this state !!!!!!");
                        break;
                }
                Log.v(TAG, "STATE: "+state +" INPUT: "+inputSmMessage);
                break;

            case WAIT_FOR_CONNECT:
                switch (inputSmMessage) {

                    /*
                    Versuche Discovery für ~12 secs. Wenn erfolgreich, nehme den Server-Socket des Device und
                    erstelle einen RFComm-Socket und initiatiere mit connect().
                     */
                    case FIND_DEVICE:
                        Log.d(TAG, "suche devices");
                        //bluetooth broadcast receiver here TODO
                        if(!mBluetoothAdapter.startDiscovery()){
                            Log.d(TAG, "discovery not starting...");
                        }
                        state = State.WAIT_FOR_CONNECT;
                        break;

                    case CONNECT_AS_CLIENT:
                        Log.d(TAG, "instanziere ClientThread");
                        //der Broadcast Receiver aus der MainActivity sendet das Bluetoothdevice
                        BluetoothDevice bluetoothDevice = (BluetoothDevice) message.obj;
                        mClientThread = new ClientThread(bluetoothDevice, mBluetoothAdapter, this);
                        mClientThread.start();
                        state = State.WAIT_FOR_CONNECT;
                        break;



                    //ServerThread startet
                    case CONNECT_AS_SERVER:
                        Log.d(TAG, "instanziere ServerThread");
                        mAcceptThread = new ServerThread(mBluetoothAdapter, this, mServiceName);
                        mAcceptThread.start();

                        mUiListener.onControllerServerInfo(true);
                        mUiListener.onControllerConnectInfo("Wait for connect\nattempt");
                        state = State.WAIT_FOR_CONNECT;
                        break;

                    case AT_MANAGE_CONNECTED_SOCKET_AS_SERVER:
                        Log.d(TAG, "manage connected Socket als Server");
                        //Accept thread wird abbgebroche und ein neuer ClientThread startet
                        //An dieser Stelle muss ein neuer ServerThread gestartet werden. Es kann nicht
                        //mehr als 7 Geräte gleichzeitig gestartet werden (Bluetooth Standard)
                        //TODO Neuer State
                        mAcceptThread.cancel();
                        mConnectedThread = new ConnectedThread((BluetoothSocket)message.obj, this);
                        mConnectedThread.start();

                        mUiListener.onControllerServerInfo(true);
                        mUiListener.onControllerConnectInfo("Connected");
                        state = State.CONNECTED;
                        break;

                    case AT_MANAGE_CONNECTED_SOCKET_AS_CLIENT:
                        Log.d(TAG, "manage connected Socket als Client");
                        mClientThread.cancel();
                        mConnectedThread = new ConnectedThread((BluetoothSocket)message.obj, this);
                        mConnectedThread.start();

                        state = State.CONNECTED;
                        break;

                    default:
                        Log.v(TAG, "SM WAIT_FOR_CONNECT: not a valid input in this state !!!!!!");
                        break;
                }
                Log.v(TAG, "STATE: "+state +" INPUT: "+inputSmMessage);
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
                        bt_model.setMessageReceivedFrom(str);

                        //mUiListener.onControllerReceived( str );
                        break;

                    //Das verbundene Gerät aus dem Geräte speicher löschen. ? Einfach mit boundDevices(?) im
                    case CT_CONNECTION_CLOSED:
                    case UI_STOP_SERVER:
                        mConnectedThread.cancel();

                        mUiListener.onControllerServerInfo(false);
                        mUiListener.onControllerConnectInfo("INIT_BT");
                        state = State.INIT_BT;
                        break;

                    default:
                        Log.v(TAG, "SM: not a valid input in this state !!!!!!");
                        break;
                }
                Log.v(TAG, "STATE: "+state +" INPUT: "+inputSmMessage);
        }
         Log.i(TAG, "SM: new State: " + state);
     }


    public void bluetoothAdapterEnabled(){
        Log.d(TAG, "bluetoothAdapterEnabled");
        sendSmMessage(SmMessage.ENABLE_DISCOVERABILITY.ordinal(), 0, 0,null);
    }

    public void discoverabilityEnabled() {
        Log.d(TAG, "discoverabilityEnabled()");
        sendSmMessage(SmMessage.READ_PAIRED_DEVICES.ordinal(), 0, 0, null);
    }

    public void deviceDiscovered(BluetoothDevice bluetoothDevice){
        Log.d(TAG, "deviceDiscovered: "+bluetoothDevice.getName());
        sendSmMessage(SmMessage.CONNECT_AS_CLIENT.ordinal(), 0, 0, bluetoothDevice);
    }

    public interface OnControllerInteractionListener {
        public void onControllerReceived(String str);
        public void onControllerConnectInfo(String strState);
        public void onControllerServerInfo(Boolean serverInfo);
    }

}

