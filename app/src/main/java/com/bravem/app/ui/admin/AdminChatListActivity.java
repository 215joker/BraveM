package com.bravem.app.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.adapter.RecentChatAdapter;
import com.bravem.app.data.AuthRepository;
import com.bravem.app.data.ChatRepository;
import com.bravem.app.data.DataCallback;
import com.bravem.app.model.User;
import com.bravem.app.ui.community.ChatActivity;
import com.bravem.app.utils.UiUtils;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminChatListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecentChatAdapter adapter;
    private View emptyState;
    private CircularProgressIndicator progressIndicator;
    private ChatRepository chatRepository;
    private AuthRepository authRepository;
    private EditText etSearch;
    private List<ChatRepository.RecentChat> allChats;
    private List<User> allStudents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat_list);

        UiUtils.handleTopInset(findViewById(R.id.app_bar));
        UiUtils.handleBottomInset(findViewById(android.R.id.content));

        chatRepository = new ChatRepository(this);
        authRepository = new AuthRepository(this);

        initViews();
        loadChats();
        loadStudents();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_chats);
        emptyState = findViewById(R.id.empty_state);
        progressIndicator = findViewById(R.id.progress_indicator);
        etSearch = findViewById(R.id.et_search);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterChats(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        adapter = new RecentChatAdapter(user -> {
            if (user != null) {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra(ChatActivity.EXTRA_USER, user);
                startActivity(intent);
            } else {
                Toast.makeText(this, "User data is missing", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadChats() {
        progressIndicator.setVisibility(View.VISIBLE);
        chatRepository.getRecentChats(new DataCallback<List<ChatRepository.RecentChat>>() {
            @Override
            public void onSuccess(List<ChatRepository.RecentChat> result) {
                progressIndicator.setVisibility(View.GONE);
                allChats = result;
                filterChats(etSearch.getText().toString());
            }

            @Override
            public void onError(Exception e) {
                progressIndicator.setVisibility(View.GONE);
                Toast.makeText(AdminChatListActivity.this, "Failed to load chats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStudents() {
        authRepository.fetchAllUsers(new DataCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                allStudents = new ArrayList<>();
                for (User u : result) {
                    if (User.ROLE_STUDENT.equals(u.getRole())) {
                        allStudents.add(u);
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                // Ignore
            }
        });
    }

    private void filterChats(String query) {
        if (allChats == null) return;

        if (query.isEmpty()) {
            adapter.submitList(allChats);
            emptyState.setVisibility(allChats.isEmpty() ? View.VISIBLE : View.GONE);
            return;
        }

        List<ChatRepository.RecentChat> filtered = new ArrayList<>();
        Set<String> addedUserIds = new HashSet<>();

        // Add matching recent chats first
        for (ChatRepository.RecentChat chat : allChats) {
            if (chat.user.getFullName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(chat);
                addedUserIds.add(chat.user.getUid());
            }
        }

        // Add matching students who are not in recent chats
        if (allStudents != null) {
            for (User student : allStudents) {
                if (!addedUserIds.contains(student.getUid()) && 
                    student.getFullName().toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(new ChatRepository.RecentChat(student, null, 0));
                }
            }
        }

        adapter.submitList(filtered);
        emptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChats();
    }
}
