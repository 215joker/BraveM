package com.bravem.app.ui.papers;

import android.content.Intent;
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
import com.bravem.app.data.PaperRepository;
import com.bravem.app.model.PastPaper;
import com.bravem.app.utils.FileUtils;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DegreePapersActivity extends AppCompatActivity {

    public static final String EXTRA_DEGREE_ID = "extra_degree_id";
    public static final String EXTRA_DEGREE_NAME = "extra_degree_name";
    public static final String EXTRA_INTAKE = "extra_intake";

    private RecyclerView recyclerView;
    private PastPaperAdapter adapter;
    private TextView titleView;
    private EditText searchInput;
    private View filterButton;
    private CircularProgressIndicator progressIndicator;
    private View emptyState;
    private View backButton;

    private PaperRepository paperRepository;

    private String degreeId;
    private String degreeName;
    private String selectedIntake;
    
    private List<PastPaper> allPapers = new ArrayList<>();
    private List<PastPaper> filteredPapers = new ArrayList<>();
    
    private int currentSortType = 0; // 0: All/Per Intake, 1: Latest, 2: Oldest

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_degree_papers);

        paperRepository = new PaperRepository(this);

        recyclerView = findViewById(R.id.recycler_papers);
        titleView = findViewById(R.id.text_title);
        searchInput = findViewById(R.id.edit_search);
        filterButton = findViewById(R.id.btn_filter);
        progressIndicator = findViewById(R.id.progress_indicator);
        emptyState = findViewById(R.id.empty_state);
        backButton = findViewById(R.id.btn_back);

        backButton.setOnClickListener(v -> finish());
        filterButton.setOnClickListener(v -> showFilterDialog());

        degreeId = getIntent().getStringExtra(EXTRA_DEGREE_ID);
        degreeName = getIntent().getStringExtra(EXTRA_DEGREE_NAME);
        selectedIntake = getIntent().getStringExtra(EXTRA_INTAKE);

        titleView.setText(degreeName != null ? degreeName : getString(R.string.past_papers));

        adapter = new PastPaperAdapter(new PastPaperAdapter.OnPaperActionListener() {
            @Override
            public void onPaperClick(PastPaper paper) {
                Intent intent = new Intent(DegreePapersActivity.this, PaperViewerActivity.class);
                intent.putExtra(PaperViewerActivity.EXTRA_PAPER, paper);
                startActivity(intent);
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

        setupSearch();

        if (degreeId != null) {
            loadPapers();
        }
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadPapers() {
        progressIndicator.setVisibility(View.VISIBLE);
        paperRepository.fetchPapersForDegree(degreeId, 100, new DataCallback<List<PastPaper>>() {
            @Override
            public void onSuccess(List<PastPaper> papers) {
                progressIndicator.setVisibility(View.GONE);
                allPapers = papers;
                applyFilters();
            }

            @Override
            public void onError(Exception e) {
                progressIndicator.setVisibility(View.GONE);
                Toast.makeText(DegreePapersActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        String query = searchInput.getText().toString().toLowerCase().trim();
        List<PastPaper> result = new ArrayList<>();

        for (PastPaper paper : allPapers) {
            boolean matchesSearch = paper.getTitle().toLowerCase().contains(query) || 
                                   (paper.getCourseName() != null && paper.getCourseName().toLowerCase().contains(query)) ||
                                   (paper.getCourseCode() != null && paper.getCourseCode().toLowerCase().contains(query));
            
            boolean matchesIntake = true;
            if (currentSortType == 0 && selectedIntake != null) {
                // If "Per Intake" is active, filter by intake
                String semNum = "1";
                if (paper.getSemester() != null) {
                    if (paper.getSemester().contains("2")) semNum = "2";
                    else if (paper.getSemester().contains("3")) semNum = "3";
                }
                String paperIntake = paper.getYear() + "." + semNum;
                matchesIntake = paperIntake.equals(selectedIntake);
            }

            if (matchesSearch && matchesIntake) {
                result.add(paper);
            }
        }

        // Apply Sorting
        if (currentSortType == 1) {
            // Latest Upload
            Collections.sort(result, (p1, p2) -> Long.compare(p2.getCreatedAt(), p1.getCreatedAt()));
        } else if (currentSortType == 2) {
            // Oldest Upload
            Collections.sort(result, (p1, p2) -> Long.compare(p1.getCreatedAt(), p2.getCreatedAt()));
        } else if (currentSortType == 0 && selectedIntake == null) {
            // Default sort if no intake selected
            Collections.sort(result, (p1, p2) -> Long.compare(p2.getCreatedAt(), p1.getCreatedAt()));
        }

        filteredPapers = result;
        adapter.submitList(filteredPapers);
        emptyState.setVisibility(filteredPapers.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(filteredPapers.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showFilterDialog() {
        String[] options = {
                "Show Selected Intake (" + (selectedIntake != null ? selectedIntake : "None") + ")",
                "Latest Upload",
                "Oldest Upload",
                "Show All Papers"
        };

        new AlertDialog.Builder(this)
                .setTitle("Filter & Sort")
                .setSingleChoiceItems(options, currentSortType == 0 && selectedIntake == null ? 3 : currentSortType, (dialog, which) -> {
                    if (which == 3) {
                        currentSortType = 0;
                        selectedIntake = null; // Clear intake filter for "Show All"
                    } else if (which == 0) {
                        currentSortType = 0;
                        // Restore selected intake if it was cleared
                        if (selectedIntake == null) {
                            selectedIntake = getIntent().getStringExtra(EXTRA_INTAKE);
                        }
                    } else {
                        currentSortType = which;
                    }
                    applyFilters();
                    dialog.dismiss();
                })
                .show();
    }

    private void togglePin(PastPaper paper) {
        paperRepository.togglePin(paper.getId(), new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isPinned) {
                loadPapers();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(DegreePapersActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
