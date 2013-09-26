/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import org.redPanda.ListMessage.Mes;

/**
 *
 * @author Tyrael
 */
public class inAdapter extends BaseAdapter {

    Context mContext;
    ArrayList<Mes> mMessages;

    public inAdapter(Context mContext, ArrayList<Mes> mMessages) {
        super();
        this.mContext = mContext;
        this.mMessages = mMessages;
    }

    public int getCount() {
        return mMessages.size();
    }

    public Object getItem(int i) {
        return mMessages.get(i);
    }

    public long getItemId(int i) {
        return 0;
    }

    public View getView(int i, View convertView, ViewGroup parent) {
        Mes mes = (Mes) this.getItem(i);

        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.chatext, parent, false);
            holder.message = (TextView) convertView.findViewById(R.id.message_text);

            holder.im = (ImageView) convertView.findViewById(R.id.thereic);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.message.setText(ChatAdapter.genReadableText(mes));

        if (mes.fromMe) {
            holder.im.setVisibility(View.VISIBLE);
            holder.im.getLayoutParams().width = 30;
            holder.im.getLayoutParams().height = 30;
        } else {
            holder.im.setVisibility(View.INVISIBLE);
            holder.im.getLayoutParams().width = 0;
            holder.im.getLayoutParams().height = 0;
        }



        return convertView;
    }

    private static class ViewHolder {

        ImageView im;
        TextView message;
    }
}