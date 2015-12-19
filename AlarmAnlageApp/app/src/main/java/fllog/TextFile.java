/**
 * Speichert und laedt Textfiles auf/vom externen Speicher (bei aelteren Systemen der SD-Card).
 *
 * Diese Klasse wird im Projekt notes2 gepflegt.
 * verwendet in: NotesD, im Debug-Fragment: HFragStaticDebug, ...
 *
 *
 * Einstellungen:
 *     benötigt Lese/Schreibrechte !!!:
 *     <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 *
 * History:
 *   09.02.2015  tas  ver 1.0
 *   13.02.2015  tas  getFile() als statische Methode eingefuehrt
 *   14.02.2015  tas  File() NulPointerException abgefangen
 *   23.02.2015  tas  return Type ueberarbeitet
 *   21.09.2015  tas  appendText()
 *   25.09.2015  tas  DebugSwitch und logD()
 *   27.11.2015  tas  Debugging erweitert
 *
 */

package fllog;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class TextFile {

    private static final String TAG = "fhflTextFile";
    private static boolean deb = true;         // Debug enaable/disable
    private File file = null;

    /**
     * konstruiert Textfile Objekt, falls das File nicht vorhanden ist, wird es angelegt.
     *
     * @param directory Directory im externen Speicherbereich, z.B.:
     *                  Environment.DIRECTORY_DOWNLOADS oder "Ftp"
     * @param fileName Filename inkl. Extension (immer .txt)
     * @param debugMode schaltet Logausgaben ein/aus
     */
    public TextFile(String directory, String fileName, boolean debugMode) {
        logD("TextFile()");
        deb = debugMode;
        file = getFile(directory, fileName);

        if (! file.exists() ) {
            try {
                file.createNewFile();
                logD("TextFile(): new File created");

            } catch (IOException e) {
                file = null;
                logD("TextFile(): Exception: during create file" + e);
            }
        }
    }

    /**
     * testet, ob der externe Speicher 'gemountet' und 'read/write' ist und legt ein
     * File-Objekt fuers directory an. Falls notwendig, wird das Directory ezeugt.
     *
     * Achtung: bei älteren Systemen (ca. Android 2.3) wird die SD-Card angesprochen, später
     * ist der physikalische Flash-Speicher in einen internen und einen externen Bereich
     * getrennt. Der externe Bereich kann u.U. (.B. wg. USB-Remount) nicht zugreifbar sein. ->
     * Der Aufwand mt .getExternalStorageState() usw. ist weiterhin notwendig.
     *
     *  @param directory Directory im externen Speicherbereich, z.B.:
     *                  Environment.DIRECTORY_DOWNLOADS oder "Ftp"
     *  @return ile Objekt, dass auf das gewuenschte Directory verweist, oder null bei Problemen
     */
    public static File getPath(String directory){

        logD("getPath( " + directory + " )");

        String storageState = Environment.getExternalStorageState();
        logD("getPath(): storageState: " + storageState);

        // Test ob gemountet und gleichzeitig, ob Read/Write faehig
        if ( ! Environment.MEDIA_MOUNTED.equals(storageState)) {
            logD("getPath(): Error: SD-Card not mounted or not Read/Write");
            return null;
        }

        File path = Environment.getExternalStoragePublicDirectory(directory);
        // das zurueckgegebene directory/path muss nicht unbedingt existieren

        path.mkdirs();      // throws no exception !!!

        return path;
    }

    /**
     * legt ein File-Objekt an, das auf das (Text-)File verweist. Das File selber wird nicht angelegt.
     *
     *  @param directory Directory im externen Speicherbereich, z.B.:
     *                  Environment.DIRECTORY_DOWNLOADS oder "Ftp"
     *  @param fileName File Name inkl. Extension (immer .txt) ohne Pfad
     *  @return File Objekt oder null bei Fehler
     */
    public static File getFile(String directory, String fileName){

        logD("getFile( " + fileName + " )");

        File path = getPath(directory);

        File file = null;

        try {
            file = new File(path, fileName);
        } catch (NullPointerException e) {
            logD("getFile(): NullPointerException: " + e.toString());
        }
        logD("getFile(): return: file: " + file.toString());

        return file;
    }

    /**
     * testet, ob das dem Textfile Objekt zugeordnete File existiert
     *
     * @return liefert false falls das File nicht existiert, sonst true
     */
    public boolean exist() {
        logD("existFile(): file: " + file.toString());
        logD("existFile(): return: " + file.exists());

        return file.exists();
    }

    /**
     * liest das File aus
     *
     * @return liefert Inhalt des Files, bei Problemen null
     */
    public String readText(){
        // liest den Inhalt des Textfiles aus.
        //
        logD("readText()");

        if (file == null) {
            logD("readText(): Error: file-object not initialized");
            return null;
        }

        StringBuilder sb = new StringBuilder();
        String tempStr;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            tempStr = br.readLine();
            while (tempStr != null ) {
                sb.append(tempStr);
                sb.append("\n");
                tempStr = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            logD("readText(): Error: problems during external file-io read: file: " + file + "  " + e.toString());
            return (null);
        }
        tempStr = sb.toString();
        logD("readText(): String read: " + tempStr);

        return tempStr;
    }

    /**
     * schreibt in das File
     *
     * @return  bei Problemen false, sonst true
     */
    public boolean saveText(String str) {
        logD("saveText()");

        if (file == null) {
            logD("saveText(): Error: file-object not initialized");
            return false;
        }

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(str);
            bw.close();
            logD("saveText(): String write: " + str);
            return true;

        } catch (IOException e) {
            logD("saveText(): Error: problems during external file-io write: file: " + "  " + e.toString());
            return false;
        }
    }

    /**
     * append einzelner Textsegmente (z.B. Zeilen) fuer Debugzwecke: File wird jeweils
     *    geoeffnet und geschlossen ->
     *    hohe Wahrscheinlichkeit, dass auch die letzte Debugausgabe vor einem Absturz
     *    abgespeichert ist.
     *
     *    Achtung: extrem langsame Funktion !!!!
     *
     * @return  bei Problemen false, sonst true
     */
    public boolean appendText(String str) {
        logD("appendText()");

        if (file == null) {
            logD("appendText(): Error: file-object not initialized");
            return false;
        }

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));  //true = append !!!
            bw.write(str);
            bw.close();
            logD("appendText(): String write: " + str);
            return true;

        } catch (IOException e) {
            logD("appendText(): Error: problems during external file-io append: file: " + "  " + e.toString());
            return false;
        }
    }

    private static void logD(String strLog) {
        if (deb)
            Log.d(TAG, strLog);
    }
}
