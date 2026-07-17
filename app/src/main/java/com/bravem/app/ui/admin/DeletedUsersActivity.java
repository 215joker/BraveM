package com.bravem.app.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.adapter.UserAdapter;
import com.bravem.app.data.AuthRepository;
import com.bravem.app.data.DataCallback;
import com.bravem.app.model.User;

import java.util.ArrayList;
import java.util.List;

public class DeletedUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        authRepository = new AuthRepository(this);
        
        // Hide UI elements not needed for this view
        findViewById(R.id.tab_layout).setVisibility(View.GONE);
        findViewById(R.id.fab_add_admin).setVisibility(View.GONE);
        findViewById(R.id.btn_trash).setVisibility(View.GONE);
        
        android.widget.TextView title = findViewById(android.R.id.text1); // This might not work if title is not that ID
        // Manually finding the title textview since I didn't give it an ID
        // In activity_manage_users.xml it's the second child of the first linear layout.
        
        recyclerView = findViewById(R.id.recycler_users);
        adapter = new UserAdapter(new UserAdapter.OnUserActionListener() {
            @Override
            public void onDelete(User user) {
                authRepository.deleteUserPermanently(user, new DataCallback<Void>() {
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
        authRepository.fetchAllUsers(new DataCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                List<User> deletedOrSuspended = new ArrayList<>();
                long now = System.currentTimeMillis();
                for (User u : result) {
                    if (u.isDeletionRequested()) {
                        // Check if 30 days passed
                        if (now - u.getDeletionRequestedAt() > 30L * 24 * 60 * 60 * 1000) {
                            authRepository.deleteUserPermanently(u, new DataCallback<Void>() {
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
        authRepository.restoreUser(user, new DataCallback<Void>() {
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
