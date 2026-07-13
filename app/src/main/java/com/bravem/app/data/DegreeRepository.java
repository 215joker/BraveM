package com.bravem.app.data;

import android.content.Context;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.DegreeDao;
import com.bravem.app.model.Degree;

import java.util.List;
import java.util.UUID;

/**
 * Handles reads/writes for degrees using local Room storage.
 */
public class DegreeRepository {

    private final DegreeDao degreeDao;

    public DegreeRepository(Context context) {
        this.degreeDao = AppDatabase.getInstance(context).degreeDao();
    }

    public void fetchAllDegrees(DataCallback<List<Degree>> callback) {
        callback.onSuccess(degreeDao.getAll());
    }

    public void fetchDegreesByUniversity(String university, DataCallback<List<Degree>> callback) {
        callback.onSuccess(degreeDao.getByUniversity(university));
    }

    public void addDegree(String name, String description, DataCallback<Degree> callback) {
        addDegree(name, description, null, callback);
    }

    public void addDegree(String name, String description, String university, DataCallback<Degree> callback) {
        String id = UUID.randomUUID().toString();
        Degree degree = new Degree(id, name, description, university, System.currentTimeMillis());
        degreeDao.insert(degree);
        callback.onSuccess(degree);
    }

    public void updateDegree(String id, String name, String description, DataCallback<Void> callback) {
        updateDegree(id, name, description, null, callback);
    }

    public void updateDegree(String id, String name, String description, String university, DataCallback<Void> callback) {
        Degree degree = degreeDao.getById(id);
        if (degree != null) {
            degree.setName(name);
            degree.setDescription(description);
            degree.setUniversity(university);
            degreeDao.update(degree);
            callback.onSuccess(null);
        } else {
            callback.onError(new Exception("Degree not found"));
        }
    }

    public void deleteDegree(String id, DataCallback<Void> callback) {
        Degree degree = degreeDao.getById(id);
        if (degree != null) {
            degreeDao.delete(degree);
            callback.onSuccess(null);
        } else {
            callback.onError(new Exception("Degree not found"));
        }
    }
}
