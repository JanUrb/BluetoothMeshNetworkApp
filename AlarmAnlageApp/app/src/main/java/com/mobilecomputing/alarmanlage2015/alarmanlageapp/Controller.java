package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.UUID;

import fllog.Log;

/**
 * Created by Jan Urbansky on 19.12.2015.
 * <p/>
 * Übernommen aus dem RFCOMM-Server Projekt und unserem Projekt angepasst.
 */
public class Controller extends StateMachine {

    private static final String TAG = "fhflController";
    private Activity mActivity = null;
    private BluetoothModel bt_model;
    private MessageStorage messageStorage = null;
    /**
     * Gibt an, ob die max Anzahl der Geräte erreicht wurde. Wird in MAX_THREAD_NUMBER und in
     * START_NEW_CONNECTION_CYCLE gesetzt
     */
    private boolean maxNumberOfDevicesReached = false;
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

    /**
     * Gibt die Maximale Anzahl der verbundenen Geräte an (7 laut Bt-Standard).
     */
    /*package*/ static final int MAX_NUMBER_OF_DEVICES = 7;

    private static final String mServiceName = "BluetoothMesh";    //"KT-Service";

    //wird in der Activity abgefangen
    public static final int REQUEST_ENABLE_BT = 1;
    //Enable discoverability
    public static final int REQUEST_ENABLE_DISCO = 2;


    //TODO: UI_States entfernen und neue hinzufügen.
    public enum SmMessage {
        SEND_MESSAGE,       // from UI
        ENABLE_BT, ENABLE_DISCOVERABILITY, WAIT_FOR_INTENT, INIT_FINISHED,
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
        messageStorage = new MessageStorage();
    }

    public void init(Activity a, BluetoothModel bt_model) {
        Log.d(TAG, "init()");

        mActivity = a;
        this.bt_model = bt_model;

        // send message for start transition
        sendSmMessage(SmMessage.CO_INIT.ordinal(), 0, 0, null);
    }

    /**
     * Die Statemachine
     * <p/>
     * Für diese App wurde die Statemachine in zwei Teile unterteilt.
     * Anfangs werden alle SmMessage abgefangen, die von Threads kommen. Diese sind ausgelagert, da
     * sie nicht an einen State gebunden sind.
     * <p/>
     * In der eigentlichen Statemachine wird die Initialisation von Bluetooth, der Verbindungsaufbau
     * und das Überwachen der Anzahl der Verbindungen abgebildet.
     *
     * @param message
     */
    @Override
    void theBrain(android.os.Message message) {

        /**
         * inputSmMessage ist nicht die Message selber. Nur der enum Wert.
         */
        SmMessage inputSmMessage = messageIndex[message.what];

        //Alle diese Messages kommen von Threads.
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
            readReceivedMessageAndRoute(message);
            return;
        }

        //Sende eine Nachricht: Aufruf kommt entweder von der GUI oder vom Empfangen einer
        //Nachricht, die nicht an dieses Gerät gerichtet ist (routing).
        if (inputSmMessage == SmMessage.SEND_MESSAGE) {
            Log.d(TAG, "StateMachine: SmMessage.SEND_MESSAGE");
            try {
                String target_mac = ((String) message.obj).toUpperCase();
                sendMessageToDevice(new Message(mBluetoothAdapter.getAddress(), target_mac));
            } catch (Exception e) {
                Log.d(TAG, "Sending message failed!!");
                e.printStackTrace();
            }

        }

        if (inputSmMessage == SmMessage.CT_CONNECTION_CLOSED) {
            long connectionID = (Long) message.obj; //long als Objekt, wie int -> Integer
            Log.d(TAG, "CT_CONNECTION_CLOSED ID: " + connectionID);
            if (!bt_model.removeConnection(connectionID)) {
                Log.d(TAG, "connection not found..");
            }
            //wenn dies der Fall ist, befindet sich die StateMachine in MAX_NUMBER_OF_DEVICES
            // und es gibt keinen nuene Connection Cycle. Deswegen wird hier ein neuer angestossen.
            if(state == State.THREAD_CONNECTED && maxNumberOfDevicesReached){
                Log.d(TAG, "");
                sendSmMessage(SmMessage.START_NEW_CONNECTION_CYCLE.ordinal(), 0, 0, null);
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

                    case INIT_FINISHED:
                        state = State.WAIT_FOR_CONNECT;
                        sendSmMessage(SmMessage.FIND_DEVICE.ordinal(), 0, 0, null);

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

                    Die Discovery Zeit hängt von dem jeweiligen Gerät ab. Zb. hat eins meiner Testgeräte nur ~5sec.
                     */
                    case FIND_DEVICE:
                        Log.d(TAG, "suche devices");
                        //siehe BroadcastReceiver und Filter in der MainActivity
                        if (mBluetoothAdapter.isDiscovering()) {
                            Log.d(TAG, "already discovering");
                        }
                        if (!mBluetoothAdapter.startDiscovery()) {
                            Log.d(TAG, "discovery not starting...");
                            //starte einen server

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
                        if (bt_model.getNumberOfConnections() < MAX_NUMBER_OF_DEVICES) {
                            maxNumberOfDevicesReached = false;
                            state = State.WAIT_FOR_CONNECT;
                            sendSmMessage(SmMessage.FIND_DEVICE.ordinal(), 0, 0, null);
                        } else {
                            sendSmMessage(SmMessage.MAX_THREAD_NUMBER.ordinal(), 0, 0, null);
                        }
                        break;


                    /*
                    Wenn dieser State erreicht wurde, wird kein Verbindungsaufbau versucht, da dieser,
                    abgesehen vom ersten Connection Cycle, immer über START_NEW_CONNECTION_CYCLE aus-
                    gelöst wird
                    warte auf das beenden eines ConnectedThreads
                    */
                    case MAX_THREAD_NUMBER:
                        maxNumberOfDevicesReached = true;
                        state = State.THREAD_CONNECTED;
                        break;

                    default:
                        Log.v(TAG, "SM: not a valid input in this state !!!!!!");
                        break;
                }
                Log.v(TAG, "STATE: " + state + " INPUT: " + inputSmMessage);
        }
        Log.i(TAG, "SM: new State: " + state);
    }

    /**
     * Liest eine eingegangen Nachricht von einem anderen Gerät, liest diese und gibt sie an senden weiter,
     * wenn sie nicht an dieses Gerät gerichtet ist.
     *
     * @param message
     */
    private void readReceivedMessageAndRoute(android.os.Message message) {
        Log.d(TAG, "readReceivedMessageAndRoute");
        Message receivedMsg = null;
        byte[] bytes = (byte[]) message.obj;

        //Quelle: https://stackoverflow.com/questions/5837698/converting-any-object-to-a-byte-array-in-java
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream o = new ObjectInputStream(b);
            receivedMsg = (Message) o.readObject();
            Log.d(ConnectedThread.TAG, "MessageReceived: " + receivedMsg.getMessageId());
            //Routen der Nachricht
            if (messageStorage.checkMessage(receivedMsg)) {
                Log.d(TAG, "nachricht schon erhalten");
                //nachricht wurde erhalten -> ignorieren
            } else {
                //nachricht ist an mich gerichtet
                Log.d(TAG, "Message routing \n Target: " + receivedMsg.getMessageTargetMac() +
                        "\nMy_Addr: " + mBluetoothAdapter.getAddress());
                if (receivedMsg.getMessageTargetMac().equals(mBluetoothAdapter.getAddress())) {
                    Log.d(TAG, "message an mich gerichtet");
                    bt_model.setCurrentMessage(receivedMsg);
                }
                //nachricht nicht an mich gerichtet -> an senden weiter geben
                else {
                    Log.d(TAG, "message nicht an mich gerichtet");
                    sendMessageToDevice(receivedMsg);
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "SmMessage.CT_RECEIVED IOError: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "SmMessage.CT_RECEIVED ClassNotFound: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "SmMessage.CT_RECEIVED sendMessageToDevice: " + e.getMessage());
        }
    }


    /**
     * Sendet eine Nachricht an eine Adresse.
     * <p/>
     * Zuerst wird überprüft, ob die TargetAddress eine valide Mac-Adresse ist.
     * Wenn die Adresse direkt verbunden ist, wird sie dort hingeschickt. Wenn sie nicht bekannt ist,
     * wird sie an alle geschickt.
     *
     * @param msg Message
     * @throws Exception
     */
    private void sendMessageToDevice(Message msg) throws Exception {
        Log.d(TAG, "sendMessageToDevice");
        String btAddress = msg.getMessageTargetMac();

        //validiere MAC TODO; Exceptions -> Fehlerausgabe als Toast(?)
        if (!BluetoothAdapter.checkBluetoothAddress(btAddress)) {
            Log.d(TAG, "mac addresse nicht valid: " + btAddress);
            Log.d(TAG, "übergebene Mac: " + msg.getMessageTargetMac());
            throw new Exception("Bluetooth address not valid");
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

//        Kenne ich das Gerät? Ja -> sende an Gerät
        if (directlyConnected) {
            Log.d(TAG, "sendMessageToD: directly connected");
            try {
                directConnection.write(msg);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "IOException directlyConnected " + e.getMessage());
                throw new Exception("Sending via directly connected device failed");
            }
//      Kenne ich das Gerät? Nein -> sende an alle
        } else {
            Log.d(TAG, "sendMessageToD: not directly connected");
            for (Connection connection : bt_model.getConnections()) {
                try {
                    connection.write(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "IOException Indirectly Connected " + e.getMessage());
                    throw new Exception("Sending via non-directly connected devices failed");
                }
            }
        }
    }


    public void bluetoothAdapterEnabled() {
        Log.d(TAG, "bluetoothAdapterEnabled");
        sendSmMessage(SmMessage.ENABLE_DISCOVERABILITY.ordinal(), 0, 0, null);
    }

    public void discoverabilityEnabled() {
        Log.d(TAG, "discoverabilityEnabled()");
        sendSmMessage(SmMessage.INIT_FINISHED.ordinal(), 0, 0, null);
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

