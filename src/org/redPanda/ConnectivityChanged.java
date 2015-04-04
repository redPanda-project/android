/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import org.redPanda.ChannelList.Preferences;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import java.util.ArrayList;
import java.util.Calendar;
import org.redPandaLib.Main;
import org.redPandaLib.core.Peer;
import org.redPandaLib.core.Settings;
import org.redPandaLib.core.Test;

/**
 *
 * @author rflohr
 */
public class ConnectivityChanged extends BroadcastReceiver {

    //private int lastConnectionType = -1; //-1: unknown, 1: mobile, 2: wlan
    private boolean lastNoInternet = false;

    @Override
    public void onReceive(Context context, Intent intent) {

//        //Check if backend started successful
//        if (Test.localSettings == null) {
//            return;
//        }
        new ExceptionLogger(context);

        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi != null && wifi.isConnected()) {

            Settings.REDUCE_TRAFFIC = false;

            Settings.connectToNewClientsTill = Long.MAX_VALUE;

            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.getApplicationContext().registerReceiver(null, ifilter);

            if (batteryStatus != null) {

                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                float batteryPct = level / (float) scale;

                if (batteryPct > 0.95) {
                    Settings.MIN_CONNECTIONS = 20;
                } else {
                    Settings.MIN_CONNECTIONS = 3;
                }
            } else {
                Main.sendBroadCastMsg("batteryStatus was null...");//ToDo: remove
            }

//            if (lastNoInternet) {
//                Main.internetConnectionInterrupted();
//            }
//            lastNoInternet = false;
//            lastConnectionType = 2;

        } else if (mobile != null && mobile.isConnected()) {

            Settings.REDUCE_TRAFFIC = true;

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
                        peer.disconnect("reducing traffic...");
                    }
                }

            } else {
                Settings.connectToNewClientsTill = Long.MAX_VALUE;
            }

//            if (lastNoInternet) {
//                Main.internetConnectionInterrupted();
//            }
//            lastNoInternet = false;

            //lastConnectionType = 1;
        } else {
            Settings.connectToNewClientsTill = Long.MIN_VALUE;
            //lastConnectionType = -1;
//            lastNoInternet = true;
            Main.internetConnectionInterrupted();
        }

    }
}
