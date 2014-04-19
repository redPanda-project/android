/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.*;
import android.preference.PreferenceManager;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.redPanda.ChannelList.ChanPref;
import org.redPanda.ChannelList.Preferences;
import org.redPandaLib.Main;
import org.redPandaLib.NewMessageListener;
import org.redPandaLib.core.Channel;
import org.redPandaLib.core.Settings;
import org.redPandaLib.core.Test;
import org.redPandaLib.core.messages.DeliveredMsg;
import org.redPandaLib.core.messages.ImageMsg;
import org.redPandaLib.core.messages.TextMessageContent;
import org.redPandaLib.core.messages.TextMsg;
import org.redPandaLib.crypt.AddressFormatException;

/**
 *
 * @author mflohr
 */
public class BS extends Service {

    /**
     * For showing and hiding our notification.
     */
    NotificationManager mNM;
    /**
     * Keeps track of all current registered clients.
     */
    // ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    /**
     * Command to the service to register a client, receiving callbacks from the
     * service. The Message's replyTo field must be a Messenger of the client
     * where callbacks should be sent.
     */
    public static final int VERSION = 330;
    public static final int SEND_MSG = 1;
    public static final int MSG_REGISTER_CLIENT = 2;
    public static final int MSG_UNREGISTER_CLIENT = 3;
    public static final int NEW_MSG = 4;
    public static final int GET_CHANNELS = 5;
    public static final int CHANNELS = 6;
    public static final int Send_MM = 7;
    public static final int ADD_CHANNEL = 8;
    public static final int CHANGE_NAME = 9;
    public static final int FL_REG = 10;
    public static final int FL_UNREG = 11;
    public static final int NEW_MSGL = 12;
    public static final int FL_DSC = 13;
    private static long lastUpdateChecked = 0;
    public static int currentViewedChannel = -100;
    private long lastTrimmed = 0;
    /**
     * Handler of incoming messages from clients.
     */
    private ArrayList<Channel> chanlist;
    private final HashMap<Channel, ArrayList<Messenger>> hm = new HashMap<Channel, ArrayList<Messenger>>();
    private Messenger flm;
    private SqLiteConnection sqLiteConnection;

    class IncomingHandler extends Handler {

        //Channel chan;
        @Override
        public void handleMessage(final Message mesg) {

            checkForUpdate();

            Bundle b;
            Message ms;

            switch (mesg.what) {
                case MSG_REGISTER_CLIENT:
                    //Toast.makeText(BS.this, "regclient", Toast.LENGTH_SHORT).show();
                    //   mClients.add(msg.replyTo);

                    int chanid = mesg.getData().getInt("chanid");
                    chanlist = Main.getChannels();

                    Channel chan = Channel.getChannelById(chanid);
                    ArrayList<Messenger> al = hm.get(chan);
                    if (al == null) {
                        al = new ArrayList<Messenger>();
                    }

                    ArrayList<TextMessageContent> ml = Main.getMessages(chan, System.currentTimeMillis() - 48 * 60 * 60 * 1000, Long.MAX_VALUE);

                    al.add(mesg.replyTo);
                    hm.put(chan, al);
                    Bundle b2 = new Bundle();
                    if (ml != null) {

                        ms = Message.obtain(null,
                                BS.NEW_MSGL);
                        b2.putSerializable("msgList", ml);
                        ms.setData(b2);
                        try {
                            mesg.replyTo.send(ms);
                        } catch (RemoteException ex) {
                            Logger.getLogger(BS.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                    break;
                case MSG_UNREGISTER_CLIENT:

                    int unchanid = mesg.getData().getInt("chanid");
                    final Channel unchan = Channel.getChannelById(unchanid);

                    ArrayList<Messenger> all = hm.get(unchan);
                    if (all != null) {
                        all.remove(mesg.replyTo);
                    }
                    break;
                case SEND_MSG:
                    // Toast.makeText(BS.this, "sendmsganserv " + chan.toString(), Toast.LENGTH_SHORT).show();

                    int schanid = mesg.getData().getInt("chanid");
                    final Channel schan = Channel.getChannelById(schanid);

                    final String msgContent = mesg.getData().getString("msg");
                    new Thread() {
                        @Override
                        public void run() {
                            setPriority(Thread.MIN_PRIORITY);
                            Main.sendMessageToChannel(schan, msgContent);
                        }
                    }.start();
                    break;
                case GET_CHANNELS:

                    final Messenger replyTo = mesg.replyTo;

                    new Thread() {
                        @Override
                        public void run() {
                            chanlist = Main.getChannels();
                            while (true) {
                                if (chanlist != null && !chanlist.isEmpty()) {
                                    break;
                                }

                                try {
                                    sleep(100);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(BS.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                chanlist = Main.getChannels();
                            }
                            Bundle b = new Bundle();
                            Message ms = Message.obtain(null,
                                    BS.CHANNELS);
//                    int i = chanlist.size();
//                    b.putInt("size", i);
//                    for (int j = 0; j < i; j++) {
//                        b.putSerializable("Channels" + j, chanlist.get(j));
//                    }

                            b.putSerializable(
                                    "CHANNELS", chanlist);
                            ms.setData(b);

                            try {

                                replyTo.send(ms);
                            } catch (RemoteException ex) {
                                Logger.getLogger(BS.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }.start();

                    break;
                case Send_MM:
                    Main.sendBroadCastMsg(mesg.getData().getString("msg"));
                    break;
                case ADD_CHANNEL:
                    b = mesg.getData();
                    try {
                        Main.importChannelFromHuman(b.getString("key"), b.getString("name"));
                    } catch (AddressFormatException ex) {
                        Logger.getLogger(BS.class.getName()).log(Level.SEVERE, null, ex);
                        //todo
                    }
                    chanlist = Main.getChannels();
                    break;
                case CHANGE_NAME:
                    chanlist = Main.getChannels();
                    b = mesg.getData();
                    int id = b.getInt("ChanId");
                    String newName = b.getString("name");
                    Channel c = Channel.getChannelById(id);
                    if (c.toString() != "Main Channel") {
                        c.setName(newName);
                    }
                    //Aktualisieren in der flactiviy

                    b = new Bundle();
                    ms = Message.obtain(null,
                            BS.CHANNELS);
//                    int i = chanlist.size();
//                    b.putInt("size", i);
//                    for (int j = 0; j < i; j++) {
//                        b.putSerializable("Channels" + j, chanlist.get(j));
//                    }

                    b.putSerializable(
                            "CHANNELS", chanlist);
                    ms.setData(b);

                    try {
                        if (flm != null) {
                            flm.send(ms);
                        }
                    } catch (RemoteException ex) {
                        Logger.getLogger(BS.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    break;
                case FL_REG:
                    flm = mesg.replyTo;
                    break;
                case FL_UNREG:
                    flm = null;
                    break;
                default:
                    super.handleMessage(mesg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    @Override
    public void onCreate() {
        //Toast.makeText(this, "Sevice onCreate", Toast.LENGTH_SHORT).show();

//        super.onCreate();
//
//        new ExceptionLogger(this);
//
//        try {
//            Toast.makeText(this, "Init bitchatj.", Toast.LENGTH_SHORT).show();
//            AndroidSaver androidSaver = new AndroidSaver(this);
//
//            Settings.STD_PORT += 2;
//
//            Main.startUp(false, androidSaver);
//            PopupListener popupListener = new PopupListener(this);
//            Main.addListener(popupListener);
//
//            Settings.till = System.currentTimeMillis() - 1000 * 60 * 60 * 2;
//
//        } catch (IOException ex) {
//            Logger.getLogger(BackgroundService.class.getName()).log(Level.SEVERE, null, ex);
//        };
        super.onCreate();

        new ExceptionLogger(this);

        new Thread() {
            @Override
            public void run() {

                PRNGFixes.apply();

                try {
                    File albumStorageDir = getAlbumStorageDir("redPanda");
                    Main.setImageStoreFolder(albumStorageDir.getAbsolutePath() + "/");


                    //            Toast.makeText(this, "Init bitchatj.", Toast.LENGTH_SHORT).show();
                    AndroidSaver androidSaver = new AndroidSaver(BS.this);
                    //Settings.STD_PORT += 2;
                    Settings.lightClient = true;
                    Settings.MIN_CONNECTIONS = 2;
                    Settings.REMOVE_OLD_MESSAGES = true;
                    //Settings.connectToNewClientsTill = System.currentTimeMillis() + 1000*60*5;
                    //Settings.till = System.currentTimeMillis() - 1000 * 60 * 60 * 12;
                    //HsqlConnection.db_file = getFilesDir() + "/data/";
                    sqLiteConnection = new SqLiteConnection(BS.this);

                    Main.setMessageStore(sqLiteConnection.getConnection());

                    Main.startUp(false, androidSaver);
                    PopupListener popupListener = new PopupListener(BS.this);
                    Main.addListener(popupListener);
                    Main.addListener(new MessageListener());

                } catch (SQLException ex) {
                    Logger.getLogger(BS.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(BackgroundService.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                ConnectivityChanged connectivityChanged = new ConnectivityChanged();

                while (true) {
                    connectivityChanged.onReceive(BS.this, null);
                    try {
                        sleep(1000 * 60 * 2);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(30000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BS.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {
                    Channel importChannelFromHuman = Main.importChannelFromHuman("prAZqUNKAu9D4Xtrpiv7yLHL3Pc4gUV6bQ86t86sgrJQ3SkDLn6E1ffez", "All Android Users");
                    if (importChannelFromHuman != null) {
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(BS.this);
                        if (!sharedPref.contains(ChanPref.CHAN_SILENT + importChannelFromHuman.getId())) {
                            sharedPref.edit().putBoolean(ChanPref.CHAN_SILENT + importChannelFromHuman.getId(), true).commit();
                        }
                    }

                } catch (AddressFormatException ex) {
                    Logger.getLogger(BS.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();

//        Intent intent = new Intent(this, FlActivity.class);
//        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.icon).setContentTitle("redPanda").setContentText("service working..").setContentIntent(contentIntent);
//        final Notification foregroundNotification = mBuilder.build();
//
//        startForeground(-1, foregroundNotification);
//
//        new Thread() {
//
//            @Override
//            public void run() {
//
//                while (true) {
//                    try {
//                        sleep(1000);
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(BS.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//
//                    if (Test.peerList == null) {
//                        continue;
//                    }
//
//                    int cnt = 0;
//                    for (Peer peer : Test.peerList) {
//                        if (peer.isConnected()) {
//                            cnt++;
//                        } else {
//                        }
//                    }
//
//
//                    DecimalFormat f = new DecimalFormat("#0.00");
//
//                    String trafficStatus = "Traffic: " + f.format(Test.inBytes / 1024.) + " kb / " + f.format(Test.outBytes / 1024.) + " kb.";
//
//                    final String text = "" + cnt + " peers. " + trafficStatus;
//                    foregroundNotification.setLatestEventInfo(BS.this, "redPanda", text, contentIntent);
//
//                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//
//                    nm.notify(-1, foregroundNotification);
//
//
//                }
//
//            }
//        }.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY_COMPATIBILITY;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        Main.shutdown();
    }

    @Override
    public void onTrimMemory(int level) {

        if (lastTrimmed < System.currentTimeMillis() - 1000 * 60 * 30) {
            lastTrimmed = System.currentTimeMillis();

            // if (level == TRIM_MEMORY_COMPLETE || level == TRIM_MEMORY_MODERATE) {
            if (level == TRIM_MEMORY_COMPLETE) {

                //HACK!
                try {
                    Test.savePeers();
                } catch (Exception e) {
                    String ownStackTrace = ExceptionLogger.stacktrace2String(e);
                    Main.sendBroadCastMsg("prevented exception: \n" + ownStackTrace);
                }

                try {
                    Toast.makeText(BS.this, "rebooting database - low memory", Toast.LENGTH_SHORT).show();
                    //SqLiteConnection oldCon = sqLiteConnection;
                    Statement stmt = sqLiteConnection.getConnection().createStatement();
                    stmt.executeUpdate("CHECKPOINT");//shutdown + reopen
//                sqLiteConnection.getConnection().close();
//                sqLiteConnection = new SqLiteConnection(this);
                    //Main.setMessageStore(sqLiteConnection.getConnection());
                    //Main.sendBroadCastMsg("low memory - rebooted database...");
                } catch (SQLException ex) {
                    Logger.getLogger(BS.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * When binding to the service, we return an interface to our messenger for
     * sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        //     Toast.makeText(this, "onBindService", Toast.LENGTH_SHORT).show();
        return mMessenger.getBinder();
    }

    class MessageListener implements NewMessageListener {

        @SuppressWarnings("empty-statement")
        public void newMessage(TextMessageContent msg) {

            if (msg.message_type == ImageMsg.BYTE) {

                String pathToFile = msg.getText();
                //Gallery scan file!
                MediaScannerConnection.scanFile(BS.this,
                        new String[]{pathToFile}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {

                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                //....                              
                            }
                        });

            }
            //   Toast.makeText(BS.this, "new MSG in SERVICE", Toast.LENGTH_SHORT).show();
            Channel chan = msg.getChannel();
            Message ms;
            Bundle b;
            Messenger m;
            Iterator<Messenger> its;
            System.out.println(chan.getId() + " " + chan.toString() + " " + hm.get(chan));

            if (msg.message_type == TextMsg.BYTE || msg.message_type == ImageMsg.BYTE) {
                //Set shared Pref for FLActivity
                int id = msg.getChannel().getId();
                long time = msg.getTimestamp();
                String from;
                if (msg.fromMe) {
                    from = "Me";
                } else {
                    from = msg.getName();
                }
                String text = "";
                if (msg.message_type == TextMsg.BYTE) {
                    text = from + ": " + msg.getText();
                } else if (msg.message_type == ImageMsg.BYTE) {
                    text = from + ": " + "Picture";
                }
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(BS.this);
                SharedPreferences.Editor edit = sharedPref.edit();
                edit.putLong("lastMessageForChannel" + id, time);
                edit.putString("lastMessageTextForChannel" + id, text);
                edit.commit();

                //
                //Msg FlAct for DataSetChanged
                if (flm != null) {
                    ms = Message.obtain(null,
                            BS.FL_DSC);
                    try {
                        flm.send(ms);
                    } catch (RemoteException ex) {
                        Logger.getLogger(BS.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                // Toast.makeText(BS.this, text+" ", Toast.LENGTH_SHORT).show();
                //
            }

            for (Channel a : hm.keySet()) {
                System.out.println("chans: " + a.getId() + " " + a.toString());
            }

            if (hm.get(chan) != null) {
                System.out.println("hm nicht null");

                its = hm.get(chan).iterator();
                while (its.hasNext()) {
                    m = its.next();
                    if (msg.getChannel().equals(chan)) {
                        b = new Bundle();

                        //b.putString("msg", msg.getText());
                        //b.putLong("sendtime", msg.getTimestamp());
                        //b.putBoolean("fromMe", msg.isFromMe());
                        b.putSerializable("msg", msg);
                        ms = Message.obtain(null,
                                BS.NEW_MSG);
                        ms.setData(b);
                        try {
                            m.send(ms);
                        } catch (RemoteException ex) {
                            Logger.getLogger(BS.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            } else {
                System.out.println("hm null");
            }

            if (msg.text != null && msg.text.length() > 1 && msg.text.substring(0, 2).equals("pr")) {
                try {
                    Main.importChannelFromHuman(msg.text, "auto import: " + chan.getName() + " - " + msg.getName());
                } catch (AddressFormatException ex) {
                    Logger.getLogger(BS.class.getName()).log(Level.SEVERE, null, ex);
                    //todo
                }
            }

        }
    }

    private void checkForUpdate() {

        if (System.currentTimeMillis() - lastUpdateChecked < 1000 * 60 * 5) {
            return;
        }

        lastUpdateChecked = System.currentTimeMillis();

        new Thread() {
            @Override
            public void run() {
//                Properties systemProperties = System.getProperties();
//                systemProperties.setProperty("sun.net.client.defaultConnectTimeout", "300");
//                systemProperties.setProperty("sun.net.client.defaultReadTimeout", "300");

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(BS.this);
                boolean developerUpdates = sharedPref.getBoolean(Preferences.KEY_SEARCH_DEVELOPER_UPDATES, false);

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

                        //if (v > VERSION && developerUpdates || !developerUpdates && v - VERSION > 50) {
                        if (v > VERSION) {
                            //System.out.println("MVersion: " + LBS.VERSION + " found: " + v);
                            updateFound();
                            break;
                        } else {
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
    }

    private void updateFound() {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(BS.this);
        boolean developerUpdates = sharedPref.getBoolean(Preferences.KEY_SEARCH_DEVELOPER_UPDATES, true);

        // The PendingIntent to launch our activity if the user selects this notification
        String url2 = "http://redpanda.hopto.org/android/redPanda" + (developerUpdates ? "-developer" : "") + ".apk";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url2));
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);
        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification(R.drawable.icon, "Update found.", System.currentTimeMillis());
        //notification.defaults |= Notification.FLAG_AUTO_CANCEL;
        notification.setLatestEventInfo(getApplicationContext(), "Update found.", "Click to download.", contentIntent);
        //notification.defaults |= Notification.FLAG_AUTO_CANCEL;
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        //notification.sound = Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI, "6");

        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(-10, notification);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals("c")) {
                String cmd = intent.getStringExtra("cmd");
                if (cmd.equals("fullSync")) {
                    Settings.initFullNetworkSync = true;
                } else if (cmd.equals("removeOldMessages")) {
                    Main.removeOldMessagesDecryptedContent();
                    Main.removeOldMessages();

                }

            }
        }

    }

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            System.out.println("filedir not created...");
        }
        return file;
    }

    public File getXmlStorageDir(String xmlDirName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                "Documents"), xmlDirName);
        if (!file.mkdirs()) {
            System.out.println("filedir not created...");
        }
        return file;
    }
}
