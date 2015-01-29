package org.redPanda;

import org.redPanda.ChannelList.Preferences;
import org.redPanda.ChannelList.FlActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView conversationText;
    private ScrollView scrollView;
    private EditText editText;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new ExceptionLogger(this);
        Intent intent = new Intent(this, BS.class);
        //startService(intent);
    }
//        setContentView(R.layout.main);
//
//        Intent intent = new Intent(this, BackgroundService.class);
////        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
////        
////
////
//        final TextView mainLayouthead = (TextView) findViewById(R.id.mainLayouthead);
//
//        conversationText = (TextView) findViewById(R.id.mainConversationText);
//        scrollView = (ScrollView) findViewById(R.id.mainScrollView);
//
////
////
////        scrollView = new ScrollView(this);
////        textView = new TextView(this);
////        textView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
////        scrollView.addView(textView);
////        scrollView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
////        mainLayout.addView(scrollView);
////
////
////        LinearLayout linearLayout = new LinearLayout(this);
////        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
////        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
////
////
////        final EditText editText = new EditText(this);
////        editText.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
////        linearLayout.addView(editText);
////        Button button = new Button(this);
////        button.setText("Send");
////        button.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//
//        editText = (EditText) findViewById(R.id.mainEditText);
//
//        Button button = (Button) findViewById(R.id.mainSendButton);
//        button.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View arg0) {
//
//                final String text = editText.getText().toString();
//                Runnable runnable = new Runnable() {
//
//                    public void run() {
//                        editText.setText("");
//                    }
//                };
//
//                editText.post(runnable);
//
//                new Thread() {
//
//                    @Override
//                    public void run() {
//                        Main.sendBroadCastMsg(text);
//                    }
//                }.start();
//
//            }
//        });
////        linearLayout.addView(button);
////        mainLayout.addView(linearLayout);
////
////
//        startService(intent);
////
//        Main.addListener(new TextFieldUpdateListener());
////
//        new Thread() {
//
//            @Override
//            public void run() {
////
////                ArrayList<Msg> messages = (ArrayList<Msg>) Test.getMessages().clone();
////
////                if (messages == null || messages.isEmpty()) {
////                    messages = new AndroidSaver(MainActivity.this).loadMsgs();
////                } else {
////                    //
////            }
//                ArrayList<Msg> messages = new AndroidSaver(MainActivity.this).loadMsgs();
////                Main.sendBroadCastMsg("[DEBUG] displaying all messages... " + messages.size());
//
//                if (messages == null) {
//                    conversationText.post(new Runnable() {
//
//                        public void run() {
//                            conversationText.append("no messages...");
//                        }
//                    });
//                    return;
//                }
//
//                for (final Msg msg : messages) {
//                    conversationText.post(new Runnable() {
//
//                        public void run() {
//                            conversationText.append(genReadableText(msg));
//
//                        }
//                    });
//                }
//
//                try {
//                    sleep(50);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
//                }
//
//                scrollView.post(new Runnable() {
//
//                    public void run() {
//                        scrollView.fullScroll(View.FOCUS_DOWN);
//                    }
//                });
//
//
//                while (true) {
//                    try {
//                        sleep(500);
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//
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
//                    String trafficStatus = "Traffic: " + Test.inBytes / 1024. + " kb / " + Test.outBytes / 1024. + " kb.";
//
//                    final String text = "connected to " + cnt + " peers...\n" + trafficStatus;
//
//                    mainLayouthead.post(new Runnable() {
//
//                        public void run() {
//                            mainLayouthead.setText(text);
//                        }
//                    });
//
//
//                }
//            }
//        }.start();
//    }
//
//    class TextFieldUpdateListener implements NewMessageListener {
//
//        public void newMessage(final Msg msg, boolean bln) {
//
//            conversationText.post(new Runnable() {
//
//                public void run() {
//                    conversationText.append(genReadableText(msg));
//                    scrollView.fullScroll(View.FOCUS_DOWN);
//                }
//            });
//
//        }
//    }
//
//    public static String genReadableText(Msg msg) {
//        long sendTime = msg.getSendTime();
//        int indexOf = Test.channels.indexOf(msg.getChannel());
//
//        Date date = new Date(sendTime);
//
//        String out = "";
//
//        out += formatTime(date);
//        out += "  [";
//        out += indexOf;
//        out += "]  ";
//
//
//        if (msg.findPrivateKeyForChannel()) {
//            out += msg.getDecryptedContent() + "\n\n";
//        } else {
//            out += "Message not for me :(" + "\n\n";
//        }
//
//
//
//        return out;
//    }
//
//    public static String formatTime(Date date) {
//
//        String hours = "" + date.getHours();
//        String minutes = "" + date.getMinutes();
//        String seconds = "" + date.getSeconds();
//
//        if (hours.length() == 1) {
//            hours = "0" + hours;
//        }
//        if (minutes.length() == 1) {
//            minutes = "0" + minutes;
//        }
//        if (seconds.length() == 1) {
//            seconds = "0" + seconds;
//        }
//
//        return hours + ":" + minutes + ":" + seconds;
//
//
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
//            case R.id.FL:
//                Intent intent = new Intent(this, FlActivity.class);
//                startActivity(intent);
//                return true;
//            case R.id.prefMenuButton:
//                Intent intent2 = new Intent(this, Preferences.class);
//                startActivity(intent2);
//                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
