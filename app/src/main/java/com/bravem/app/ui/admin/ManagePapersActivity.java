package com.bravem.app.ui.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.adapter.PastPaperAdapter;
import com.bravem.app.data.DataCallback;
import com.bravem.app.data.PaperRepositoryImpl;
import com.bravem.app.domain.repository.PaperRepository;
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
    private PastPaper paperForMemo;

    private final androidx.activity.result.ActivityResultLauncher<Intent> memoPickerLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null && paperForMemo != null) {
                        uploadMemoOnly(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list); // reuses header+list shell

        UiUtils.handleTopInset(findViewById(R.id.app_bar));
        UiUtils.handleBottomInset(findViewById(android.R.id.content));

        paperRepository = new PaperRepositoryImpl(this);

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

        ((TextView) findViewById(R.id.text_title)).setText(R.string.manage_papers);

        View btnAdd = findViewById(R.id.btn_add_paper);
        btnAdd.setVisibility(View.VISIBLE);
        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, com.bravem.app.ui.upload.UploadPaperActivity.class)));

        adapter = new PastPaperAdapter(new PastPaperAdapter.OnPaperActionListener() {
            @Override
            public void onPaperClick(PastPaper paper) {
                showPaperSelectionDialog(paper);
            }

            @Override
            public void onDownloadClick(PastPaper paper) {
                com.bravem.app.utils.FileUtils.downloadFile(ManagePapersActivity.this, paper.getFileUrl(),
                        paper.getFileName() != null ? paper.getFileName() : paper.getTitle());
            }

            @Override
            public void onPinClick(PastPaper paper) {}
        });

        adapter.setAdminActionListener(new PastPaperAdapter.OnAdminActionListener() {
            @Override
            public void onApproveToggle(PastPaper paper, boolean approve) {
                paperRepository.setPaperApproved(paper.getId(), approve, new DataCallback<>() {
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
                com.bravem.app.utils.DialogUtils.showConfirmation(ManagePapersActivity.this,
                        getString(R.string.confirm_delete_title),
                        getString(R.string.confirm_delete_message),
                        getString(R.string.delete),
                        () -> paperRepository.deletePaper(paper.getId(), new DataCallback<>() {
                            @Override
                            public void onSuccess(Void result) {
                                loadPapers();
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(ManagePapersActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                            }
                        }));
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadPapers();
    }

    private void showPaperSelectionDialog(PastPaper paper) {
        boolean hasMemo = paper.getMemoUrl() != null;
        String[] options = hasMemo 
                ? new String[]{"View Question Paper", "View Memorandum"} 
                : new String[]{"View Question Paper", "Upload Memorandum (Missing)"};
        int[] icons = {R.drawable.ic_document, hasMemo ? R.drawable.ic_document : R.drawable.ic_upload};

        com.bravem.app.utils.DialogUtils.showOptions(this, paper.getTitle(), options, icons, index -> {
            if (index == 0) {
                openPaperViewer(paper, false);
            } else {
                if (hasMemo) {
                    openPaperViewer(paper, true);
                } else {
                    paperForMemo = paper;
                    launchMemoPicker();
                }
            }
        });
    }

    private void launchMemoPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        memoPickerLauncher.launch(Intent.createChooser(intent, "Select Memorandum (PDF)"));
    }

    private void uploadMemoOnly(Uri uri) {
        String fileName = com.bravem.app.utils.FileUtils.getFileName(this, uri);
        Toast.makeText(this, "Uploading memorandum...", Toast.LENGTH_SHORT).show();
        
        paperRepository.uploadMemorandumOnly(paperForMemo.getId(), uri, fileName, null, new DataCallback<PastPaper>() {
            @Override
            public void onSuccess(PastPaper result) {
                runOnUiThread(() -> {
                    Toast.makeText(ManagePapersActivity.this, "Memorandum uploaded successfully!", Toast.LENGTH_LONG).show();
                    loadPapers();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> Toast.makeText(ManagePapersActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void openPaperViewer(PastPaper paper, boolean isMemo) {
        Intent intent = new Intent(this, com.bravem.app.ui.papers.PaperViewerActivity.class);
        intent.putExtra(com.bravem.app.ui.papers.PaperViewerActivity.EXTRA_PAPER, paper);
        intent.putExtra("is_memo", isMemo);
        startActivity(intent);
    }

    private void loadPapers() {
        progressIndicator.setVisibility(View.VISIBLE);
        paperRepository.fetchAllPapersForAdmin(new DataCallback<>() {
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
