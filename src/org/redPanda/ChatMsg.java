/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import android.graphics.Color;



/**
 *
 * @author Pizza
 */
public class ChatMsg {

    private String text, time, deliverdTo = "", name;
    private long identity, timestamp;
    private boolean fromMe;
    private byte MsgType;
    private int color;

    public ChatMsg(String text, String time, String name, long identity, long timestamp, boolean fromMe, Byte MsgType) {
        this.text = text;
        this.time = time;
        this.name = name;
        this.identity = identity;
        this.timestamp = timestamp;
        this.fromMe = fromMe;
        this.MsgType = MsgType;
        this.color = Lighten((int) identity, 0.2);
    }

    public static int Lighten(int color, double inAmount)
{
  return Color.rgb((int) Math.min(255, Color.red(color)+ 255 * inAmount) 
          , (int) Math.min(255, Color.green(color)+ 255 * inAmount), 
          (int) Math.min(255, Color.blue(color)+ 255 * inAmount));
//    inColor.A,
//    (int) Math.Min(255, inColor.+ 255 * inAmount),
//    (int) Math.Min(255, inColor.G + 255 * inAmount),
//    (int) Math.Min(255, inColor.B + 255 * inAmount) );
}

    public byte getMsgType() {
        return MsgType;
    }

    public int getColor() {
        return color;
    }
    
    
    
    public void setName(String name) {
        this.name = name;
    }


    public long getTimestamp() {
        return timestamp;
    }

    public long getIdentity() {
        return identity;
    }

    public String getName() {
        return name;
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
