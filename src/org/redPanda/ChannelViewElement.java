/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import org.redPanda.ChannelList.FlActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import org.redPandaLib.Main;
import org.redPandaLib.core.Channel;
import org.redPandaLib.core.Test;

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
        long l = getLastMessageTime() - o.getLastMessageTime();

        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            //not an integer! move to last position
            return Integer.MIN_VALUE;
        }

        return (int) l;
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

            lastMessage = sharedPref.getLong("lastMessageForChannel" + id, 1404584866193L);//TODO: hack

        }
        return lastMessage;
    }

    public String getLastMessageText() {

        if (lastMessageText == null) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(FlActivity.context);
            long identity = Long.parseLong(sharedPref.getString("lastMessageTextForChannelid" + id, "0"));

            if (identity == 0) {
                lastMessageText = "-";
            } else {
                if (Test.localSettings.identity == identity) {
                    lastMessageText = "Me: " + sharedPref.getString("lastMessageTextForChannel" + id, "-");
                } else {
                    String from = "unknown";
                    if (Test.localSettings.identity2Name.containsKey(identity)) {
                        from = Test.localSettings.identity2Name.get(identity);
                    }
                    lastMessageText = from + ": " + sharedPref.getString("lastMessageTextForChannel" + id, "-");
                }
                // Toast.makeText(FlActivity.context, Test.localSettings.identity2Name.get(identity) + "", Toast.LENGTH_SHORT).show();
            }
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
