package com.bravem.app.ui.community;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.adapter.StudentAdapter;
import com.bravem.app.data.CommunityRepository;
import com.bravem.app.data.DataCallback;
import com.bravem.app.model.User;
import com.bravem.app.utils.UiUtils;

import java.util.List;

public class CommunityActivity extends AppCompatActivity {

    private RecyclerView recyclerRecommendations;
    private RecyclerView recyclerResults;
    private StudentAdapter recommendationsAdapter;
    private StudentAdapter friendsAdapter;
    private StudentAdapter searchAdapter;
    private CommunityRepository repository;
    private EditText etSearch;
    private View emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        repository = new CommunityRepository(this);

        UiUtils.handleTopInset(findViewById(R.id.app_bar));
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        etSearch = findViewById(R.id.et_search);
        emptyState = findViewById(R.id.empty_state);

        View recommendationsHeader = findViewById(R.id.text_recommendations_header);
        recyclerRecommendations = findViewById(R.id.recycler_recommendations);
        TextView resultsHeader = findViewById(R.id.text_results_header);
        recyclerResults = findViewById(R.id.recycler_students);

        setupRecyclers();
        loadRecommendations();
        loadFriends();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    recommendationsHeader.setVisibility(View.VISIBLE);
                    recyclerRecommendations.setVisibility(View.VISIBLE);
                    resultsHeader.setText(R.string.friends);
                    recyclerResults.setAdapter(friendsAdapter);
                    loadFriends();
                } else {
                    recommendationsHeader.setVisibility(View.GONE);
                    recyclerRecommendations.setVisibility(View.GONE);
                    resultsHeader.setText(R.string.search_results);
                    recyclerResults.setAdapter(searchAdapter);
                    search(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclers() {
        recommendationsAdapter = new StudentAdapter(new StudentAdapter.OnStudentActionListener() {
            @Override
            public void onStudentClick(User student) {
                openChat(student);
            }

            @Override
            public void onAddFriendClick(User student) {
                sendFriendRequest(student);
            }
        });

        friendsAdapter = new StudentAdapter(new StudentAdapter.OnStudentActionListener() {
            @Override
            public void onStudentClick(User student) {
                openChat(student);
            }

            @Override
            public void onAddFriendClick(User student) {
                // Not needed for friends
            }
        }, false);

        searchAdapter = new StudentAdapter(new StudentAdapter.OnStudentActionListener() {
            @Override
            public void onStudentClick(User student) {
                openChat(student);
            }

            @Override
            public void onAddFriendClick(User student) {
                sendFriendRequest(student);
            }
        }, true);

        recyclerRecommendations.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecommendations.setAdapter(recommendationsAdapter);

        recyclerResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerResults.setAdapter(friendsAdapter);
    }

    private void loadRecommendations() {
        repository.getRecommendations(new DataCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                recommendationsAdapter.submitList(users);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(CommunityActivity.this, "Failed to load recommendations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFriends() {
        repository.getFriends(new DataCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                friendsAdapter.submitList(users);
                emptyState.setVisibility(users.isEmpty() && etSearch.getText().toString().isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(CommunityActivity.this, "Failed to load friends", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void search(String query) {
        repository.searchStudents(query, new DataCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                searchAdapter.submitList(users);
                emptyState.setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(CommunityActivity.this, "Search failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openChat(User student) {
        android.content.Intent intent = new android.content.Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_USER, student);
        startActivity(intent);
    }

    private void sendFriendRequest(User student) {
        repository.sendFriendRequest(student.getUid(), new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                if (success) {
                    Toast.makeText(CommunityActivity.this, R.string.friend_request_sent, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CommunityActivity.this, "Request already pending or you are already friends", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(CommunityActivity.this, "Failed to send request", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
