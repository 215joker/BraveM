package com.bravem.app.ui.supervisor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bravem.app.R;
import com.bravem.app.ui.auth.LoginActivity;
import com.bravem.app.utils.SessionManager;
import com.bravem.app.utils.UiUtils;

public class SupervisorDashboardActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextView tvStatusFirebase, tvApiLatency, tvActiveWorkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiUtils.applyEdgeToEdge(this);
        setContentView(R.layout.activity_supervisor_dashboard);

        sessionManager = new SessionManager(this);
        
        // Security check: Only allow 'supervisor' or 'developer' role
        String role = sessionManager.getRole();
        if (!"supervisor".equalsIgnoreCase(role) && !"developer".equalsIgnoreCase(role)) {
            Toast.makeText(this, "Unauthorised: Developer access required", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadSystemHealth();
    }

    private void initViews() {
        tvStatusFirebase = findViewById(R.id.tv_status_firebase);
        tvApiLatency = findViewById(R.id.tv_api_latency);
        tvActiveWorkers = findViewById(R.id.tv_active_workers);
    }

    private void setupListeners() {
        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.card_feature_toggles).setOnClickListener(v -> 
            showFeatureToggles());

        findViewById(R.id.card_version_control).setOnClickListener(v -> 
            showVersionControl());

        findViewById(R.id.card_worker_monitor).setOnClickListener(v -> 
            showWorkerMonitor());

        findViewById(R.id.card_backup_logs).setOnClickListener(v -> 
            showSystemLogs());

        findViewById(R.id.btn_cache_manager).setOnClickListener(v -> 
            Toast.makeText(this, "Cache Invalidation Triggered", Toast.LENGTH_SHORT).show());
            
        findViewById(R.id.btn_maintenance_mode).setOnClickListener(v -> 
            Toast.makeText(this, "Maintenance Mode Toggled", Toast.LENGTH_SHORT).show());
    }

    private void loadSystemHealth() {
        // In a real app, this would fetch from Firebase Remote Config or a custom Monitoring API
        tvStatusFirebase.setText(R.string.status_online);
        tvApiLatency.setText("38ms");
        tvActiveWorkers.setText("3");
    }

    private void showFeatureToggles() {
        Toast.makeText(this, "Feature Toggles UI Coming Soon", Toast.LENGTH_SHORT).show();
    }

    private void showVersionControl() {
        Toast.makeText(this, "Version Control UI Coming Soon", Toast.LENGTH_SHORT).show();
    }

    private void showWorkerMonitor() {
        Toast.makeText(this, "Worker Monitor UI Coming Soon", Toast.LENGTH_SHORT).show();
    }

    private void showSystemLogs() {
        Toast.makeText(this, "System Logs UI Coming Soon", Toast.LENGTH_SHORT).show();
    }
}
