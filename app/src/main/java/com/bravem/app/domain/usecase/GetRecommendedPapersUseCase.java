package com.bravem.app.domain.usecase;

import com.bravem.app.data.DataCallback;
import com.bravem.app.domain.repository.PaperRepository;
import com.bravem.app.model.PastPaper;

import java.util.List;

public class GetRecommendedPapersUseCase {
    private final PaperRepository paperRepository;

    public GetRecommendedPapersUseCase(PaperRepository paperRepository) {
        this.paperRepository = paperRepository;
    }

    public void execute(String degreeId, String intake, DataCallback<List<PastPaper>> callback) {
        if (degreeId == null || degreeId.isEmpty()) {
            callback.onError(new IllegalArgumentException("Degree ID is required"));
            return;
        }
        // Logic: if intake is null, we can still fetch papers but maybe less targeted
        paperRepository.fetchRecommendedPapers(degreeId, intake, 20, callback);
    }
}
