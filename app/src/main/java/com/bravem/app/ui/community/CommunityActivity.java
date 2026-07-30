package com.bravem.app.ui.community;

import android.content.Intent;
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
import com.bravem.app.adapter.GroupAdapter;
import com.bravem.app.adapter.StudentAdapter;
import com.bravem.app.data.ChatRepository;
import com.bravem.app.data.CommunityRepository;
import com.bravem.app.data.DataCallback;
import com.bravem.app.domain.model.User;
import com.bravem.app.model.Group;
import com.bravem.app.utils.UiUtils;

import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class CommunityActivity extends AppCompatActivity {

    private RecyclerView recyclerRecommendations;
    private RecyclerView recyclerResults;
    private RecyclerView recyclerGroups;
    private StudentAdapter recommendationsAdapter;
    private StudentAdapter friendsAdapter;
    private StudentAdapter searchAdapter;
    private GroupAdapter groupAdapter;
    private CommunityRepository repository;
    private ChatRepository chatRepository;
    private EditText etSearch;
    private View emptyState;
    private View fabCreateGroup;
    private TabLayout tabLayout;
    private boolean crossUniversity = false;
    private int selectedTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        repository = new CommunityRepository(this);
        chatRepository = new ChatRepository(this);

        UiUtils.handleTopInset(findViewById(R.id.app_bar));
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        etSearch = findViewById(R.id.et_search);
        emptyState = findViewById(R.id.empty_state);
        tabLayout = findViewById(R.id.tab_layout);

        View recommendationsHeader = findViewById(R.id.text_recommendations_header);
        recyclerRecommendations = findViewById(R.id.recycler_recommendations);
        TextView resultsHeader = findViewById(R.id.text_results_header);
        recyclerResults = findViewById(R.id.recycler_students);
        recyclerGroups = findViewById(R.id.recycler_groups);
        fabCreateGroup = findViewById(R.id.fab_create_group);

        UiUtils.handleFabBottomInset(fabCreateGroup, 16);

        setupRecyclers();
        loadRecommendations();
        loadFriends();
        loadGroups();

        fabCreateGroup.setOnClickListener(v -> showCreateGroupDialog());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                crossUniversity = selectedTab == 1;
                
                if (selectedTab == 2) {
                    recommendationsHeader.setVisibility(View.GONE);
                    recyclerRecommendations.setVisibility(View.GONE);
                    resultsHeader.setVisibility(View.GONE);
                    recyclerResults.setVisibility(View.GONE);
                    recyclerGroups.setVisibility(View.VISIBLE);
                    fabCreateGroup.setVisibility(View.VISIBLE);
                } else {
                    recommendationsHeader.setVisibility(View.VISIBLE);
                    recyclerRecommendations.setVisibility(View.VISIBLE);
                    resultsHeader.setVisibility(View.VISIBLE);
                    recyclerResults.setVisibility(View.VISIBLE);
                    recyclerGroups.setVisibility(View.GONE);
                    fabCreateGroup.setVisibility(View.GONE);
                    loadRecommendations();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

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

        groupAdapter = new GroupAdapter(this::openGroupChat);
        recyclerGroups.setLayoutManager(new LinearLayoutManager(this));
        recyclerGroups.setAdapter(groupAdapter);
    }

    private void loadRecommendations() {
        repository.getRecommendations(crossUniversity, new DataCallback<>() {
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
        repository.getFriends(new DataCallback<>() {
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
        repository.searchStudents(query, new DataCallback<>() {
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

    private void loadGroups() {
        chatRepository.getGroups(new DataCallback<>() {
            @Override
            public void onSuccess(List<Group> groups) {
                groupAdapter.submitList(groups);
            }

            @Override
            public void onError(Exception e) {
                // Silently fail
            }
        });
    }

    private void showCreateGroupDialog() {
        repository.getFriends(new DataCallback<>() {
            @Override
            public void onSuccess(List<User> friends) {
                if (friends.size() < 2) {
                    Toast.makeText(CommunityActivity.this, "You need at least 2 friends to create a group", Toast.LENGTH_LONG).show();
                    return;
                }
                showGroupCreationUI(friends);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(CommunityActivity.this, "Failed to load friends", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showGroupCreationUI(List<User> friends) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_group, null);
        EditText editGroupName = dialogView.findViewById(R.id.edit_group_name);
        RecyclerView recyclerFriends = dialogView.findViewById(R.id.recycler_friend_selector);

        com.bravem.app.adapter.FriendSelectorAdapter adapter = new com.bravem.app.adapter.FriendSelectorAdapter(friends);
        recyclerFriends.setLayoutManager(new LinearLayoutManager(this));
        recyclerFriends.setAdapter(adapter);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Create Group")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = editGroupName.getText().toString().trim();
                    List<String> selectedIds = adapter.getSelectedIds();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Group name required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (selectedIds.size() < 2) {
                        Toast.makeText(this, "Select at least 2 friends", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    chatRepository.createGroup(name, null, selectedIds, new DataCallback<>() {
                        @Override
                        public void onSuccess(com.bravem.app.model.Group group) {
                            loadGroups();
                            Toast.makeText(CommunityActivity.this, "Group created", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(CommunityActivity.this, "Failed to create group", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openChat(User student) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_USER, student);
        startActivity(intent);
    }

    private void openGroupChat(Group group) {
        Intent intent = new Intent(this, GroupChatActivity.class);
        intent.putExtra(GroupChatActivity.EXTRA_GROUP, group);
        startActivity(intent);
    }

    private void sendFriendRequest(User student) {
        repository.sendFriendRequest(student.getUid(), new DataCallback<>() {
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
