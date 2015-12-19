package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import fllog.Log;

/**
 * Created by Jan Urbansky on 19.12.2015.
 */
public class MainActivityFragment extends Fragment {
    private static final String TAG ="fhflMainActFragment";

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
     *  Konstruktor
     */
    public MainActivityFragment() {
        Log.d(TAG, "MainActivityFragment()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");

        View view = inflater.inflate(R.layout.activity_main_fragment, container, false);
        sendButton = (Button) view.findViewById(R.id.send_message_button);
        address_input = (EditText) view.findViewById(R.id.send_to_input);
        device_list = (TextView) view.findViewById(R.id.connected_devices_list);
        my_bt_addr = (TextView) view.findViewById(R.id.my_bt_addr);


        /*Benötigte Elemente: Liste der verbundenen Geräte, Eingabefeld zum Senden an eine Adresse, ein senden Knopf*/

        return view;
    }
}
