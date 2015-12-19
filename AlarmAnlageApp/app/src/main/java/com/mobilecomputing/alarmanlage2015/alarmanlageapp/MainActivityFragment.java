package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fllog.Log;

/**
 * Created by Jan Urbansky on 19.12.2015.
 */
public class MainActivityFragment extends Fragment {
    private static final String TAG ="fhflMainActFragment";


    public MainActivityFragment() {
        Log.d(TAG, "MainActivityFragment()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");

        View view = inflater.inflate(R.layout.activity_main_fragment, container, false);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
