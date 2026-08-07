package com.bravem.app.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.source.PaperLocalDataSource;
import com.bravem.app.domain.repository.PaperRepository;
import com.bravem.app.model.PastPaper;
import com.bravem.app.utils.SessionManager;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaperRepositoryImpl implements PaperRepository {

    private final PaperLocalDataSource localDataSource;
    private final SessionManager sessionManager;
    private final NotificationRepository notificationRepository;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Context context;

    public PaperRepositoryImpl(Context context) {
        this.context = context.getApplicationContext();
        this.localDataSource = new PaperLocalDataSource(AppDatabase.getInstance(context).paperDao());
        this.sessionManager = new SessionManager(context);
        this.notificationRepository = new NotificationRepository(context);
    }

    @Override
    public void fetchRecommendedPapers(String degreeId, String intake, int limit, DataCallback<List<PastPaper>> callback) {
        String uid = sessionManager.getUid();
        executor.execute(() -> {
            List<PastPaper> localPapers = localDataSource.getRecommended(degreeId, intake, uid);
            mainHandler.post(() -> callback.onSuccess(localPapers));
        });
    }

    @Override
    public void togglePin(String paperId, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            PastPaper paper = localDataSource.getById(paperId);
            if (paper != null) {
                boolean newState = !paper.isPinned();
                if (newState && localDataSource.getPinnedCount() >= 10) {
                    mainHandler.post(() -> callback.onError(new Exception("Limit of 10 pins reached")));
                    return;
                }
                paper.setPinned(newState);
                localDataSource.updatePaper(paper);
                mainHandler.post(() -> callback.onSuccess(newState));
            } else {
                mainHandler.post(() -> callback.onError(new Exception("Paper not found")));
            }
        });
    }

    @Override
    public void fetchPapersForDegree(String degreeId, int limit, DataCallback<List<PastPaper>> callback) {
        executor.execute(() -> {
            List<PastPaper> papers = localDataSource.getByDegreeId(degreeId, sessionManager.getUid());
            mainHandler.post(() -> callback.onSuccess(papers));
        });
    }

    @Override
    public void fetchPapersByCourse(String courseId, DataCallback<List<PastPaper>> callback) {
        executor.execute(() -> {
            List<PastPaper> papers = localDataSource.getByCourseId(courseId);
            mainHandler.post(() -> callback.onSuccess(papers));
        });
    }

    @Override
    public void searchPapers(String query, DataCallback<List<PastPaper>> callback) {
        executor.execute(() -> {
            List<PastPaper> results = localDataSource.search(query);
            mainHandler.post(() -> callback.onSuccess(results));
        });
    }

    @Override
    public void fetchPapersUploadedBy(String uid, DataCallback<List<PastPaper>> callback) {
        executor.execute(() -> {
            List<PastPaper> papers = localDataSource.getUploadedBy(uid);
            mainHandler.post(() -> callback.onSuccess(papers));
        });
    }

    @Override
    public void fetchAllPapersForAdmin(DataCallback<List<PastPaper>> callback) {
        executor.execute(() -> {
            List<PastPaper> papers = localDataSource.getAll();
            mainHandler.post(() -> callback.onSuccess(papers));
        });
    }

    @Override
    public void deletePaper(String paperId, DataCallback<Void> callback) {
        executor.execute(() -> {
            PastPaper paper = localDataSource.getById(paperId);
            if (paper != null) {
                localDataSource.deletePaper(paper);
                if (callback != null) mainHandler.post(() -> callback.onSuccess(null));
            }
        });
    }

    @Override
    public void setPaperApproved(String paperId, boolean approved, DataCallback<Void> callback) {
        executor.execute(() -> {
            PastPaper paper = localDataSource.getById(paperId);
            if (paper != null) {
                paper.setApproved(approved);
                localDataSource.updatePaper(paper);
                if (callback != null) mainHandler.post(() -> callback.onSuccess(null));
            }
        });
    }

    @Override
    public void uploadPaper(android.net.Uri fileUri, String originalFileName, android.net.Uri memoUri, String memoFileName, PastPaper paperMeta, UploadProgressListener progressListener, DataCallback<PastPaper> callback) {
        executor.execute(() -> {
            String paperId = UUID.randomUUID().toString();
            paperMeta.setId(paperId);
            paperMeta.setFileUrl(fileUri.toString());
            paperMeta.setFileName(originalFileName);
            paperMeta.setCreatedAt(System.currentTimeMillis());
            paperMeta.setSynced(true);
            paperMeta.setApproved(true); // Automatically approve in local mode

            if (memoUri != null) {
                paperMeta.setMemoUrl(memoUri.toString());
                paperMeta.setMemoName(memoFileName);
            }

            localDataSource.savePaper(paperMeta);
            String currentUid = sessionManager.getUid();
            notificationRepository.addNotification(
                    "Upload Complete",
                    "Paper '" + paperMeta.getTitle() + "' uploaded successfully.",
                    "upload",
                    currentUid,
                    paperId
            );
            mainHandler.post(() -> {
                com.bravem.app.utils.NotificationHelper.showNotification(context, "Upload Complete", "Paper '" + paperMeta.getTitle() + "' uploaded successfully.");
                callback.onSuccess(paperMeta);
            });
        });
    }

    @Override
    public void uploadMemorandumOnly(String paperId, android.net.Uri memoUri, String memoFileName, UploadProgressListener progressListener, DataCallback<PastPaper> callback) {
        executor.execute(() -> {
            PastPaper paper = localDataSource.getById(paperId);
            if (paper != null) {
                paper.setMemoUrl(memoUri.toString());
                paper.setMemoName(memoFileName);
                localDataSource.updatePaper(paper);
                mainHandler.post(() -> callback.onSuccess(paper));
            } else {
                mainHandler.post(() -> callback.onError(new Exception("Paper not found")));
            }
        });
    }
}
