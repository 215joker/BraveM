package com.bravem.app.data;

import android.content.Context;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.DegreeDao;
import com.bravem.app.model.Degree;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles reads/writes for degrees using local Room storage.
 */
public class DegreeRepository {

    private final DegreeDao degreeDao;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    public DegreeRepository(Context context) {
        this.degreeDao = AppDatabase.getInstance(context).degreeDao();
    }

    public void fetchAllDegrees(DataCallback<List<Degree>> callback) {
        executor.execute(() -> {
            List<Degree> degrees = degreeDao.getAll();
            mainHandler.post(() -> callback.onSuccess(degrees));
        });
    }

    public void fetchAllUniversities(DataCallback<List<String>> callback) {
        executor.execute(() -> {
            List<String> universities = degreeDao.getUniqueUniversities();
            mainHandler.post(() -> callback.onSuccess(universities));
        });
    }

    public void fetchDegreesByUniversity(String university, DataCallback<List<Degree>> callback) {
        executor.execute(() -> {
            List<Degree> degrees = degreeDao.getByUniversity(university);
            mainHandler.post(() -> callback.onSuccess(degrees));
        });
    }

    public void addDegree(String name, String description, DataCallback<Degree> callback) {
        addDegree(name, description, null, callback);
    }

    public void addDegree(String name, String description, String university, DataCallback<Degree> callback) {
        executor.execute(() -> {
            String id = UUID.randomUUID().toString();
            Degree degree = new Degree(id, name, description, university, System.currentTimeMillis());
            degreeDao.insert(degree);
            mainHandler.post(() -> callback.onSuccess(degree));
        });
    }

    public void updateDegree(String id, String name, String description, DataCallback<Void> callback) {
        updateDegree(id, name, description, null, callback);
    }

    public void updateDegree(String id, String name, String description, String university, DataCallback<Void> callback) {
        executor.execute(() -> {
            Degree degree = degreeDao.getById(id);
            if (degree != null) {
                degree.setName(name);
                degree.setDescription(description);
                degree.setUniversity(university);
                degreeDao.update(degree);
                mainHandler.post(() -> callback.onSuccess(null));
            } else {
                mainHandler.post(() -> callback.onError(new Exception("Degree not found")));
            }
        });
    }

    public void deleteDegree(String id, DataCallback<Void> callback) {
        executor.execute(() -> {
            Degree degree = degreeDao.getById(id);
            if (degree != null) {
                degreeDao.delete(degree);
                mainHandler.post(() -> callback.onSuccess(null));
            } else {
                mainHandler.post(() -> callback.onError(new Exception("Degree not found")));
            }
        });
    }
}
