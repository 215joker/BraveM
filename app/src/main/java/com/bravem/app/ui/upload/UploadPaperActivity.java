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
import com.bravem.app.data.CourseRepository;
import com.bravem.app.data.DataCallback;
import com.bravem.app.data.DegreeRepository;
import com.bravem.app.data.PaperRepository;
import com.bravem.app.model.Course;
import com.bravem.app.model.Degree;
import com.bravem.app.model.PastPaper;
import com.bravem.app.utils.FileUtils;
import com.bravem.app.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class UploadPaperActivity extends AppCompatActivity {

    private View backButton;
    private View pickFileButton;
    private TextView selectedFileText;
    private TextInputEditText titleInput;
    private AutoCompleteTextView degreeDropdown;
    private AutoCompleteTextView courseDropdown;
    private TextInputEditText yearInput;
    private AutoCompleteTextView semesterDropdown;
    private MaterialButton submitButton;
    private LinearProgressIndicator uploadProgress;

    private DegreeRepository degreeRepository;
    private CourseRepository courseRepository;
    private PaperRepository paperRepository;

    private SessionManager sessionManager;

    private Uri selectedFileUri;
    private String selectedFileName;
    private long selectedFileSize;

    private List<Degree> degrees = new ArrayList<>();
    private List<Course> courses = new ArrayList<>();
    private Degree chosenDegree;
    private Course chosenCourse;

    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        handleFileSelected(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_paper);

        degreeRepository = new DegreeRepository(this);
        courseRepository = new CourseRepository(this);
        paperRepository = new PaperRepository(this);
        sessionManager = new SessionManager(this);

        backButton = findViewById(R.id.btn_back);
        pickFileButton = findViewById(R.id.btn_pick_file);
        selectedFileText = findViewById(R.id.text_selected_file);
        titleInput = findViewById(R.id.input_title);
        degreeDropdown = findViewById(R.id.dropdown_degree);
        courseDropdown = findViewById(R.id.dropdown_course);
        yearInput = findViewById(R.id.input_year);
        semesterDropdown = findViewById(R.id.dropdown_semester);
        submitButton = findViewById(R.id.btn_submit);
        uploadProgress = findViewById(R.id.upload_progress);

        backButton.setOnClickListener(v -> finish());
        pickFileButton.setOnClickListener(v -> launchFilePicker());
        submitButton.setOnClickListener(v -> attemptUpload());

        setupSemesterDropdown();
        loadDegrees();

        courseDropdown.setEnabled(false);
    }

    private void setupSemesterDropdown() {
        String[] semesters = {"Semester 1", "Semester 2", "Semester 3", "Annual"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, semesters);
        semesterDropdown.setAdapter(adapter);
    }

    private void loadDegrees() {
        degreeRepository.fetchAllDegrees(new DataCallback<List<Degree>>() {
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
                    courseDropdown.setText("", false);
                    courseDropdown.setEnabled(true);
                    chosenCourse = null;
                    loadCoursesForDegree(chosenDegree.getId());
                });

                String myDegreeId = sessionManager.getDegreeId();
                if (myDegreeId != null) {
                    for (Degree d : result) {
                        if (d.getId().equals(myDegreeId)) {
                            chosenDegree = d;
                            degreeDropdown.setText(d.getName(), false);
                            courseDropdown.setEnabled(true);
                            loadCoursesForDegree(d.getId());
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

    private void loadCoursesForDegree(String degreeId) {
        courseRepository.fetchCoursesForDegree(degreeId, new DataCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> result) {
                courses = result;
                List<String> names = new ArrayList<>();
                for (Course c : result) names.add(c.getName() + " (" + c.getCode() + ")");

                ArrayAdapter<String> adapter = new ArrayAdapter<>(UploadPaperActivity.this,
                        android.R.layout.simple_list_item_1, names);
                courseDropdown.setAdapter(adapter);

                courseDropdown.setOnItemClickListener((parent, view, position, id) ->
                        chosenCourse = courses.get(position));
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(UploadPaperActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void launchFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        });
        filePickerLauncher.launch(Intent.createChooser(intent, getString(R.string.select_file)));
    }

    private void handleFileSelected(Uri uri) {
        selectedFileUri = uri;
        selectedFileName = FileUtils.getFileName(this, uri);
        selectedFileSize = FileUtils.getFileSize(this, uri);

        String type = FileUtils.detectFileType(selectedFileName);
        if ("unknown".equals(type)) {
            Toast.makeText(this, "Please select a PDF or Word document.", Toast.LENGTH_LONG).show();
            selectedFileUri = null;
            return;
        }

        selectedFileText.setText(selectedFileName + " (" + FileUtils.humanReadableSize(selectedFileSize) + ")");
    }

    private void attemptUpload() {
        String title = titleInput.getText() != null ? titleInput.getText().toString().trim() : "";
        String yearStr = yearInput.getText() != null ? yearInput.getText().toString().trim() : "";
        String semester = semesterDropdown.getText() != null ? semesterDropdown.getText().toString().trim() : "";

        if (selectedFileUri == null) {
            Toast.makeText(this, R.string.select_file, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, R.string.error_required_field, Toast.LENGTH_SHORT).show();
            return;
        }
        if (chosenDegree == null) {
            Toast.makeText(this, R.string.select_degree, Toast.LENGTH_SHORT).show();
            return;
        }
        if (chosenCourse == null) {
            Toast.makeText(this, R.string.select_course, Toast.LENGTH_SHORT).show();
            return;
        }
        int year;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.select_year, Toast.LENGTH_SHORT).show();
            return;
        }

        String fileType = FileUtils.detectFileType(selectedFileName);

        PastPaper paper = new PastPaper();
        paper.setTitle(title);
        paper.setDegreeId(chosenDegree.getId());
        paper.setDegreeName(chosenDegree.getName());
        paper.setCourseId(chosenCourse.getId());
        paper.setCourseName(chosenCourse.getName());
        paper.setCourseCode(chosenCourse.getCode());
        paper.setYear(year);
        paper.setSemester(semester);
        paper.setFileType(fileType);
        paper.setFileSizeBytes(selectedFileSize);
        paper.setUploadedByUid(sessionManager.getUid());
        paper.setUploadedByName(sessionManager.getFullName());
        paper.setApproved(true);

        setUploading(true);

        paperRepository.uploadPaper(selectedFileUri, selectedFileName, paper,
                percent -> runOnUiThread(() -> uploadProgress.setProgress(percent)),
                new DataCallback<PastPaper>() {
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

    private void setUploading(boolean uploading) {
        submitButton.setEnabled(!uploading);
        submitButton.setText(uploading ? getString(R.string.uploading) : getString(R.string.submit));
        uploadProgress.setVisibility(uploading ? View.VISIBLE : View.GONE);
        uploadProgress.setProgress(0);
    }
}
