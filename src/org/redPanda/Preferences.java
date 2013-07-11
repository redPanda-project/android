/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import org.redPandaLib.Main;

/**
 *
 * @author robin
 */
public class Preferences extends PreferenceActivity {

    public static final String KEY_NICK = "a";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        new ExceptionLogger(this);

        super.onCreate(savedInstanceState);
        setPreferenceScreen(createPreferenceHierarchy());
    }

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);



        PreferenceCategory mainc = new PreferenceCategory(this);
        mainc.setTitle("General");
        root.addPreference(mainc);

        EditTextPreference activePref = new EditTextPreference(this);
        activePref.setKey(KEY_NICK);
        activePref.setTitle("Master Key");
        activePref.setSummary("Dies ist dein Master Key, er wird fuer ");
        //activePref.setText(Test.getNick());
        activePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference arg0, Object arg1) {
                String newNick = arg1.toString();
                //Test.setNick(newNick);
                return true;
            }
        });
        mainc.addPreference(activePref);

        Preference fullSyncInit = new Preference(this);
        fullSyncInit.setTitle("Full Sync");
        fullSyncInit.setSummary("Initializes a full sync with the network, may cause huge traffic.");
        fullSyncInit.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference pref) {
                Intent i = new Intent(Preferences.this, BS.class);
                i.setAction("c");
                i.putExtra("cmd", "fullSync");
                startService(i);
                return true;
            }
        });
        mainc.addPreference(fullSyncInit);

        Preference button = new Preference(this);
        button.setTitle("Add Main Channel");
        button.setSummary("Adds the Main Channel to your channel list.");

        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                Main.addMainChannel();
                return true;
            }
        });
        mainc.addPreference(button);
        return root;
    }
}
