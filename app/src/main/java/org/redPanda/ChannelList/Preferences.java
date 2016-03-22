/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda.ChannelList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.*;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
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
import java.util.Locale;
import org.hsqldb.lib.LineReader;
import org.redPanda.BS;
import org.redPanda.ChatActivity;
import org.redPanda.ExceptionLogger;
import static org.redPanda.ExceptionLogger.stacktrace2String;
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
    public static final String KEY_PICTURE_QUALITY = "e";
    public int result = Activity.RESULT_CANCELED;
    private int picQual = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        new ExceptionLogger(this);

        super.onCreate(savedInstanceState);
        setPreferenceScreen(createPreferenceHierarchy());
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

        PreferenceCategory mainc = new PreferenceCategory(this);
        mainc.setTitle(getString(R.string.general));
        root.addPreference(mainc);

        if (BS.updateAbleViaWeb) {
            Preference updateButton = new Preference(this);
            updateButton.setTitle(getString(R.string.search_for_update));
            updateButton.setSummary(getString(R.string.current_version, BS.VERSION));

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
                                                Toast.makeText(Preferences.this, getString(R.string.update_found), Toast.LENGTH_SHORT).show();

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
                                                Toast.makeText(Preferences.this, getString(R.string.latest_version_installed), Toast.LENGTH_SHORT).show();
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

        } else {

            Preference updateButton = new Preference(this);
            updateButton.setTitle(getString(R.string.search_for_update));
            updateButton.setSummary(getString(R.string.current_version, BS.VERSION));

            updateButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    String url2 = "https://play.google.com/store/apps/details?id=org.redPanda";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url2));
                    startActivity(i);
                    return true;
                }
            });
            mainc.addPreference(updateButton);

        }

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


        Preference removeOldMessages = new Preference(this);
        removeOldMessages.setTitle(getString(R.string.remove_old_messages));
        removeOldMessages.setSummary(getString(R.string.removes_messages_older_than_one_week));
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
        saveMobileInternet.setTitle(getString(R.string.reduce_traffic_over_2G_3G_4G));
        saveMobileInternet.setSummary(getString(R.string.doesnt_stay_connected_over_mobile_internet_messages_may_be_delayed_but_saves_your_mobile_traffic));
        mainc.addPreference(saveMobileInternet);

        CheckBoxPreference startAfterBoot = new CheckBoxPreference(this);
        startAfterBoot.setDefaultValue(true);
        startAfterBoot.setKey(KEY_START_AFTER_BOOTING);
        startAfterBoot.setTitle(getString(R.string.autostart));
        startAfterBoot.setSummary(getString(R.string.if_checked_this_app_will_start_after_the_system_booted_recommended));
        mainc.addPreference(startAfterBoot);

        CheckBoxPreference searchDeveloperUpdates = new CheckBoxPreference(this);
        searchDeveloperUpdates.setDefaultValue(false);
        searchDeveloperUpdates.setKey(KEY_SEARCH_DEVELOPER_UPDATES);
        searchDeveloperUpdates.setTitle(getString(R.string.developer_updates));
        searchDeveloperUpdates.setSummary(getString(R.string.if_checked_all_available_updates_will_be_displayed_maybe_annoying));
        mainc.addPreference(searchDeveloperUpdates);

        Preference button = new Preference(this);
        button.setTitle(getString(R.string.add_main_channel));
        button.setSummary(getString(R.string.adds_the_main_channel_to_your_channel_list));

        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                Main.addMainChannel();
                return true;
            }
        });
        mainc.addPreference(button);

        Preference expbutton = new Preference(this);
        expbutton.setTitle(getString(R.string.export));
        expbutton.setSummary(getString(R.string.export_channels_and_ids_to_sdcard));

        expbutton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Preferences.this);
                builder.setTitle(getString(R.string.password));

//// Set up the input
                final LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(R.layout.ippchandiag, null);

                final EditText name = (EditText) ll.findViewById(R.id.channame);
                final EditText key = (EditText) ll.findViewById(R.id.chankey);
                name.setHintTextColor(Color.RED);
                key.setHintTextColor(Color.CYAN);
                key.setText("");
                key.setHint(getString(R.string.password));
                key.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                name.setVisibility(View.GONE);
//                final EditText input = new EditText(FlActivity.this);
//                
//// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
//                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                builder.setView(ll);

// Set up the buttons
                builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy_hh-mm-ss");

                        boolean mounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
                        File file;
                        if (mounted) {
                            file = Environment.getExternalStorageDirectory();
                        } else {
                            file = Environment.getDataDirectory();
                        }

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
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
        impbutton.setTitle(getString(R.string.import_));
        impbutton.setSummary(getString(R.string.import_channels_and_ids_from_sdcard));

        impbutton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(Preferences.this);
                builder1.setTitle(getString(R.string.path));

                boolean mounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
                File file;
                if (mounted) {
                    file = Environment.getExternalStorageDirectory();
                } else {
                    file = Environment.getDataDirectory();
                }
                final String path = file.getAbsolutePath() + "/redpanda/";
                file = new File(path);
                if (!file.isDirectory()) {
                    file.mkdir();
                }
                final String[] fileList = file.list();
                if (fileList.length == 0) {
                    builder1.setTitle(getString(R.string.error));
                    builder1.setMessage(getString(R.string.no_files_to_import_found_move_the_files_to, file.getAbsolutePath()));
                } else {
                    builder1.setSingleChoiceItems(fileList, 0, null);
                    builder1.setPositiveButton(getString(R.string.import_), new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                            final String p = path + fileList[position];
                            AlertDialog.Builder builder = new AlertDialog.Builder(Preferences.this);
                            builder.setTitle(getString(R.string.password));

//// Set up the input
                            final LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(R.layout.ippchandiag, null);

                            final EditText name = (EditText) ll.findViewById(R.id.channame);
                            final EditText key = (EditText) ll.findViewById(R.id.chankey);
                            name.setHintTextColor(Color.RED);
                            key.setHintTextColor(Color.CYAN);
                            key.setText("");
                            key.setHint(getString(R.string.password));
                            key.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            name.setVisibility(View.GONE);
//                final EditText input = new EditText(FlActivity.this);
//                
//// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
//                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                            builder.setView(ll);

// Set up the buttons
                            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    new Thread() {

                                        @Override
                                        public void run() {
                                            try {

                                                final boolean asd = Main.restoreBackup(p, key.getText().toString());
                                                if (asd) {
                                                    result = Activity.RESULT_OK;
                                                }

                                                runOnUiThread(new Runnable() {

                                                    public void run() {
                                                        if (asd) {
                                                            Toast.makeText(Preferences.this, "Imported " + path + " successful", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(Preferences.this, "Import failed.", Toast.LENGTH_SHORT).show();

                                                        }
                                                    }
                                                });

                                            } catch (Exception e) {
                                                String ownStackTrace = stacktrace2String(e);
                                                Main.sendBroadCastMsg("Version: " + BS.VERSION + " \n" + ownStackTrace);
                                            }

                                        }
                                    }.start();
                                }
                            });
                            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            builder.show();
                        }
                    });

                    ArrayAdapter<String> ad = new ArrayAdapter<String>(con, android.R.layout.simple_list_item_1, android.R.id.text1, fileList);

                    if (fileList.length != 0) {
                        Toast.makeText(Preferences.this, "Files: " + fileList.length + "\n" + file.getAbsolutePath() + "\n" + fileList[0], Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Preferences.this, "Files: " + fileList.length + "\n" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    }
                }
                builder1.show();
                return true;
            }
        });
        mainc.addPreference(impbutton);

        Preference lanSearchButton = new Preference(this);
        lanSearchButton.setTitle(getString(R.string.search_nodes_on_the_local_area_network));
        lanSearchButton.setSummary(getString(R.string.adds_all_local_addresses_and_set_min_con_to_100));

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

        Preference changeLangButton = new Preference(this);
        changeLangButton.setTitle(this.getString(R.string.change_language));
        changeLangButton.setSummary(this.getString(R.string.change_the_language_of_the_app));

        changeLangButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(Preferences.this);
                builder1.setTitle(getString(R.string.change_language));
                String[] lNames = getResources().getStringArray(R.array.language_array);
                final String[] lan = {"en", "de"};
                Locale current = getResources().getConfiguration().locale;
                int currentLang = 0;
                if (current.getLanguage().equals("de")) {
                    currentLang = 1;
                }
                builder1.setSingleChoiceItems(lNames, currentLang, null);
                builder1.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        String langPref = "Language";
                        SharedPreferences prefs = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);

                        prefs.edit().putString(langPref, lan[position]).commit();

                        result = Activity.RESULT_OK;
                        setResult(result, new Intent());
                        finish();
                    }
                });
                builder1.setNegativeButton(getString(R.string.cancel), null);
                builder1.show();
                return true;
            }
        });
        mainc.addPreference(changeLangButton);

        Preference JPGQualityButton = new Preference(this);
        JPGQualityButton.setTitle(this.getString(R.string.picture_quality));
        JPGQualityButton.setSummary(this.getString(R.string.percentage_to_scale_down_pictures));

        JPGQualityButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                AlertDialog.Builder percent = new AlertDialog.Builder(con);
                percent.setTitle(con.getString(R.string.picture_quality));
                LinearLayout linear = new LinearLayout(con);
                linear.setOrientation(LinearLayout.VERTICAL);
                TextView text = new TextView(con);
                text.setText(con.getString(R.string.percentage_to_scale_down_pictures));
                text.setPadding(10, 10, 10, 10);
                TextView progress = new TextView(con);
                progress.setPadding(10, 10, 10, 10);

                SeekBar seek = new SeekBar(con);
                seek.setMax(100);
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(Preferences.this);
                picQual = sharedPref.getInt(Preferences.KEY_PICTURE_QUALITY, 80);
                progress.setText(picQual + "/100");
                seek.setProgress(picQual);

                seek.setOnSeekBarChangeListener(
                        new SeekBar.OnSeekBarChangeListener() {
                            int prog;
                            TextView text;

                            public SeekBar.OnSeekBarChangeListener setParams(int prog, TextView text) {
                                this.prog = prog;
                                this.text = text;
                                return this;
                            }

                            public SeekBar.OnSeekBarChangeListener setText(TextView text) {
                                this.text = text;
                                return this;
                            }

                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                prog = progress;
                                text.setText(prog + "/100");
                            }

                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            public void onStopTrackingTouch(SeekBar seekBar) {
                                setPicQual(prog);
//                                prog = round(prog);
//                                seekBar.setProgress(prog);
                            }

                            public int round(int pr) {
                                if (pr % 5 == 1) {
                                    pr -= 1;
                                }
                                if (pr % 5 == 4) {
                                    pr += 1;
                                }
                                return pr;
                            }
                        }.setParams(picQual, progress));
                linear.addView(text);
                linear.addView(seek);
                linear.addView(progress);
                percent.setView(linear);
                percent.setPositiveButton(con.getString(R.string.ok), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Preferences.this);
                        prefs.edit().putInt(KEY_PICTURE_QUALITY, getPicQual()).commit();
                    }
                });
                percent.setNegativeButton(con.getString(R.string.cancel), null);

                percent.show();
                return true;
            }
        }
        );
        mainc.addPreference(JPGQualityButton);

        Preference licenseButton = new Preference(this);
        licenseButton.setTitle(getString(R.string.show_license));
        licenseButton.setSummary(getString(R.string.redpanda_is_distributed_over_gpl_30_license));

        licenseButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public
                    boolean onPreferenceClick(Preference arg0) {

                startActivity(new Intent(Preferences.this, License.class
                ));

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

    public int getPicQual() {
        return picQual;
    }

    public void setPicQual(int picQual) {
        this.picQual = picQual;
    }

    @Override
    public void onBackPressed() {

        setResult(result, new Intent());
        finish();
    }

    @Override

    protected void onResume() {
        super.onResume();
        Settings.connectToNewClientsTill = Long.MAX_VALUE;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ChatActivity.backToFlActivity(Preferences.this);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
