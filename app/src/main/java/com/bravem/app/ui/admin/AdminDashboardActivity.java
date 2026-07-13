package com.bravem.app.ui.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bravem.app.R;
import com.bravem.app.data.AuthRepository;
import com.bravem.app.ui.auth.LoginActivity;
import com.bravem.app.utils.SessionManager;

public class AdminDashboardActivity extends AppCompatActivity {

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        authRepository = new AuthRepository(this);
        SessionManager sessionManager = new SessionManager(this);

        findViewById(R.id.card_manage_degrees).setOnClickListener(v ->
                startActivity(new Intent(this, ManageDegreesActivity.class)));

        findViewById(R.id.card_manage_courses).setOnClickListener(v ->
                startActivity(new Intent(this, ManageCoursesActivity.class)));

        findViewById(R.id.card_manage_papers).setOnClickListener(v ->
                startActivity(new Intent(this, ManagePapersActivity.class)));

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            authRepository.logout();
            sessionManager.clear();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
