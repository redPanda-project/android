/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import android.app.Activity;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import org.redPanda.ChannelList.FlActivity;
import org.redPanda.ListMessage.Mes;

import org.redPandaLib.Main;
import org.redPandaLib.core.Channel;
import org.redPandaLib.core.Settings;
import org.redPandaLib.core.messages.DeliveredMsg;
import org.redPandaLib.core.messages.ImageMsg;
import org.redPandaLib.core.messages.TextMessageContent;

/**
 *
 * @author mflohr
 */
public class ChatActivity extends FragmentActivity implements EmojiconGridFragment.OnEmojiconClickedListener, EmojiconsFragment.OnEmojiconBackspaceClickedListener {

    private TextView conversationText;
    //  private ListView listView;
    private EditText editText;
    private Channel chan;
    private long lastTouched = 0;
    private ArrayList<ChatMsg> messages;
    private ChatAdapter cA;
    public static final int MENU_IMAGE = Menu.FIRST;
    private static final int SELECT_PHOTO = 100;
    private boolean emojiconKeyboardVisible = false;
    private EmojiconsFragment emojiconsFragment;
    private LinearLayout mainLayoutInputAndSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ExceptionLogger(this);
        Intent in = getIntent();
        this.setTitle(in.getExtras().getString("title"));
        chan = (Channel) in.getExtras().get("Channel");
        setContentView(R.layout.chatlayout);
        
        getWindow().getDecorView().setBackgroundColor(Color.LTGRAY);

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        Intent intent = new Intent(this, BS.class);
        startService(intent);
        doBindService();
        final TextView mainLayouthead = (TextView) findViewById(R.id.mainLayouthead);
        LayoutInflater.from(this).inflate(R.layout.chatrow, null);
        // conversationText = (TextView) findViewById(R.id.message_text);
        //scrollView = (ScrollView) findViewById(R.id.mainScrollView);
        // listView = (ListView) findViewById(R.id.chatlist);
        editText = (EditText) findViewById(R.id.mainEditText);
        messages = new ArrayList<ChatMsg>();
        cA = new ChatAdapter(this, messages);

        ListView lv = (ListView) findViewById(R.id.chatlayout_bubblelist);
        lv.setAdapter(cA);

        mainLayoutInputAndSend = (LinearLayout) findViewById(R.id.mainLayoutInputAndSend);

        //hide smiley keyboard at beginning
        emojiconsFragment = (EmojiconsFragment) getSupportFragmentManager().findFragmentById(R.id.emojicons);
        FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
        tr.hide(emojiconsFragment);
        tr.commit();

        Button toogleEmojiconKeyboard = (Button) findViewById(R.id.emojiconEnableButton);
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

        //getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.red_bg));
        

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
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

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
                    merge(t);
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
                        merge(it.next());
                    }
                    cA.notifyDataSetChanged();
                    //getListView().setSelection(cA.mMessages.size() - 1);
                    //System.out.println("12345 " + cA.mMessages.size());
                    break;

                default:
                    super.handleMessage(msg);
            }
        }

        public void merge(final TextMessageContent tmc) {
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

            cM = new ChatMsg(tmc.getText(), time, tmc.identity, tmc.getTimestamp(), tmc.fromMe, tmc.message_type);
            ChatMsg oCM;
            if (cA.mMessages.isEmpty()) {
                TextMessageContent tmptmc = new TextMessageContent();
                tmptmc.text = ChatAdapter.formatTime(new Date(tmc.timestamp), true);
                tmptmc.message_type = ChatAdapter.daydevider;
                cA.mMessages.add(new ChatMsg(tmptmc));

            } else {
                oCM = cA.mMessages.get(cA.mMessages.size() - 1);
                String d, oD;
                d = ChatAdapter.formatTime(new Date(tmc.timestamp), true);
                oD = ChatAdapter.formatTime(new Date(oCM.getTimestamp()), true);
                if (!d.equals(oD)) {
                    TextMessageContent tmptmc = new TextMessageContent();
                    tmptmc.text = ChatAdapter.formatTime(new Date(tmc.timestamp), true);
                    tmptmc.message_type = ChatAdapter.daydevider;
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

            // We want to monitor the service for as long as we are
            // connected to it.
            new Thread() {

                @Override
                public void run() {
                    Message msg = Message.obtain(null,
                            BS.MSG_REGISTER_CLIENT);
                    Bundle b = new Bundle();
                    b.putInt("chanid", chan.getId());
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
                return true;
            case R.id.imageSendButtonFromChat:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
            final Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        new Thread() {

            @Override
            public void run() {

                switch (requestCode) {
                    case SELECT_PHOTO:
                        if (resultCode == RESULT_OK) {

                            Uri selectedImage = imageReturnedIntent.getData();
                            String[] filePathColumn = {MediaStore.Images.Media.DATA};

                            Cursor cursor = getContentResolver().query(
                                    selectedImage, filePathColumn, null, null, null);
                            cursor.moveToFirst();

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            final String filePath = cursor.getString(columnIndex);
                            cursor.close();

                            Main.sendImageToChannel(chan, filePath);

                            runOnUiThread(new Runnable() {

                                public void run() {
                                    Toast.makeText(ChatActivity.this, "send", Toast.LENGTH_SHORT).show();
                                }
                            });

                            //Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
                        }
                }

            }
        }.start();
    }
}
