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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import java.util.Date;
import org.redPanda.ListMessage.Mes;

/**
 *
 */
public class ChatAdapter extends BaseAdapter {

    private Context mContext;
    public ArrayList<ListMessage> mMessages;

    public ChatAdapter(Context context, ArrayList<ListMessage> messages) {
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
        ListMessage b = (ListMessage) this.getItem(position);

        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.chatrow, parent, false);
            holder.ll = (LinearLayout) convertView.findViewById(R.id.chatrow);
//            holder.head = (TextView) convertView.findViewById(R.id.head);
            holder.bubble = (ListView) convertView.findViewById(R.id.bubble);
            //    holder.im = (ImageView) convertView.findViewById(R.id.thereic);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        inAdapter iA =new inAdapter(mContext, mMessages.get(position).text);
        holder.bubble.setAdapter(iA);

        //System.out.println("1234 "+message.getData().getString("msg"));       
//        holder.message.setText(genReadableText(b));
        boolean fromMe = b.fromMe;


        LayoutParams lp = (LayoutParams) holder.bubble.getLayoutParams();

//check if it is a status message then remove background, and change text color.
//        if (message.isStatusMessage()) {
//            holder.message.setBackgroundDrawable(null);
//            lp.gravity = Gravity.LEFT;
//
//        } else {
//Check whether message is mine to show green background and align to right

        if (fromMe) {
            holder.bubble.setBackgroundResource(R.drawable.ich);
            // System.out.println(" ich");
               holder.ll.setGravity(Gravity.RIGHT);
            lp.gravity = Gravity.RIGHT;
//            holder.im.setVisibility(View.VISIBLE);
//            holder.im.getLayoutParams().width = 30;
//            holder.im.getLayoutParams().height = 30;
        } //If not mine then it is from sender to show orange background and align to left
        else {
//            holder.im.setVisibility(View.INVISIBLE);
//            holder.im.getLayoutParams().width = 0;
//            holder.im.getLayoutParams().height = 0;
            holder.bubble.setBackgroundResource(R.drawable.du);
            // System.out.println(" du");
             holder.ll.setGravity(Gravity.LEFT);
            lp.gravity = Gravity.LEFT;
        }
        //Math.min(lp.width, (int) (getWidestView(mContext, iA)*1.05));
        holder.bubble.setLayoutParams(lp);
//            holder.message.setTextColor(R.color.textColor);
        System.out.println("123456 " + b.text.size());
     //   holder.bubble.getLayoutParams().width = (int) (getWidestView(mContext, iA)*1.05);
        return convertView;
    }

    private static class ViewHolder {

        //  ImageView im;
        ListView bubble;
//        TextView head;
        LinearLayout ll;
    }

    @Override
    public long getItemId(int position) {
//Unimplemented, because we aren't using Sqlite.
        return 0;
    }

    public static String genReadableText(Mes msg) {
        long sendTime = msg.ts;
        String str = msg.getMes();

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

    public static int getWidestView(Context context, Adapter adapter) {
        int maxWidth = 0;
        View view = null;
        FrameLayout fakeParent = new FrameLayout(context);
        for (int i = 0, count = adapter.getCount(); i < count; i++) {
            view = adapter.getView(i, view, fakeParent);
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int width = view.getMeasuredWidth();
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        return maxWidth;
    }
}