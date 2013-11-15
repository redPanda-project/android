/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import org.redPandaLib.core.Settings;

/**
 *
 * @author rflohr
 */
public class ConnectivityChanged extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isAvailable()) {
            Settings.connectToNewClientsTill = Long.MAX_VALUE;
            Settings.MIN_CONNECTIONS = 6;
        } else if (mobile.isAvailable()) {
            Settings.connectToNewClientsTill = Long.MAX_VALUE;
            Settings.MIN_CONNECTIONS = 2;
        } else {
            Settings.connectToNewClientsTill = Long.MIN_VALUE;
        }

    }
}
