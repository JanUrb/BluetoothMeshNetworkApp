package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Jan Urbansky on 22.02.2016.
 */
public class AppModel extends Observable {
    @Override
    public void addObserver(Observer observer) {
        super.addObserver(observer);
    }

    @Override
    public boolean hasChanged() {
        return super.hasChanged();
    }

    @Override
    public void notifyObservers() {
        super.notifyObservers();
    }
}
