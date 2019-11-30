package com.kpstv.youtube;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;


public class AppNotify extends Application {
    public static final String CHANNEL_ID = "downloadServiceChannel";
    public static NotificationManager appnotification;
    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Download Service",
                    NotificationManager.IMPORTANCE_LOW
            );

            appnotification = getSystemService(NotificationManager.class);
            appnotification.createNotificationChannel(serviceChannel);
        }
    }
}
