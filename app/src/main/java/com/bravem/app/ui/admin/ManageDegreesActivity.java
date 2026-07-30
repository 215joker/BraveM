package com.bravem.app.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.adapter.DegreeAdapter;
import com.bravem.app.data.DataCallback;
import com.bravem.app.data.DegreeRepositoryImpl;
import com.bravem.app.domain.model.Degree;
import com.bravem.app.domain.repository.DegreeRepository;
import com.bravem.app.utils.UiUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

public class ManageDegreesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DegreeAdapter adapter;
    private CircularProgressIndicator progressIndicator;
    private View emptyState;
    private View backButton;
    private FloatingActionButton fabAdd;
    private EditText etSearch;

    private DegreeRepository degreeRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_degrees);

        UiUtils.handleTopInset(findViewById(R.id.app_bar));
        UiUtils.handleFabBottomInset(findViewById(R.id.fab_add), 24);

        degreeRepository = new DegreeRepositoryImpl(this);

        recyclerView = findViewById(R.id.recycler_list);
        progressIndicator = findViewById(R.id.progress_indicator);
        emptyState = findViewById(R.id.empty_state);
        backButton = findViewById(R.id.btn_back);
        fabAdd = findViewById(R.id.fab_add);
        etSearch = findViewById(R.id.et_search);

        backButton.setOnClickListener(v -> finish());
        fabAdd.setOnClickListener(v -> showDegreeDialog(null));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        adapter = new DegreeAdapter(degree -> showDegreeDialog(degree));
        adapter.setActionListener(new DegreeAdapter.OnDegreeActionListener() {
            @Override
            public void onEdit(Degree degree) {
                showDegreeDialog(degree);
            }

            @Override
            public void onDelete(Degree degree) {
                confirmDelete(degree);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadDegrees();
    }

    private void loadDegrees() {
        progressIndicator.setVisibility(View.VISIBLE);
        degreeRepository.fetchAllDegrees(new DataCallback<>() {
            @Override
            public void onSuccess(List<Degree> degrees) {
                progressIndicator.setVisibility(View.GONE);
                adapter.submitList(degrees);
                emptyState.setVisibility(degrees.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception e) {
                progressIndicator.setVisibility(View.GONE);
                Toast.makeText(ManageDegreesActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDegreeDialog(Degree existing) {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this, R.style.Theme_BraveM_BottomSheetDialog);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_degree, null);
        dialog.setContentView(dialogView);

        android.widget.EditText nameInput = dialogView.findViewById(R.id.input_name);
        android.widget.EditText uniInput = dialogView.findViewById(R.id.input_university);
        android.widget.TextView titleView = dialogView.findViewById(R.id.text_dialog_title);
        View btnSave = dialogView.findViewById(R.id.btn_save);

        if (titleView != null) {
            titleView.setText(existing == null ? R.string.add_degree : R.string.edit);
        }

        if (existing != null) {
            nameInput.setText(existing.getName());
            uniInput.setText(existing.getUniversity());
        }

        btnSave.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String university = uniInput.getText().toString().trim();
            if (name.isEmpty() || university.isEmpty()) {
                Toast.makeText(this, R.string.error_required_field, Toast.LENGTH_SHORT).show();
                return;
            }
            if (existing == null) {
                createDegree(name, "", university);
            } else {
                updateDegree(existing.getId(), name, "", university);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void createDegree(String name, String description, String university) {
        degreeRepository.addDegree(name, description, university, new DataCallback<>() {
            @Override
            public void onSuccess(Degree result) {
                loadDegrees();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ManageDegreesActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDegree(String id, String name, String description, String university) {
        degreeRepository.updateDegree(id, name, description, university, new DataCallback<>() {
            @Override
            public void onSuccess(Void result) {
                loadDegrees();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ManageDegreesActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDelete(Degree degree) {
        com.bravem.app.utils.DialogUtils.showConfirmation(this,
                getString(R.string.confirm_delete_title),
                getString(R.string.confirm_delete_message),
                getString(R.string.delete),
                () -> degreeRepository.deleteDegree(degree.getId(), new DataCallback<>() {
                    @Override
                    public void onSuccess(Void result) {
                        loadDegrees();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(ManageDegreesActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                    }
                }));
    }
}
