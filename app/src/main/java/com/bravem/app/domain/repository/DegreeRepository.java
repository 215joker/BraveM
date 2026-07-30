package com.bravem.app.domain.repository;

import com.bravem.app.data.DataCallback;
import com.bravem.app.domain.model.Degree;

import java.util.List;

public interface DegreeRepository {
    void fetchDegreesByUniversity(String university, DataCallback<List<Degree>> callback);
    void fetchAllUniversities(DataCallback<List<String>> callback);
    void addDegree(String name, String description, String university, DataCallback<Degree> callback);
    void fetchAllDegrees(DataCallback<List<Degree>> callback);
    void updateDegree(String id, String name, String description, String university, DataCallback<Void> callback);
    void deleteDegree(String id, DataCallback<Void> callback);
}
