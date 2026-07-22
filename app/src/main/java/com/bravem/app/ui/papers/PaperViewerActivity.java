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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_viewer);

        UiUtils.handleTopInset(findViewById(R.id.app_bar));
        UiUtils.handleBottomInset(findViewById(R.id.btn_download));

        pdfView = findViewById(R.id.pdf_view);
        docxPlaceholder = findViewById(R.id.layout_docx_placeholder);
        titleView = findViewById(R.id.text_title);
        metaView = findViewById(R.id.text_meta);
        downloadButton = findViewById(R.id.btn_download);
        backButton = findViewById(R.id.btn_back);

        backButton.setOnClickListener(v -> finish());

        paper = (PastPaper) getIntent().getSerializableExtra(EXTRA_PAPER);
        if (paper == null) {
            finish();
            return;
        }

        bind();
    }

    private void bind() {
        titleView.setText(paper.getTitle());

        String meta = paper.getCourseCode() != null ? paper.getCourseCode() + " · " : "";
        meta += paper.getYear() > 0 ? String.valueOf(paper.getYear()) : "";
        metaView.setText(meta);

        downloadButton.setOnClickListener(v -> downloadPaper());

        if (PastPaper.TYPE_PDF.equals(paper.getFileType()) && paper.getFileUrl() != null) {
            pdfView.setVisibility(View.VISIBLE);
            docxPlaceholder.setVisibility(View.GONE);

            File file = new File(paper.getFileUrl());
            if (file.exists()) {
                pdfView.fromFile(file).load();
            } else {
                Toast.makeText(this, "Local file not found.", Toast.LENGTH_SHORT).show();
            }
        } else {
            pdfView.setVisibility(View.GONE);
            docxPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    private void downloadPaper() {
        if (paper.getFileUrl() == null) {
            Toast.makeText(this, R.string.download_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        // In local mode, "download" just copies to the public download folder
        FileUtils.downloadFile(this, paper.getFileUrl(),
                paper.getFileName() != null ? paper.getFileName() : paper.getTitle());
        Toast.makeText(this, R.string.downloading, Toast.LENGTH_SHORT).show();
    }
}
