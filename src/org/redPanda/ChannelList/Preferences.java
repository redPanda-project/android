/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda.ChannelList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.*;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import org.redPanda.BS;
import org.redPanda.ExceptionLogger;
import org.redPanda.License;
import org.redPandaLib.Main;
import org.redPandaLib.core.Settings;

/**
 *
 * @author robin
 */
public class Preferences extends PreferenceActivity {

    public static final String KEY_NICK = "a";
    public static final String KEY_SAVE_MOBILE_INTERNET = "b";
    public static final String KEY_START_AFTER_BOOTING = "c";
    public static final String KEY_SEARCH_DEVELOPER_UPDATES = "d";

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

        Preference updateButton = new Preference(this);
        updateButton.setTitle("Search for update.");
        updateButton.setSummary("Current version " + BS.VERSION + ".");

        updateButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {

                new Thread() {
                    @Override
                    public void run() {
//                Properties systemProperties = System.getProperties();
//                systemProperties.setProperty("sun.net.client.defaultConnectTimeout", "300");
//                systemProperties.setProperty("sun.net.client.defaultReadTimeout", "300");

                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(Preferences.this);
                        final boolean developerUpdates = sharedPref.getBoolean(Preferences.KEY_SEARCH_DEVELOPER_UPDATES, false);

                        try {
                            // Create a URL for the desired page
                            URL url = new URL("http://redpanda.hopto.org/android/version" + (developerUpdates ? "-developer" : ""));
                            // Read all the text returned by the server
                            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                            String str;
                            while ((str = in.readLine()) != null) {
                                // str is one line of text;readLine() strips the newline character(s)
                                //System.out.println("line: " + str);

                                int v = Integer.parseInt(str);

                                //if (v > BS.VERSION && developerUpdates || !developerUpdates && v - BS.VERSION > 50) {
                                if (v > BS.VERSION) {
                                    //System.out.println("MVersion: " + LBS.VERSION + " found: " + v);



                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(Preferences.this, "Update found.", Toast.LENGTH_SHORT).show();

                                            String url2 = "http://redpanda.hopto.org/android/redPanda" + (developerUpdates ? "-developer" : "") + ".apk";
                                            Intent i = new Intent(Intent.ACTION_VIEW);
                                            i.setData(Uri.parse(url2));
                                            startActivity(i);
                                        }
                                    });



//                                    String url2 = "http://xana.hopto.org/redPanda/redPanda.apk";
//                                    // The PendingIntent to launch our activity if the user selects this notification
//                                    Intent intent2 = new Intent(Intent.ACTION_VIEW);
//                                    intent2.setData(Uri.parse(url2));
//                                    PendingIntent contentIntent = PendingIntent.getActivity(Preferences.this, 0, intent2, 0);
//                                    // Set the info for the views that show in the notification panel.
//                                    Notification notification = new Notification(R.drawable.icon, "Update found.", System.currentTimeMillis());
//                                    //notification.defaults |= Notification.FLAG_AUTO_CANCEL;
//                                    notification.setLatestEventInfo(getApplicationContext(), "Update found.", "Click to download.", contentIntent);
//                                    //notification.defaults |= Notification.FLAG_AUTO_CANCEL;
//                                    notification.flags = Notification.FLAG_AUTO_CANCEL;
//                                    //notification.sound = Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI, "6");
//
//                                    // Send the notification.
//                                    // We use a string id because it is a unique number.  We use it later to cancel.
//                                    ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(-10, notification);

                                    break;
                                } else {


                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(Preferences.this, "Latest version installed.", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }

                            }
                            in.close();

                        } catch (MalformedURLException e) {
                        } catch (IOException e) {
                        } catch (NumberFormatException e) {
                        }
//                System.getProperties().remove("sun.net.client.defaultConnectTimeout");
//                System.getProperties().remove("sun.net.client.defaultReadTimeout");
                    }
                }.start();

                return true;
            }
        });
        mainc.addPreference(updateButton);


//        EditTextPreference activePref = new EditTextPreference(this);
//        activePref.setKey(KEY_NICK);
//        activePref.setTitle("Master Key");
//        activePref.setSummary("Dies ist dein Master Key, er wird fuer ");
//        //activePref.setText(Test.getNick());
//        activePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//
//            public boolean onPreferenceChange(Preference arg0, Object arg1) {
//                String newNick = arg1.toString();
//                //Test.setNick(newNick);
//                return true;
//            }
//        });
//        mainc.addPreference(activePref);

        Preference fullSyncInit = new Preference(this);
        fullSyncInit.setTitle("Init new network discovery.");
        fullSyncInit.setSummary("Initializes a full network discovery, may cause huge traffic.");
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


        Preference removeOldMessages = new Preference(this);
        removeOldMessages.setTitle("Remove old messages.");
        removeOldMessages.setSummary("Removes messages older then one week.");
        removeOldMessages.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference pref) {
                Intent i = new Intent(Preferences.this, BS.class);
                i.setAction("c");
                i.putExtra("cmd", "removeOldMessages");
                startService(i);
                return true;
            }
        });
        mainc.addPreference(removeOldMessages);

        CheckBoxPreference saveMobileInternet = new CheckBoxPreference(this);
        saveMobileInternet.setDefaultValue(false);
        saveMobileInternet.setKey(KEY_SAVE_MOBILE_INTERNET);
        saveMobileInternet.setTitle("Reduce traffic over 2G/3G/4G.");
        saveMobileInternet.setSummary("Doesn't stay connected over mobile internet. Messages may be delayed,"
                + " but saves your mobile traffic.");
        mainc.addPreference(saveMobileInternet);


        CheckBoxPreference startAfterBoot = new CheckBoxPreference(this);
        startAfterBoot.setDefaultValue(true);
        startAfterBoot.setKey(KEY_START_AFTER_BOOTING);
        startAfterBoot.setTitle("Autostart.");
        startAfterBoot.setSummary("If checked, this app will start after the system booted. (Recommended)");
        mainc.addPreference(startAfterBoot);

        CheckBoxPreference searchDeveloperUpdates = new CheckBoxPreference(this);
        searchDeveloperUpdates.setDefaultValue(false);
        searchDeveloperUpdates.setKey(KEY_SEARCH_DEVELOPER_UPDATES);
        searchDeveloperUpdates.setTitle("Developer updates.");
        searchDeveloperUpdates.setSummary("If checked, all available updates will be displayed. May be annoying.");
        mainc.addPreference(searchDeveloperUpdates);

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

        Preference licenseButton = new Preference(this);
        licenseButton.setTitle("Show license.");
        licenseButton.setSummary("redPanda is distributed over GPL 3.0 license.");

        licenseButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {

                startActivity(new Intent(Preferences.this, License.class));

                return true;
            }
        });
        mainc.addPreference(licenseButton);

//        Preference shutdownButton = new Preference(this);
//        shutdownButton.setTitle("Shutdown.");
//        shutdownButton.setSummary("Kills the background service.");
//
//        shutdownButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference arg0) {
//                stopService(new Intent(Preferences.this, BS.class));
//                finish();
//                return true;
//            }
//        });
//        mainc.addPreference(shutdownButton);

        return root;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Settings.connectToNewClientsTill = Long.MAX_VALUE;
    }
}
