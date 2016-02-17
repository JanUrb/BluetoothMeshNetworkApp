package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

import BluetoothCommunication.BluetoothConnection;
import BluetoothCommunication.BluetoothModel;
import BluetoothCommunication.Controller;
import BluetoothCommunication.Message;
import fllog.Log;

/**
 * Created by Jan Urbansky on 19.12.2015.
 */
public class MainActivityFragment extends Fragment implements Observer {
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


//    an eine bt_addr senden
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClickListener");
                String address = address_input.getText().toString();
                controller.sendSmMessage(Controller.SmMessage.SEND_MESSAGE.ordinal(), 0, 0, address);
            }
        });
// für debugging und vorführung. Nach langem klicken wird das Feld leer gemacht.
        received_message.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d(TAG, "address_input: onLongClick");
                received_message.setText("No message received");
                return true;
            }
        });


        return view;
    }


    public void setController(Controller controller) {
        Log.d(TAG, "setController");
        this.controller = controller;
    }

    public void setBt_model(BluetoothModel bt_model) {
        Log.d(TAG, "setBt_model");
        this.bt_model = bt_model;
        this.bt_model.addObserver(this);
    }


    @Override
    public void update(Observable observable, Object data) {
        Log.d(TAG, "update");

        //my_bt_addr
        if (!bt_model.getMyBT_ADDR().isEmpty()) {
            my_bt_addr.setText(bt_model.getMyBT_ADDR());
        }

        //received_message
        if (bt_model.getCurrentMessage() != null) {
            Message currentMsg = bt_model.getCurrentMessage();
            String txt = "SRC: " + currentMsg.getMessageSourceMac();
            received_message.setText(txt);
        }

        //device_list
        if (bt_model.getBluetoothConnections() != null) {
            StringBuilder str = new StringBuilder();
            if (!bt_model.getBluetoothConnections().isEmpty()) {
                for (BluetoothConnection connection : bt_model.getBluetoothConnections()) {
                    str.append(connection.getDeviceAddress() + "\n");
                }
            } else {
                str.append("No connected devices");
            }
            device_list.setText(str.toString());
        }
    }
}
