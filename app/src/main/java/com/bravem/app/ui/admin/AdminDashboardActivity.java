package com.bravem.app.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bravem.app.R;
import com.bravem.app.data.AuthRepository;
import com.bravem.app.data.DataCallback;
import com.bravem.app.data.DegreeRepository;
import com.bravem.app.data.PaperRepository;
import com.bravem.app.model.Degree;
import com.bravem.app.model.PastPaper;
import com.bravem.app.model.User;
import com.bravem.app.ui.auth.LoginActivity;
import com.bravem.app.utils.SessionManager;
import com.bravem.app.utils.UiUtils;

import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private AuthRepository authRepository;
    private DegreeRepository degreeRepository;
    private PaperRepository paperRepository;
    private SessionManager sessionManager;

    private TextView tvStatUsers, tvStatDegrees, tvStatUniversities, tvStatPapers, tvUnreadChats;
    private View cardManageChats;
    private com.bravem.app.data.ChatRepository chatRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        UiUtils.handleTopInset(findViewById(R.id.app_bar));
        UiUtils.handleBottomInset(findViewById(android.R.id.content));

        authRepository = new AuthRepository(this);
        degreeRepository = new DegreeRepository(this);
        paperRepository = new PaperRepository(this);
        chatRepository = new com.bravem.app.data.ChatRepository(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupClickListeners();
        refreshStats();
    }

    private void initViews() {
        tvStatUsers = findViewById(R.id.tv_stat_users);
        tvStatDegrees = findViewById(R.id.tv_stat_degrees);
        tvStatUniversities = findViewById(R.id.tv_stat_universities);
        tvStatPapers = findViewById(R.id.tv_stat_papers);
        tvUnreadChats = findViewById(R.id.tv_unread_chats);
        cardManageChats = findViewById(R.id.card_manage_chats);
    }

    private void setupClickListeners() {
        findViewById(R.id.card_manage_degrees).setOnClickListener(v ->
                startActivity(new Intent(this, ManageDegreesActivity.class)));

        findViewById(R.id.card_manage_universities).setOnClickListener(v ->
                startActivity(new Intent(this, ManageUniversitiesActivity.class)));

        findViewById(R.id.card_manage_papers).setOnClickListener(v ->
                startActivity(new Intent(this, ManagePapersActivity.class)));

        findViewById(R.id.card_manage_users).setOnClickListener(v ->
                startActivity(new Intent(this, ManageUsersActivity.class)));

        cardManageChats.setOnClickListener(v ->
                startActivity(new Intent(this, AdminChatListActivity.class)));

        findViewById(R.id.card_search_logs).setOnClickListener(v ->
                startActivity(new Intent(this, SearchLogsActivity.class)));

        findViewById(R.id.btn_sync).setOnClickListener(v -> {
            Toast.makeText(this, "Synchronizing database...", Toast.LENGTH_SHORT).show();
            refreshStats();
        });

        findViewById(R.id.btn_clear_cache).setOnClickListener(v ->
                startActivity(new Intent(this, ClearTempFilesActivity.class)));

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            authRepository.logout();
            sessionManager.clear();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void refreshStats() {
        authRepository.fetchAllUsers(new DataCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                int studentCount = 0;
                for (User user : result) {
                    if (User.ROLE_STUDENT.equals(user.getRole()) && !user.isDeletionRequested()) {
                        studentCount++;
                    }
                }
                tvStatUsers.setText(String.valueOf(studentCount));
            }

            @Override
            public void onError(Exception e) {
                tvStatUsers.setText("0");
            }
        });

        degreeRepository.fetchAllDegrees(new DataCallback<List<Degree>>() {
            @Override
            public void onSuccess(List<Degree> result) {
                tvStatDegrees.setText(String.valueOf(result.size()));
            }

            @Override
            public void onError(Exception e) {
                tvStatDegrees.setText("0");
            }
        });

        degreeRepository.fetchAllUniversities(new DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                tvStatUniversities.setText(String.valueOf(result.size()));
            }

            @Override
            public void onError(Exception e) {
                tvStatUniversities.setText("0");
            }
        });

        paperRepository.fetchAllPapersForAdmin(new DataCallback<List<PastPaper>>() {
            @Override
            public void onSuccess(List<PastPaper> result) {
                tvStatPapers.setText(String.valueOf(result.size()));
            }

            @Override
            public void onError(Exception e) {
                tvStatPapers.setText("0");
            }
        });

        chatRepository.getTotalUnreadCount(new DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                if (count > 0) {
                    tvUnreadChats.setVisibility(View.VISIBLE);
                    tvUnreadChats.setText(String.valueOf(count));
                } else {
                    tvUnreadChats.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Exception e) {
                tvUnreadChats.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStats();
    }
}

