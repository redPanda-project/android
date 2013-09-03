/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.redPandaLib.Main;
import org.redPandaLib.core.Settings;
import org.redPandaLib.core.Test;

/**
 *
 * @author robin
 */
public class BackgroundService extends Service {

    @Override
    public IBinder onBind(Intent arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        new ExceptionLogger(this);
        
        
        PRNGFixes.apply();
        

        try {




            Toast.makeText(this, "Init bitchatj.", Toast.LENGTH_SHORT).show();
            AndroidSaver androidSaver = new AndroidSaver(this);

            Settings.STD_PORT += 2;

            Main.startUp(false, androidSaver);
            PopupListener popupListener = new PopupListener(this);
            Main.addListener(popupListener);

            Settings.till = System.currentTimeMillis() - 1000 * 60 * 60 * 2;

        } catch (IOException ex) {
            Logger.getLogger(BackgroundService.class.getName()).log(Level.SEVERE, null, ex);
        }






    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "bitchatj service destroyed...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY_COMPATIBILITY;
    }
}
