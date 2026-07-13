package com.bravem.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.bravem.app.data.AuthRepository;
import com.bravem.app.data.DataCallback;
import com.bravem.app.model.User;
import com.bravem.app.ui.admin.AdminDashboardActivity;
import com.bravem.app.ui.dashboard.DashboardActivity;
import com.bravem.app.ui.auth.SelectUniversityActivity;
import com.bravem.app.utils.SessionManager;

/**
 * Entry point activity. Decides where to route the user.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 700;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.bravem.app.R.layout.activity_splash);

        authRepository = new AuthRepository(this);

        new Handler(Looper.getMainLooper()).postDelayed(this::route, SPLASH_DELAY_MS);
    }

    private void route() {
        SessionManager session = new SessionManager(this);

        if (session.getUniversity() == null) {
            startActivity(new Intent(this, SelectUniversityActivity.class));
            finish();
            return;
        }

        if (!authRepository.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        authRepository.fetchCurrentUserProfile(new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    goToLogin();
                    return;
                }
                session.saveSession(user.getUid(), user.getFullName(), user.getRole(),
                        user.getUniversity(), user.getDegreeId(), user.getDegreeName(), user.getIntake());
                routeBasedOnSession(session);
            }

            @Override
            public void onError(Exception e) {
                goToLogin();
            }
        });
    }

    private void routeBasedOnSession(SessionManager session) {
        if (session.isAdmin()) {
            startActivity(new Intent(this, AdminDashboardActivity.class));
        } else if (!session.hasSelectedDegree()) {
            startActivity(new Intent(this, SelectDegreeActivity.class));
        } else {
            startActivity(new Intent(this, DashboardActivity.class));
        }
        finish();
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
