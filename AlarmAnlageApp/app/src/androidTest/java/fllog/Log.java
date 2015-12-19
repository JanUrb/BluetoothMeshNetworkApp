package fllog;

import android.os.Environment;
import android.text.format.DateFormat;

import java.util.Date;

/**
 * Universelle Logging-Klasse.
 *     Ueberschreibt die LogAusgaben von android.util.Log
 *
 *  Einsatz: s. Foliensatz Fragments/HFragDebugLog
 *
 *
 *  30.11.2015 tas  Erstausgabe
 *
 */

public class Log {          //!!a LOG1 !

    private static final String TAG = "fhflLog";

    private static LogFragment mLogFrag;
    private static StringBuilder mStringBuilder = new StringBuilder();

    private static long startTimeMs = System.currentTimeMillis();
    // private static int seqNumber = 0;     // nur fuer Testzwecke

    private static TextFile textFile = null;

    public static void init(Boolean showTimeStamp, Boolean showTag) {   //!!a LOG2 !
        mLogFrag = LogFragment.newInstance(showTimeStamp, showTag);
    }

    public static LogFragment getFragment( ) {
        return mLogFrag;
    }

    public static void v(String tag, String str) {
        log(tag, str);
        android.util.Log.v(tag, " DL " + str);
    }

    public static void d(String tag, String str) {      //!!a LOG3 !
        log(tag, str);
        android.util.Log.d(tag, " DL " + str);
    }

    public static void i(String tag, String str) {
        log(tag, str);
        android.util.Log.i(tag, " DL " + str);
    }

    public static void w(String tag, String str) {
        log(tag, str);
        android.util.Log.w(tag, " DL " + str);
    }

    public static void e(String tag, String str)  {
        log(tag, str);
        android.util.Log.e(tag, " DL " + str);
    }

    private static void log(String tag, String outStr){
        // seqNumber++;        // nur für Testzwecke

        // aktuelle Zeit
        long timeS = (System.currentTimeMillis() - startTimeMs) / 1000;
        String strTimeS = Long.toString(timeS);

        // Ausgabestrings formatieren
        // String strDebTag = String.format("%d %s %s %s\n", seqNumber, strTimeS, tag, outStr);
        // String strDeb = String.format("%d %s %s\n", seqNumber, strTimeS, outStr);
        String strDebTag = String.format("%s %s %s\n", strTimeS, tag, outStr);
        String strDeb = String.format("%s %s\n", strTimeS, outStr);
        String strView;

        // Tag anzeigen ?
        if ( mLogFrag.isShowingTag ) {
            strView = strDebTag;
        } else {
            strView = strDeb;
        }

        // abspeichern
        if ( mLogFrag.isSaving2File ) {
            if ( textFile == null ) {
                newLogFile();
            }
            textFile.appendText(strView);
        }
        else {
            textFile = null;
        }

        // buffern
        mStringBuilder.append(strView);

        // ausgeben falls Fragment sichtbar
        if ( mLogFrag.isVisible() ) {
            mLogFrag.setTextView(mStringBuilder.toString());
            mStringBuilder.delete(0, mStringBuilder.length());
        }
    }

    /**
     * legt ein neues Log-File an
     */
    private static void newLogFile() {
        android.util.Log.d(TAG, "newLogFile()");
        DateFormat df = new DateFormat();
        String strDate = (String) df.format("_yyyy_MM_dd-hh_mm_ss.txt", new Date());

        String strFileName =  "Log_" ; // + strDate;
        android.util.Log.d(TAG, "newLogFile(): " + strFileName);
        textFile = new TextFile(Environment.DIRECTORY_DOWNLOADS, strFileName, false);

        if ( mLogFrag.isVisible() ) {
            mLogFrag.setTextView("LOGGER: new logging file: " + strFileName + "\n");
        }
    }

}
