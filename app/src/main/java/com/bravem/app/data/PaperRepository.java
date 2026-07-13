package com.bravem.app.data;

import android.content.Context;
import android.net.Uri;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.PaperDao;
import com.bravem.app.model.PastPaper;
import com.bravem.app.utils.FileUtils;

import java.util.List;
import java.util.UUID;

/**
 * Handles past-paper local file storage and metadata read/write in Room.
 */
public class PaperRepository {

    private final Context context;
    private final PaperDao paperDao;

    public interface UploadProgressListener {
        void onProgress(int percent);
    }

    public PaperRepository(Context context) {
        this.context = context.getApplicationContext();
        this.paperDao = AppDatabase.getInstance(context).paperDao();
    }

    public void uploadPaper(Uri fileUri, String originalFileName, PastPaper paperMeta,
                             UploadProgressListener progressListener,
                             DataCallback<PastPaper> callback) {

        String localPath = FileUtils.copyFileToInternalStorage(context, fileUri, originalFileName);
        if (localPath != null) {
            if (progressListener != null) progressListener.onProgress(100);

            paperMeta.setId(UUID.randomUUID().toString());
            paperMeta.setFileUrl(localPath);
            paperMeta.setFileName(originalFileName);
            paperMeta.setCreatedAt(System.currentTimeMillis());

            paperDao.insert(paperMeta);
            callback.onSuccess(paperMeta);
        } else {
            callback.onError(new Exception("Failed to copy file to local storage"));
        }
    }

    public void fetchPapersForCourse(String courseId, DataCallback<List<PastPaper>> callback) {
        callback.onSuccess(paperDao.getByCourseId(courseId));
    }

    public void fetchPapersForDegree(String degreeId, int limit, DataCallback<List<PastPaper>> callback) {
        List<PastPaper> papers = paperDao.getByDegreeId(degreeId);
        if (papers.size() > limit) {
            papers = papers.subList(0, limit);
        }
        callback.onSuccess(papers);
    }

    public void fetchRecentPapers(int limit, DataCallback<List<PastPaper>> callback) {
        List<PastPaper> papers = paperDao.getAll();
        if (papers.size() > limit) {
            papers = papers.subList(0, limit);
        }
        callback.onSuccess(papers);
    }

    public void fetchPapersUploadedBy(String uid, DataCallback<List<PastPaper>> callback) {
        callback.onSuccess(paperDao.getByUploader(uid));
    }

    public void fetchAllPapersForAdmin(DataCallback<List<PastPaper>> callback) {
        callback.onSuccess(paperDao.getAll());
    }

    public void searchPapersByTitlePrefix(String queryText, DataCallback<List<PastPaper>> callback) {
        callback.onSuccess(paperDao.search(queryText));
    }

    public void deletePaper(String paperId, DataCallback<Void> callback) {
        PastPaper paper = paperDao.getById(paperId);
        if (paper != null) {
            paperDao.delete(paper);
            callback.onSuccess(null);
        } else {
            callback.onError(new Exception("Paper not found"));
        }
    }

    public void setPaperApproved(String paperId, boolean approved, DataCallback<Void> callback) {
        PastPaper paper = paperDao.getById(paperId);
        if (paper != null) {
            paper.setApproved(approved);
            paperDao.update(paper);
            callback.onSuccess(null);
        } else {
            callback.onError(new Exception("Paper not found"));
        }
    }
}
