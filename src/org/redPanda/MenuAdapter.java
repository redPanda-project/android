/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mflohr
 */
public class MenuAdapter extends BaseAdapter {

    Context context;
    String[] list;

    public MenuAdapter(Context context, String[] list) {
        super();
        this.context = context;
        this.list = list;
    }

    public int getCount() {
        return list.length;
    }

    public Object getItem(int position) {
        return list[position];
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        String text = list[position];
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.menu_list_item, parent, false);
            holder.name = (TextView) convertView.findViewById(R.id.menu_list_text);
            holder.icon = (ImageView) convertView.findViewById(R.id.menu_icon);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        switch (position) {
            case 0://create new Channel
                holder.name.setText(text);
                holder.icon.setImageResource(R.drawable.ic_action_new);
                break;
            case 1://import channel
                holder.name.setText(text);
                holder.icon.setImageResource(R.drawable.ic_action_new);
                break;
            case 2://import channel QR
                holder.name.setText(text);
                holder.icon.setImageResource(R.drawable.qr_icon);
                break;
            case 3://Settings
                holder.name.setText(text);
                holder.icon.setImageResource(R.drawable.ic_action_settings);
                break;
            case 4://Help
                holder.name.setText(text);
                holder.icon.setImageResource(R.drawable.ic_help_outline_black_48dp);
                break;

        }
        return convertView;
    }

    private static class ViewHolder {

        ImageView icon;
        TextView name;
    }

}
