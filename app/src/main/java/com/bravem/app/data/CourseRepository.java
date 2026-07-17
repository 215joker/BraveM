package com.bravem.app.data;

import android.content.Context;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.CourseDao;
import com.bravem.app.model.Course;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles reads/writes for courses using local Room storage.
 */
public class CourseRepository {

    private final CourseDao courseDao;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    public CourseRepository(Context context) {
        this.courseDao = AppDatabase.getInstance(context).courseDao();
    }

    public void fetchCoursesForDegree(String degreeId, DataCallback<List<Course>> callback) {
        executor.execute(() -> {
            List<Course> courses = courseDao.getByDegreeId(degreeId);
            mainHandler.post(() -> callback.onSuccess(courses));
        });
    }

    public void fetchAllCourses(DataCallback<List<Course>> callback) {
        executor.execute(() -> {
            List<Course> courses = courseDao.getAll();
            mainHandler.post(() -> callback.onSuccess(courses));
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
