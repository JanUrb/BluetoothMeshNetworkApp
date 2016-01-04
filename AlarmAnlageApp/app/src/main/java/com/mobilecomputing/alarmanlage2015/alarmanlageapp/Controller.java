package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Set;
import java.util.UUID;

import fllog.Log;

/**
 * Created by Jan Urbansky on 19.12.2015.
 * <p/>
 * Übernommen aus dem RFCOMM-Server Projekt und unserem Projekt angepasst.
 */
public class Controller extends StateMachine {

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

    public static final int MAX_NUMBER_OF_DEVICES = 7;
    private static final String mServiceName = "SerialPort";    //"KT-Service";

    //wird in der Activity abgefangen
    public static final int REQUEST_ENABLE_BT = 1;
    //Enable discoverability
    public static final int REQUEST_ENABLE_DISCO = 2;


    //TODO: UI_States entfernen und neue hinzufügen.
    public enum SmMessage {
        UI_START_SERVER, UI_STOP_SERVER, SEND_MESSAGE,       // from UI
        ENABLE_BT, ENABLE_DISCOVERABILITY, WAIT_FOR_INTENT, CONNECT_TO_DEVICE, READ_PAIRED_DEVICES,
        // Bluetooth Initiation
        CO_INIT,                                        // to Controller
        //Try connecting
        FIND_DEVICE, CONNECT_AS_SERVER, CONNECT_AS_CLIENT,
        //THREAD_CONNECTED
        MAX_THREAD_NUMBER, START_NEW_CONNECTION_CYCLE,
        AT_MANAGE_CONNECTED_SOCKET_AS_SERVER, AT_MANAGE_CONNECTED_SOCKET_AS_CLIENT, AT_DEBUG_SERVER, AT_DEBUG_TIMER,          // from ServerThread
        CT_RECEIVED, CT_CONNECTION_CLOSED, AT_DEBUG_CLIENT, CT_DEBUG     // from ConnectedThread
    }

    private enum State {
        START, INIT_BT, WAIT_FOR_CONNECT, THREAD_CONNECTED
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
     * <p/>
     * call it only via sendSmMessage()
     *
     * @param message
     */
    @Override
    void theBrain(android.os.Message message) {

        /**
         * inputSmMessage ist nicht die Message selber. Nur der enum Wert.
         */
        SmMessage inputSmMessage = messageIndex[message.what];


        // erstmal ohne SM-Logging die Debug-Meldungen der Threads verarbeiten
        if (inputSmMessage == SmMessage.AT_DEBUG_SERVER) {
            Log.d(ServerThread.TAG, (String) message.obj);
            return;
        }

        if (inputSmMessage == SmMessage.AT_DEBUG_CLIENT) {
            Log.d(ClientThread.TAG, (String) message.obj);
            return;
        }

        if (inputSmMessage == SmMessage.AT_DEBUG_TIMER) {
            Log.d(ServerTimerThread.TAG, (String) message.obj);
            return;
        }

        if (inputSmMessage == SmMessage.CT_DEBUG) {
            Log.d(ConnectedThread.TAG, (String) message.obj);
            return;
        }

        if (inputSmMessage == SmMessage.CT_RECEIVED) {
            Log.d(TAG, "inputSmMessage == SmMessage.CT_RECEIVED");
            Message receivedMsg = null;
            byte[] bytes = (byte[]) message.obj;

            //Quelle: https://stackoverflow.com/questions/5837698/converting-any-object-to-a-byte-array-in-java
            ByteArrayInputStream b = new ByteArrayInputStream(bytes);
            try {
                ObjectInputStream o = new ObjectInputStream(b);
                receivedMsg = (Message) o.readObject();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "SmMessage.CT_RECEIVED IOError: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Log.d(TAG, "SmMessage.CT_RECEIVED ClassNotFound: " + e.getMessage());
            }
            Log.v(ConnectedThread.TAG, "MessageReceived: " + receivedMsg.getMessageId());
            bt_model.setCurrentMessage(receivedMsg);
            return;
        }


        if (inputSmMessage == SmMessage.SEND_MESSAGE) {
            Log.d(TAG, "StateMachine: SmMessage.SEND_MESSAGE");
            String btAddress = (String) message.obj;
            Message sendMessage = null;
            //TODO: in Methode auslagern!
            //validiere MAC TODO; Exceptions -> Fehlerausgabe als Toast(?)
            if (!BluetoothAdapter.checkBluetoothAddress(btAddress)) {
                //Error TODO: Throw Exception
                Log.d(TAG, "mac addresse nicht valid");
                //testing... TODO: Entfernen!!
                btAddress = ((Connection) bt_model.getConnections().toArray()[0]).getDeviceAddress();
                sendMessage = new Message(mBluetoothAdapter.getAddress(), btAddress);
            }
            //überprüfe ob die BT Adresse direkt zu erreichen ist.
            boolean directlyConnected = false;
            Connection directConnection = null;
            for (Connection connection : bt_model.getConnections()) {
                if (connection.getDeviceAddress().equals(btAddress)) {
                    directlyConnected = true;
                    directConnection = connection;
                    break; //das Gerät wurde schon gefunden -> keine Suche mehr nötig.
                }
            }

            //senden der Nachricht TODO: Nachrichten Klasse erstellen mit ID und TargetMac
            if (directlyConnected) {
                try {
                    directConnection.write(sendMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "IOException directlyConnected " + e.getMessage());
                }
            } else {
                for (Connection connection : bt_model.getConnections()) {
                    try {
                        connection.write(sendMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG, "IOException Indirectly Connected " + e.getMessage());
                    }
                }
            }

        }

        // jetzt gehts erst richtig los
        Log.i(TAG, "SM: state: " + state + ", input message: " +
                inputSmMessage.toString() + ", arg1: " +
                message.arg1 + ", arg2: " + message.arg2);
        if (message.obj != null) {
            Log.i(TAG, "SM: data: " + message.obj.toString());
        }

        // der Rest
        switch (state) {
            case START:
                switch (inputSmMessage) {
                    case CO_INIT:
                        Log.v(TAG, "in Init");

//                        mUiListener.onControllerConnectInfo("INIT_BT"); //kann raus

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
//                            mUiListener.onControllerServerInfo(false);

                            state = State.INIT_BT; //fallback in init_bt state
                            break;
                        }

                        //die eigene Bluetoothadresse auslesen
                        bt_model.setMyBT_ADDR(mBluetoothAdapter.getAddress());


                        if (!mBluetoothAdapter.isEnabled()) {
                            Log.d(TAG, "Try to enable Bluetooth.");
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            mActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                            //warte auf den Intent
                            sendSmMessage(SmMessage.WAIT_FOR_INTENT.ordinal(), 0, 0, null);

                        } else {
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
                Log.v(TAG, "STATE: " + state + " INPUT: " + inputSmMessage);
                break;

            //verwaltet das Erstellen einer Verbindung als Client oder als Server.
            case WAIT_FOR_CONNECT:
                switch (inputSmMessage) {

                    /*
                    Versuche Discovery für ~12 secs. Wenn erfolgreich, nehme den Server-Socket des Device und
                    erstelle einen RFComm-Socket und initiatiere mit connect().
                     */
                    case FIND_DEVICE:
                        Log.d(TAG, "suche devices");
                        //siehe BroadcastReceiver und Filter in der MainActivity
                        if (!mBluetoothAdapter.startDiscovery()) {
                            Log.d(TAG, "discovery not starting...");
                        }
                        state = State.WAIT_FOR_CONNECT;
                        break;

                    //clientseitiger Verbindungsaufbau
                    case CONNECT_AS_CLIENT:
                        Log.d(TAG, "instanziere ClientThread");
                        //Aufräumarbeiten falls etwas falsch läuft.
                        if (mAcceptThread != null && mAcceptThread.isAlive()) {
                            Log.d(TAG, "clean up AcceptThread");
                            mAcceptThread.cancel();
                            mAcceptThread = null;
                        }
                        //der Broadcast Receiver aus der MainActivity sendet das Bluetoothdevice
                        BluetoothDevice bluetoothDevice = (BluetoothDevice) message.obj;
                        mClientThread = new ClientThread(bluetoothDevice, mBluetoothAdapter, this);
                        mClientThread.start();
                        state = State.WAIT_FOR_CONNECT;
                        break;

                    //Serverseitiger Verbindungsaufbau
                    case CONNECT_AS_SERVER:
                        Log.d(TAG, "instanziere ServerThread");
                        if (mClientThread != null && mClientThread.isAlive()) {
                            Log.d(TAG, "clean up ClientThread");
                            mClientThread.cancel();
                            mClientThread = null;
                        }
                        mAcceptThread = new ServerThread(mBluetoothAdapter, this, mServiceName);
                        ServerTimerThread timerThread = new ServerTimerThread(mAcceptThread, this);
                        mAcceptThread.start();
                        timerThread.start();
//                        mUiListener.onControllerServerInfo(true);
//                        mUiListener.onControllerConnectInfo("Wait for connect\nattempt");
                        state = State.WAIT_FOR_CONNECT;
                        break;

                    case AT_MANAGE_CONNECTED_SOCKET_AS_SERVER:
                        Log.d(TAG, "manage connected Socket als Server");
                        //Accept thread wird abbgebroche und ein neuer ClientThread startet
                        //An dieser Stelle muss ein neuer ServerThread gestartet werden. Es kann nicht
                        //mehr als 7 Geräte gleichzeitig gestartet werden (Bluetooth Standard)

                        mAcceptThread.cancel();
                        BluetoothSocket socket = (BluetoothSocket) message.obj;
                        BluetoothDevice btDevice = socket.getRemoteDevice();
                        mConnectedThread = new ConnectedThread(socket, this);
                        Connection connection = new Connection(mConnectedThread, btDevice);
                        connection.start();
                        bt_model.addConnection(connection);
                        //TODO Connection Class verwenden
//                        mUiListener.onControllerServerInfo(true);
//                        mUiListener.onControllerConnectInfo("Connected");
                        state = State.THREAD_CONNECTED;
                        sendSmMessage(SmMessage.START_NEW_CONNECTION_CYCLE.ordinal(), 0, 0, null);
                        break;

                    case AT_MANAGE_CONNECTED_SOCKET_AS_CLIENT:
                        Log.d(TAG, "manage connected Socket als Client");
                        //mClientThread.cancel();
                        mConnectedThread = new ConnectedThread((BluetoothSocket) message.obj, this);
                        Connection clientConnection = new Connection(mConnectedThread, ((BluetoothSocket) message.obj).getRemoteDevice());
                        clientConnection.start();
                        bt_model.addConnection(clientConnection);
                        //TODO Connection Class verwenden
                        state = State.THREAD_CONNECTED;
                        sendSmMessage(SmMessage.START_NEW_CONNECTION_CYCLE.ordinal(), 0, 0, null);
                        break;

                    default:
                        Log.v(TAG, "SM WAIT_FOR_CONNECT: not a valid input in this state !!!!!!");
                        break;
                }
                Log.v(TAG, "STATE: " + state + " INPUT: " + inputSmMessage);
                break;

            //THREAD_CONNECTED verwaltet einen Zustand mit mindestens einem verbundenen Thread.
            case THREAD_CONNECTED:
                switch (inputSmMessage) {

                    case START_NEW_CONNECTION_CYCLE:
                        //teste ob die MAX_NUM_CONNECTION_THREADS erreicht wurde.
                        state = State.WAIT_FOR_CONNECT;
                        sendSmMessage(SmMessage.FIND_DEVICE.ordinal(), 0, 0, null);
                        break;


                    //warte auf das beenden eines ConnectedThreads
                    case MAX_THREAD_NUMBER:

                        state = State.THREAD_CONNECTED;
                        break;


                    //bei uns gibt es mehrere CT. Hier muss gefiltert werden, ob die Nachricht an mich
                    //gesendet werden soll.
                    //TODO RM
//                    case CT_RECEIVED:
//                        String str = new String((byte[]) message.obj, 0, message.arg1);
//                        bt_model.setMessageReceivedFrom(str);
//
//                        //mUiListener.onControllerReceived( str );
//                        break;

                    //Das verbundene Gerät aus dem Geräte speicher löschen. ? Einfach mit boundDevices(?) im
                    case CT_CONNECTION_CLOSED:
                    case UI_STOP_SERVER:
                        mConnectedThread.cancel();

//                        mUiListener.onControllerServerInfo(false);
//                        mUiListener.onControllerConnectInfo("INIT_BT");
                        state = State.INIT_BT;
                        break;

                    default:
                        Log.v(TAG, "SM: not a valid input in this state !!!!!!");
                        break;
                }
                Log.v(TAG, "STATE: " + state + " INPUT: " + inputSmMessage);
        }
        Log.i(TAG, "SM: new State: " + state);
    }

    //TODO Rename!!
    public void bluetoothAdapterEnabled() {
        Log.d(TAG, "bluetoothAdapterEnabled");
        sendSmMessage(SmMessage.ENABLE_DISCOVERABILITY.ordinal(), 0, 0, null);
    }

    public void discoverabilityEnabled() {
        Log.d(TAG, "discoverabilityEnabled()");
        sendSmMessage(SmMessage.READ_PAIRED_DEVICES.ordinal(), 0, 0, null);
    }

    public void startClientThread(BluetoothDevice bluetoothDevice) {
        Log.d(TAG, "startClientThread: " + bluetoothDevice.getName());
        sendSmMessage(SmMessage.CONNECT_AS_CLIENT.ordinal(), 0, 0, bluetoothDevice);
    }

    public void startServerThread() {
        Log.d(TAG, "startServerThread()");
        sendSmMessage(SmMessage.CONNECT_AS_SERVER.ordinal(), 0, 0, null);
    }

    public interface OnControllerInteractionListener {
        //TODO: Parameter in MessageObj ändern.
        public void onControllerReceived(String str);
    }

}

