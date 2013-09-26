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
 * @author Tyrael
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
        text.add(new Mes(tmc.database_id, tmc.timestamp, tmc.text, fromMe));
        fromMe = tmc.fromMe;
        name = tmc.name;
    }

    public static class Mes {

        boolean fromMe;
        int database_id;
        String mes = "";
        long ts;

        public Mes(int database_id, long ts, String mes, boolean fromMe) {
            this.database_id = database_id;
            this.ts = ts;
            this.mes = mes;
            this.fromMe = fromMe;
        }

        public String getMes() {
            return mes;
        }

        public long getTs() {
            return ts;
        }
    }
}
