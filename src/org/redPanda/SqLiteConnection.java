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



        //stmt.executeUpdate("SET FILES CACHE ROWS 5000");//rows
        //stmt.executeUpdate("SET FILES CACHE SIZE 5000");//kb
        //stmt.executeUpdate("SET AUTOCOMMIT TRUE");

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
        stmt.executeUpdate("create table if not exists channel (channel_id integer PRIMARY KEY IDENTITY, pubkey_id INTEGER UNIQUE, private_key BINARY(32) UNIQUE, name LONGVARBINARY)");



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
        stmt.executeUpdate("create table if not exists message (message_id INTEGER PRIMARY KEY IDENTITY, pubkey_id INTEGER, public_type TINYINT, timestamp BIGINT, nonce INTEGER,  signature BINARY(72), content LONGVARBINARY, verified boolean)");

        stmt.executeUpdate("create table if not exists channelmessage (pubkey_id INTEGER, message_id INTEGER, message_type INTEGER, decryptedContent LONGVARBINARY, identity BIGINT, fromMe BOOLEAN, FOREIGN KEY (pubkey_id) REFERENCES pubkey(pubkey_id))");
//        ResultSet executeQuery = stmt.executeQuery("SELECT * FROM information_schema.statistics");
//
//
//        System.out.println("d3uwne3quzne " + executeQuery.getFetchSize());
//        executeQuery.close();

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


        //stmt.executeUpdate("create CACHED table if not exists stick (stick_id INTEGER PRIMARY KEY IDENTITY, pubkey_id INTEGER, timestamp BIGINT, nonce INTEGER,  signature BINARY(72), content LONGVARBINARY, verified boolean)");

//            stmt.executeUpdate("insert into person values(1, 'leo')");
//            stmt.executeUpdate("insert into person values(2, 'yui')");
//            ResultSet rs2 = stmt.executeQuery("select * from person");
//            while (rs2.next()) {
//                // read the result set
//                System.out.println("name = " + rs2.getString("name"));
//                System.out.println("id = " + rs2.getInt("id"));
//            }
//
//
//
//
//
//            String sql2 = "CREATE TABLE if not exists test (id INT NOT NULL,content INT NOT NULL ,PRIMARY KEY (id))";
//            stmt.execute(sql2);
//
////            String sql3 = "INSERT INTO test (id ,content) VALUES ('1',  '55');";
////            stmt.execute(sql3);
//
//            // Alle Kunden ausgeben
//            String sql = "SELECT * FROM test";
//            ResultSet rs = stmt.executeQuery(sql);
//
//            while (rs.next()) {
//                String id = rs.getString(1);
//                String content = rs.getString(2);
//                System.out.println(id + ", " + content + " ");
//            }
//
//            // Resultset schließen
//            rs.close();

        // Statement schließen
        stmt.close();

    }

    public Connection getConnection() {
        return con;
    }
}
