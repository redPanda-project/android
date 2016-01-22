/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.redPandaLib.Main;

/**
 * Class for catching uncaught exceptions.
 *
 * @author rflohr
 */
public class ExceptionLogger {

    private boolean DEBUG = false;
    Context context;
    Thread.UncaughtExceptionHandler defaultUEH;

    /**
     * Just create a new object, then all uncaught exceptions will be send to
     * redPanda network for this thread.
     *
     * @param context - Android context
     */
    public ExceptionLogger(Context context) {
        this.context = context;
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        getAllUncaughtExceptions();
    }

    private void getAllUncaughtExceptions() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            public void uncaughtException(Thread thread, Throwable thrwbl) {

                String ownStackTrace = stacktrace2String(thrwbl);

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                sharedPref.edit().putString("stacktrace", ownStackTrace).commit();

//                ownStackTrace = ownStackTrace.replaceAll(":", "");
                Main.sendBroadCastMsg("Version: " + BS.VERSION + " \n" + ownStackTrace);
                sharedPref.edit().putString("stacktrace", "").commit();

                try {
                    defaultUEH.uncaughtException(thread, thrwbl);
                } catch (Throwable e) {
                    System.out.println("exception here? android is annoying, have to abort. Otherwise stackoverflow exception...");
                }

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
