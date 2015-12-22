/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import android.app.ActionBar;
import android.app.Activity;
import static android.app.Activity.RESULT_OK;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import static android.content.Context.CLIPBOARD_SERVICE;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.RemoteInput;
import android.text.ClipboardManager;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;
import static java.lang.Thread.sleep;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.redPanda.ChannelList.ChanPref;
import org.redPanda.ChannelList.FlActivity;
import org.redPanda.ChannelList.QRCodeActivity;

import org.redPandaLib.Main;
import org.redPandaLib.core.Blocks;
import org.redPandaLib.core.Channel;
import org.redPandaLib.core.Settings;
import org.redPandaLib.core.Test;
import org.redPandaLib.core.messages.DeliveredMsg;
import org.redPandaLib.core.messages.ImageMsg;
import org.redPandaLib.core.messages.TextMessageContent;

/**
 *
 * @author mflohr
 */
public class ChatActivity extends FragmentActivity implements EmojiconGridFragment.OnEmojiconClickedListener, EmojiconsFragment.OnEmojiconBackspaceClickedListener {

    private TextView conversationText;
    private ListView listView;
    private EditText editText;
    private Channel chan;
    private long lastTouched = 0;
    private ArrayList<ChatMsg> messages;
    private ChatAdapter cA;
    public static final int MENU_IMAGE = Menu.FIRST;
    private static final int SELECT_PHOTO = 100;
    private static final int CAPTURE_PHOTO = 101;
    private boolean emojiconKeyboardVisible = false;
    private EmojiconsFragment emojiconsFragment;
    private LinearLayout mainLayoutInputAndSend;
    private boolean hasUnreadMesDev = false;
    private int jumpTo = -1;
    private Message toSendMessage = null;
    private Menu menu;
    private ContentResolver contentResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ExceptionLogger(this);

        Intent in = getIntent();
        String string = in.getExtras().getString("title");

        if (string == null) {
            Toast.makeText(ChatActivity.this, "oops, no channel defined...", Toast.LENGTH_LONG).show();
            Intent intent;
            intent = new Intent(ChatActivity.this, FlActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        this.setTitle(string);
        chan = (Channel) in.getExtras().get("Channel");
        setContentView(R.layout.chatlayout);

//        getWindow().getDecorView().setBackgroundColor(Color.LTGRAY);
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        Intent intent = new Intent(this, BS.class);
        //startService(intent);
        doBindService();
        final TextView mainLayouthead = (TextView) findViewById(R.id.mainLayouthead);
        LayoutInflater.from(this).inflate(R.layout.chatrow, null);
        // conversationText = (TextView) findViewById(R.id.message_text);
        //scrollView = (ScrollView) findViewById(R.id.mainScrollView);
        // listView = (ListView) findViewById(R.id.chatlist);
        editText = (EditText) findViewById(R.id.mainEditText);
        //   editText.setScrollContainer(true);
        messages = new ArrayList<ChatMsg>();
        cA = new ChatAdapter(this, messages);

        listView = (ListView) findViewById(R.id.chatlayout_bubblelist);
        listView.setAdapter(cA);

        mainLayoutInputAndSend = (LinearLayout) findViewById(R.id.mainLayoutInputAndSend);
        if (chan.isWriteable()) {
            mainLayoutInputAndSend.setVisibility(View.VISIBLE);
        } else {
            mainLayoutInputAndSend.setVisibility(View.GONE);
        }

        //hide smiley keyboard at beginning
        emojiconsFragment = (EmojiconsFragment) getSupportFragmentManager().findFragmentById(R.id.emojicons);
        FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
        tr.hide(emojiconsFragment);
        tr.commit();

        ImageButton toogleEmojiconKeyboard = (ImageButton) findViewById(R.id.emojiconEnableButton);
        toogleEmojiconKeyboard.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {

                FragmentTransaction tr = getSupportFragmentManager().beginTransaction();

                InputMethodManager imm = (InputMethodManager) ChatActivity.this.getSystemService(Service.INPUT_METHOD_SERVICE);

                if (emojiconKeyboardVisible) {
                    RelativeLayout.LayoutParams lpEmo = (RelativeLayout.LayoutParams) emojiconsFragment.getView().getLayoutParams();
                    //lpEmo.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    lpEmo.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);

                    RelativeLayout.LayoutParams lpLin = (RelativeLayout.LayoutParams) mainLayoutInputAndSend.getLayoutParams();
                    lpLin.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    tr.hide(emojiconsFragment);
                    imm.showSoftInput(editText, 0);
                } else {
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) emojiconsFragment.getView().getLayoutParams();
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

                    RelativeLayout.LayoutParams lpLin = (RelativeLayout.LayoutParams) mainLayoutInputAndSend.getLayoutParams();
                    //lpLin.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    lpLin.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
                    tr.show(emojiconsFragment);

                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }

                tr.commit();

                emojiconKeyboardVisible = !emojiconKeyboardVisible;

            }
        });

//        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//
//            public void onFocusChange(View v, boolean hasFocus) {
//
//                InputMethodManager imm = (InputMethodManager) ChatActivity.this.getSystemService(Service.INPUT_METHOD_SERVICE);
//
//                if (emojiconKeyboardVisible) {
//                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
//                }
//
//            }
//        });
        editText.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (emojiconKeyboardVisible) {
                    FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
                    RelativeLayout.LayoutParams lpEmo = (RelativeLayout.LayoutParams) emojiconsFragment.getView().getLayoutParams();
                    //lpEmo.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    lpEmo.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);

                    RelativeLayout.LayoutParams lpLin = (RelativeLayout.LayoutParams) mainLayoutInputAndSend.getLayoutParams();
                    lpLin.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    tr.hide(emojiconsFragment);
                    tr.commit();
                    emojiconKeyboardVisible = false;

                }
                return false;
            }
        });

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
        ImageButton button = (ImageButton) findViewById(R.id.mainSendButton);

        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {

                sendMessageFromTextView();

            }

        });

        lookForMessageToSend();
        if (savedInstanceState != null) {
            hasPhotoToSend = savedInstanceState.getBoolean("hasPhotoToSend");
            photoToSend = savedInstanceState.getString("photoToSend");
            imageSavePath = savedInstanceState.getString("imageSavePath");
            if (hasPhotoToSend) {
                if (mService != null) {
                    sendPictureDialog(photoToSend, this, chan, mMessenger, mService, false);
                }
            }
        }

        contentResolver = getContentResolver();
    }

    private void lookForMessageToSend() {
        getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.red_bg));
////        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
////        ActionBar abar = getActionBar();
////        abar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#80000000")));
        //look for voice-to-text message from wearable...
        CharSequence messageText = getMessageText(getIntent());
        if (messageText != null) {

            try {
                Message msg = Message.obtain(null,
                        BS.SEND_MSG);
                Bundle b = new Bundle();
                b.putInt("chanid", chan.getId());
                b.putString("msg", messageText.toString());
                msg.setData(b);
                msg.replyTo = mMessenger;
                toSendMessage = msg;
            } catch (final Throwable e) {

                new Thread() {

                    @Override
                    public void run() {
                        String ownStackTrace = ExceptionLogger.stacktrace2String(e);
                        Main.sendBroadCastMsg("could not send message: \n" + ownStackTrace);

                        runOnUiThread(new Runnable() {

                            public void run() {
                                Toast.makeText(ChatActivity.this, getString(R.string.could_not_send_text_please_restart_the_service), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }.start();

            }
            return;
        }
        Intent intent = getIntent();
        String filePath = intent.getExtras().getString("filePath");
        if (filePath != null) {// picture to send
            boolean lowPriority = intent.getExtras().getBoolean("lowPriority");
            Message msg = Message.obtain(null,
                    BS.SEND_PICTURE);
            Bundle b = new Bundle();
            b.putInt("chanid", chan.getId());
            b.putString("filePath", filePath);
            b.putBoolean("lowPriority", lowPriority);
            msg.setData(b);
            msg.replyTo = mMessenger;

            toSendMessage = msg;

        }
        String textMsg = intent.getExtras().getString("textMsg");
        if (textMsg != null) {
            Message msg = Message.obtain(null,
                    BS.SEND_MSG);
            Bundle b = new Bundle();
            b.putInt("chanid", chan.getId());
            b.putString("msg", textMsg);
            msg.setData(b);
            msg.replyTo = mMessenger;

            toSendMessage = msg;

        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //we need the old channel for unbind!
        doUnbindService();
        if (cA != null && cA.mMessages != null) {
            cA.mMessages.clear();
            cA.notifyDataSetChanged();
        }
        this.setIntent(intent);
        this.setTitle(intent.getExtras().getString("title"));
        chan = (Channel) intent.getExtras().get("Channel");
        InputMethodManager imm = (InputMethodManager) ChatActivity.this.getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        if (chan.isWriteable()) {
            mainLayoutInputAndSend.setVisibility(View.VISIBLE);
            lookForMessageToSend();
        } else {
            mainLayoutInputAndSend.setVisibility(View.GONE);
        }

        //we can binde again to the service
        doBindService();
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

    private void backToFlActivity() {
        //super.onBackPressed();
        Intent intent;
        intent = new Intent(ChatActivity.this, FlActivity.class);
        //TODO look at flags
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(editText, emojicon);
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(editText);
    }

    class MergeTask extends AsyncTask<Integer, Void, ArrayList<ChatMsg>> {

        public MergeTask(ImageView imageView, String path, int scale, int width, int height) {
        }

        @Override
        protected ArrayList<ChatMsg> doInBackground(Integer... params) {
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<ChatMsg> al) {
        }
    }

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
                    merge(t, false);
                    //von robin ende

                    cA.notifyDataSetChanged();

//                    if (t.message_type != DeliveredMsg.BYTE) {
//                        getListView().setSelection(cA.mMessages.size() - 1);
//                    }
                    //  System.out.println( "12345 "+genReadableText(msg));                   
                    break;
                case BS.NEW_MSGL:
                    cA.mMessages = new ArrayList<ChatMsg>();
                    al = (ArrayList<TextMessageContent>) msg.getData().getSerializable("msgList");
                    Iterator<TextMessageContent> it = al.iterator();
                    while (it.hasNext()) {
                        merge(it.next(), true);
                    }
                    cA.notifyDataSetChanged();
                    //getListView().setSelection(cA.mMessages.size() - 1);
                    //System.out.println("12345 " + cA.mMessages.size());
                    if (jumpTo != -1) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                listView.setSelection(jumpTo);
                            }
                        });
                        jumpTo = -1;
                    }
                    break;

                default:
                    super.handleMessage(msg);
            }
        }

        public void merge(final TextMessageContent tmc, boolean initalMessage) {
//TODO do the merge in Background
            if (tmc.message_type == DeliveredMsg.BYTE) {

                if (cA.mMessages == null || tmc.decryptedContent.length != 1 + 8 + 1 + 8 + 4) {
                    new Thread() {

                        @Override
                        public void run() {
                            Main.sendBroadCastMsg("delivered msg wrong bytes.... " + tmc.decryptedContent.length);
                        }
                    }.start();
                    return;
                }

                ByteBuffer wrap = ByteBuffer.wrap(tmc.decryptedContent);

                //delivered msg points to a message, getting infos from that message
                wrap.get();//skip message_type
                long identity = wrap.getLong();
                byte public_type = wrap.get();
                long timestamp = wrap.getLong();
                int nonce = wrap.getInt();

                tmc.identity = identity;

                boolean found = false;
                //String hans = "";
                String deliveredTo = tmc.getName();
                for (ChatMsg cM : cA.mMessages) {

                    //hans += " " + message.ts;
                    //todo: wird nur timestamp überprüft
                    if (timestamp == cM.getTimestamp()) {
                        deliveredTo = cM.getDeliverdTo();
                        found = true;

                        if (deliveredTo.equals("")) {
                            deliveredTo = tmc.getName();
                        } else {
                            deliveredTo += " " + tmc.getName();
                        }
                        cM.setDeliverdTo(deliveredTo);
                        return;
                    } else {
                    }
                }
//                }

                //final String hhans = hans;
//                if (!found) {
//                    new Thread() {
//
//                        @Override
//                        public void run() {
//                            Main.sendBroadCastMsg("not found -.- " + tmc.timestamp + " " + tmc.getName() + hhans);
//                        }
//                    }.start();
//                }
                return;
            }

            ChatMsg cM;
            if (cA.mMessages == null || cA.mMessages.isEmpty()) {
                cA.mMessages = new ArrayList<ChatMsg>();
            } else {
                //  cM = cA.mMessages.get(cA.mMessages.size() - 1);
                //nicht angepasst!!!
//                if (tmc.getIdentity() == cM.getIdentity() && false) {
//                    Mes mes = new Mes(tmc.database_id, tmc.timestamp, tmc.text, tmc.fromMe, tmc.message_type);
//                    //lm.text.add(mes);
//                    cA.mMessages.remove(cA.mMessages.size() - 1);
//                } else {
//                    cM = new ListMessage(tmc);
//                }
            }

            if (tmc.message_type == ImageMsg.BYTE) {

                String[] tmp = tmc.text.split("\n");
                if (tmp.length == 3) {
                    double width = Integer.parseInt(tmp[1]);
                    double height = Integer.parseInt(tmp[2]);
                    int scale = 1;
//                                while (width / 2 > ChatAdapter.imageMaxSize) {
//                                    width = width / 2;
//                                    height = height / 2;
//                                    scale *= 2;
//                                }
                    double tmpdouble = ChatAdapter.imageMaxSize * 0.6;
                    int reqWidth = (int) tmpdouble;

                    if (width > reqWidth) {
                        final double ratio = width / reqWidth;

                        height = height * 1 / ratio;
                        width = tmpdouble;
                        scale = (int) Math.round(ratio);
                    }
                    tmc.text = tmp[0] + "\n" + (int) width + "\n" + (int) height + "\n" + scale;
                }

            }

            Date date = new Date(tmc.getTimestamp());
            String time = ChatAdapter.formatTime(date, false);

            cM = new ChatMsg(tmc.getText(), time, tmc.identity, tmc.getTimestamp(), tmc.fromMe, tmc.message_type, tmc.read, tmc.database_id);
            ChatMsg oCM;
            if (cA.mMessages.isEmpty()) { // Daydivider
                TextMessageContent tmptmc2 = new TextMessageContent();
                tmptmc2.text = "";
                tmptmc2.message_type = ChatAdapter.emptyView;
                cA.mMessages.add(new ChatMsg(tmptmc2));

                TextMessageContent tmptmc = new TextMessageContent();
                tmptmc.text = ChatAdapter.formatTime(new Date(tmc.timestamp), true);
                tmptmc.message_type = ChatAdapter.dayDivider;
                cA.mMessages.add(new ChatMsg(tmptmc));
            } else {
                oCM = cA.mMessages.get(cA.mMessages.size() - 1);
                String d, oD;
                d = ChatAdapter.formatTime(new Date(tmc.timestamp), true);
                oD = ChatAdapter.formatTime(new Date(oCM.getTimestamp()), true);
                if (initalMessage && tmc.read != oCM.isRead() && !hasUnreadMesDev) {
                    hasUnreadMesDev = true;
                    TextMessageContent tmptmc = new TextMessageContent();
                    tmptmc.text = getString(R.string.unread_messages);
                    tmptmc.message_type = ChatAdapter.unreadMesDivider;
                    tmptmc.read = true;
                    cA.mMessages.add(new ChatMsg(tmptmc));
                    jumpTo = cA.mMessages.size() - 1;
                }
                if (!d.equals(oD)) {
                    TextMessageContent tmptmc = new TextMessageContent();
                    tmptmc.text = ChatAdapter.formatTime(new Date(tmc.timestamp), true);
                    tmptmc.message_type = ChatAdapter.dayDivider;
                    tmptmc.read = true;
                    cA.mMessages.add(new ChatMsg(tmptmc));
                }
            }
            cA.mMessages.add(cM);

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
            if (hasPhotoToSend) {
                sendPictureDialog(photoToSend, ChatActivity.this, chan, mMessenger, mService, false);
            }

            // We want to monitor the service for as long as we are
            // connected to it.
            new Thread() {

                @Override
                public void run() {
                    Message msg = Message.obtain(null,
                            BS.MSG_REGISTER_CLIENT);
                    Bundle b = new Bundle();
                    b.putInt("chanid", chan.getId());//ToDoE: NullPointer, chan null?!?
                    msg.setData(b);
                    msg.replyTo = mMessenger;
                    try {
                        mService.send(msg);
                    } catch (RemoteException e) {
                        // In this case the service has crashed before we could even
                        // do anything with it; we can count on soon being
                        // disconnected (and then reconnected if it can be restarted)
                        // so there is no need to do anything here.
                    }

                    if (toSendMessage != null) {
                        try {
                            mService.send(toSendMessage);
                            toSendMessage = null;
                            Intent intent = getIntent();
                            String tmp = null;
                            intent.putExtra("filePath", tmp);
                        } catch (RemoteException e) {
                        }
                    }

                }
            }.start();

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

        new Thread() {

            @Override
            public void run() {

                startService(new Intent(ChatActivity.this, BS.class));

                while (true) {

                    if (Test.STARTED_UP_SUCCESSFUL) {
                        break;
                    }

                    try {
                        sleep(10);
                    } catch (InterruptedException ex) {
                    }

                }

                mIsBound = bindService(new Intent(ChatActivity.this,
                        BS.class), mConnection, Context.BIND_IMPORTANT);
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
                            BS.MSG_UNREGISTER_CLIENT);
                    Bundle b = new Bundle();
                    b.putInt("chanid", chan.getId()); //ToDoE: chan null pointer -.-
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
        if (emojiconKeyboardVisible) {
            FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
            RelativeLayout.LayoutParams lpEmo = (RelativeLayout.LayoutParams) emojiconsFragment.getView().getLayoutParams();
            //lpEmo.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lpEmo.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);

            RelativeLayout.LayoutParams lpLin = (RelativeLayout.LayoutParams) mainLayoutInputAndSend.getLayoutParams();
            lpLin.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            tr.hide(emojiconsFragment);
            tr.commit();
            emojiconKeyboardVisible = false;
        } else if (editText != null) {
            InputMethodManager imm = (InputMethodManager) ChatActivity.this.getSystemService(Service.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
        if (chan != null) {
            BS.currentViewedChannel = chan.getId();
            NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(chan.getId());
        }

        Settings.connectToNewClientsTill = Long.MAX_VALUE;

    }

    @Override
    protected void onPause() {
        super.onPause();
        BS.currentViewedChannel = -100;
    }

    //    @Override
    //    public boolean onKeyDown(int keyCode, KeyEvent event) {
    //        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
    //            startActivity(new Intent(this, FlActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    //            finish();
    //            return true;
    //        }
    //    }
    //    }
    /**
     * Clears all activitys and starts the FlActivity
     */
    @Override
    public void onBackPressed() {

        if (emojiconKeyboardVisible) {
            FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
            RelativeLayout.LayoutParams lpEmo = (RelativeLayout.LayoutParams) emojiconsFragment.getView().getLayoutParams();
            //lpEmo.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lpEmo.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            RelativeLayout.LayoutParams lpLin = (RelativeLayout.LayoutParams) mainLayoutInputAndSend.getLayoutParams();
            lpLin.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            tr.hide(emojiconsFragment);

            //mainLayoutInputAndSend.invalidate();
            tr.commit();

            emojiconKeyboardVisible = false;
            return;
        }

        backToFlActivity();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                backToFlActivity();
                finish();
                return true;
            case R.id.openCameraButtonFromChat:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                imageSavePath = BS.getAlbumStorageDir("redPanda/full res").getAbsolutePath()
                        + "/JPEG_" + timeStamp + ".jpg";
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse("file:" + imageSavePath));
                startActivityForResult(intent, CAPTURE_PHOTO);
                return true;
            case R.id.imageSendButtonFromChat:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                return true;

            case R.id.cm_QR://Share by QR
                Intent inte;
                inte = new Intent(ChatActivity.this, QRCodeActivity.class);
                inte.putExtra("title", chan.getName());
                inte.putExtra("Key", chan.exportForHumans());
                startActivity(inte);
                return true;
            case R.id.cm_Share://Share
                ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

                cm.setText(chan.exportForHumans());
                Toast.makeText(ChatActivity.this,
                        getString(R.string.copied_privatekey_to_clipboard), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.cm_Ed://edit
                Intent intent2 = new Intent(this, ChanPref.class);
                Bundle b = new Bundle();

                b.putSerializable(
                        "Channel", chan);
                intent2.putExtras(b);

                startActivity(intent2);
                return true;
            case R.id.cm_generateBlock:
                Blocks.generate(chan);
                return true;
            case R.id.cm_Del://Delete Messages

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle(getString(R.string.confirm));
                builder.setMessage(getString(R.string.are_you_sure_you_want_to_delete_all_messages_in_this_channel));

                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ChatActivity.this);
                        SharedPreferences.Editor edit = sharedPref.edit();
                        edit.putString("lastMessageTextForChannelid" + chan.getId(), "0");
                        edit.commit();

                        new Runnable() {

                            public void run() {
                                cA.mMessages.clear();
                                cA.notifyDataSetChanged();
                                Main.removeMessagesForChannel(chan);
                            }
                        }.run();

                        //cA.notifyDataSetInvalidated();
                        dialog.dismiss();
                    }

                });

                builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//
//        menu.add(Menu.NONE, MENU_IMAGE, Menu.NONE, "Image");
//
//        return true;
//    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        menu.setGroupVisible(R.id.menu_group_writeable, chan.isWriteable());
        return true;
    }

//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        MenuItem it = menu.getItem(1);
//
//        return super.onPrepareOptionsMenu(menu); //To change body of generated methods, choose Tools | Templates.
//    }
    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        return super.dispatchGenericMotionEvent(ev); //To change body of generated methods, choose Tools | Templates.
    }

    public static boolean hasPhotoToSend = false;
    public static String photoToSend = "";

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("hasPhotoToSend", hasPhotoToSend);
        outState.putString("photoToSend", photoToSend);
        outState.putString("imageSavePath", imageSavePath);

    }

    String imageSavePath = "";

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
            final Intent imageReturnedIntent) {
        String filePath = imageSavePath;
        imageSavePath = "";
        if (requestCode == SELECT_PHOTO) {

            if (resultCode != RESULT_OK) {
                //no image selected...
                runOnUiThread(new Runnable() {

                    public void run() {
                        Toast.makeText(ChatActivity.this, getString(R.string.no_image_selected), Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }

            Uri selectedImage = imageReturnedIntent.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = contentResolver.query(
                    selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();

            Toast.makeText(ChatActivity.this, filePath, Toast.LENGTH_SHORT).show();
            photoToSend = filePath;
            hasPhotoToSend = true;
            if (mService != null) {
                sendPictureDialog(photoToSend, this, chan, mMessenger, mService, false);
            }

        } else if (requestCode == CAPTURE_PHOTO) {
            if (resultCode != RESULT_OK) {
                //no image selected...
                runOnUiThread(new Runnable() {

                    public void run() {
                        Toast.makeText(ChatActivity.this, getString(R.string.no_image_selected), Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
            Toast.makeText(ChatActivity.this, filePath, Toast.LENGTH_SHORT).show();
            photoToSend = filePath;
            hasPhotoToSend = true;
            if (mService != null) {
                sendPictureDialog(photoToSend, this, chan, mMessenger, mService, false);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        }
    }

    public static void sendPictureDialog(final String filePath, final Activity act, final Channel channel, final Messenger messenger, final Messenger service, final boolean openChannel) {
        View checkBoxView = View.inflate(act, R.layout.checkboxdialog, null);
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(act);
        CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
        checkBox.setChecked(sharedPref.getBoolean("lastSendImageWithMinPriotiy", false));
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                sharedPref.edit().putBoolean("lastSendImageWithMinPriotiy", isChecked).commit();
            }
        });
        checkBox.setText(act.getString(R.string.send_with_low_priority_if_checked_the_image_will_only_be_downloaded_from_others_if_they_are_connected_to_wifi));
        AlertDialog.Builder builder = new AlertDialog.Builder(act);

        builder.setTitle(act.getString(R.string.send_picture));
        String str = filePath.split("/")[filePath.split("/").length - 1];
        builder.setMessage(act.getString(R.string.do_you_want_to_send_the_picture_to, str, channel.getName()))
                .setView(checkBoxView)
                .setCancelable(false)
                .setPositiveButton(act.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        hasPhotoToSend = false;
                        photoToSend = "";
                        new Thread() {

                            @Override
                            public void run() {

                                try {
                                    if (openChannel) {
                                        Intent intent;
                                        intent
                                                = new Intent(act, ChatActivity.class
                                                );

                                        intent.putExtra(
                                                "title", channel.toString());
                                        intent.putExtra(
                                                "Channel", channel);
                                        intent.putExtra(
                                                "filePath", filePath);
                                        intent.putExtra(
                                                "lowPriority", sharedPref.getBoolean("lastSendImageWithMinPriotiy", false));
                                        //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                        act.startActivity(intent);
                                    } else {
                                        Message msg = Message.obtain(null,
                                                BS.SEND_PICTURE);
                                        Bundle b = new Bundle();
                                        b.putInt("chanid", channel.getId());
                                        b.putString("filePath", filePath);
                                        b.putBoolean("lowPriority", sharedPref.getBoolean("lastSendImageWithMinPriotiy", false));
                                        msg.setData(b);
                                        msg.replyTo = messenger;
                                        service.send(msg);
                                    }
                                } catch (final Throwable e) {

                                    new Thread() {

                                        @Override
                                        public void run() {
                                            String ownStackTrace = ExceptionLogger.stacktrace2String(e);
                                            Main.sendBroadCastMsg("could not send picture: \n" + ownStackTrace);

                                            act.runOnUiThread(new Runnable() {

                                                public void run() {
                                                    Toast.makeText(act, act.getString(R.string.could_not_send_text_please_restart_the_service), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                    }.start();

                                }

                                act.runOnUiThread(new Runnable() {

                                    public void run() {
                                        Toast.makeText(act, "send", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                //Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
                            }
                        }.start();
                    }
                });
        builder.setNegativeButton(act.getString(R.string.no), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                hasPhotoToSend = false;
                photoToSend = "";
            }
        });
        builder.show();
    }

//    @Override
//    public void openOptionsMenu() {
//        super.openOptionsMenu();
//        Configuration config = getResources().getConfiguration();
//        if ((config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) > Configuration.SCREENLAYOUT_SIZE_LARGE) {
//            int originalScreenLayout = config.screenLayout;
//            config.screenLayout = Configuration.SCREENLAYOUT_SIZE_LARGE;
//            super.openOptionsMenu();
//            config.screenLayout = originalScreenLayout;
//        } else {
//            
//            super.openOptionsMenu();
//        }
//    }
    /**
     * Obtain the intent that started this activity by calling
     * Activity.getIntent() and pass it into this method to get the associated
     * voice input string.
     */
    private static CharSequence getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(PopupListener.EXTRA_VOICE_REPLY);
        }
        return null;
    }

    private void sendMessageFromTextView() {
        final String text = editText.getText().toString();
        Runnable runnable = new Runnable() {

            public void run() {
                editText.setText("");
                editText.requestFocus();
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
        } catch (final Throwable e) {

            new Thread() {

                @Override
                public void run() {
                    String ownStackTrace = ExceptionLogger.stacktrace2String(e);
                    Main.sendBroadCastMsg("could not send message: \n" + ownStackTrace);

                    runOnUiThread(new Runnable() {

                        public void run() {
                            Toast.makeText(ChatActivity.this, getString(R.string.could_not_send_text_please_restart_the_service), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }.start();

        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                if (event.isCtrlPressed()) {
                    sendMessageFromTextView();
                    return true;
                }

            default:
                return super.onKeyUp(keyCode, event);
        }

    }
}
