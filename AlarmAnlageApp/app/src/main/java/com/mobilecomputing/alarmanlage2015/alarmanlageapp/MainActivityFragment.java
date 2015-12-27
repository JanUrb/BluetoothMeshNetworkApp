package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Observable;
import java.util.Observer;

import fllog.Log;

/**
 * Created by Jan Urbansky on 19.12.2015.
 */
public class MainActivityFragment extends Fragment implements Controller.OnControllerInteractionListener, Observer{
    private static final String TAG = "fhflMainActFragment";

    private BluetoothModel bt_model;
    private Controller controller;



    /**
     * Sendet eine Nachricht zur im address_input angegebenen BT_ADDR
     */
    private Button sendButton;
    /**
     * Enthält eine BT_ADDR
     */
    private EditText address_input;
    /**
     * Listet die direkt verbundenen Geräte auf (BT_ADDR)
     */
    private TextView device_list;

    /**
     * Stellt die eigenen BT_ADDR dar.
     */
    private TextView my_bt_addr;


    /**
     * Stellt eine empfangene Nachricht dar.
     */
    private TextView received_message;
    /**
     * Konstruktor
     */
    public MainActivityFragment() {
        Log.d(TAG, "MainActivityFragment()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");

        View view = inflater.inflate(R.layout.activity_main_fragment, container, false);
        sendButton = (Button) view.findViewById(R.id.send_message_button);
        address_input = (EditText) view.findViewById(R.id.send_to_input);
        device_list = (TextView) view.findViewById(R.id.connected_devices_list);
        my_bt_addr = (TextView) view.findViewById(R.id.my_bt_addr);
        received_message = (TextView) view.findViewById(R.id.message_received_field);


    //an eine bt_addr senden
//        address_input.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                controller.sendSmMessage(Controller.SmMessage.UI_SEND, );
//            }
//        });


        return view;
    }


    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void setBt_model(BluetoothModel bt_model){
        this.bt_model = bt_model;
        this.bt_model.addObserver(this);
    }

    @Override
    public void onControllerReceived(String str) {
        Log.d(TAG, "onControllerReceived");
    }

    @Override
    public void onControllerConnectInfo(String strState) {
        Log.d(TAG, "onControllerConnectInfo");
    }

    @Override
    public void onControllerServerInfo(Boolean serverInfo) {
        Log.d(TAG, "onControllerServerInfo");
    }

    @Override
    public void update(Observable observable, Object data) {
        Log.d(TAG, "update");
        //ignoriere null oder  leere Werte
        if(bt_model.getPairedDevices() != null){
            StringBuilder str = new StringBuilder();
            for(BluetoothDevice device:bt_model.getPairedDevices()){
                str.append(device.getAddress()+"\n");
            }
            device_list.setText(str.toString());
        }
        if(!bt_model.getMyBT_ADDR().isEmpty()){
            my_bt_addr.setText(bt_model.getMyBT_ADDR());
        }
        if(!bt_model.getMessageReceivedFrom().isEmpty()){
            received_message.setText(bt_model.getMessageReceivedFrom());
        }
    }
}
