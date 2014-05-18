/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda.ChannelList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.widget.CheckBox;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.redPanda.BS;
import org.redPandaLib.core.Channel;

/**
 *
 * @author mflohr
 */
public class ChanPref extends PreferenceActivity {

    // public static final String KEY_NICK = "a";
    public static String CHAN_NAME = "CN";
    public static String CHAN_SILENT = "chan_silent";
    public static String CHAN_NOTIFICATIONS = "chan_notify";
    public Intent intent;
    public Channel chan;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doBindService();
        intent = getIntent();
        chan = (Channel) intent.getExtras().get("Channel");
        setPreferenceScreen(createPreferenceHierarchy());
    }

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

        final Channel chan = (Channel) intent.getExtras().get("Channel");


        PreferenceCategory mainc = new PreferenceCategory(this);
        mainc.setTitle("Channel Einstellungen");
        root.addPreference(mainc);

        EditTextPreference activePref = new EditTextPreference(this);
        activePref.setKey(CHAN_NAME + chan.getId());
        activePref.setTitle("Channel name");
        activePref.setSummary("The name of the channel. Visible only for you.");
        activePref.setText(chan.toString());
        activePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            public boolean onPreferenceChange(Preference arg0, Object arg1) {

                try {
                    Message ms = Message.obtain(null, BS.CHANGE_NAME);
                    Bundle b = new Bundle();
                    b.putString("name", arg1.toString());
                    b.putInt("ChanId", chan.getId());
                    ms.setData(b);
                    mService.send(ms);
                } catch (RemoteException ex) {
                    Logger.getLogger(ChanPref.class.getName()).log(Level.SEVERE, null, ex);
                }
                return true;
            }
        });
        mainc.addPreference(activePref);


        CheckBoxPreference silentPref = new CheckBoxPreference(this);
        silentPref.setKey(CHAN_SILENT + chan.getId());
        silentPref.setTitle("Silent Mode");
        silentPref.setSummary("If activated, the notification will not make any sound or will vibrate.");
        silentPref.setDefaultValue(false);
        mainc.addPreference(silentPref);

        CheckBoxPreference noNotifications = new CheckBoxPreference(this);
        noNotifications.setKey(CHAN_NOTIFICATIONS + chan.getId());
        noNotifications.setTitle("Notifications");
        noNotifications.setSummary("If disabled, you will not get any notification for new messages.");
        noNotifications.setDefaultValue(true);
        mainc.addPreference(noNotifications);

        return root;
    }
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    boolean mIsBound = false;
    Messenger mService;
    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = new Messenger(service);

            // We want to monitor the service for as long as we are
            // connected to it.


        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            doBindService();
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        mIsBound = bindService(new Intent(ChanPref.this,
                BS.class), mConnection, Context.BIND_AUTO_CREATE);

    }

    void doUnbindService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;

        }
    }
}
