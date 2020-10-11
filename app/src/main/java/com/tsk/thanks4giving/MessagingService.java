package com.tsk.thanks4giving;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.TaskStackBuilder;
import androidx.lifecycle.Lifecycle;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {

    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d("fcm", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {

            Log.d("fcm", "Message data payload: " + remoteMessage.getData());
            Log.d("fcm", "Message data payload: " + remoteMessage.getData().get("postID"));
            String postID = remoteMessage.getData().get("postID");

            Intent resultIntent = new Intent(this, MainActivity.class);
            resultIntent.setAction("fromNotif");
            resultIntent.putExtra("post", postID);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            // if the application is not in foreground post notification
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification.Builder builder = new Notification.Builder(this);
            builder.setPriority(Notification.PRIORITY_MAX);

            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel channel = new NotificationChannel("id_1", "name_1", NotificationManager.IMPORTANCE_HIGH);
                manager.createNotificationChannel(channel);
                builder.setChannelId("id_1");
            }
            builder.setContentTitle(getString(R.string.new_comment_title)).setContentText(remoteMessage.getData().get("message")).setSmallIcon(R.drawable.ic_giftbox_outline);
            builder.setContentIntent(resultPendingIntent);
            builder.setAutoCancel(true);
            manager.notify(1, builder.build());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("fcm", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }
}
