package com.bravem.app.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.DegreeDao;
import com.bravem.app.domain.model.DegreeMapper;
import com.bravem.app.domain.repository.DegreeRepository;
import com.bravem.app.model.Degree;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DegreeRepositoryImpl implements DegreeRepository {

    private final DegreeDao degreeDao;
    private final DatabaseReference degreesRef;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public DegreeRepositoryImpl(Context context) {
        this.degreeDao = AppDatabase.getInstance(context).degreeDao();
        this.degreesRef = FirebaseDatabase.getInstance().getReference("degrees");
    }

    private void syncDegrees(DataCallback<Void> callback) {
        degreesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                executor.execute(() -> {
                    for (DataSnapshot degreeSnapshot : snapshot.getChildren()) {
                        Degree degree = degreeSnapshot.getValue(Degree.class);
                        if (degree != null) {
                            degree.setSynced(true);
                            degreeDao.insert(degree);
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

    @Override
    public void fetchDegreesByUniversity(String university, DataCallback<List<com.bravem.app.domain.model.Degree>> callback) {
        syncDegrees(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                executor.execute(() -> {
                    List<Degree> degrees = degreeDao.getByUniversity(university);
                    mainHandler.post(() -> callback.onSuccess(DegreeMapper.toDomain(degrees)));
                });
            }
            @Override
            public void onError(Exception e) {
                executor.execute(() -> {
                    List<Degree> degrees = degreeDao.getByUniversity(university);
                    mainHandler.post(() -> callback.onSuccess(DegreeMapper.toDomain(degrees)));
                });
            }
        });
    }

    @Override
    public void fetchAllUniversities(DataCallback<List<String>> callback) {
        syncDegrees(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                executor.execute(() -> {
                    List<String> universities = degreeDao.getUniqueUniversities();
                    mainHandler.post(() -> callback.onSuccess(universities));
                });
            }
            @Override
            public void onError(Exception e) {
                executor.execute(() -> {
                    List<String> universities = degreeDao.getUniqueUniversities();
                    mainHandler.post(() -> callback.onSuccess(universities));
                });
            }
        });
    }

    @Override
    public void addDegree(String name, String description, String university, DataCallback<com.bravem.app.domain.model.Degree> callback) {
        executor.execute(() -> {
            String id = UUID.randomUUID().toString();
            Degree degree = new Degree(id, name, description, university, System.currentTimeMillis());
            degreeDao.insert(degree);
            mainHandler.post(() -> callback.onSuccess(DegreeMapper.toDomain(degree)));
        });
    }

    @Override
    public void fetchAllDegrees(DataCallback<List<com.bravem.app.domain.model.Degree>> callback) {
        syncDegrees(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                executor.execute(() -> {
                    List<Degree> degrees = degreeDao.getAll();
                    mainHandler.post(() -> callback.onSuccess(DegreeMapper.toDomain(degrees)));
                });
            }

            @Override
            public void onError(Exception e) {
                executor.execute(() -> {
                    List<Degree> degrees = degreeDao.getAll();
                    mainHandler.post(() -> callback.onSuccess(DegreeMapper.toDomain(degrees)));
                });
            }
        });
    }

    @Override
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

    @Override
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
