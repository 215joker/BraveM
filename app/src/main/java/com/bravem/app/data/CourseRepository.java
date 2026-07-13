package com.bravem.app.data;

import android.content.Context;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.CourseDao;
import com.bravem.app.model.Course;

import java.util.List;
import java.util.UUID;

/**
 * Handles reads/writes for courses using local Room storage.
 */
public class CourseRepository {

    private final CourseDao courseDao;

    public CourseRepository(Context context) {
        this.courseDao = AppDatabase.getInstance(context).courseDao();
    }

    public void fetchCoursesForDegree(String degreeId, DataCallback<List<Course>> callback) {
        callback.onSuccess(courseDao.getByDegreeId(degreeId));
    }

    public void fetchAllCourses(DataCallback<List<Course>> callback) {
        callback.onSuccess(courseDao.getAll());
    }

    public void addCourse(String degreeId, String name, String code, DataCallback<Course> callback) {
        String id = UUID.randomUUID().toString();
        Course course = new Course(id, degreeId, name, code, System.currentTimeMillis());
        courseDao.insert(course);
        callback.onSuccess(course);
    }

    public void updateCourse(String courseId, String name, String code, DataCallback<Void> callback) {
        Course course = courseDao.getById(courseId);
        if (course != null) {
            course.setName(name);
            course.setCode(code);
            courseDao.update(course);
            callback.onSuccess(null);
        } else {
            callback.onError(new Exception("Course not found"));
        }
    }

    public void deleteCourse(String courseId, DataCallback<Void> callback) {
        Course course = courseDao.getById(courseId);
        if (course != null) {
            courseDao.delete(course);
            callback.onSuccess(null);
        } else {
            callback.onError(new Exception("Course not found"));
        }
    }
}
