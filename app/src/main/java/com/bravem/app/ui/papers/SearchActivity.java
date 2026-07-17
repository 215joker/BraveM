package com.bravem.app.ui.papers;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.adapter.PastPaperAdapter;
import com.bravem.app.data.DataCallback;
import com.bravem.app.data.LogRepository;
import com.bravem.app.data.PaperRepository;
import com.bravem.app.model.PastPaper;
import com.bravem.app.utils.FileUtils;
import com.bravem.app.utils.SessionManager;
import com.bravem.app.utils.UiUtils;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private TextInputEditText searchInput;
    private RecyclerView recyclerView;
    private PastPaperAdapter adapter;
    private CircularProgressIndicator progressIndicator;
    private View emptyState;
    private View backButton;

    private PaperRepository paperRepository;
    private LogRepository logRepository;
    private SessionManager sessionManager;
    private final android.os.Handler debounceHandler = new android.os.Handler();
    private Runnable pendingSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        UiUtils.handleTopInset(findViewById(R.id.layout_header));
        UiUtils.handleBottomInset(findViewById(android.R.id.content));

        paperRepository = new PaperRepository(this);
        logRepository = new LogRepository(this);
        sessionManager = new SessionManager(this);

        searchInput = findViewById(R.id.input_search);
        recyclerView = findViewById(R.id.recycler_results);
        progressIndicator = findViewById(R.id.progress_indicator);
        emptyState = findViewById(R.id.empty_state);
        backButton = findViewById(R.id.btn_back);

        backButton.setOnClickListener(v -> finish());

        adapter = new PastPaperAdapter(new PastPaperAdapter.OnPaperActionListener() {
            @Override
            public void onPaperClick(PastPaper paper) {
                Intent intent = new Intent(SearchActivity.this, PaperViewerActivity.class);
                intent.putExtra(PaperViewerActivity.EXTRA_PAPER, paper);
                startActivity(intent);
            }

            @Override
            public void onDownloadClick(PastPaper paper) {
                if (paper.getFileUrl() == null) return;
                FileUtils.downloadFile(SearchActivity.this, paper.getFileUrl(),
                        paper.getFileName() != null ? paper.getFileName() : paper.getTitle());
                Toast.makeText(SearchActivity.this, R.string.downloading, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPinClick(PastPaper paper) {
                togglePin(paper);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                debounceSearch(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        searchInput.requestFocus();
    }

    private void debounceSearch(String query) {
        if (pendingSearch != null) {
            debounceHandler.removeCallbacks(pendingSearch);
        }
        if (query.isEmpty()) {
            adapter.submitList(new java.util.ArrayList<>());
            emptyState.setVisibility(View.GONE);
            return;
        }
        pendingSearch = () -> performSearch(query);
        debounceHandler.postDelayed(pendingSearch, 350);
    }

    private void performSearch(String query) {
        progressIndicator.setVisibility(View.VISIBLE);
        logRepository.logSearch(query, sessionManager.getEmail());
        paperRepository.searchPapersByTitlePrefix(query, new DataCallback<List<PastPaper>>() {
            @Override
            public void onSuccess(List<PastPaper> papers) {
                progressIndicator.setVisibility(View.GONE);
                adapter.submitList(papers);
                emptyState.setVisibility(papers.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception e) {
                progressIndicator.setVisibility(View.GONE);
                Toast.makeText(SearchActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void togglePin(PastPaper paper) {
        paperRepository.togglePin(paper.getId(), new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isPinned) {
                performSearch(searchInput.getText().toString().trim());
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(SearchActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
