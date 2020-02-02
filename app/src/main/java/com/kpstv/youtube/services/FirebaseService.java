package com.kpstv.youtube.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.kpstv.youtube.R;
import com.kpstv.youtube.receivers.SongBroadCast;

public class FirebaseService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Intent intent = new Intent(getApplicationContext(), SongBroadCast.class);
        if (remoteMessage.getNotification().getTitle().toLowerCase().contains("update"))
            intent.setAction("com.kpstv.youtube.SHOW_UPDATE");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                10,intent,0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_01")
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setContentIntent(pendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.drawable.ic_download)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(2, notificationBuilder.build());


        super.onMessageReceived(remoteMessage);
    }

}
