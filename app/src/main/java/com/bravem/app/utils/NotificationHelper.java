package com.bravem.app.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.bravem.app.BraveMApp;
import com.bravem.app.R;

public class NotificationHelper {

    public static void showNotification(Context context, String title, String message) {
        showNotification(context, title, message, null);
    }

    public static void showNotification(Context context, String title, String message, Intent intent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, BraveMApp.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_document)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        if (intent != null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 
                    (int) System.currentTimeMillis(), 
                    intent, 
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            builder.setContentIntent(pendingIntent);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
