package com.bravem.app.data;

import android.content.Context;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.CourseDao;
import com.bravem.app.model.Course;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles reads/writes for courses using local Room storage and Firebase sync.
 */
public class CourseRepository {

    private final CourseDao courseDao;
    private final DatabaseReference coursesRef;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    public CourseRepository(Context context) {
        this.courseDao = AppDatabase.getInstance(context).courseDao();
        this.coursesRef = FirebaseDatabase.getInstance().getReference("courses");
    }

    public void syncCourses(DataCallback<Void> callback) {
        coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                executor.execute(() -> {
                    for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                        Course course = courseSnapshot.getValue(Course.class);
                        if (course != null) {
                            course.setSynced(true);
                            courseDao.insert(course);
                        }
                    }
                    mainHandler.post(() -> callback.onSuccess(null));
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                mainHandler.post(() -> callback.onError(error.toException()));
            }
        });
    }

    public void fetchCoursesForDegree(String degreeId, DataCallback<List<Course>> callback) {
        syncCourses(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                executor.execute(() -> {
                    List<Course> courses = courseDao.getByDegreeId(degreeId);
                    mainHandler.post(() -> callback.onSuccess(courses));
                });
            }

            @Override
            public void onError(Exception e) {
                executor.execute(() -> {
                    List<Course> courses = courseDao.getByDegreeId(degreeId);
                    mainHandler.post(() -> callback.onSuccess(courses));
                });
            }
        });
    }

    public void fetchAllCourses(DataCallback<List<Course>> callback) {
        syncCourses(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                executor.execute(() -> {
                    List<Course> courses = courseDao.getAll();
                    mainHandler.post(() -> callback.onSuccess(courses));
                });
            }

            @Override
            public void onError(Exception e) {
                executor.execute(() -> {
                    List<Course> courses = courseDao.getAll();
                    mainHandler.post(() -> callback.onSuccess(courses));
                });
            }
        });
    }

    public void addCourse(String degreeId, String name, String code, DataCallback<Course> callback) {
        executor.execute(() -> {
            String id = UUID.randomUUID().toString();
            Course course = new Course(id, degreeId, name, code, System.currentTimeMillis());
            courseDao.insert(course);
            mainHandler.post(() -> callback.onSuccess(course));
        });
    }

    public void updateCourse(String courseId, String name, String code, DataCallback<Void> callback) {
        executor.execute(() -> {
            Course course = courseDao.getById(courseId);
            if (course != null) {
                course.setName(name);
                course.setCode(code);
                courseDao.update(course);
                mainHandler.post(() -> callback.onSuccess(null));
            } else {
                mainHandler.post(() -> callback.onError(new Exception("Course not found")));
            }
        });
    }

    public void deleteCourse(String courseId, DataCallback<Void> callback) {
        executor.execute(() -> {
            Course course = courseDao.getById(courseId);
            if (course != null) {
                courseDao.delete(course);
                mainHandler.post(() -> callback.onSuccess(null));
            } else {
                mainHandler.post(() -> callback.onError(new Exception("Course not found")));
            }
        });
    }
}
