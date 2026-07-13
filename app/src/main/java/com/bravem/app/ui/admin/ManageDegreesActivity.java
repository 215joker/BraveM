package com.bravem.app.ui.admin;

import android.os.Bundle;
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
import com.bravem.app.data.DegreeRepository;
import com.bravem.app.model.Degree;
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

    private DegreeRepository degreeRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_degrees);

        degreeRepository = new DegreeRepository(this);

        recyclerView = findViewById(R.id.recycler_list);
        progressIndicator = findViewById(R.id.progress_indicator);
        emptyState = findViewById(R.id.empty_state);
        backButton = findViewById(R.id.btn_back);
        fabAdd = findViewById(R.id.fab_add);

        backButton.setOnClickListener(v -> finish());
        fabAdd.setOnClickListener(v -> showDegreeDialog(null));

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
        degreeRepository.fetchAllDegrees(new DataCallback<List<Degree>>() {
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
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_degree, null);
        EditText nameInput = dialogView.findViewById(R.id.input_name);
        EditText descInput = dialogView.findViewById(R.id.input_description);
        EditText uniInput = dialogView.findViewById(R.id.input_university);

        if (existing != null) {
            nameInput.setText(existing.getName());
            descInput.setText(existing.getDescription());
            uniInput.setText(existing.getUniversity());
        }

        new AlertDialog.Builder(this)
                .setTitle(existing == null ? R.string.add_degree : R.string.edit)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String description = descInput.getText().toString().trim();
                    String university = uniInput.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, R.string.error_required_field, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (existing == null) {
                        createDegree(name, description, university);
                    } else {
                        updateDegree(existing.getId(), name, description, university);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void createDegree(String name, String description, String university) {
        degreeRepository.addDegree(name, description, university, new DataCallback<Degree>() {
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
        degreeRepository.updateDegree(id, name, description, university, new DataCallback<Void>() {
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
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.delete, (dialog, which) ->
                        degreeRepository.deleteDegree(degree.getId(), new DataCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                loadDegrees();
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(ManageDegreesActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
