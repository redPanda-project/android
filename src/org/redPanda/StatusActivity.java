/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import static java.lang.Thread.sleep;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.redPandaLib.core.Peer;
import org.redPandaLib.core.PeerTrustData;
import org.redPandaLib.core.Settings;
import org.redPandaLib.core.Test;

/**
 *
 * @author robin
 */
public class StatusActivity extends Activity {

    private boolean active;
    private TextView infotext;
    private TextView maintext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //To change body of generated methods, choose Tools | Templates.
        new ExceptionLogger(this);

        setContentView(R.layout.statusactivity);

        infotext = (TextView) findViewById(R.id.statusactivity_infotext);
        infotext.setTextColor(Color.GRAY);

        maintext = (TextView) findViewById(R.id.statusactivity_mainInformations);
        maintext.setTypeface(Typeface.MONOSPACE);

        //just here is the transparent theme active
        if (android.os.Build.VERSION.SDK_INT >= 14) {

            // Calculate ActionBar height
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
                infotext.setPadding(0, actionBarHeight, 0, 0);
            }

        }

        Button toogleEmojiconKeyboard = (Button) findViewById(R.id.statusactivity_button_defrag_database);
        toogleEmojiconKeyboard.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {

                try {
                    Statement createStatement = Test.hsqlConnection.getConnection().createStatement();
                    createStatement.execute("CHECKPOINT DEFRAG");
                    createStatement.close();
                    infotext.post(new Runnable() {

                        public void run() {
                            infotext.setText("job");
                            maintext.setText("done");
                        }
                    });
                } catch (SQLException ex) {
                    Test.sendStacktrace(ex);
                }

            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                            if (peer.isConnected() && peer.isAuthed() && peer.isCryptedConnection()) {
                                actCons++;
                            } else if (peer.isConnecting || peer.isConnected() || peer.isAuthed()) {
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

                        final int messageCount = Test.messageStore.getMessageCount();

                        //format += String.format("%50s %22s %12s %12s %7s %8s %10s %10s %10s %8s %10s %10s %10s %10s\n", "[IP]:PORT", "nonce", "last answer", "conntected", "retries", "ping", "loaded Msg", "bytes out", "bytes in", "bad Msg", "ToSyncM", "intrMsgs", "RSM", "BackSyncdT");
                        String mtext = "Nodes:\n";
                        ArrayList<Peer> clonedPeerList = Test.getClonedPeerList();
                        Collections.sort(clonedPeerList);
                        for (Peer p : clonedPeerList) {

                            String ip = p.getIp();
                            if (ip.length() > 12) {
                                ip = ip.substring(0, 12);
                            }

                            mtext += String.format("%12s %5d %4s %5d\n", ip, p.port, "" + p.authed, (p.peerTrustData != null ? p.getLoadedMsgs().size() : -1));
                        }

                        final String finalText = mtext;
                        infotext.post(new Runnable() {

                            public void run() {
                                infotext.setText("Nodes: " + activeConnections + "/" + connectingConnections + "/" + list.size() + " - " + clonedTrusts.size() + " - " + trustedIpsFinal + ". Msgs: " + messageCount);
                                maintext.setText(finalText);
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
                        sleep(1000);
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

}
