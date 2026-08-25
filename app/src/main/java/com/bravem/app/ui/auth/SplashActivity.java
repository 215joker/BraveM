package com.bravem.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bravem.app.data.AuthRepositoryImpl;
import com.bravem.app.data.DataCallback;
import com.bravem.app.domain.model.User;
import com.bravem.app.domain.repository.UserRepository;
import com.bravem.app.ui.admin.AdminDashboardActivity;
import com.bravem.app.ui.dashboard.DashboardActivity;
import com.bravem.app.ui.supervisor.SupervisorDashboardActivity;
import com.bravem.app.utils.SessionManager;

/**
 * Entry point activity. Decides where to route the user.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 700;

    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(com.bravem.app.R.layout.activity_splash);

        View root = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userRepository = new AuthRepositoryImpl(this);

        new Handler(Looper.getMainLooper()).postDelayed(this::route, SPLASH_DELAY_MS);
    }

    private void route() {
        SessionManager session = new SessionManager(this);

        // Firebase Test Lab Automation
        if (getIntent().getBooleanExtra("FIREBASE_TEST_LAB", false)) {
            userRepository.login("test@bravem.com", "password123", new DataCallback<>() {
                @Override
                public void onSuccess(User user) {
                    routeBasedOnSession(session);
                }

                @Override
                public void onError(Exception e) {
                    goToLogin();
                }
            });
            return;
        }

        if (!userRepository.isUserLoggedIn()) {
            goToLogin();
            return;
        }

        userRepository.getCurrentUser(new DataCallback<>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    goToLogin();
                    return;
                }
                routeBasedOnSession(session);
            }

            @Override
            public void onError(Exception e) {
                goToLogin();
            }
        });
    }

    private void routeBasedOnSession(SessionManager session) {
        // check session
        String role = session.getRole();
        if ("supervisor".equalsIgnoreCase(role) || "developer".equalsIgnoreCase(role)) {
            startActivity(new Intent(this, SupervisorDashboardActivity.class));
        } else if ("admin".equalsIgnoreCase(role)) {
            startActivity(new Intent(this, AdminDashboardActivity.class));
        } else if (session.getDegreeId() == null || session.getDegreeId().isEmpty()) {
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
