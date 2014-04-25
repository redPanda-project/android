/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import android.graphics.Color;
import java.util.Date;
import org.redPandaLib.core.Test;
import org.redPandaLib.core.messages.TextMessageContent;

/**
 *
 * @author mflohr
 */
public class ChatMsg {

    private String text, time, deliverdTo = "";
    private long identity, timestamp;
    private boolean fromMe;
    private int MsgType;
    private int color;

    public ChatMsg(String text, String time, long identity, long timestamp, boolean fromMe, int MsgType) {
        this.text = text;
        this.time = time;
        this.identity = identity;
        this.timestamp = timestamp;
        this.fromMe = fromMe;
        this.MsgType = MsgType;
        this.color = Lighten((int) identity, 0.2);
    }

    public ChatMsg(TextMessageContent tmc) {
        this.text = tmc.text;
        this.time = ChatAdapter.formatTime(new Date(tmc.timestamp), false);
        this.identity = tmc.identity;
        this.timestamp = tmc.timestamp;
        this.fromMe = tmc.fromMe;
        this.MsgType = tmc.message_type;
        this.color = Lighten((int) identity, 0.2);
    }

    public static int Lighten(int color, double inAmount) {
        return Color.rgb((int) Math.min(255, Color.red(color) + 255 * inAmount), (int) Math.min(255, Color.green(color) + 255 * inAmount),
                (int) Math.min(255, Color.blue(color) + 255 * inAmount));
//    inColor.A,
//    (int) Math.Min(255, inColor.+ 255 * inAmount),
//    (int) Math.Min(255, inColor.G + 255 * inAmount),
//    (int) Math.Min(255, inColor.B + 255 * inAmount) );
    }

    public int getMsgType() {
        return MsgType;
    }

    public int getColor() {
        return color;
    }


    public long getTimestamp() {
        return timestamp;
    }

    public long getIdentity() {
        return identity;
    }

    public String getName() {
        return Test.localSettings.identity2Name.get(identity);
    }

    public boolean isFromMe() {
        return fromMe;
    }

    public String getText() {
        return text;
    }

    public String getTime() {
        return time;
    }

    public String getDeliverdTo() {
        return deliverdTo;
    }

    public void setDeliverdTo(String deliverdTo) {
        this.deliverdTo = deliverdTo;
    }

}
