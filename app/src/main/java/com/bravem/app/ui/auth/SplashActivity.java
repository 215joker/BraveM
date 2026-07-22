package com.bravem.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(com.bravem.app.R.layout.activity_splash);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authRepository = new AuthRepository(this);

        new Handler(Looper.getMainLooper()).postDelayed(this::route, SPLASH_DELAY_MS);
    }

    private void route() {
        SessionManager session = new SessionManager(this);

        // Firebase Test Lab Automation: Auto-login for Robo crawler if flag present
        if (getIntent().getBooleanExtra("FIREBASE_TEST_LAB", false)) {
            authRepository.login("test@bravem.com", "password123", new DataCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    session.saveSession(user.getUid(), user.getEmail(), user.getFullName(), user.getRole(),
                            user.getUniversity(), user.getDegreeId(), user.getDegreeName(), user.getIntake());
                    routeBasedOnSession(session);
                }

                @Override
                public void onError(Exception e) {
                    goToLogin();
                }
            });
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
                session.saveSession(user.getUid(), user.getEmail(), user.getFullName(), user.getRole(),
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
