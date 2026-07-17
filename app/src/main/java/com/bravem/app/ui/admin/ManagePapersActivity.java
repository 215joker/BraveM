package com.bravem.app.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.adapter.PastPaperAdapter;
import com.bravem.app.data.DataCallback;
import com.bravem.app.data.PaperRepository;
import com.bravem.app.model.PastPaper;
import com.bravem.app.utils.UiUtils;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

public class ManagePapersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PastPaperAdapter adapter;
    private CircularProgressIndicator progressIndicator;
    private View emptyState;
    private View backButton;
    private EditText etSearch;

    private PaperRepository paperRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list); // reuses header+list shell

        UiUtils.handleTopInset(findViewById(R.id.layout_header));
        UiUtils.handleBottomInset(findViewById(android.R.id.content));

        paperRepository = new PaperRepository(this);

        recyclerView = findViewById(R.id.recycler_courses);
        progressIndicator = findViewById(R.id.progress_indicator);
        emptyState = findViewById(R.id.empty_state);
        backButton = findViewById(R.id.btn_back);
        etSearch = findViewById(R.id.et_search);

        backButton.setOnClickListener(v -> finish());

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

        ((android.widget.TextView) findViewById(R.id.text_title)).setText(R.string.manage_papers);
        ((android.widget.TextView) findViewById(R.id.empty_state)).setText(R.string.no_papers_found);

        View btnAdd = findViewById(R.id.btn_add_paper);
        btnAdd.setVisibility(View.VISIBLE);
        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, com.bravem.app.ui.upload.UploadPaperActivity.class)));

        adapter = new PastPaperAdapter(new PastPaperAdapter.OnPaperActionListener() {
            @Override
            public void onPaperClick(PastPaper paper) {
                // No-op here; admin manages via the approve/delete row actions.
            }

            @Override
            public void onDownloadClick(PastPaper paper) {
                // Admin screen focuses on moderation, not downloading.
            }

            @Override
            public void onPinClick(PastPaper paper) {
                // Admin usually doesn't need to pin, but we must implement the interface
            }
        });

        adapter.setAdminActionListener(new PastPaperAdapter.OnAdminActionListener() {
            @Override
            public void onApproveToggle(PastPaper paper, boolean approve) {
                paperRepository.setPaperApproved(paper.getId(), approve, new DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        loadPapers();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(ManagePapersActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onDelete(PastPaper paper) {
                new AlertDialog.Builder(ManagePapersActivity.this)
                        .setTitle(R.string.confirm_delete_title)
                        .setMessage(R.string.confirm_delete_message)
                        .setPositiveButton(R.string.delete, (dialog, which) ->
                                paperRepository.deletePaper(paper.getId(), new DataCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        loadPapers();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Toast.makeText(ManagePapersActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                                    }
                                }))
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadPapers();
    }

    private void loadPapers() {
        progressIndicator.setVisibility(View.VISIBLE);
        paperRepository.fetchAllPapersForAdmin(new DataCallback<List<PastPaper>>() {
            @Override
            public void onSuccess(List<PastPaper> papers) {
                progressIndicator.setVisibility(View.GONE);
                adapter.submitList(papers);
                emptyState.setVisibility(papers.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(papers.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onError(Exception e) {
                progressIndicator.setVisibility(View.GONE);
                Toast.makeText(ManagePapersActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
