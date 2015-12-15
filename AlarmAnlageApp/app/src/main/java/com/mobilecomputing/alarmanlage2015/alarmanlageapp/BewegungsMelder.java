package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import java.util.Observable;

/**
 * Created by Jan Urbansky on 03.12.2015.
 *
 * Evtl würde ich den eigentlichen BewegungsMelder Quellcode in ein eigenes Package packen und das hier nur als API-Endpunkt nutzen.
 * Blablabla
 */
public class BewegungsMelder extends Observable {

    @Override
    public void notifyObservers(Object data) {
        super.notifyObservers(data);
    }
}
