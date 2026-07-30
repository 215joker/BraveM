package com.bravem.app.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.adapter.UserAdapter;
import com.bravem.app.data.AuthRepositoryImpl;
import com.bravem.app.data.DataCallback;
import com.bravem.app.domain.model.User;
import com.bravem.app.domain.repository.UserRepository;
import com.bravem.app.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class DeletedUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        UiUtils.handleTopInset(findViewById(R.id.app_bar));
        
        userRepository = new AuthRepositoryImpl(this);
        
        // Hide UI elements not needed for this view
        findViewById(R.id.tab_layout).setVisibility(View.GONE);
        findViewById(R.id.fab_add_admin).setVisibility(View.GONE);
        findViewById(R.id.btn_trash).setVisibility(View.GONE);
        findViewById(R.id.et_search).setVisibility(View.GONE);
        
        TextView title = findViewById(R.id.text_title);
        if (title != null) title.setText("Deleted & Suspended Users");
        
        recyclerView = findViewById(R.id.recycler_users);
        adapter = new UserAdapter(new UserAdapter.OnUserActionListener() {
            @Override
            public void onDelete(User user) {
                userRepository.deleteUserPermanently(user, new DataCallback<>() {
                    @Override
                    public void onSuccess(Void result) {
                        loadDeletedUsers();
                    }
                    @Override
                    public void onError(Exception e) {}
                });
            }

            @Override
            public void onSuspend(User user) {
                restoreUser(user);
            }

            @Override
            public void onRoleChange(User user, String newRole) {}
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        loadDeletedUsers();
    }

    private void loadDeletedUsers() {
        userRepository.fetchAllUsers(new DataCallback<>() {
            @Override
            public void onSuccess(List<User> result) {
                List<User> deletedOrSuspended = new ArrayList<>();
                long now = System.currentTimeMillis();
                for (User u : result) {
                    if (u.getDeletionRequestedAt() > 0) {
                        // Check if 30 days passed
                        if (now - u.getDeletionRequestedAt() > 30L * 24 * 60 * 60 * 1000) {
                            userRepository.deleteUserPermanently(u, new DataCallback<>() {
                                @Override
                                public void onSuccess(Void res) {}
                                @Override
                                public void onError(Exception e) {}
                            });
                        } else {
                            deletedOrSuspended.add(u);
                        }
                    } else if (u.isSuspended()) {
                        deletedOrSuspended.add(u);
                    }
                }
                adapter.submitList(deletedOrSuspended);
            }

            @Override
            public void onError(Exception e) {}
        });
    }

    private void restoreUser(User user) {
        userRepository.restoreUser(user, new DataCallback<>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(DeletedUsersActivity.this, "User restored", Toast.LENGTH_SHORT).show();
                loadDeletedUsers();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(DeletedUsersActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
