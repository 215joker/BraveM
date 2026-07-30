package com.bravem.app.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.adapter.LogAdapter;
import com.bravem.app.data.DataCallback;
import com.bravem.app.data.LogRepository;
import com.bravem.app.model.SearchLog;
import com.bravem.app.utils.UiUtils;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

public class SearchLogsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LogAdapter adapter;
    private CircularProgressIndicator progressIndicator;
    private View emptyState;

    private LogRepository logRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_logs);

        UiUtils.handleTopInset(findViewById(R.id.app_bar));

        logRepository = new LogRepository(this);

        recyclerView = findViewById(R.id.recycler_logs);
        progressIndicator = findViewById(R.id.progress_indicator);
        emptyState = findViewById(R.id.empty_state);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_clear_logs).setOnClickListener(v -> confirmClearLogs());

        adapter = new LogAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        android.widget.EditText etSearch = findViewById(R.id.et_search);
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

        loadLogs();
    }

    private void loadLogs() {
        progressIndicator.setVisibility(View.VISIBLE);
        logRepository.fetchAllLogs(new DataCallback<List<SearchLog>>() {
            @Override
            public void onSuccess(List<SearchLog> logs) {
                progressIndicator.setVisibility(View.GONE);
                adapter.submitList(logs);
                emptyState.setVisibility(logs.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception e) {
                progressIndicator.setVisibility(View.GONE);
                Toast.makeText(SearchLogsActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmClearLogs() {
        com.bravem.app.utils.DialogUtils.showConfirmation(this,
                "Clear all logs?",
                "This action cannot be undone.",
                "Clear",
                () -> {
                    logRepository.clearLogs(new DataCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            loadLogs();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(SearchLogsActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                        }
                    });
                });
    }
}
