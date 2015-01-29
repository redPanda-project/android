/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redPanda;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.TaskStackBuilder;
import org.redPanda.ChannelList.ChanPref;
import org.redPanda.ChannelList.FlActivity;
import org.redPandaLib.NewMessageListener;
import org.redPandaLib.core.messages.DeliveredMsg;
import org.redPandaLib.core.messages.ImageMsg;
import org.redPandaLib.core.messages.TextMessageContent;

/**
 *
 * @author robin
 */
public class PopupListener implements NewMessageListener {

    Context context;
    int dot = 50;      // Length of a Morse Code "dot" in milliseconds
    int dash = 300;     // Length of a Morse Code "dash" in milliseconds
    int short_gap = 200;    // Length of Gap Between dots/dashes
    int medium_gap = 500;   // Length of Gap Between Letters
    int long_gap = 1000;    // Length of Gap Between Words
    long[] pattern = {
        0, // Start immediately
        dot, short_gap, dot, short_gap, dot
    };
    static long lastVibrated = 0;

    public PopupListener(Context context) {
        this.context = context;
    }

    public void newMessage(TextMessageContent msg) {

        if (msg.fromMe) {
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(msg.getChannel().getId());
            return;
        }

        if (BS.currentViewedChannel == msg.getChannel().getId()) {
            return;
        }

        if (msg.message_type == DeliveredMsg.BYTE) {
            return;
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notifications = sharedPref.getBoolean(ChanPref.CHAN_NOTIFICATIONS + msg.channel.getId(), true);
        boolean silent = sharedPref.getBoolean(ChanPref.CHAN_SILENT + msg.channel.getId(), false);

        if (!notifications) {
            return;
        }

        //System.out.println("Display msg....");
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // The PendingIntent to launch our activity if the user selects this notification
//        Intent i = new Intent(context, MainActivity.class);
        Intent intent;
        intent = new Intent(context, ChatActivity.class);
        intent.putExtra("title", msg.getChannel().toString());
        intent.putExtra("Channel", msg.getChannel());
        //intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        // Set the info for the views that show in the notification panel.

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.icon).setContentTitle(msg.getChannel().toString()).setContentText(msg.getText()).setContentIntent(contentIntent);

        if (!silent) {
            mBuilder = mBuilder.setSound(soundUri);
        }

        mBuilder.setAutoCancel(true);

        BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();

        if (msg.message_type == ImageMsg.BYTE) {
            bigTextStyle.bigText(msg.getName() + ": bild...");
        } else {
            bigTextStyle.bigText(msg.getName() + ": " + msg.getText());
        }

        bigTextStyle.setBigContentTitle(msg.getChannel().toString());
        bigTextStyle.setSummaryText("message from redPanda");
        mBuilder.setStyle(bigTextStyle);

        mBuilder.setLights(0x88ff0000, 300, 5000);
        mBuilder.setPriority(1);

        if (!silent) {

            if (System.currentTimeMillis() - lastVibrated > 5000) {
                lastVibrated = System.currentTimeMillis();
                mBuilder.setVibrate(pattern);
            }
        }

        //        NotificationCompat.InboxStyle inboxStyle =
        //                new NotificationCompat.InboxStyle();
        //
        //// Sets a title for the Inbox style big view
        //        inboxStyle.setBigContentTitle(msg.getChannel().toString());
        //
        //        inboxStyle.setSummaryText("ka");
        //
        //        inboxStyle.addLine(msg.getDecryptedContent());
        //
        //// Moves the big view style object into the notification object.
        //        mBuilder.setStyle(inboxStyle);
        //        notification.flags = Notification.FLAG_AUTO_CANCEL;
        //        notification.sound = soundUri;
        //notification.sound = Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI, "6");
        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        // ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(msg.getChannel().getId(), notification);
////        Intent resultIntent = new Intent(context, ChatActivity.class);
////        resultIntent.putExtra("title", msg.getChannel().toString());
////        resultIntent.putExtra("Channel", msg.getChannel());
////
////
////// The stack builder object will contain an artificial back stack for the
////// started Activity.
////// This ensures that navigating backward from the Activity leads out of
////// your application to the Home screen.
////        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
////// Adds the back stack for the Intent (but not the Intent itself)
////        stackBuilder.addParentStack(ChatActivity.class);
////// Adds the Intent that starts the Activity to the top of the stack
////        stackBuilder.addNextIntent(resultIntent);
////        PendingIntent resultPendingIntent =
////                stackBuilder.getPendingIntent(
////                0,
////                PendingIntent.FLAG_UPDATE_CURRENT);
////        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.

        Notification build = mBuilder.build();
        //build.flags = Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(msg.getChannel().getId(), build);

    }
}
