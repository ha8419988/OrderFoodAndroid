package com.example.orderfoodandroid.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.orderfoodandroid.R;

public class NotificationHelper extends ContextWrapper {

    private static final String CHANEL_ID = "orderfood.Android";
    private static final String CHANEL_NAME = "Orderfood Android";
    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)//only working when Api >=26
            createChannel();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel orderFoodChannel = new NotificationChannel(CHANEL_ID,
                CHANEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        orderFoodChannel.enableLights(false);
        orderFoodChannel.enableVibration(true);
        orderFoodChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(orderFoodChannel);

    }

    public NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public android.app.Notification.Builder getNotificationChannel(String title, String body, PendingIntent contenIntent
            , Uri soundUri) {
        return new android.app.Notification.Builder(getApplicationContext(),
                CHANEL_ID).setContentIntent(contenIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setAutoCancel(false);
    }
}
