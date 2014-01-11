/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Wird aufgerufen, wenn Android bootet.
 *
 * @author rflohr
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent arg1) {
        new ExceptionLogger(context);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean autoStart = sharedPref.getBoolean(Preferences.KEY_START_AFTER_BOOTING, true);

        if (!autoStart) {
            return;
        }

        context.startService(new Intent(context, BS.class));

    }
}
