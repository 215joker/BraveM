package com.bravem.app.ui.admin;

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
import com.bravem.app.adapter.UniversityAdapter;
import com.bravem.app.data.DataCallback;
import com.bravem.app.data.DegreeRepository;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

public class ManageUniversitiesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UniversityAdapter adapter;
    private CircularProgressIndicator progressIndicator;
    private View emptyState;
    private EditText etSearch;

    private DegreeRepository degreeRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_degrees);

        degreeRepository = new DegreeRepository(this);

        recyclerView = findViewById(R.id.recycler_list);
        progressIndicator = findViewById(R.id.progress_indicator);
        emptyState = findViewById(R.id.empty_state);
        etSearch = findViewById(R.id.et_search);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.fab_add).setVisibility(View.GONE);

        ((TextView) findViewById(R.id.text_title)).setText(R.string.manage_universities);

        adapter = new UniversityAdapter(degreeRepository);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        loadUniversities();
    }

    private void loadUniversities() {
        progressIndicator.setVisibility(View.VISIBLE);
        degreeRepository.fetchAllUniversities(new DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> universities) {
                progressIndicator.setVisibility(View.GONE);
                adapter.submitList(universities);
                emptyState.setVisibility(universities.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception e) {
                progressIndicator.setVisibility(View.GONE);
                Toast.makeText(ManageUniversitiesActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
