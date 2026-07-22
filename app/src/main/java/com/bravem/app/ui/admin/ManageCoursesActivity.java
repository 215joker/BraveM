package com.bravem.app.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.adapter.CourseAdapter;
import com.bravem.app.data.CourseRepository;
import com.bravem.app.data.DataCallback;
import com.bravem.app.data.DegreeRepository;
import com.bravem.app.model.Course;
import com.bravem.app.model.Degree;
import com.bravem.app.utils.UiUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class ManageCoursesActivity extends AppCompatActivity {

    private AutoCompleteTextView degreeFilterDropdown;
    private RecyclerView recyclerView;
    private CourseAdapter adapter;
    private CircularProgressIndicator progressIndicator;
    private View emptyState;
    private View backButton;
    private FloatingActionButton fabAdd;

    private DegreeRepository degreeRepository;
    private CourseRepository courseRepository;

    private List<Degree> degrees = new ArrayList<>();
    private Degree selectedFilterDegree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_courses);

        UiUtils.handleTopInset(findViewById(R.id.app_bar));
        UiUtils.handleFabBottomInset(findViewById(R.id.fab_add), 24);

        degreeRepository = new DegreeRepository(this);
        courseRepository = new CourseRepository(this);

        degreeFilterDropdown = findViewById(R.id.dropdown_degree_filter);
        recyclerView = findViewById(R.id.recycler_list);
        progressIndicator = findViewById(R.id.progress_indicator);
        emptyState = findViewById(R.id.empty_state);
        backButton = findViewById(R.id.btn_back);
        fabAdd = findViewById(R.id.fab_add);

        backButton.setOnClickListener(v -> finish());
        fabAdd.setOnClickListener(v -> showCourseDialog(null));

        adapter = new CourseAdapter(course -> showCourseDialog(course));
        adapter.setActionListener(new CourseAdapter.OnCourseActionListener() {
            @Override
            public void onEdit(Course course) {
                showCourseDialog(course);
            }

            @Override
            public void onDelete(Course course) {
                confirmDelete(course);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadDegreesThenCourses();
    }

    private void loadDegreesThenCourses() {
        degreeRepository.fetchAllDegrees(new DataCallback<List<Degree>>() {
            @Override
            public void onSuccess(List<Degree> result) {
                degrees = result;
                List<String> names = new ArrayList<>();
                for (Degree d : result) names.add(d.getName());

                ArrayAdapter<String> dAdapter = new ArrayAdapter<>(ManageCoursesActivity.this,
                        android.R.layout.simple_list_item_1, names);
                degreeFilterDropdown.setAdapter(dAdapter);

                degreeFilterDropdown.setOnItemClickListener((parent, view, position, id) -> {
                    selectedFilterDegree = degrees.get(position);
                    loadCourses();
                });

                if (!degrees.isEmpty()) {
                    selectedFilterDegree = degrees.get(0);
                    degreeFilterDropdown.setText(selectedFilterDegree.getName(), false);
                    loadCourses();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ManageCoursesActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCourses() {
        if (selectedFilterDegree == null) return;
        progressIndicator.setVisibility(View.VISIBLE);
        courseRepository.fetchCoursesForDegree(selectedFilterDegree.getId(), new DataCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> courses) {
                progressIndicator.setVisibility(View.GONE);
                adapter.submitList(courses);
                emptyState.setVisibility(courses.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception e) {
                progressIndicator.setVisibility(View.GONE);
                Toast.makeText(ManageCoursesActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCourseDialog(Course existing) {
        if (selectedFilterDegree == null) {
            Toast.makeText(this, R.string.select_degree, Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_course, null);
        EditText nameInput = dialogView.findViewById(R.id.input_name);
        EditText codeInput = dialogView.findViewById(R.id.input_code);

        if (existing != null) {
            nameInput.setText(existing.getName());
            codeInput.setText(existing.getCode());
        }

        new AlertDialog.Builder(this)
                .setTitle(existing == null ? R.string.add_course : R.string.edit)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String code = codeInput.getText().toString().trim();
                    if (name.isEmpty() || code.isEmpty()) {
                        Toast.makeText(this, R.string.error_required_field, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (existing == null) {
                        createCourse(name, code);
                    } else {
                        updateCourse(existing.getId(), name, code);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void createCourse(String name, String code) {
        courseRepository.addCourse(selectedFilterDegree.getId(), name, code, new DataCallback<Course>() {
            @Override
            public void onSuccess(Course result) {
                loadCourses();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ManageCoursesActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCourse(String id, String name, String code) {
        courseRepository.updateCourse(id, name, code, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadCourses();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ManageCoursesActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDelete(Course course) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.delete, (dialog, which) ->
                        courseRepository.deleteCourse(course.getId(), new DataCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                loadCourses();
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(ManageCoursesActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
