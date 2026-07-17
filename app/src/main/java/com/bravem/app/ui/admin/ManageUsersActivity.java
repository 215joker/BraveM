package com.bravem.app.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.adapter.UserAdapter;
import com.bravem.app.data.AuthRepository;
import com.bravem.app.data.DataCallback;
import com.bravem.app.model.User;
import com.bravem.app.utils.UiUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private CircularProgressIndicator progressIndicator;
    private View emptyState;
    private TabLayout tabLayout;
    private FloatingActionButton fabAddAdmin;
    private EditText etSearch;

    private AuthRepository authRepository;
    private List<User> allUsers = new ArrayList<>();
    private String currentTab = User.ROLE_STUDENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        UiUtils.handleTopInset(findViewById(R.id.layout_header));
        UiUtils.handleFabBottomInset(findViewById(R.id.fab_add_admin), 24);

        authRepository = new AuthRepository(this);
        initViews();
        loadUsers();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_users);
        progressIndicator = findViewById(R.id.progress_indicator);
        emptyState = findViewById(R.id.empty_state);
        tabLayout = findViewById(R.id.tab_layout);
        fabAddAdmin = findViewById(R.id.fab_add_admin);
        etSearch = findViewById(R.id.et_search);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_trash).setOnClickListener(v -> startActivity(new Intent(this, DeletedUsersActivity.class)));

        adapter = new UserAdapter(new UserAdapter.OnUserActionListener() {
            @Override
            public void onDelete(User user) {
                confirmMarkDeletion(user);
            }

            @Override
            public void onSuspend(User user) {
                if (user.isSuspended()) {
                    restoreUser(user);
                } else {
                    showSuspendDialog(user);
                }
            }

            @Override
            public void onRoleChange(User user, String newRole) {
                authRepository.updateUserRole(user.getUid(), newRole, new DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        loadUsers();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(ManageUsersActivity.this, "Failed to update role", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition() == 0 ? User.ROLE_STUDENT : User.ROLE_ADMIN;
                fabAddAdmin.setVisibility(tab.getPosition() == 1 ? View.VISIBLE : View.GONE);
                filterUsers();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        fabAddAdmin.setOnClickListener(v -> showAddAdminDialog());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUsers() {
        progressIndicator.setVisibility(View.VISIBLE);
        authRepository.fetchAllUsers(new DataCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                progressIndicator.setVisibility(View.GONE);
                allUsers = result;
                filterUsers();
            }

            @Override
            public void onError(Exception e) {
                progressIndicator.setVisibility(View.GONE);
                Toast.makeText(ManageUsersActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterUsers() {
        List<User> filtered = new ArrayList<>();
        for (User u : allUsers) {
            if (u.getRole().equals(currentTab) && !u.isDeletionRequested()) {
                filtered.add(u);
            }
        }
        adapter.submitList(filtered);
        emptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void confirmMarkDeletion(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Mark for Deletion")
                .setMessage("Mark " + user.getFullName() + " for deletion? They will be removed in 30 days.")
                .setPositiveButton("Mark", (dialog, which) -> {
                    authRepository.markUserForDeletion(user, new DataCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(ManageUsersActivity.this, "User marked for deletion", Toast.LENGTH_SHORT).show();
                            loadUsers();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(ManageUsersActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showSuspendDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Suspend User")
                .setMessage("Suspend " + user.getFullName() + " for 14 days?")
                .setPositiveButton("Suspend", (dialog, which) -> {
                    long duration = 14L * 24 * 60 * 60 * 1000;
                    authRepository.suspendUser(user, duration, new DataCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(ManageUsersActivity.this, "User suspended", Toast.LENGTH_SHORT).show();
                            loadUsers();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(ManageUsersActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void restoreUser(User user) {
        authRepository.restoreUser(user, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(ManageUsersActivity.this, "User restored", Toast.LENGTH_SHORT).show();
                loadUsers();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ManageUsersActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddAdminDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_admin, null);
        EditText nameInput = dialogView.findViewById(R.id.input_name);
        EditText emailInput = dialogView.findViewById(R.id.input_email);
        EditText passwordInput = dialogView.findViewById(R.id.input_password);

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_admin)
                .setView(dialogView)
                .setPositiveButton(R.string.add, (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String email = emailInput.getText().toString().trim();
                    String pass = passwordInput.getText().toString().trim();

                    if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                        Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // This is simplified, usually register would handle role or there's a specific admin creation
                    authRepository.register(name, email, pass, new DataCallback<User>() {
                        @Override
                        public void onSuccess(User result) {
                            authRepository.updateUserRole(result.getUid(), User.ROLE_ADMIN, new DataCallback<Void>() {
                                @Override
                                public void onSuccess(Void res) {
                                    loadUsers();
                                }
                                @Override
                                public void onError(Exception e) {}
                            });
                        }
                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(ManageUsersActivity.this, "Failed to add admin", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
