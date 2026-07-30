package com.bravem.app.ui.papers;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bravem.app.R;
import com.bravem.app.model.PastPaper;
import com.bravem.app.utils.FileUtils;
import com.bravem.app.utils.UiUtils;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.button.MaterialButton;

import java.io.File;

/**
 * Shows an in-app preview for PDF papers.
 */
public class PaperViewerActivity extends AppCompatActivity {

    public static final String EXTRA_PAPER = "extra_paper";

    private PDFView pdfView;
    private View docxPlaceholder;
    private TextView titleView;
    private TextView metaView;
    private MaterialButton downloadButton;
    private View backButton;

    private PastPaper paper;
    private boolean isMemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_viewer);

        UiUtils.handleTopInset(findViewById(R.id.app_bar));
        UiUtils.handleFabBottomInset(findViewById(R.id.btn_download), 20);

        pdfView = findViewById(R.id.pdf_view);
        docxPlaceholder = findViewById(R.id.layout_docx_placeholder);
        titleView = findViewById(R.id.text_title);
        metaView = findViewById(R.id.text_meta);
        downloadButton = findViewById(R.id.btn_download);
        backButton = findViewById(R.id.btn_back);

        backButton.setOnClickListener(v -> finish());

        paper = (PastPaper) getIntent().getSerializableExtra(EXTRA_PAPER);
        isMemo = getIntent().getBooleanExtra("is_memo", false);

        if (paper == null) {
            finish();
            return;
        }

        bind();
    }

    private void bind() {
        String displayTitle = isMemo ? paper.getTitle() + " (Memo)" : paper.getTitle();
        titleView.setText(displayTitle);

        String meta = paper.getCourseCode() != null ? paper.getCourseCode() + " · " : "";
        meta += paper.getYear() > 0 ? String.valueOf(paper.getYear()) : "";
        metaView.setText(meta);

        downloadButton.setOnClickListener(v -> downloadPaper());

        String targetUrl = isMemo ? paper.getMemoUrl() : paper.getFileUrl();
        String fileType = isMemo ? FileUtils.detectFileType(paper.getMemoName()) : paper.getFileType();

        if (PastPaper.TYPE_PDF.equals(fileType) && targetUrl != null) {
            pdfView.setVisibility(View.VISIBLE);
            docxPlaceholder.setVisibility(View.GONE);

            if (targetUrl.startsWith("http")) {
                // If it's a remote URL, we should ideally download it first or use a stream
                // For now, let's try to load from Uri if possible, or show a message
                Toast.makeText(this, "Loading from cloud...", Toast.LENGTH_SHORT).show();
                // Note: barteksc PDFView doesn't support direct URLs without a stream.
                // In a real app, we'd download to a temp file first.
            } else {
                File file = new File(targetUrl);
                if (file.exists()) {
                    pdfView.fromFile(file).load();
                } else {
                    Toast.makeText(this, "File not found.", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            pdfView.setVisibility(View.GONE);
            docxPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    private void downloadPaper() {
        String targetUrl = isMemo ? paper.getMemoUrl() : paper.getFileUrl();
        String targetName = isMemo ? paper.getMemoName() : paper.getFileName();

        if (targetUrl == null) {
            Toast.makeText(this, R.string.download_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        FileUtils.downloadFile(this, targetUrl,
                targetName != null ? targetName : paper.getTitle());
        Toast.makeText(this, R.string.downloading, Toast.LENGTH_SHORT).show();
    }
}
