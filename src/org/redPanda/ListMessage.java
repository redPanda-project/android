/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import java.util.ArrayList;
import java.util.Iterator;
import org.redPandaLib.core.Channel;
import org.redPandaLib.core.messages.TextMessageContent;

/**
 *
 * @author mflohr
 */
public class ListMessage {

    //
    public Channel channel;
    public long identity;
    public ArrayList<Mes> text;
    public boolean fromMe;
    public String name;

    public ListMessage(TextMessageContent tmc) {
        text = new ArrayList<Mes>();
        channel = tmc.channel;
        identity = tmc.identity;
        text.add(new Mes(tmc.database_id, tmc.timestamp, tmc.text, fromMe, tmc.message_type));
        fromMe = tmc.fromMe;
        name = tmc.getName();
    }

    public static class Mes {

        boolean fromMe;
        int database_id;
        String mes = "";
        long ts;
        String name;
        int message_type;
        ArrayList<String> deliveredTo;

        public Mes(int database_id, long timestamp, String text, boolean fromMe, int message_type) {
            this.database_id = database_id;
            this.ts = timestamp;
            this.mes = text;
            this.fromMe = fromMe;
            this.message_type = message_type;
        }

        public String getMes() {
            return mes;
        }

        public long getTs() {
            return ts;
        }
    }
}
