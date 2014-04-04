/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda.ChannelList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    ListView lv;

    public void onCreate(Bundle savedInstanceState) {
        context = this;
        new ExceptionLogger(this);

        //Settings.connectToNewClientsTill = System.currentTimeMillis() + 1000*60*5;
        super.onCreate(savedInstanceState);
        adapter = new FLAdapter(this, R.layout.listitem, channels);
        startService(new Intent(this, BS.class));
//        Intent intent = new Intent(this, BS.class);
//        startService(intent);
//        Toast.makeText(FlActivity.this, "doBindService", Toast.LENGTH_SHORT).show();
        setContentView(R.layout.fl);

        Button newChButton = (Button) findViewById(R.id.NKButton);
        infotext = (TextView) findViewById(R.id.infotext);
        infotext.setTextColor(Color.BLUE);
        newChButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                AlertDialog.Builder builder = new AlertDialog.Builder(FlActivity.this);
                builder.setTitle("Erstelle neuen Channel");

//// Set up the input
                final EditText input = new EditText(FlActivity.this);
//                
//// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
                input.setHint("Neuer Channel Name");
                input.setHintTextColor(Color.RED);

                builder.setView(input);

// Set up the buttons
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

            }
        });
        Button impButton = (Button) findViewById(R.id.imbutton);
        impButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                AlertDialog.Builder builder = new AlertDialog.Builder(FlActivity.this);
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

            }
        });

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

        doBindService();
        this.registerForContextMenu(lv);
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
            ChannelViewElement clickedChannel = array.get(arg2);
            new ExceptionLogger(FlActivity.this);
            if (clickedChannel != null) {
//                clickedChannel.setLastMessageTime(System.currentTimeMillis());
                adapter.sort(new Comparator<ChannelViewElement>() {
                    public int compare(ChannelViewElement t, ChannelViewElement t1) {
                        return (int) (t1.getLastMessageTime() - t.getLastMessageTime());
                    }
                });
                adapter.notifyDataSetChanged();
            }

            Intent intent;
            intent = new Intent(FlActivity.this, ChatActivity.class);

            intent.putExtra("title", clickedChannel.toString());
            intent.putExtra("Channel", clickedChannel);
            //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);

        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.chanlist) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(channels.get(info.position).toString());
            String[] menuItems = {"Open", "Share", "Edit", "Delete"};
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        int pos = info.position;
        // String[] menuItems = {"Open", "Share", "Delete"};
        //String menuItemName = menuItems[menuItemIndex];
        switch (menuItemIndex) {
            case 0:
                Intent intent;
                intent = new Intent(FlActivity.this, ChatActivity.class);

                intent.putExtra("title", channels.get(pos).toString());
                intent.putExtra("Channel", channels.get(pos));
                startActivity(intent);
                break;
            case 2:
                Intent intent2 = new Intent(this, ChanPref.class);
                Bundle b = new Bundle();
                b.putSerializable("Channel", channels.get(pos));
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
                cm.setText(channels.get(pos).exportForHumans());
                Toast.makeText(FlActivity.this, "Copied PrivateKey to Clipboard", Toast.LENGTH_SHORT).show();

                break;
            case 3:
                Main.removeChannel(channels.get(pos));

                Message msg = Message.obtain(null,
                        BS.GET_CHANNELS);
                msg.replyTo = mMessenger;
                try {
                    mService.send(msg);
                } catch (RemoteException ex) {
                    Logger.getLogger(FlActivity.class.getName()).log(Level.SEVERE, null, ex);
                }
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
                            adapter.add(chan);
//                            Toast.makeText(FlActivity.this, "name: " + chan.toString(), Toast.LENGTH_SHORT).show();
                        }
                        adapter.sort(new Comparator<ChannelViewElement>() {
                            public int compare(ChannelViewElement t, ChannelViewElement t1) {
                                return (int) (t1.getLastMessageTime() - t.getLastMessageTime());
                            }
                        });
                        adapter.notifyDataSetChanged();
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
        mIsBound = bindService(new Intent(FlActivity.this,
                BS.class), mConnection, Context.BIND_AUTO_CREATE);

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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.FL:
//                Intent intent = new Intent(this, FlActivity.class);
//                startActivity(intent);
//                return true;
            case R.id.prefMenuButton:
                Intent intent2 = new Intent(this, Preferences.class);
                startActivity(intent2);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Settings.connectToNewClientsTill = Long.MAX_VALUE;
        adapter.notifyDataSetChanged();
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

        new Thread() {
            @Override
            public void run() {
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

                        final int activeConnections = actCons;
                        final int connectingConnections = connectingCons;

                        infotext.post(new Runnable() {
                            public void run() {
                                infotext.setText("Nodes: " + activeConnections + "/" + connectingConnections + "/" + list.size());
                            }
                        });
                    } else {
                        infotext.post(new Runnable() {
                            public void run() {
                                infotext.setText("loading...");
                            }
                        });
                    }
                    try {
                        sleep(100);
                    } catch (InterruptedException ex) {
                    }
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
}
