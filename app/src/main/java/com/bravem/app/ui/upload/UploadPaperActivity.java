package com.bravem.app.ui.upload;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bravem.app.R;
import com.bravem.app.data.DataCallback;
import com.bravem.app.data.DegreeRepositoryImpl;
import com.bravem.app.data.PaperRepositoryImpl;
import com.bravem.app.domain.model.Degree;
import com.bravem.app.domain.repository.DegreeRepository;
import com.bravem.app.domain.repository.PaperRepository;
import com.bravem.app.model.PastPaper;
import com.bravem.app.utils.FileUtils;
import com.bravem.app.utils.SessionManager;
import com.bravem.app.utils.UiUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class UploadPaperActivity extends AppCompatActivity {

    private View backButton;
    private View pickFileButton;
    private View pickMemoButton;
    private TextView selectedFileText;
    private TextView selectedMemoText;
    private TextInputEditText titleInput;
    private TextInputEditText universityInput;
    private AutoCompleteTextView degreeDropdown;
    private TextInputEditText intakeInput;
    private TextInputEditText yearInput;
    private AutoCompleteTextView semesterDropdown;
    private MaterialButton addBtn;
    private MaterialButton submitButton;
    private MaterialButton submitAllBtn;
    private TextView queueCountText;
    private LinearProgressIndicator uploadProgress;

    private DegreeRepository degreeRepository;
    private PaperRepository paperRepository;

    private SessionManager sessionManager;

    private Uri selectedFileUri;
    private String selectedFileName;
    private long selectedFileSize;

    private Uri selectedMemoUri;
    private String selectedMemoName;
    private long selectedMemoSize;

    private List<Degree> degrees = new ArrayList<>();
    private Degree chosenDegree;

    private List<QueuedPaper> paperQueue = new ArrayList<>();

    private static class QueuedPaper {
        Uri uri;
        String fileName;
        Uri memoUri;
        String memoName;
        PastPaper meta;

        QueuedPaper(Uri uri, String fileName, Uri memoUri, String memoName, PastPaper meta) {
            this.uri = uri;
            this.fileName = fileName;
            this.memoUri = memoUri;
            this.memoName = memoName;
            this.meta = meta;
        }
    }

    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        handleFileSelected(uri);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> memoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        handleMemoSelected(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_paper);

        UiUtils.handleTopInset(findViewById(R.id.app_bar));
        UiUtils.handleBottomInset(findViewById(R.id.upload_form_container));

        degreeRepository = new DegreeRepositoryImpl(this);
        paperRepository = new PaperRepositoryImpl(this);
        sessionManager = new SessionManager(this);

        backButton = findViewById(R.id.btn_back);
        pickFileButton = findViewById(R.id.btn_pick_file);
        pickMemoButton = findViewById(R.id.btn_pick_memo);
        selectedFileText = findViewById(R.id.text_selected_file);
        selectedMemoText = findViewById(R.id.text_selected_memo);
        titleInput = findViewById(R.id.input_title);
        universityInput = findViewById(R.id.input_university);
        degreeDropdown = findViewById(R.id.dropdown_degree);
        intakeInput = findViewById(R.id.input_intake);
        yearInput = findViewById(R.id.input_year);
        semesterDropdown = findViewById(R.id.dropdown_semester);
        addBtn = findViewById(R.id.btn_add);
        submitButton = findViewById(R.id.btn_submit);
        submitAllBtn = findViewById(R.id.btn_submit_all);
        queueCountText = findViewById(R.id.text_queue_count);
        uploadProgress = findViewById(R.id.upload_progress);

        universityInput.setText(sessionManager.getUniversity());
        intakeInput.setText(sessionManager.getIntake());

        backButton.setOnClickListener(v -> finish());
        pickFileButton.setOnClickListener(v -> launchFilePicker(false));
        pickMemoButton.setOnClickListener(v -> launchFilePicker(true));
        addBtn.setOnClickListener(v -> addToQueue());
        submitButton.setOnClickListener(v -> attemptUpload());
        submitAllBtn.setOnClickListener(v -> uploadQueue());

        setupSemesterDropdown();
        loadDegrees();
    }

    private void setupSemesterDropdown() {
        String[] semesters = {"Semester 1", "Semester 2", "Semester 3", "Annual"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, semesters);
        semesterDropdown.setAdapter(adapter);
    }

    private void loadDegrees() {
        degreeRepository.fetchAllDegrees(new DataCallback<>() {
            @Override
            public void onSuccess(List<Degree> result) {
                degrees = result;
                List<String> names = new ArrayList<>();
                for (Degree d : result) names.add(d.getName());

                ArrayAdapter<String> adapter = new ArrayAdapter<>(UploadPaperActivity.this,
                        android.R.layout.simple_list_item_1, names);
                degreeDropdown.setAdapter(adapter);

                degreeDropdown.setOnItemClickListener((parent, view, position, id) -> {
                    chosenDegree = degrees.get(position);
                });

                String myDegreeId = sessionManager.getDegreeId();
                if (myDegreeId != null) {
                    for (Degree d : result) {
                        if (d.getId().equals(myDegreeId)) {
                            chosenDegree = d;
                            degreeDropdown.setText(d.getName(), false);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(UploadPaperActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void launchFilePicker(boolean isMemo) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        });
        if (isMemo) {
            memoPickerLauncher.launch(Intent.createChooser(intent, "Select Memorandum"));
        } else {
            filePickerLauncher.launch(Intent.createChooser(intent, getString(R.string.select_file)));
        }
    }

    private void handleFileSelected(Uri uri) {
        selectedFileUri = uri;
        selectedFileName = FileUtils.getFileName(this, uri);
        selectedFileSize = FileUtils.getFileSize(this, uri);

        String type = FileUtils.detectFileType(selectedFileName);
        if ("unknown".equals(type)) {
            Toast.makeText(this, "Please select a PDF or Word document.", Toast.LENGTH_LONG).show();
            selectedFileUri = null;
            selectedFileName = null;
            selectedFileText.setText("");
            return;
        }

        selectedFileText.setText(selectedFileName + " (" + FileUtils.humanReadableSize(selectedFileSize) + ")");
    }

    private void handleMemoSelected(Uri uri) {
        selectedMemoUri = uri;
        selectedMemoName = FileUtils.getFileName(this, uri);
        selectedMemoSize = FileUtils.getFileSize(this, uri);

        selectedMemoText.setText(selectedMemoName + " (" + FileUtils.humanReadableSize(selectedMemoSize) + ")");
    }

    private void addToQueue() {
        PastPaper paper = validateInputs();
        if (paper == null) return;

        if (!paperQueue.isEmpty()) {
            if (!paperQueue.get(0).meta.getDegreeId().equals(paper.getDegreeId())) {
                Toast.makeText(this, R.string.bulk_upload_degree_mismatch, Toast.LENGTH_LONG).show();
                return;
            }
        }

        paperQueue.add(new QueuedPaper(selectedFileUri, selectedFileName, selectedMemoUri, selectedMemoName, paper));
        updateQueueUI();
        clearFileAndTitle();
        
        universityInput.setEnabled(false);
        degreeDropdown.setEnabled(false);
        intakeInput.setEnabled(false);
        yearInput.setEnabled(false);
        semesterDropdown.setEnabled(false);
    }

    private void updateQueueUI() {
        int count = paperQueue.size();
        if (count > 0) {
            queueCountText.setVisibility(View.VISIBLE);
            queueCountText.setText(getString(R.string.queued_papers_count, count));
            submitAllBtn.setVisibility(View.VISIBLE);
        } else {
            queueCountText.setVisibility(View.GONE);
            submitAllBtn.setVisibility(View.GONE);
            
            universityInput.setEnabled(true);
            degreeDropdown.setEnabled(true);
            intakeInput.setEnabled(true);
            yearInput.setEnabled(true);
            semesterDropdown.setEnabled(true);
        }
    }

    private void clearFileAndTitle() {
        selectedFileUri = null;
        selectedFileName = null;
        selectedFileSize = 0;
        selectedFileText.setText("");
        selectedMemoUri = null;
        selectedMemoName = null;
        selectedMemoSize = 0;
        selectedMemoText.setText("");
        titleInput.setText("");
    }

    private PastPaper validateInputs() {
        String title = titleInput.getText() != null ? titleInput.getText().toString().trim() : "";
        String university = universityInput.getText() != null ? universityInput.getText().toString().trim() : "";
        String intake = intakeInput.getText() != null ? intakeInput.getText().toString().trim() : "";
        String yearStr = yearInput.getText() != null ? yearInput.getText().toString().trim() : "";
        String semester = semesterDropdown.getText() != null ? semesterDropdown.getText().toString().trim() : "";

        if (selectedFileUri == null) {
            Toast.makeText(this, R.string.select_file, Toast.LENGTH_SHORT).show();
            return null;
        }
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, R.string.error_required_field, Toast.LENGTH_SHORT).show();
            return null;
        }
        if (TextUtils.isEmpty(university)) {
            Toast.makeText(this, R.string.error_required_field, Toast.LENGTH_SHORT).show();
            return null;
        }
        if (chosenDegree == null) {
            Toast.makeText(this, R.string.select_degree, Toast.LENGTH_SHORT).show();
            return null;
        }
        int year;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.select_year, Toast.LENGTH_SHORT).show();
            return null;
        }

        String fileType = FileUtils.detectFileType(selectedFileName);

        PastPaper paper = new PastPaper();
        paper.setTitle(title);
        paper.setUniversity(university);
        paper.setDegreeId(chosenDegree.getId());
        paper.setDegreeName(chosenDegree.getName());
        paper.setIntake(intake);
        paper.setYear(year);
        paper.setSemester(semester);
        paper.setFileType(fileType);
        paper.setFileSizeBytes(selectedFileSize);
        paper.setUploadedByUid(sessionManager.getUid());
        paper.setUploadedByName(sessionManager.getFullName());
        paper.setApproved(true);
        
        return paper;
    }

    private void attemptUpload() {
        PastPaper paper = validateInputs();
        if (paper == null) return;

        setUploading(true);

        paperRepository.uploadPaper(selectedFileUri, selectedFileName, selectedMemoUri, selectedMemoName, paper,
                percent -> runOnUiThread(() -> uploadProgress.setProgress(percent)),
                new DataCallback<>() {
                    @Override
                    public void onSuccess(PastPaper result) {
                        setUploading(false);
                        Toast.makeText(UploadPaperActivity.this, R.string.upload_success, Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void onError(Exception e) {
                        setUploading(false);
                        Toast.makeText(UploadPaperActivity.this,
                                e.getMessage() != null ? e.getMessage() : getString(R.string.upload_failed),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void uploadQueue() {
        if (paperQueue.isEmpty()) return;
        
        setUploading(true);
        uploadNextInQueue();
    }

    private void uploadNextInQueue() {
        if (paperQueue.isEmpty()) {
            setUploading(false);
            Toast.makeText(this, "All papers uploaded successfully", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        QueuedPaper next = paperQueue.get(0);
        paperRepository.uploadPaper(next.uri, next.fileName, next.memoUri, next.memoName, next.meta,
                percent -> {
                    runOnUiThread(() -> uploadProgress.setProgress(percent));
                },
                new DataCallback<>() {
                    @Override
                    public void onSuccess(PastPaper result) {
                        paperQueue.remove(0);
                        updateQueueUI();
                        uploadNextInQueue();
                    }

                    @Override
                    public void onError(Exception e) {
                        setUploading(false);
                        Toast.makeText(UploadPaperActivity.this, "Failed at: " + next.meta.getTitle(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setUploading(boolean uploading) {
        addBtn.setEnabled(!uploading);
        submitButton.setEnabled(!uploading);
        submitAllBtn.setEnabled(!uploading);
        submitButton.setText(uploading ? getString(R.string.uploading) : getString(R.string.upload));
        uploadProgress.setVisibility(uploading ? View.VISIBLE : View.GONE);
        uploadProgress.setProgress(0);
    }
}
