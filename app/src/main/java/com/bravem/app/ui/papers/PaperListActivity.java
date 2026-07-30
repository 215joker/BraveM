package com.bravem.app.ui.papers;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.adapter.PastPaperAdapter;
import com.bravem.app.data.DataCallback;
import com.bravem.app.data.PaperRepositoryImpl;
import com.bravem.app.domain.repository.PaperRepository;
import com.bravem.app.model.PastPaper;
import com.bravem.app.utils.FileUtils;
import com.bravem.app.utils.UiUtils;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

public class PaperListActivity extends AppCompatActivity {

    public static final String EXTRA_COURSE_ID = "extra_course_id";
    public static final String EXTRA_COURSE_NAME = "extra_course_name";
    public static final String EXTRA_COURSE_CODE = "extra_course_code";

    private RecyclerView recyclerView;
    private PastPaperAdapter adapter;
    private TextView titleView;
    private CircularProgressIndicator progressIndicator;
    private View emptyState;
    private View backButton;

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
        setContentView(R.layout.activity_course_list); // reuses the same header+list shell

        UiUtils.handleTopInset(findViewById(R.id.layout_header));
        UiUtils.handleBottomInset(findViewById(android.R.id.content));

        paperRepository = new PaperRepositoryImpl(this);

        recyclerView = findViewById(R.id.recycler_courses);
        titleView = findViewById(R.id.text_title);
        progressIndicator = findViewById(R.id.progress_indicator);
        emptyState = findViewById(R.id.empty_state);
        backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> finish());

        String courseId = getIntent().getStringExtra(EXTRA_COURSE_ID);
        String courseName = getIntent().getStringExtra(EXTRA_COURSE_NAME);
        titleView.setText(courseName != null ? courseName : getString(R.string.past_papers));

        adapter = new PastPaperAdapter(new PastPaperAdapter.OnPaperActionListener() {
            @Override
            public void onPaperClick(PastPaper paper) {
                showPaperSelectionDialog(paper);
            }

            @Override
            public void onDownloadClick(PastPaper paper) {
                downloadPaper(paper);
            }

            @Override
            public void onPinClick(PastPaper paper) {
                togglePin(paper);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        if (courseId != null) {
            loadPapers(courseId);
        }

        ((TextView) findViewById(R.id.empty_state)).setText(R.string.no_papers_found);
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
        String fileName = FileUtils.getFileName(this, uri);
        Toast.makeText(this, "Uploading memorandum...", Toast.LENGTH_SHORT).show();
        
        paperRepository.uploadMemorandumOnly(paperForMemo.getId(), uri, fileName, null, new DataCallback<PastPaper>() {
            @Override
            public void onSuccess(PastPaper result) {
                runOnUiThread(() -> {
                    Toast.makeText(PaperListActivity.this, "Memorandum uploaded successfully!", Toast.LENGTH_LONG).show();
                    String courseId = getIntent().getStringExtra(EXTRA_COURSE_ID);
                    if (courseId != null) loadPapers(courseId);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> Toast.makeText(PaperListActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void openPaperViewer(PastPaper paper, boolean isMemo) {
        Intent intent = new Intent(this, PaperViewerActivity.class);
        intent.putExtra(PaperViewerActivity.EXTRA_PAPER, paper);
        intent.putExtra("is_memo", isMemo);
        startActivity(intent);
    }

    private void loadPapers(String courseId) {
        progressIndicator.setVisibility(View.VISIBLE);
        paperRepository.fetchPapersByCourse(courseId, new DataCallback<List<PastPaper>>() {
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
                Toast.makeText(PaperListActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void togglePin(PastPaper paper) {
        paperRepository.togglePin(paper.getId(), new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isPinned) {
                String courseId = getIntent().getStringExtra(EXTRA_COURSE_ID);
                if (courseId != null) loadPapers(courseId);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(PaperListActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadPaper(PastPaper paper) {
        if (paper.getFileUrl() == null) {
            Toast.makeText(this, R.string.download_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        FileUtils.downloadFile(this, paper.getFileUrl(),
                paper.getFileName() != null ? paper.getFileName() : paper.getTitle());
        Toast.makeText(this, R.string.downloading, Toast.LENGTH_SHORT).show();
    }
}
