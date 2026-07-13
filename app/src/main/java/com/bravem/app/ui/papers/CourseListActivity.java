package com.bravem.app.ui.papers;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.adapter.CourseAdapter;
import com.bravem.app.data.CourseRepository;
import com.bravem.app.data.DataCallback;
import com.bravem.app.model.Course;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

/**
 * Lists all Courses belonging to a specific Degree.
 */
public class CourseListActivity extends AppCompatActivity {

    public static final String EXTRA_DEGREE_ID = "extra_degree_id";
    public static final String EXTRA_DEGREE_NAME = "extra_degree_name";

    private RecyclerView recyclerView;
    private CourseAdapter adapter;
    private TextView titleView;
    private CircularProgressIndicator progressIndicator;
    private View emptyState;
    private View backButton;

    private CourseRepository courseRepository;

    private String degreeId;
    private String degreeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);

        courseRepository = new CourseRepository(this);

        recyclerView = findViewById(R.id.recycler_courses);
        titleView = findViewById(R.id.text_title);
        progressIndicator = findViewById(R.id.progress_indicator);
        emptyState = findViewById(R.id.empty_state);
        backButton = findViewById(R.id.btn_back);

        backButton.setOnClickListener(v -> finish());

        adapter = new CourseAdapter(course -> {
            Intent intent = new Intent(CourseListActivity.this, PaperListActivity.class);
            intent.putExtra(PaperListActivity.EXTRA_COURSE_ID, course.getId());
            intent.putExtra(PaperListActivity.EXTRA_COURSE_NAME, course.getName());
            intent.putExtra(PaperListActivity.EXTRA_COURSE_CODE, course.getCode());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        degreeId = getIntent().getStringExtra(EXTRA_DEGREE_ID);
        degreeName = getIntent().getStringExtra(EXTRA_DEGREE_NAME);

        if (degreeId != null) {
            titleView.setText(degreeName != null ? degreeName : getString(R.string.courses));
            loadCourses();
        } else {
            titleView.setText(getString(R.string.courses));
            loadAllCoursesFallback();
        }
    }

    private void loadCourses() {
        progressIndicator.setVisibility(View.VISIBLE);
        courseRepository.fetchCoursesForDegree(degreeId, new DataCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> courses) {
                progressIndicator.setVisibility(View.GONE);
                adapter.submitList(courses);
                emptyState.setVisibility(courses.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(courses.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onError(Exception e) {
                progressIndicator.setVisibility(View.GONE);
                Toast.makeText(CourseListActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllCoursesFallback() {
        progressIndicator.setVisibility(View.VISIBLE);
        courseRepository.fetchAllCourses(new DataCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> courses) {
                progressIndicator.setVisibility(View.GONE);
                adapter.submitList(courses);
                emptyState.setVisibility(courses.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(courses.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onError(Exception e) {
                progressIndicator.setVisibility(View.GONE);
                Toast.makeText(CourseListActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
