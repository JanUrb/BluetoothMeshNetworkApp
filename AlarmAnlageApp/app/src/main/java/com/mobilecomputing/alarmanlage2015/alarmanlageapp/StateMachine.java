package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import android.os.Handler;

import fllog.Log;

/**
 * Statemachine Klasse
 * <p/>
 * wird im Projekt SmParkingMeter2 gepflegt.
 * <p/>
 * History:
 * 07.12.15 tas Erstellung
 * <p/>
 * <p/>
 * Eingebunden für das Projekt Alarmanlage!
 */

public class StateMachine extends Handler {
    private static final String TAG = "fhflStateMachine";

    @Override
    public void handleMessage(android.os.Message message) {
        theBrain(message);
    }           //##0b

    /**
     * virtual method, must be overwritten in subclass
     *
     * @param message
     */
    void theBrain(android.os.Message message) {
    }

    protected void setTimer(int messageType, long durationMs) {
        Log.d(TAG, "setTimer");
        android.os.Message msg = new android.os.Message();
        msg.what = messageType;
        msg.arg1 = 0;
        msg.arg2 = 0;
        sendMessageDelayed(msg, durationMs);                                                //##4a
    }

    protected void stopTimer(int messageType) {
        Log.d(TAG, "stopTimer");
        removeMessages(messageType);                                                        //##4b
    }

    public void sendSmMessage(int messageType, int arg1, int arg2, Object obj) {
        Log.d(TAG, "sendSmMessage");
        // PrintData d = new PrintData(3);                                                   //##3a
        android.os.Message msg = new android.os.Message();                                   //##0a
        msg.what = messageType;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        if (obj != null) {
            msg.obj = obj;
        }
        this.sendMessage(msg);
    }

    /**
     * Alternative sendSmMessage Methode
     * <p/>
     * Der ServerTimerThread hat das Programm mit der Fehlermeldung: Only the original thread that created a view hierarchy can touch its views
     * abgestürzt. Der komplette Stacktrace ist:
     * <p/>
     * Process: com.mobilecomputing.alarmanlage2015.alarmanlageapp, PID: 1210
     * android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
     * at android.view.ViewRootImpl.checkThread(ViewRootImpl.java:6373)
     * at android.view.ViewRootImpl.invalidateChildInParent(ViewRootImpl.java:913)
     * at android.view.ViewGroup.invalidateChild(ViewGroup.java:4691)
     * at android.view.View.invalidateInternal(View.java:11877)
     * at android.view.View.invalidate(View.java:11841)
     * at android.view.View.invalidate(View.java:11825)
     * at android.widget.TextView.updateAfterEdit(TextView.java:7745)
     * at android.widget.TextView.handleTextChanged(TextView.java:7768)
     * at android.widget.TextView$ChangeWatcher.onTextChanged(TextView.java:9514)
     * at android.text.SpannableStringBuilder.sendTextChanged(SpannableStringBuilder.java:964)
     * at android.text.SpannableStringBuilder.replace(SpannableStringBuilder.java:515)
     * at android.text.SpannableStringBuilder.append(SpannableStringBuilder.java:272)
     * at android.text.SpannableStringBuilder.append(SpannableStringBuilder.java:33)
     * at android.widget.TextView.append(TextView.java:3661)
     * at android.widget.TextView.append(TextView.java:3648)
     * at fllog.LogFragment.setTextView(LogFragment.java:171)
     * at fllog.Log.log(Log.java:101)
     * at fllog.Log.d(Log.java:45)
     * at com.mobilecomputing.alarmanlage2015.alarmanlageapp.StateMachine.sendSmMessageWithoutLog(StateMachine.java:63)
     * at BluetoothCommunication.ServerTimerThread.run(ServerTimerThread.java:51)
     *
     * Daher habe ich die Log Methode entfernt, um den ServerTimerThread die Statemachine beinflussen zu lassen.
     *
     *
     *
     * @param messageType
     * @param arg1
     * @param arg2
     * @param obj
     */
    public void sendSmMessageWithoutLog(int messageType, int arg1, int arg2, Object obj) {
        // PrintData d = new PrintData(3);                                                   //##3a
        android.os.Message msg = new android.os.Message();                                   //##0a
        msg.what = messageType;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        if (obj != null) {
            msg.obj = obj;
        }
        this.sendMessage(msg);
    }

}