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
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.TaskStackBuilder;
import org.redPandaLib.NewMessageListener;
import org.redPandaLib.core.messages.TextMessageContent;

/**
 *
 * @author robin
 */
public class PopupListener implements NewMessageListener {

    Context context;

    public PopupListener(Context context) {
        this.context = context;
    }

    public void newMessage(TextMessageContent msg) {

        if (msg.fromMe) {
            return;
        }

        if (BS.currentViewedChannel == msg.getChannel().getId()) {
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
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Set the info for the views that show in the notification panel.




        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.icon).setContentTitle(msg.getChannel().toString()).setContentText(msg.getText()).setSound(soundUri).setContentIntent(contentIntent);



        BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();


        bigTextStyle.bigText(msg.getName() + ": " + msg.getText());
        bigTextStyle.setBigContentTitle(msg.getChannel().toString());
        bigTextStyle.setSummaryText("message from redPanda");
        mBuilder.setStyle(bigTextStyle);

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

        Intent resultIntent = new Intent(context, ChatActivity.class);
        resultIntent.putExtra("title", msg.getChannel().toString());
        resultIntent.putExtra("Channel", msg.getChannel());


// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ChatActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.


        Notification build = mBuilder.build();
        build.flags = Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(msg.getChannel().getId(), build);

    }
}
