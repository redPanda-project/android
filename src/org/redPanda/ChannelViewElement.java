/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import org.redPanda.ChannelList.FlActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.redPandaLib.core.Channel;

/**
 *
 * @author Tyrael
 */
public class ChannelViewElement extends Channel {

    private long lastMessage = 0;
    private String lastMessageText;

    public static ChannelViewElement getInstanceFromChannel(Channel channel) {

        ChannelViewElement channelViewElement = new ChannelViewElement();

        channelViewElement.id = channel.getId();
        channelViewElement.name = channel.getName();
        channelViewElement.key = channel.getKey();
        channelViewElement.displayPriority = channel.displayPriority;
        return channelViewElement;
    }

    public int compareTo(ChannelViewElement o) {
        return (int) (getLastMessageTime() - o.getLastMessageTime());
    }

    //currently just last Clicked!!!
    public void setLastMessageTime(long time) {
        lastMessage = time;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(FlActivity.context);
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.putLong("lastMessageForChannel" + id, time);
        edit.commit();
    }

    //currently just last Clicked!!!
    public void setLastMessageText(String text) {
        lastMessageText = text;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(FlActivity.context);
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.putString("lastMessageTextForChannel" + id, text);
        edit.commit();
    }

    public long getLastMessageTime() {

        if (lastMessage == 0) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(FlActivity.context);
            lastMessage = sharedPref.getLong("lastMessageForChannel" + id, -1);
        }
        return lastMessage;
    }

    public String getLastMessageText() {

        if (lastMessageText == null) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(FlActivity.context);
            lastMessageText = sharedPref.getString("lastMessageTextForChannel" + id, "-");
        }
        return lastMessageText;
    }

    @Override
    public String toString() {
        return name;
    }

    public void resetPersistentData() {
        lastMessage = 0;
        lastMessageText = null;
    }
}
