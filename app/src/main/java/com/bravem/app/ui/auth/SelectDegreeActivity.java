package com.bravem.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.adapter.DegreeAdapter;
import com.bravem.app.data.AuthRepositoryImpl;
import com.bravem.app.data.DataCallback;
import com.bravem.app.data.DegreeRepositoryImpl;
import com.bravem.app.domain.model.Degree;
import com.bravem.app.domain.repository.DegreeRepository;
import com.bravem.app.domain.repository.UserRepository;
import com.bravem.app.ui.dashboard.DashboardActivity;
import com.bravem.app.utils.SessionManager;
import com.bravem.app.utils.UiUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Shown once, right after registration. Sets the student's degree and intake.
 */
public class SelectDegreeActivity extends AppCompatActivity {

    public static final String EXTRA_BROWSE_MODE = "extra_browse_mode";

    private RecyclerView recyclerView;
    private DegreeAdapter adapter;
    private MaterialButton continueButton;
    private View btnAddMissing;
    private CircularProgressIndicator loadingIndicator;
    private View emptyState;
    private android.widget.EditText searchInput;

    private DegreeRepository degreeRepository;
    private UserRepository authRepository;
    private SessionManager sessionManager;

    private Degree selectedDegree;
    private String regName, regEmail, regPassword;
    private boolean isBrowseMode = false;
    private List<Degree> allDegrees = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_degree);

        UiUtils.handleTopInset(findViewById(R.id.app_bar));
        UiUtils.handleBottomInset(findViewById(R.id.btn_continue));

        isBrowseMode = getIntent().getBooleanExtra(EXTRA_BROWSE_MODE, false);
        regName = getIntent().getStringExtra(RegisterActivity.EXTRA_NAME);
        regEmail = getIntent().getStringExtra(RegisterActivity.EXTRA_EMAIL);
        regPassword = getIntent().getStringExtra(RegisterActivity.EXTRA_PASSWORD);

        degreeRepository = new DegreeRepositoryImpl(this);
        authRepository = new AuthRepositoryImpl(this);
        sessionManager = new SessionManager(this);

        recyclerView = findViewById(R.id.recycler_degrees);
        continueButton = findViewById(R.id.btn_continue);
        btnAddMissing = findViewById(R.id.btn_add_missing);
        loadingIndicator = findViewById(R.id.progress_loading);
        emptyState = findViewById(R.id.empty_state);
        searchInput = findViewById(R.id.edit_search);

        btnAddMissing.setOnClickListener(v -> showAddDegreeDialog());

        adapter = new DegreeAdapter(degree -> {
            selectedDegree = degree;
            adapter.setSelectedDegreeId(degree.getId());
            continueButton.setEnabled(true);
            showIntakeBottomSheet();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDegrees(s.toString().trim());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        continueButton.setEnabled(false);
        continueButton.setOnClickListener(v -> showIntakeBottomSheet());

        loadDegrees();
    }

    private void loadDegrees() {
        loadingIndicator.setVisibility(View.VISIBLE);
        String university = sessionManager.getUniversity();
        degreeRepository.fetchDegreesByUniversity(university, new DataCallback<>() {
            @Override
            public void onSuccess(List<Degree> degrees) {
                loadingIndicator.setVisibility(View.GONE);
                allDegrees = degrees;
                adapter.submitList(degrees);
                emptyState.setVisibility(degrees.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception e) {
                loadingIndicator.setVisibility(View.GONE);
                Toast.makeText(SelectDegreeActivity.this, R.string.error_generic, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void filterDegrees(String query) {
        if (query.isEmpty()) {
            adapter.submitList(allDegrees, false);
            return;
        }

        List<Degree> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Degree d : allDegrees) {
            if (d.getName().toLowerCase().contains(lowerQuery)) {
                filtered.add(d);
            }
        }
        adapter.submitList(filtered, true);
    }

    private void showAddDegreeDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_BraveM_BottomSheetDialog);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_degree, null);
        dialog.setContentView(dialogView);

        TextInputEditText editSchool = dialogView.findViewById(R.id.edit_school);
        TextInputEditText editDegreeName = dialogView.findViewById(R.id.edit_degree_name);
        TextInputEditText editIntake = dialogView.findViewById(R.id.edit_intake);
        View btnSave = dialogView.findViewById(R.id.btn_save);

        btnSave.setOnClickListener(v -> {
            String school = editSchool.getText() != null ? editSchool.getText().toString().trim() : "";
            String name = editDegreeName.getText() != null ? editDegreeName.getText().toString().trim() : "";
            String intake = editIntake.getText() != null ? editIntake.getText().toString().trim() : "";

            if (school.isEmpty() || name.isEmpty() || intake.isEmpty()) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();
            loadingIndicator.setVisibility(View.VISIBLE);

            String university = sessionManager.getUniversity();
            degreeRepository.addDegree(name, school, university, new DataCallback<>() {
                @Override
                public void onSuccess(Degree degree) {
                    selectedDegree = degree;
                    loadDegrees();
                    confirmSelection(intake);
                }

                @Override
                public void onError(Exception e) {
                    loadingIndicator.setVisibility(View.GONE);
                    Toast.makeText(SelectDegreeActivity.this, R.string.failed_to_add_degree, Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showIntakeBottomSheet() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_intake, null);
        RecyclerView intakeRecycler = dialogView.findViewById(R.id.recycler_intakes);
        
        List<String> intakes = new ArrayList<>();
        for (int year = 1; year <= 7; year++) {
            intakes.add(year + ".1");
            intakes.add(year + ".2");
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_BraveM_BottomSheetDialog);
        dialog.setContentView(dialogView);

        intakeRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        intakeRecycler.setAdapter(new RecyclerView.Adapter<IntakeViewHolder>() {
            @NonNull
            @Override
            public IntakeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_intake_chip, parent, false);
                return new IntakeViewHolder(v);
            }

            @Override
            public void onBindViewHolder(@NonNull IntakeViewHolder holder, int position) {
                String intake = intakes.get(position);
                holder.text.setText(intake);
                holder.itemView.setOnClickListener(v -> {
                    dialog.dismiss();
                    confirmSelection(intake);
                });
            }

            @Override
            public int getItemCount() { return intakes.size(); }
        });

        dialog.show();
    }

    private void confirmSelection(String intake) {
        if (selectedDegree == null) return;

        if (isBrowseMode) {
            Intent intent = new Intent(this, com.bravem.app.ui.papers.DegreePapersActivity.class);
            intent.putExtra("extra_degree_id", selectedDegree.getId());
            intent.putExtra("extra_degree_name", selectedDegree.getName());
            intent.putExtra("extra_intake", intake);
            startActivity(intent);
            finish();
            return;
        }

        continueButton.setEnabled(false);
        loadingIndicator.setVisibility(View.VISIBLE);

        if (regEmail != null) {
            authRepository.register(regName, regEmail, regPassword,
                    selectedDegree.getId(), selectedDegree.getName(), intake,
                    new DataCallback<>() {
                        @Override
                        public void onSuccess(com.bravem.app.domain.model.User user) {
                            Intent intent = new Intent(SelectDegreeActivity.this, DashboardActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onError(Exception e) {
                            loadingIndicator.setVisibility(View.GONE);
                            continueButton.setEnabled(true);
                            Toast.makeText(SelectDegreeActivity.this,
                                    e.getMessage() != null ? e.getMessage() : getString(R.string.error_generic),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            String uid = sessionManager.getUid();
            if (uid == null) return;

            authRepository.updateUserDegree(uid, selectedDegree.getId(), selectedDegree.getName(), intake,
                    new DataCallback<>() {
                        @Override
                        public void onSuccess(Void result) {
                            startActivity(new Intent(SelectDegreeActivity.this, DashboardActivity.class));
                            finish();
                        }

                        @Override
                        public void onError(Exception e) {
                            loadingIndicator.setVisibility(View.GONE);
                            continueButton.setEnabled(true);
                            Toast.makeText(SelectDegreeActivity.this, R.string.error_generic, Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    static class IntakeViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        IntakeViewHolder(View v) {
            super(v);
            text = v.findViewById(R.id.text_intake);
        }
    }
}
