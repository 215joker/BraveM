package com.bravem.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.bravem.app.data.sync.SyncManager;
import com.bravem.app.utils.SessionManager;

/**
 * Application entry point.
 */
public class BraveMApp extends Application {

    public static final String CHANNEL_ID = "downloads_channel";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        
        // Disable Crashlytics in debug mode to prevent the "Build ID missing" crash
        // This allows the app to run during development even if the plugin task fails
        boolean isDebug = (getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!isDebug);
        
        // Apply saved theme preference (defaults to Light/White)
        SessionManager sessionManager = new SessionManager(this);
        AppCompatDelegate.setDefaultNightMode(sessionManager.getTheme());

        createNotificationChannel();
        SyncManager.scheduleSync(this);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "File Downloads";
            String description = "Notifications for finished downloads";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
