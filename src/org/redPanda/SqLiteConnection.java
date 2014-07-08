/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import android.content.Context;
import java.sql.*;
import java.util.Properties;
import org.redPandaLib.database.MessageStore;

/**
 *
 * @author rflohr
 */
public class SqLiteConnection {

    public static String db_file = "/messages";
    private Connection con = null;

    public SqLiteConnection(Context context) throws SQLException {


//        try {
//            // Treiberklasse laden
//            Class.forName("org.sqldroid.SqldroidDriver");
//        } catch (ClassNotFoundException e) {
//            System.err.println("Treiberklasse nicht gefunden!");
//            return;
//        }

        try {
            // Treiberklasse laden
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Treiberklasse nicht gefunden!");
            return;
        }

        con = null;

        con = DriverManager.getConnection("jdbc:hsqldb:" + context.getFilesDir() + db_file);
        //con = new org.sqldroid.SQLDroidDriver().connect("jdbc:sqldroid:" + context.getFilesDir() + db_file, new Properties());

        Statement stmt = con.createStatement();



        stmt.executeUpdate("SET FILES CACHE ROWS 10000");//rows
        stmt.executeUpdate("SET FILES CACHE SIZE 20000");//kb
        stmt.executeUpdate("SET AUTOCOMMIT TRUE");

//            PubKey
//id INTEGER
//key BINARY(33)


        if (false) {
            stmt.executeUpdate("drop table if exists channelmessage");
            stmt.executeUpdate("drop table if exists message");
            stmt.executeUpdate("drop table if exists channel");
            stmt.executeUpdate("drop table if exists pubkey");
        }

        //AUTOINCREMENT -- IDENTITY
 stmt.executeUpdate("create CACHED table if not exists pubkey (pubkey_id integer PRIMARY KEY IDENTITY, pubkey BINARY(33) UNIQUE)");

//Channel
//id INTEGER
//pubkey_id INTEGER
//private_key BINARY(32)
//name LONGVARBINARY
        stmt.executeUpdate("create CACHED table if not exists channel (channel_id integer PRIMARY KEY IDENTITY, pubkey_id INTEGER UNIQUE, private_key BINARY(32) UNIQUE, name LONGVARBINARY)");

//Message
//id INTEGER
//key_id INTEGER
//channel_id integer
//timestamp BIGINT
//nonce INTEGER
//signature BINARY(72)
//content LONGVARBINARY
//verified boolean
//readable boolean
//decrypted_content LONGVARBINARY
        //stmt.executeUpdate("drop table if exists message");
        stmt.executeUpdate("create CACHED table if not exists message (message_id INTEGER PRIMARY KEY IDENTITY, pubkey_id INTEGER, public_type TINYINT, timestamp BIGINT, nonce INTEGER,  signature BINARY(72), content LONGVARBINARY, verified boolean)");

        stmt.executeUpdate("create CACHED table if not exists channelmessage (pubkey_id INTEGER, message_id INTEGER, message_type INTEGER, decryptedContent LONGVARBINARY, identity BIGINT, fromMe BOOLEAN, FOREIGN KEY (pubkey_id) REFERENCES pubkey(pubkey_id))");

        //table for sticks
        stmt.executeUpdate("create CACHED table if not exists sticks (pubkey_id INTEGER, message_id INTEGER, difficulty DOUBLE, validTill BIGINT, FOREIGN KEY (pubkey_id) REFERENCES pubkey(pubkey_id))");

        stmt.executeUpdate("create CACHED table if not exists peerMessagesIntroducedToMe (peer_id BIGINT, message_id INTEGER)");
        stmt.executeUpdate("create CACHED table if not exists peerMessagesIntroducedToHim (peer_id BIGINT, message_id INTEGER, FOREIGN KEY (message_id) REFERENCES message(message_id) ON DELETE CASCADE)");

        stmt.executeUpdate("create CACHED table if not exists haveToSendMessageToPeer (peer_id BIGINT, message_id INTEGER, FOREIGN KEY (message_id) REFERENCES message(message_id) ON DELETE CASCADE)");
        stmt.executeUpdate("create CACHED table if not exists filterChannels (peer_id BIGINT, channel_id INTEGER)");//, FOREIGN KEY (channel_id) REFERENCES channel(channel_id) ON DELETE CASCADE

//        ResultSet executeQuery = stmt.executeQuery("SELECT * FROM information_schema.statistics");
//
//
//        System.out.println("d3uwne3quzne " + executeQuery.getFetchSize());
//        executeQuery.close();
//        stmt.executeUpdate("create CACHED table if not exists syncHash (channel_id integer, from BIGINT, to BIGINT, count INTEGER, hashcode INTEGER)");
        try {
            stmt.executeUpdate("CREATE INDEX messagePubkeyIndex ON message(pubkey_id)");
        } catch (SQLSyntaxErrorException e) {
        }
        try {
            stmt.executeUpdate("CREATE INDEX messageTimestampIndex ON message(timestamp)");
        } catch (SQLSyntaxErrorException e) {
        }
        try {
            stmt.executeUpdate("CREATE INDEX messageNonceIndex ON message(nonce)");
        } catch (SQLSyntaxErrorException e) {
        }
        try {
            stmt.executeUpdate("CREATE INDEX messageMsgIdIndex ON message(message_id)");
        } catch (SQLSyntaxErrorException e) {
        }

        try {
            stmt.executeUpdate("CREATE INDEX peerMessagesIntroducedToMeIndex ON peerMessagesIntroducedToMe(peer_id,message_id)");
        } catch (SQLSyntaxErrorException e) {
        }

        try {
            stmt.executeUpdate("CREATE INDEX peerMessagesIntroducedToHimIndex ON peerMessagesIntroducedToHim(peer_id,message_id)");
        } catch (SQLSyntaxErrorException e) {
        }

        try {
            stmt.executeUpdate("CREATE INDEX peerMessagesIntroducedToHimIndexForMsgId ON peerMessagesIntroducedToHim(message_id)");
        } catch (SQLSyntaxErrorException e) {
        }

        String[] keys = {"pubkey_id", "message_type", "message_id"};
        String tableName = "channelmessage";
        for (String key : keys) {
            try {
                stmt.executeUpdate("CREATE INDEX " + tableName + key + "Index ON " + tableName + "(" + key + ")");
            } catch (SQLSyntaxErrorException e) {
            }
        }

        try {
            stmt.executeUpdate("CREATE INDEX syncHashchannel_idIndex ON syncHash(channel_id)");
        } catch (SQLSyntaxErrorException e) {
        }
        stmt.close();

    }

    public Connection getConnection() {
        return con;
    }
}
