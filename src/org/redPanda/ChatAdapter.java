/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

/**
 *
 * @author Tyrael
 */
import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import java.util.Date;

/**
 *
 */
public class ChatAdapter extends BaseAdapter {

    private Context mContext;
    public ArrayList<Bundle> mMessages;

    public ChatAdapter(Context context, ArrayList<Bundle> messages) {
        super();
        this.mContext = context;
        this.mMessages = messages;
    }

    @Override
    public int getCount() {
        return mMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return mMessages.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Bundle b = (Bundle) this.getItem(position);

        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.chatrow, parent, false);
            holder.message = (TextView) convertView.findViewById(R.id.message_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //System.out.println("1234 "+message.getData().getString("msg"));
        holder.message.setText(genReadableText(b));
        boolean fromMe = b.getBoolean("fromMe");


        LayoutParams lp = (LayoutParams) holder.message.getLayoutParams();
//check if it is a status message then remove background, and change text color.
//        if (message.isStatusMessage()) {
//            holder.message.setBackgroundDrawable(null);
//            lp.gravity = Gravity.LEFT;
//
//        } else {
//Check whether message is mine to show green background and align to right
        if (fromMe) {
            holder.message.setBackgroundResource(R.drawable.ich);
            System.out.println(" ich");
            lp.gravity = Gravity.RIGHT;
        } //If not mine then it is from sender to show orange background and align to left
        else {
            holder.message.setBackgroundResource(R.drawable.du);
            System.out.println(" du");
            lp.gravity = Gravity.LEFT;
        }
        holder.message.setLayoutParams(lp);
//            holder.message.setTextColor(R.color.textColor);

        return convertView;
    }

    private static class ViewHolder {

        TextView message;
    }

    @Override
    public long getItemId(int position) {
//Unimplemented, because we aren't using Sqlite.
        return 0;
    }

    public static String genReadableText(Bundle msg) {
        long sendTime = msg.getLong("sendtime");
        String str = msg.getString("msg");

        Date date = new Date(sendTime);

        String out = "";

        out += formatTime(date) + ": " + str;

        return out;
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
}