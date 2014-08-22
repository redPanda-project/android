/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda.ChannelList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.RemoteException;
import android.preference.*;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.redPanda.BS;
import org.redPanda.ExceptionLogger;
import org.redPanda.License;
import org.redPanda.R;
import org.redPandaLib.Main;
import org.redPandaLib.core.Settings;
import org.redPandaLib.services.SearchLan;

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
                            URL url = new URL("http://files.redpanda.im/android/version" + (developerUpdates ? "-developer" : ""));
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

                                            String url2 = "http://files.redpanda.im/android/redPanda" + (developerUpdates ? "-developer" : "") + ".apk";
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

        Preference expbutton = new Preference(this);
        expbutton.setTitle("Export");
        expbutton.setSummary("Export channels and IDs to SD-card.");

        expbutton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Preferences.this);
                builder.setTitle("Password");

//// Set up the input
                final LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(R.layout.ippchandiag, null);

                final EditText name = (EditText) ll.findViewById(R.id.channame);
                final EditText key = (EditText) ll.findViewById(R.id.chankey);
                name.setHintTextColor(Color.RED);
                key.setHintTextColor(Color.CYAN);
                key.setText("");
                key.setHint("Password");
                key.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                name.setVisibility(View.GONE);
//                final EditText input = new EditText(FlActivity.this);
//                
//// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
//                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                builder.setView(ll);

// Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy_hh-mm-ss");
                        File file = Environment.getExternalStorageDirectory();
                        String path = file.getAbsolutePath() + "/redpanda/" + formatter.format(new Date(System.currentTimeMillis())) + ".exp";
                        file = new File(file.getAbsolutePath() + "/redpanda/");
                        file.mkdir();

                        if (Main.backup(path, key.getText().toString())) {
                            Toast.makeText(Preferences.this, "Saved to " + path + "." + file.list().length, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Preferences.this, "Export failed.", Toast.LENGTH_SHORT).show();

                        }
                        if (file.list().length != 0) {
                            Toast.makeText(Preferences.this, file.list()[0], Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                return true;
            }
        });
        mainc.addPreference(expbutton);
        final Context con = this;
        Preference impbutton = new Preference(this);
        impbutton.setTitle("Import");
        impbutton.setSummary("Import channels and IDs from SD-card.");

        impbutton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(Preferences.this);
                builder1.setTitle("Path");
                File file = Environment.getExternalStorageDirectory();
                final String path = file.getAbsolutePath() + "/redpanda/";
                file = new File(path);
                ListView lv = new ListView(con);
                final String[] asd = file.list();
                ArrayAdapter<String> ad = new ArrayAdapter<String>(con, android.R.layout.simple_list_item_1, android.R.id.text1, asd);
                lv.setAdapter(ad);
                lv.setFocusableInTouchMode(true);
                if (asd.length != 0) {
                    Toast.makeText(Preferences.this, "Files: " + asd.length + "\n" + file.getAbsolutePath() + "\n" + asd[0], Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Preferences.this, "Files: " + asd.length + "\n" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                }
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        final String p = path + asd[position];
                        AlertDialog.Builder builder = new AlertDialog.Builder(Preferences.this);
                        builder.setTitle("Password");

//// Set up the input
                        final LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(R.layout.ippchandiag, null);

                        final EditText name = (EditText) ll.findViewById(R.id.channame);
                        final EditText key = (EditText) ll.findViewById(R.id.chankey);
                        name.setHintTextColor(Color.RED);
                        key.setHintTextColor(Color.CYAN);
                        key.setText("");
                        key.setHint("Password");
                        key.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        name.setVisibility(View.GONE);
//                final EditText input = new EditText(FlActivity.this);
//                
//// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
//                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                        builder.setView(ll);

// Set up the buttons
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean asd = Main.restoreBackup(p, key.getText().toString());
                                if (asd) {
                                    Toast.makeText(Preferences.this, "Imported " + path + " successful", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Preferences.this, "Import failed.", Toast.LENGTH_SHORT).show();

                                }

                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();
                    }
                });
                builder1.setView(lv);
                builder1.show();
                return true;
            }
        });
        mainc.addPreference(impbutton);

        Preference lanSearchButton = new Preference(this);
        lanSearchButton.setTitle("Search nodes on the local area network.");
        lanSearchButton.setSummary("Adds all local addresses and set MIN_CON to 100");

        lanSearchButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                SearchLan.searchLan();
                Settings.MIN_CONNECTIONS = 100;
                Settings.MAX_CONNECTIONS = 120;
                return true;
            }
        });
        mainc.addPreference(lanSearchButton);

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
