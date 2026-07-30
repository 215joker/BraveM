package com.bravem.app.domain.repository;

import com.bravem.app.data.DataCallback;
import com.bravem.app.model.PastPaper;

import java.util.List;

public interface PaperRepository {
    interface UploadProgressListener {
        void onProgress(int percent);
    }

    void fetchRecommendedPapers(String degreeId, String intake, int limit, DataCallback<List<PastPaper>> callback);
    void fetchPapersForDegree(String degreeId, int limit, DataCallback<List<PastPaper>> callback);
    void fetchPapersByCourse(String courseId, DataCallback<List<PastPaper>> callback);
    void searchPapers(String query, DataCallback<List<PastPaper>> callback);
    void togglePin(String paperId, DataCallback<Boolean> callback);
    void fetchPapersUploadedBy(String uid, DataCallback<List<PastPaper>> callback);
    void fetchAllPapersForAdmin(DataCallback<List<PastPaper>> callback);
    void setPaperApproved(String paperId, boolean approved, DataCallback<Void> callback);
    void deletePaper(String paperId, DataCallback<Void> callback);
    void uploadPaper(android.net.Uri fileUri, String originalFileName, android.net.Uri memoUri, String memoFileName, PastPaper paperMeta, UploadProgressListener progressListener, DataCallback<PastPaper> callback);
    void uploadMemorandumOnly(String paperId, android.net.Uri memoUri, String memoFileName, UploadProgressListener progressListener, DataCallback<PastPaper> callback);
}
