package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import android.util.Log;

/**
 * Created by Jan Urbansky on 02.01.2016.
 * <p/>
 * Dieser Thread bricht einen Serverthread über die ServerThread.cancel()
 * Methode ab. Die Zeit des Abbruch ist 12000 ms + eine zufällige Zeit zwischen
 * 0 u. 5000 ms.
 * Dadurch wird verhindert, dass ein Deadlock ensteht, bei dem nur Server oder nur Client
 * Modus eingeschaltet ist.
 *
 * Wenn ein Serverthread mit cancel beendet wird, fordert der ServerTimer einen neuen ClientThread an.
 */
public class ServerTimerThread extends Thread {
    private static final String TAG = "fhflServerTimer";
    private long runtime = 0;
    private ServerThread serverThread = null;
    private Controller mController = null;

    /**
     * Basiszeit für die Laufzeit des Threads.
     * <p/>
     * Die maximale Laufzeit ist zusammen mit MAX_ADD_TIME 17000ms.
     * Die MAX_ADD_TIME wird zufällig gewählt, damit kein Deadlock entstehen kann bei dem beide
     * Threads entweder im Clientmodus sind oder im Servermodus sind.
     */
    private long RUN_TIME = 12000;
    private long MAX_ADD_TIME = 5000;


    public ServerTimerThread(ServerThread serverThread, Controller controller) {
        Log.d(TAG, "ServerTimerThread()");
        this.serverThread = serverThread;
        mController = controller;
        runtime = calculateRuntime();
    }


    @Override
    public void run() {
        Log.d(TAG, "run()");
        try {
            sleep(runtime);
            Log.d(TAG, "Attempting to cancel..");
            if (serverThread != null && serverThread.isAlive()) {
                Log.d(TAG, "serverThread wird gecancelt.");
                serverThread.cancel();
                Log.d(TAG, "sende FIND_DEVICE an controller");
                mController.sendSmMessage(Controller.SmMessage.FIND_DEVICE.ordinal(),0,0,null);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d(TAG, "InterruptedException: " + e.getMessage());
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.d(TAG, "Exception: " + e.getMessage());
        }
        Log.d(TAG, "ServerTimerThread finished");
    }

    private long calculateRuntime() {
        long add_time = (long) (Math.random() * MAX_ADD_TIME);
        Log.d(TAG, "calculateRuntime(): " + (RUN_TIME + add_time) + " add_time: " + add_time);
        return RUN_TIME + add_time;
    }

}
