/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.http.util.ExceptionUtils;
import org.redPandaLib.Main;
import org.redPandaLib.core.MessageHolder;

/**
 * Diese Klasse fängt alle Exceptions ab, welche die App zum abstürzen bringen.
 * Der StackTrace wird dann in der Datenbank gespeichert und dann der normale
 * ExceptionHandler von Google aufgerufen, damit der Benutzer mitbekommt, dass
 * die App abgestürzt ist.
 *
 * Der StackTrace wird dann von dem Syncer an den Server übermittelt.
 *
 * @author rflohr
 */
public class ExceptionLogger {

    private boolean DEBUG = false;
    Context context;
    Thread.UncaughtExceptionHandler defaultUEH;

    public ExceptionLogger(Context context) {
        this.context = context;
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        getAllUncaughtExceptions();
    }

    private void getAllUncaughtExceptions() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            public void uncaughtException(Thread thread, Throwable thrwbl) {

                String ownStackTrace = stacktrace2String(thrwbl);


//                ownStackTrace = ownStackTrace.replaceAll(":", "");

                Main.sendBroadCastMsg("Version: " + BS.VERSION + " \n" + ownStackTrace);

                defaultUEH.uncaughtException(thread, thrwbl);

            }
        });
    }

    public static String stacktrace2String(Throwable thrwbl) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        thrwbl.printStackTrace(pw);
        return sw.toString();

//        String ownStackTrace = "";
//        ownStackTrace += thrwbl.getMessage() + "\n";
//        for (StackTraceElement a : thrwbl.getStackTrace()) {
//            ownStackTrace += a.toString() + "\n";
//        }
//
//        if (thrwbl.getCause() != null) {
//            ownStackTrace += "caused by: " + thrwbl.getCause().getMessage() + "\n";
//            for (StackTraceElement a : thrwbl.getCause().getStackTrace()) {
//                ownStackTrace += a.toString() + "\n";
//            }
//        }
//        
//        return ownStackTrace;
    }
}
