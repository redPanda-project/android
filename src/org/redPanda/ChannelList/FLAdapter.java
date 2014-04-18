/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda.ChannelList;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;
import static org.redPanda.ChannelList.FlActivity.context;
import org.redPanda.ChannelViewElement;
import org.redPanda.R;

/**
 *
 * @author mflohr
 */
public class FLAdapter extends ArrayAdapter<ChannelViewElement> {

    Context context;
    List<ChannelViewElement> objects;

    public FLAdapter(Context context, int resource, List<ChannelViewElement> objects) {
        super(context, resource, objects);
        this.context = context;
        this.objects = objects;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        Holder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.listitem, parent, false);

            holder = new Holder();
            holder.CN = (TextView) row.findViewById(R.id.Channel);
            holder.msg = (TextView) row.findViewById(R.id.lastMsg);

            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }
        ChannelViewElement ch = objects.get(position);
        holder.CN.setText(ch.getName());
        holder.msg.setText(ch.getLastMessageText());

        return row;
    }

    static class Holder {

        TextView CN, msg;
    }
}
