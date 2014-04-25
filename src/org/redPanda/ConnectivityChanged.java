/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import org.redPanda.ChannelList.Preferences;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import java.util.ArrayList;
import java.util.Calendar;
import org.redPandaLib.core.Peer;
import org.redPandaLib.core.Settings;
import org.redPandaLib.core.Test;

/**
 *
 * @author rflohr
 */
public class ConnectivityChanged extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Test.localSettings == null) {
            return;
        }

        new ExceptionLogger(context);

        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isConnected()) {
            Settings.connectToNewClientsTill = Long.MAX_VALUE;
            Settings.MIN_CONNECTIONS = 3;
        } else if (mobile.isConnected()) {


            Settings.MIN_CONNECTIONS = 2;


            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            boolean saveInternet = sharedPref.getBoolean(Preferences.KEY_SAVE_MOBILE_INTERNET, false);



            if (saveInternet) {

                Calendar c = Calendar.getInstance();
                int minute = c.get(Calendar.MINUTE);

                if (minute < 3) {
                    Settings.connectToNewClientsTill = Long.MAX_VALUE;
                } else {
                    Settings.connectToNewClientsTill = Long.MIN_VALUE;

                    ArrayList<Peer> clonedPeerList = (ArrayList<Peer>) Test.peerList.clone();
                    for (Peer peer : clonedPeerList) {
                        peer.disconnect();
                    }
                }

            } else {
                Settings.connectToNewClientsTill = Long.MAX_VALUE;
            }
        } else {
            Settings.connectToNewClientsTill = Long.MIN_VALUE;
        }

    }
}
