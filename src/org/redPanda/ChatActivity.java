/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import org.redPanda.ListMessage.Mes;

import org.redPandaLib.core.Channel;
import org.redPandaLib.core.messages.DeliveredMsg;
import org.redPandaLib.core.messages.TextMessageContent;

/**
 *
 * @author mflohr
 */
public class ChatActivity extends ListActivity {

    private TextView conversationText;
    //  private ListView listView;
    private EditText editText;
    private Channel chan;
    private long lastTouched = 0;
    private ArrayList<ListMessage> messages;
    private ChatAdapter cA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new ExceptionLogger(this);
        Intent in = getIntent();
        this.setTitle(in.getExtras().getString("title"));
        chan = (Channel) in.getExtras().get("Channel");

        setContentView(R.layout.chatlayout);
        Intent intent = new Intent(this, BS.class);
        startService(intent);
        doBindService();
        final TextView mainLayouthead = (TextView) findViewById(R.id.mainLayouthead);
        LayoutInflater.from(this).inflate(R.layout.chatrow, null);
        // conversationText = (TextView) findViewById(R.id.message_text);
        //scrollView = (ScrollView) findViewById(R.id.mainScrollView);
        // listView = (ListView) findViewById(R.id.chatlist);
        editText = (EditText) findViewById(R.id.mainEditText);
        messages = new ArrayList<ListMessage>();
        cA = new ChatAdapter(this, messages);
        setListAdapter(cA);
//        scrollView.setOnTouchListener(new View.OnTouchListener() {
//            public boolean onTouch(View arg0, MotionEvent e) {
//
//                if (e.getAction() != MotionEvent.ACTION_DOWN) {
//                    return false;
//                }
//
//                if (System.currentTimeMillis() - lastTouched < 250) {
//                    scrollView.post(new Runnable() {
//                        public void run() {
//                            scrollView.fullScroll(View.FOCUS_DOWN);
//                        }
//                    });
//
//                    lastTouched = 0;
//                    return true;
//                }
//
//                lastTouched = System.currentTimeMillis();
//
//                return false;
//            }
//        });

        Button button = (Button) findViewById(R.id.mainSendButton);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {

                final String text = editText.getText().toString();
                Runnable runnable = new Runnable() {

                    public void run() {
                        editText.setText("");
                    }
                };

                editText.post(runnable);


                try {
                    Message msg = Message.obtain(null,
                            BS.SEND_MSG);
                    Bundle b = new Bundle();
                    b.putInt("chanid", chan.getId());
                    b.putString("msg", text);
                    msg.setData(b);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                }


            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
    /**
     * Messenger for communicating with service.
     */
    Messenger mService = null;
    /**
     * Flag indicating whether we have called bind on the service.
     */
    boolean mIsBound;

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {

        @Override
        public void handleMessage(final Message msg) {
            ArrayList<TextMessageContent> al;
            switch (msg.what) {
                case BS.NEW_MSG:
                    // final String str = genReadableText(msg);
                    //Toast.makeText(MA.this, "gotnewmsg: \n" + str, Toast.LENGTH_SHORT).show();
                    //  long newid = msg.getData().getLong("id");

                    //maltes altes
                    //al = (ArrayList<TextMessageContent>) msg.getData().getSerializable("msg");
                    //merge(al.get(0));

                    //von robin
                    TextMessageContent t = (TextMessageContent) msg.getData().getSerializable("msg");
                    merge(t);
                    //von robin ende

                    cA.notifyDataSetChanged();

                    if (t.message_type != DeliveredMsg.BYTE) {
                        getListView().setSelection(cA.mMessages.size() - 1);
                    }


                    //  System.out.println( "12345 "+genReadableText(msg));                   


                    break;
                case BS.NEW_MSGL:
                    cA.mMessages = new ArrayList<ListMessage>();
                    al = (ArrayList<TextMessageContent>) msg.getData().getSerializable("msgList");
                    Iterator<TextMessageContent> it = al.iterator();
                    while (it.hasNext()) {
                        merge(it.next());
                    }
                    cA.notifyDataSetChanged();
                    getListView().setSelection(cA.mMessages.size() - 1);
                    System.out.println("12345 " + cA.mMessages.size());
                    break;


                default:
                    super.handleMessage(msg);
            }
        }

        public void merge(TextMessageContent tmc) {

            if (tmc.message_type == DeliveredMsg.BYTE) {

                if (cA.mMessages == null || tmc.decryptedContent.length != 1 + 8 + 1 + 8 + 4) {
                    System.out.println("delivered msg wrong bytes.... " + tmc.decryptedContent.length);
                    return;
                }

                System.out.println("bztes: " + tmc.decryptedContent.length);

                ByteBuffer wrap = ByteBuffer.wrap(tmc.decryptedContent);

                //delivered msg points to a message, getting infos from that message
                wrap.get();//skip message_type
                long identity = wrap.getLong();
                byte public_type = wrap.get();
                long time = wrap.getLong();
                int nonce = wrap.getInt();

                tmc.identity = identity;

                System.out.println("asdhhg " + time + " ");



                for (ListMessage listMessage : cA.mMessages) {

                    for (Mes message : listMessage.text) {
                        System.out.println(time + "          / ////      " + message.ts);
                        if (time == message.ts) {
                            System.out.println("right!");

                            if (message.deliveredTo == null) {
                                message.deliveredTo = new ArrayList<String>();
                            }

                            message.deliveredTo.add(tmc.getName());


                            return;
                        } else {
                            System.out.println("wrong!");
                        }
                    }

                }

                return;
            }

            ListMessage lm;
            if (cA.mMessages == null || cA.mMessages.size() == 0) {
                cA.mMessages = new ArrayList<ListMessage>();
                lm = new ListMessage(tmc);

            } else {
                lm = cA.mMessages.get(cA.mMessages.size() - 1);
                if (tmc.getIdentity() == lm.identity && false) {
                    Mes mes = new Mes(tmc.database_id, tmc.timestamp, tmc.text, tmc.fromMe, tmc.message_type);
                    lm.text.add(mes);
                } else {
                    lm = new ListMessage(tmc);
                }
            }
            cA.mMessages.add(lm);
        }
    }
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new ChatActivity.IncomingHandler());
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
            try {
                Message msg = Message.obtain(null,
                        BS.MSG_REGISTER_CLIENT);
                Bundle b = new Bundle();
                b.putInt("chanid", chan.getId());
                msg.setData(b);
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
        mIsBound = bindService(new Intent(ChatActivity.this,
                BS.class), mConnection, Context.BIND_AUTO_CREATE);

    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null,
                            BS.MSG_UNREGISTER_CLIENT);
                    Bundle b = new Bundle();
                    b.putInt("chanid", chan.getId());
                    msg.setData(b);
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

    public static String genReadableText(Message msg) {
        long sendTime = msg.getData().getLong("sendtime");
        String str = msg.getData().getString("msg");
        Date date = new Date(sendTime);

        String out = "";

        out += formatTime(date) + ": " + str;



        return out + "\n";
    }

    public static String formatTime(Date date) {

        String hours = "" + date.getHours();
        String minutes = "" + date.getMinutes();
        String seconds = "" + date.getSeconds();

        if (hours.length() == 1) {
            hours = "0" + hours;
        }
        if (minutes.length() == 1) {
            minutes = "0" + minutes;
        }
        if (seconds.length() == 1) {
            seconds = "0" + seconds;
        }

        return hours + ":" + minutes + ":" + seconds;


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (chan != null) {
            BS.currentViewedChannel = chan.getId();
            NotificationManager mNotificationManager =
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(chan.getId());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        BS.currentViewedChannel = -100;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
//            startActivity(new Intent(this, FlActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
//            finish();
//            return true;
//        }
//        return false;
//    }
}
