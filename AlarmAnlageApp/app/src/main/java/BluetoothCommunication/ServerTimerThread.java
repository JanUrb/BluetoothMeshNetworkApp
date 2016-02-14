package BluetoothCommunication;

import android.util.Log;

import com.mobilecomputing.alarmanlage2015.alarmanlageapp.Controller;

/**
 * Created by Jan Urbansky on 02.01.2016.
 * <p/>
 * Dieser Thread bricht einen Serverthread über die ServerThread.cancel()
 * Methode ab. Die Zeit des Abbruch ist 12000 ms + eine zufällige Zeit zwischen
 * 0 u. 5000 ms.
 * Dadurch wird verhindert, dass ein Deadlock ensteht, bei dem nur Server oder nur Client
 * Modus eingeschaltet ist.
 * <p/>
 * Wenn ein Serverthread mit cancel beendet wird, fordert der ServerTimer einen neuen ClientThread an.
 */
public final class ServerTimerThread extends Thread {
    public static final String TAG = "fhflServerTimer";
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


    protected ServerTimerThread(ServerThread serverThread, Controller controller) {

        this.serverThread = serverThread;
        mController = controller;
        debugOut("ServerTimerThread()");
        runtime = calculateRuntime();
    }


    @Override
    public void run() {
        debugOut("run()");
        try {
            sleep(runtime);
            Log.d(TAG, "Attempting to cancel..");
            if (serverThread != null && serverThread.isAlive()) {
                debugOut("serverThread wird gecancelt.");
                serverThread.cancel();
                debugOut("sende FIND_DEVICE an controller");
                //siehe Doku sendSmMessageWithoutLog
                mController.sendSmMessageWithoutLog(Controller.SmMessage.FIND_DEVICE.ordinal(), 0, 0, null);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            debugOut("InterruptedException: " + e.getMessage());
        }
        debugOut("ServerTimerThread finished");
    }

    private long calculateRuntime() {
        long add_time = (long) (Math.random() * MAX_ADD_TIME);
        debugOut("calculateRuntime(): " + (RUN_TIME + add_time) + " add_time: " + add_time);
        return RUN_TIME + add_time;
    }


    private void debugOut(String str) {
        mController.obtainMessage(Controller.SmMessage.AT_DEBUG_TIMER.ordinal(),
                -1, -1, str).sendToTarget();
    }

}
