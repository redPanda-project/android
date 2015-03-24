/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda.ChannelList;

import android.app.Activity;
import static android.app.Activity.RESULT_OK;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.text.ClipboardManager;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.redPanda.BS;
import org.redPanda.ChannelViewElement;
import org.redPanda.ChatActivity;
import org.redPanda.ExceptionLogger;
import org.redPanda.R;
import org.redPandaLib.Main;
import org.redPandaLib.core.Channel;
import org.redPandaLib.core.Peer;
import org.redPandaLib.core.Settings;
import org.redPandaLib.core.Test;
import android.support.v4.util.LruCache;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import org.redPanda.MenuAdapter;
import org.redPandaLib.core.PeerTrustData;

/**
 *
 * @author mflohr
 */
public class FlActivity extends Activity {

    public ArrayList<ChannelViewElement> channels = new ArrayList<ChannelViewElement>();
    public FLAdapter adapter;
    private boolean active;
    TextView infotext;
    public static Context context;
    public ListView lv;
    public static LruCache<String, Bitmap> mMemoryCache;
    public String qrtext = "";
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private boolean isImageAction = false, isTextAction = false;
    private String textAction = "", imageAction = "";
    private final String[] imageFileExtensions = new String[]{"jpg", "png", "gif", "jpeg"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    return bitmap.getByteCount() / 1024;
                } else {
                    return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
                }

            }
        };

        context = this;

        new ExceptionLogger(this);
        //startService(new Intent(this, BS.class));
        //Settings.connectToNewClientsTill = System.currentTimeMillis() + 1000*60*5;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fl);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        String[] str = new String[]{"Create new channel", "Import channel", "Scan QR-code", "Settings"};
        // Set the adapter for the list view
        mDrawerList.setBackgroundColor(Color.WHITE);
        mDrawerList.setAdapter(new MenuAdapter(context, str));
        // Set the list's onClickListeners
        mDrawerList.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder;

                switch (position) {
                    case 0: // Create new Channel
                        builder = new AlertDialog.Builder(FlActivity.this);
                        builder.setTitle("Create new Channel");

                        //// Set up the input
                        final EditText input = new EditText(FlActivity.this);
                        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
                        input.setHint("New channel name");
                        input.setHintTextColor(Color.RED);

                        builder.setView(input);

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Main.addChannel(Channel.generateNew(input.getText().toString()));

                                Message msg = Message.obtain(null,
                                        BS.GET_CHANNELS);
                                msg.replyTo = mMessenger;
                                try {
                                    mService.send(msg);
                                } catch (RemoteException ex) {
                                    Logger.getLogger(FlActivity.class.getName()).log(Level.SEVERE, null, ex);
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

                        break;
                    case 1: // import channel

                        builder = new AlertDialog.Builder(FlActivity.this);
                        builder.setTitle("Import Channel");

//// Set up the input
                        final LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(R.layout.ippchandiag, null);

                        final EditText name = (EditText) ll.findViewById(R.id.channame);
                        final EditText key = (EditText) ll.findViewById(R.id.chankey);
                        name.setHintTextColor(Color.RED);
                        key.setHintTextColor(Color.CYAN);
                        //key.setText("ApLd3t77vbqxnYguJ3eP61eLBnK9TVcgo15G8NxYJF6V", TextView.BufferType.NORMAL);
//                final EditText input = new EditText(FlActivity.this);
//                
//// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
//                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                        builder.setView(ll);

// Set up the buttons
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Message msg = Message.obtain(null,
                                        BS.ADD_CHANNEL);
                                Bundle b = new Bundle();
                                b.putString("name", name.getText().toString());
                                b.putString("key", key.getText().toString());
                                msg.setData(b);
                                msg.replyTo = mMessenger;
                                try {
                                    mService.send(msg);
                                } catch (RemoteException ex) {
                                    Logger.getLogger(FlActivity.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                msg = Message.obtain(null,
                                        BS.GET_CHANNELS);
                                msg.replyTo = mMessenger;
                                try {
                                    mService.send(msg);
                                } catch (RemoteException ex) {
                                    Logger.getLogger(FlActivity.class.getName()).log(Level.SEVERE, null, ex);
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

                        break;
                    case 2: //scan Qr code
                        Intent is;
                        is = new Intent(FlActivity.this, QRCaptureActivity.class);
                        startActivity(is);
                        break;
                    case 3: //settings
                        Intent intent2 = new Intent(context, Preferences.class);
                        startActivity(intent2);
                        break;

                }
                mDrawerLayout.closeDrawers();
            }
        });

        adapter = new FLAdapter(this, R.layout.listitem, channels);

//        Intent intent = new Intent(this, BS.class);
//        startService(intent);
//        Toast.makeText(FlActivity.this, "doBindService", Toast.LENGTH_SHORT).show();
        infotext = (TextView) findViewById(R.id.infotext);
        infotext.setTextColor(Color.GRAY);

//        Button newChButton = (Button) findViewById(R.id.NKButton);
//
//        newChButton.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View arg0) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(FlActivity.this);
//                builder.setTitle("Erstelle neuen Channel");
//
////// Set up the input
//                final EditText input = new EditText(FlActivity.this);
////                
////// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
//                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
//                input.setHint("Neuer Channel Name");
//                input.setHintTextColor(Color.RED);
//
//                builder.setView(input);
//
//// Set up the buttons
//                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        Main.addChannel(Channel.generateNew(input.getText().toString()));
//
//                        Message msg = Message.obtain(null,
//                                BS.GET_CHANNELS);
//                        msg.replyTo = mMessenger;
//                        try {
//                            mService.send(msg);
//                        } catch (RemoteException ex) {
//                            Logger.getLogger(FlActivity.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//
//                    }
//                });,
//                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                });
//
//                builder.show();
//
//            }
//        });
//        Button newChByQRButton = (Button) findViewById(R.id.NKByQRButton);
//
//        newChByQRButton.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View arg0) {
//                Intent is;
//                is = new Intent(FlActivity.this, QRCaptureActivity.class);
//                startActivity(is);
//            }
//        });
//
//        Button impButton = (Button) findViewById(R.id.imbutton);
//        impButton.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View arg0) {
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(FlActivity.this);
//                builder.setTitle("Import Channel");
//
////// Set up the input
//                final LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(R.layout.ippchandiag, null);
//
//                final EditText name = (EditText) ll.findViewById(R.id.channame);
//                final EditText key = (EditText) ll.findViewById(R.id.chankey);
//                name.setHintTextColor(Color.RED);
//                key.setHintTextColor(Color.CYAN);
//                //key.setText("ApLd3t77vbqxnYguJ3eP61eLBnK9TVcgo15G8NxYJF6V", TextView.BufferType.NORMAL);
////                final EditText input = new EditText(FlActivity.this);
////                
////// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
////                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
//
//                builder.setView(ll);
//
//// Set up the buttons
//                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        Message msg = Message.obtain(null,
//                                BS.ADD_CHANNEL);
//                        Bundle b = new Bundle();
//                        b.putString("name", name.getText().toString());
//                        b.putString("key", key.getText().toString());
//                        msg.setData(b);
//                        msg.replyTo = mMessenger;
//                        try {
//                            mService.send(msg);
//                        } catch (RemoteException ex) {
//                            Logger.getLogger(FlActivity.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                        msg = Message.obtain(null,
//                                BS.GET_CHANNELS);
//                        msg.replyTo = mMessenger;
//                        try {
//                            mService.send(msg);
//                        } catch (RemoteException ex) {
//                            Logger.getLogger(FlActivity.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }
//                });
//                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                });
//
//                builder.show();
//
//            }
//        });
//        ArrayList<Channel> channelslist = Main.getChannels();
//        Channel[] toArray = new Channel[channels.size()];
//        if (channelslist == null) {
//            Toast.makeText(FlActivity.this, "Channels null", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(FlActivity.this, "Channels nicht null", Toast.LENGTH_SHORT).show();
//
//            //   channels = (ArrayList<Channel>) channelslist.clone();
//        }
        lv = (ListView) findViewById(R.id.chanlist);

//        ArrayAdapter<Channel> adapter;
//        adapter = new ArrayAdapter<Channel>(this, R.layout.listitem, R.id.text1, channels);
// Assign adapter to ListView
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(
                new OnItemClickListenerImpl(channels));

        this.registerForContextMenu(lv);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                isTextAction = true;
                textAction = intent.getStringExtra(Intent.EXTRA_TEXT);

                Toast.makeText(FlActivity.this, textAction, Toast.LENGTH_LONG).show();

                //handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                isImageAction = true;
                imageAction = getImagePath((Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM));
                Toast.makeText(FlActivity.this, imageAction, Toast.LENGTH_LONG).show();
                // Handle single image being sent
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        } else {
            // Handle other intents, such as being started from the home screen
        }

//        getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.red_bg));
    }

    private class OnItemClickListenerImpl implements OnItemClickListener {

        public OnItemClickListenerImpl() {
        }
        ArrayList<ChannelViewElement> array;

        private OnItemClickListenerImpl(ArrayList<ChannelViewElement> al) {
            array = al;
        }

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                long arg3) {
            final ChannelViewElement clickedChannel = array.get(arg2);
            new ExceptionLogger(FlActivity.this);
            if (clickedChannel != null) {
//                clickedChannel.setLastMessageTime(System.currentTimeMillis());
//                adapter.sort(new Comparator<ChannelViewElement>() {
//
//                    public int compare(ChannelViewElement t, ChannelViewElement t1) {
//                        return Long.compare(t1.getLastMessageTime(),t.getLastMessageTime());
//                        
//                    }
//                });
                Collections.sort(adapter.objects);
                adapter.notifyDataSetChanged();
                adapter.notifyDataSetInvalidated();
            }

            if (isImageAction) {
                //String str = imageAction.split("/")[imageAction.split("/").length - 1];
//                builder.setMessage("Do you want to send the picture " + str + " to " + clickedChannel.getName() + "?");
//                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//
//                    public void onClick(DialogInterface dialog, int which) {
//                        new Thread() {
//
//                            @Override
//                            public void run() {
//                                try {
//                                    Message msg = Message.obtain(null,
//                                            BS.SEND_PICTURE);
//                                    Bundle b = new Bundle();
//                                    b.putInt("chanid", clickedChannel.getId());
//                                    b.putString("filePath", imageAction);
//                                    msg.setData(b);
//                                    msg.replyTo = mMessenger;
//                                    mService.send(msg);
//                                    Intent intent;
//                                    intent = new Intent(FlActivity.this, ChatActivity.class);
//
//                                    intent.putExtra(
//                                            "title", clickedChannel.toString());
//                                    intent.putExtra(
//                                            "Channel", clickedChannel);
//                                    //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    isImageAction = false;
//                                    startActivity(intent);
//                                } catch (final Throwable e) {
//
//                                    new Thread() {
//
//                                        @Override
//                                        public void run() {
//                                            String ownStackTrace = ExceptionLogger.stacktrace2String(e);
//                                            Main.sendBroadCastMsg("could not send picture: \n" + ownStackTrace);
//
//                                            runOnUiThread(new Runnable() {
//
//                                                public void run() {
//                                                    Toast.makeText(FlActivity.this, "Could not send picture. Please restart the service.", Toast.LENGTH_SHORT).show();
//                                                }
//                                            });
//                                        }
//
//                                    }.start();
//
//                                }
//
//                                runOnUiThread(new Runnable() {
//
//                                    public void run() {
//                                        Toast.makeText(FlActivity.this, "send", Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//
//                                //Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
//                            }
//                        }.start();
//                    }
//                });
//                builder.setNegativeButton("No", null);
//                builder.show();
                //Toast.makeText(FlActivity.this, imageAction, Toast.LENGTH_SHORT).show();
                ChatActivity.sendPictureDialog(imageAction, FlActivity.this, clickedChannel, mMessenger, mService, true);
                isImageAction = false;
            } else if (isTextAction) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Send text");
                builder.setMessage("Do you want to share the text with " + clickedChannel.getName() + "?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        new Thread() {

                            @Override
                            public void run() {
                                try {
                                    Message msg = Message.obtain(null,
                                            BS.SEND_MSG);
                                    Bundle b = new Bundle();
                                    b.putInt("chanid", clickedChannel.getId());
                                    b.putString("msg", textAction);
                                    msg.setData(b);
                                    msg.replyTo = mMessenger;
                                    mService.send(msg);
                                    Intent intent;
                                    intent = new Intent(FlActivity.this, ChatActivity.class);

                                    intent.putExtra(
                                            "title", clickedChannel.toString());
                                    intent.putExtra(
                                            "Channel", clickedChannel);
                                    //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    isTextAction = false;
                                    startActivity(intent);
                                } catch (final Throwable e) {

                                    new Thread() {

                                        @Override
                                        public void run() {
                                            String ownStackTrace = ExceptionLogger.stacktrace2String(e);
                                            Main.sendBroadCastMsg("could not send text: \n" + ownStackTrace);

                                            runOnUiThread(new Runnable() {

                                                public void run() {
                                                    Toast.makeText(FlActivity.this, "Could not send text. Please restart the service.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                    }.start();

                                }

                            }
                        }.start();
                    }
                });
                builder.setNegativeButton("No", null);
                builder.show();

            } else {
//                Toast.makeText(FlActivity.this,
//                        "Clicked Channel " + clickedChannel.toString(), Toast.LENGTH_SHORT).show();

                Intent intent;
                intent = new Intent(FlActivity.this, ChatActivity.class);

                intent.putExtra(
                        "title", clickedChannel.toString());
                intent.putExtra(
                        "Channel", clickedChannel);
                //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent);
            }

        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.chanlist) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(adapter.objects.get(info.position).toString());
            String[] menuItems = {"Open", "Share", "Share by QR", "Edit", "Delete"};
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        final int pos = info.position;
        // String[] menuItems = {"Open", "Share", "Delete"};
        //String menuItemName = menuItems[menuItemIndex];
        switch (menuItemIndex) {
            case 0:
                Intent intent;
                intent
                        = new Intent(FlActivity.this, ChatActivity.class
                        );

                intent.putExtra(
                        "title", adapter.objects.get(pos).toString());
                intent.putExtra(
                        "Channel", adapter.objects.get(pos));
                startActivity(intent);
                break;
            case 3:
                Intent intent2 = new Intent(this, ChanPref.class);
                Bundle b = new Bundle();

                b.putSerializable(
                        "Channel", adapter.objects.get(pos));
                intent2.putExtras(b);

                startActivity(intent2);
                break;
            case 1:
//                Message msg = Message.obtain(null,
//                        BS.Send_MM);
//                Bundle bs = new Bundle();
//                bs.putString("msg", "Hier ist mein PrivateKey des Channels: " + channels.get(pos).toString() + "\n" + channels.get(pos).getPrivateKey());
//                msg.setData(bs);
//                msg.replyTo = mMessenger;
//                try {
//                    mService.send(msg);
//                } catch (RemoteException ex) {
//                    Logger.getLogger(FlActivity.class.getName()).log(Level.SEVERE, null, ex);
//                }
                ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

                cm.setText(adapter.objects.get(pos).exportForHumans());
                Toast.makeText(FlActivity.this,
                        "Copied PrivateKey to Clipboard", Toast.LENGTH_SHORT).show();

                break;
            case 4:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle(
                        "Delete Channel");
                builder.setMessage(
                        "Do you realy want to delete the Channel " + adapter.objects.get(pos).toString() + "?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        Main.removeChannel(adapter.objects.get(pos));

                        Message msg = Message.obtain(null,
                                BS.GET_CHANNELS);
                        msg.replyTo = mMessenger;
                        try {
                            mService.send(msg);
                        } catch (RemoteException ex) {
                            Logger.getLogger(FlActivity.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                );
                builder.setNegativeButton("No", null);
                builder.show();

                break;
            case 2:
                Intent inte;
                inte
                        = new Intent(FlActivity.this, QRCodeActivity.class
                        );
                inte.putExtra(
                        "title", adapter.objects.get(pos).toString());
                inte.putExtra(
                        "Key", adapter.objects.get(pos).exportForHumans());
                startActivity(inte);
                break;
            default:
                break;
        }
        return true;
    }
    /**
     * Messenger for communicating with service.
     */
    Messenger mService = null;
    /**
     * Flag indicating whether we have called bind on the service.
     */

    boolean mIsBound;

    class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BS.CHANNELS:
                    if (isFinishing()) {
                        return;
                    }

                    //Toast.makeText(FlActivity.this, "Channels sind da.", Toast.LENGTH_SHORT).show();
//                    new ExceptionLogger(FlActivity.this);
//                    Toast.makeText(FlActivity.this, "Channels sind da: " + channels.size(), Toast.LENGTH_SHORT).show();
                    ArrayList<ChannelViewElement> arrayList = new ArrayList<ChannelViewElement>();
                    ChannelViewElement cve;
                    for (Channel ch : (ArrayList<Channel>) msg.getData().get("CHANNELS")) {
                        cve = ChannelViewElement.getInstanceFromChannel(ch);
                        arrayList.add(cve);
                    }
                    channels = arrayList;

                    //Collections.sort(channels);
//                    Toast.makeText(FlActivity.this, "Channels sind da: " + channels.size(), Toast.LENGTH_SHORT).show();
                    if (!channels.isEmpty()) {
                        //ListView lv = (ListView) findViewById(R.id.chanlist);

//                    ArrayAdapter<Channel> adapter;
                        adapter.clear();
                        for (ChannelViewElement chan : channels) {
                            chan.resetPersistentData();
                            adapter.add(chan);
//                            Toast.makeText(FlActivity.this, "name: " + chan.toString(), Toast.LENGTH_SHORT).show();
                        }

//                        adapter.sort(new Comparator<ChannelViewElement>() {
//
//                            public int compare(ChannelViewElement t, ChannelViewElement t1) {
//                                return (int) (t1.getLastMessageTime() - t.getLastMessageTime());
//                            }
//                        });                      
                        Collections.sort(adapter.objects);
                        adapter.notifyDataSetChanged();
                        adapter.notifyDataSetInvalidated();
//                        Toast.makeText(FlActivity.this, "adapterbla", Toast.LENGTH_SHORT).show();
                    }

// Assign adapter to ListView
//                    lv.setAdapter(adapter);
//                    lv.setOnItemClickListener(new OnItemClickListenerImpl(channels));
                    break;
                case BS.FL_DSC:
//                    ChannelViewElement d = new ChannelViewElement();
//                    adapter.add(d);
//                    adapter.remove(d);
//                    adapter.notifyDataSetChanged();
                    // adapter.notifyDataSetInvalidated();

                    for (ChannelViewElement channel : channels) {
                        channel.resetPersistentData();
                    }

//                    adapter.sort(new Comparator<ChannelViewElement>() {
//
//                        public int compare(ChannelViewElement t, ChannelViewElement t1) {
//                            return (int) (t1.getLastMessageTime() - t.getLastMessageTime());
//                        }
//                    });
                    Collections.sort(adapter.objects);
                    adapter.notifyDataSetInvalidated();
                    adapter.notifyDataSetChanged();

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new FlActivity.IncomingHandler());
    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                IBinder service) {
//            Toast.makeText(FlActivity.this, "Serviceconnected", Toast.LENGTH_SHORT).show();

            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = new Messenger(service);

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null,
                        BS.FL_REG);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
            }

            try {
                Message msg = Message.obtain(null,
                        BS.GET_CHANNELS);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }
            importChannelfromIntent();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            doBindService();
        }
    };

    public static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null && mMemoryCache != null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public static Bitmap getBitmapFromMemCache(String key) {
        if (mMemoryCache != null) {
            return mMemoryCache.get(key);
        }
        return null;

    }

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.

        new Thread() {

            @Override
            public void run() {

                startService(new Intent(FlActivity.this, BS.class));

                while (true) {

                    if (Test.STARTED_UP_SUCCESSFUL) {
                        break;
                    }

                    try {
                        sleep(10);
                    } catch (InterruptedException ex) {
                    }

                }

                mIsBound = bindService(new Intent(FlActivity.this,
                        BS.class
                ), mConnection, Context.BIND_IMPORTANT);
            }

        }.start();

    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null,
                            BS.FL_UNREG);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
//                String[] a = {
//                    "http://i.imgur.com/Equm7wX.jpg", "http://i.imgur.com/KeCGnyX.jpg", "http://i.imgur.com/Rtayxtv.jpg", "http://i.imgur.com/VCigjPe.jpg", "http://i.imgur.com/BkrlFxl.jpg"
//                };
//                Random r = new Random();
//                String url = a[r.nextInt(a.length)];
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(url));
//                startActivity(i);
                if (!mDrawerLayout.isDrawerOpen(mDrawerList)) {
                    mDrawerLayout.openDrawer(mDrawerList);
                } else {
                    mDrawerLayout.closeDrawers();
                }
                return true;
//            case R.id.FL:
////                Intent intent = new Intent(this, FlActivity.class);
////                startActivity(intent);
//                return true;

//            case R.id.prefMenuButton:
//                Intent intent2 = new Intent(this, Preferences.class);
//                startActivity(intent2);
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Settings.connectToNewClientsTill = Long.MAX_VALUE;
        adapter.notifyDataSetInvalidated();
        active = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        active = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        active = true;

        doBindService();

        new Thread() {

            @Override
            public void run() {
                setPriority(Thread.MIN_PRIORITY);
                while (active) {

                    if (Test.peerList != null) {

                        int actCons = 0;
                        int connectingCons = 0;
                        final ArrayList<Peer> list = (ArrayList<Peer>) Test.peerList.clone();

                        for (Peer peer : list) {
                            if (peer.isConnected()) {
                                actCons++;
                            }
                            if (peer.isConnecting) {
                                connectingCons++;
                            }
                        }

                        int trustedIps = 0;
                        final ArrayList<PeerTrustData> clonedTrusts = (ArrayList<PeerTrustData>) Test.peerTrusts.clone();

                        for (PeerTrustData ptd : clonedTrusts) {
                            trustedIps += ptd.ips.size();
                        }

                        final int trustedIpsFinal = trustedIps;
                        final int activeConnections = actCons;
                        final int connectingConnections = connectingCons;

                        if (isFinishing()) {
                            return;
                        }
                        infotext.post(new Runnable() {

                            public void run() {
                                infotext.setText("Nodes: " + activeConnections + "/" + connectingConnections + "/" + list.size() + " - " + clonedTrusts.size() + " - " + trustedIpsFinal + ". Msgs: " + Test.messageStore.getMessageCount());
                            }
                        });
                    } else {
                        if (isFinishing()) {
                            return;
                        }
                        infotext.post(new Runnable() {

                            public void run() {
                                if (BS.hsqlConnection == null) {
                                    infotext.setText("loading database...");
                                } else {
                                    infotext.setText("loading...");
                                }
                            }
                        });
                    }
                    try {
                        sleep(100);
                    } catch (InterruptedException ex) {
                    }
                }

                if (isFinishing()) {
                    return;
                }
                infotext.post(new Runnable() {

                    public void run() {
                        infotext.setText("Nodes: -/-/-");
                    }
                });

            }
        }.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
        finish();
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            // Update UI to reflect image being shared
        }
    }

    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            // Update UI to reflect multiple images being shared
        }
    }

    public void importChannelfromIntent() {
        Intent intent1 = getIntent();
        if (intent1 != null) {
            if (intent1.getExtras() != null) {
                if (intent1.getExtras().getBoolean("newChannel") == true) {
                    String keystr = intent1.getExtras().getString("ChannelKey");
                    String namestr = intent1.getExtras().getString("ChannelName");
                    AlertDialog.Builder builder = new AlertDialog.Builder(FlActivity.this);
                    builder.setTitle("Import Channel");

//// Set up the input
                    final LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(R.layout.ippchandiag, null);

                    final EditText name = (EditText) ll.findViewById(R.id.channame);
                    final EditText key = (EditText) ll.findViewById(R.id.chankey);
                    name.setHintTextColor(Color.RED);
                    key.setHintTextColor(Color.CYAN);
                    key.setText(keystr);
                    name.setText(namestr);
//                final EditText input = new EditText(FlActivity.this);
//                
//// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
//                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                    builder.setView(ll);

// Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Message msg = Message.obtain(null,
                                    BS.ADD_CHANNEL);
                            Bundle b = new Bundle();
                            b.putString("name", name.getText().toString());
                            b.putString("key", key.getText().toString());
                            msg.setData(b);
                            msg.replyTo = mMessenger;
                            try {
                                mService.send(msg);

                            } catch (RemoteException ex) {
                                Logger.getLogger(FlActivity.class
                                        .getName()).log(Level.SEVERE, null, ex);
                            }
                            msg = Message.obtain(null,
                                    BS.GET_CHANNELS);
                            msg.replyTo = mMessenger;
                            try {
                                mService.send(msg);

                            } catch (RemoteException ex) {
                                Logger.getLogger(FlActivity.class
                                        .getName()).log(Level.SEVERE, null, ex);
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

//                    Message msg = Message.obtain(null,
//                            BS.ADD_CHANNEL);
//                    Bundle b = new Bundle();
//                    b.putString("name", intent1.getExtras().getString("ChannelName"));
//                    b.putString("key", intent1.getExtras().getString("ChannelKey"));
//                    msg.setData(b);
//                    msg.replyTo = mMessenger;
//                    try {
//                        mService.send(msg);
//                    } catch (RemoteException ex) {
//                        Logger.getLogger(FlActivity.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                    msg = Message.obtain(null,
//                            BS.GET_CHANNELS);
//                    msg.replyTo = mMessenger;
//                    try {
//                        mService.send(msg);
//                    } catch (RemoteException ex) {
//                        Logger.getLogger(FlActivity.class.getName()).log(Level.SEVERE, null, ex);
//                    }
                }
            }
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            // your action...

            if (!mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.openDrawer(mDrawerList);
            } else {
                mDrawerLayout.closeDrawers();
            }
            return true;
        }
        return super.onKeyDown(keyCode, e);
    }

    public String getImagePath(Uri uri) {
        // just some safety built in 
        if (uri == null) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }

}
