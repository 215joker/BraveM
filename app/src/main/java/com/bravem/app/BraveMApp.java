package com.bravem.app;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import com.bravem.app.utils.SessionManager;

/**
 * Application entry point.
 */
public class BraveMApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Apply saved theme preference (defaults to Light/White)
        SessionManager sessionManager = new SessionManager(this);
        AppCompatDelegate.setDefaultNightMode(sessionManager.getTheme());
    }
}
