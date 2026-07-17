package com.bravem.app.data;

import android.content.Context;
import android.net.Uri;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.PaperDao;
import com.bravem.app.model.PastPaper;
import com.bravem.app.model.User;
import com.bravem.app.utils.FileUtils;
import com.bravem.app.utils.NotificationHelper;
import com.bravem.app.utils.SessionManager;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles past-paper local file storage and metadata read/write in Room.
 */
public class PaperRepository {

    private final Context context;
    private final PaperDao paperDao;
    private final com.bravem.app.data.NotificationRepository notificationRepository;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    public interface UploadProgressListener {
        void onProgress(int percent);
    }

    public PaperRepository(Context context) {
        this.context = context.getApplicationContext();
        this.paperDao = AppDatabase.getInstance(context).paperDao();
        this.notificationRepository = new com.bravem.app.data.NotificationRepository(context);
    }

    public void uploadPaper(Uri fileUri, String originalFileName, PastPaper paperMeta,
                             UploadProgressListener progressListener,
                             DataCallback<PastPaper> callback) {

        executor.execute(() -> {
            String localPath = FileUtils.copyFileToInternalStorage(context, fileUri, originalFileName);
            if (localPath != null) {
                if (progressListener != null) mainHandler.post(() -> progressListener.onProgress(100));

                paperMeta.setId(UUID.randomUUID().toString());
                paperMeta.setFileUrl(localPath);
                paperMeta.setFileName(originalFileName);
                paperMeta.setCreatedAt(System.currentTimeMillis());
                paperMeta.setSynced(false); // Mark for sync

                paperDao.insert(paperMeta);
                
                // Notification for the uploader
                String currentUid = new SessionManager(context).getUid();
                notificationRepository.addNotification(
                        "Upload Complete",
                        "Paper '" + paperMeta.getTitle() + "' uploaded successfully.",
                        "upload",
                        currentUid,
                        paperMeta.getId()
                );

                // Notification for other students in the SAME degree and intake
                executor.execute(() -> {
                    List<User> users = AppDatabase.getInstance(context).userDao().getAll();
                    for (User user : users) {
                        if (user.getUid().equals(currentUid)) continue;
                        
                        if (paperMeta.getDegreeId() != null && paperMeta.getDegreeId().equals(user.getDegreeId()) &&
                            paperMeta.getIntake() != null && paperMeta.getIntake().equals(user.getIntake())) {
                            
                            // Note: addNotification in its current form adds to the LOCAL DB of the current user.
                            // In a real app with a backend, this would be a server-side trigger or push notification.
                            // Since this project is purely local (using Room to replace Firebase), 
                            // we simulate the "others receive" by adding it here, 
                            // although in a real local-only app, "other users" don't share the same database instance on different devices.
                            // Assuming for this task that 'others' refers to other user entries in the same local DB.
                            
                            notificationRepository.addNotification(
                                "New Paper Available",
                                "A new paper '" + paperMeta.getTitle() + "' has been uploaded for your degree.",
                                "new_paper",
                                user.getUid(),
                                paperMeta.getId()
                            );
                        }
                    }
                });

                mainHandler.post(() -> {
                    NotificationHelper.showNotification(context, "Upload Complete", "Paper '" + paperMeta.getTitle() + "' uploaded successfully.");
                    callback.onSuccess(paperMeta);
                });
            } else {
                String currentUid = new SessionManager(context).getUid();
                notificationRepository.addNotification(
                        "Upload Failed",
                        "Could not upload '" + paperMeta.getTitle() + "'.",
                        "upload",
                        currentUid,
                        null
                );
                mainHandler.post(() -> {
                    NotificationHelper.showNotification(context, "Upload Failed", "Could not upload '" + paperMeta.getTitle() + "'.");
                    callback.onError(new Exception("Failed to copy file to local storage"));
                });
            }
        });
    }

    public void fetchPapersForCourse(String courseId, DataCallback<List<PastPaper>> callback) {
        executor.execute(() -> {
            List<PastPaper> papers = paperDao.getByCourseId(courseId);
            mainHandler.post(() -> callback.onSuccess(papers));
        });
    }

    public void fetchPapersForDegree(String degreeId, int limit, DataCallback<List<PastPaper>> callback) {
        String currentUid = new SessionManager(context).getUid();
        executor.execute(() -> {
            List<PastPaper> papers = paperDao.getByDegreeId(degreeId, currentUid);
            if (papers.size() > limit) {
                papers = papers.subList(0, limit);
            }
            List<PastPaper> finalPapers = papers;
            mainHandler.post(() -> callback.onSuccess(finalPapers));
        });
    }

    public void fetchRecommendedPapers(String degreeId, String intake, int limit, DataCallback<List<PastPaper>> callback) {
        String currentUid = new SessionManager(context).getUid();
        executor.execute(() -> {
            if (intake == null || !intake.contains(".")) {
                List<PastPaper> papers = paperDao.getByDegreeId(degreeId, currentUid);
                if (papers.size() > limit) papers = papers.subList(0, limit);
                List<PastPaper> finalPapers = papers;
                mainHandler.post(() -> callback.onSuccess(finalPapers));
                return;
            }

            try {
                String[] parts = intake.split("\\.");
                int year = Integer.parseInt(parts[0]);
                String semNum = parts[1];
                String semesterQuery = "Semester " + semNum;

                List<PastPaper> papers = paperDao.getRecommended(degreeId, year, semesterQuery, currentUid);
                
                // If no exact matches for the intake, fall back to degree-wide papers
                if (papers.isEmpty()) {
                    papers = paperDao.getByDegreeId(degreeId, currentUid);
                    if (papers.size() > limit) papers = papers.subList(0, limit);
                    List<PastPaper> finalPapers = papers;
                    mainHandler.post(() -> callback.onSuccess(finalPapers));
                    return;
                }

                if (papers.size() > limit) {
                    papers = papers.subList(0, limit);
                }
                List<PastPaper> finalPapers = papers;
                mainHandler.post(() -> callback.onSuccess(finalPapers));
            } catch (Exception e) {
                List<PastPaper> papers = paperDao.getByDegreeId(degreeId, currentUid);
                if (papers.size() > limit) papers = papers.subList(0, limit);
                List<PastPaper> finalPapers = papers;
                mainHandler.post(() -> callback.onSuccess(finalPapers));
            }
        });
    }

    public void fetchRecentPapers(int limit, DataCallback<List<PastPaper>> callback) {
        executor.execute(() -> {
            List<PastPaper> papers = paperDao.getAll();
            if (papers.size() > limit) {
                papers = papers.subList(0, limit);
            }
            List<PastPaper> finalPapers = papers;
            mainHandler.post(() -> callback.onSuccess(finalPapers));
        });
    }

    public void fetchPapersUploadedBy(String uid, DataCallback<List<PastPaper>> callback) {
        executor.execute(() -> {
            List<PastPaper> papers = paperDao.getByUploader(uid);
            mainHandler.post(() -> callback.onSuccess(papers));
        });
    }

    public void fetchAllPapersForAdmin(DataCallback<List<PastPaper>> callback) {
        executor.execute(() -> {
            List<PastPaper> papers = paperDao.getAll();
            mainHandler.post(() -> callback.onSuccess(papers));
        });
    }

    public void searchPapersByTitlePrefix(String queryText, DataCallback<List<PastPaper>> callback) {
        executor.execute(() -> {
            List<PastPaper> papers = paperDao.search(queryText);
            mainHandler.post(() -> callback.onSuccess(papers));
        });
    }

    public void deletePaper(String paperId, DataCallback<Void> callback) {
        executor.execute(() -> {
            PastPaper paper = paperDao.getById(paperId);
            if (paper != null) {
                paperDao.delete(paper);
                mainHandler.post(() -> callback.onSuccess(null));
            } else {
                mainHandler.post(() -> callback.onError(new Exception("Paper not found")));
            }
        });
    }

    public void fetchPinnedPapers(DataCallback<List<PastPaper>> callback) {
        executor.execute(() -> {
            List<PastPaper> papers = paperDao.getPinned();
            mainHandler.post(() -> callback.onSuccess(papers));
        });
    }

    public void togglePin(String paperId, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            PastPaper paper = paperDao.getById(paperId);
            if (paper != null) {
                boolean newState = !paper.isPinned();
                if (newState && paperDao.getPinnedCount() >= 10) {
                    mainHandler.post(() -> callback.onError(new Exception("You can only pin up to 10 papers")));
                    return;
                }
                paper.setPinned(newState);
                paperDao.update(paper);
                mainHandler.post(() -> callback.onSuccess(newState));
            } else {
                mainHandler.post(() -> callback.onError(new Exception("Paper not found")));
            }
        });
    }

    public void setPaperApproved(String paperId, boolean approved, DataCallback<Void> callback) {
        executor.execute(() -> {
            PastPaper paper = paperDao.getById(paperId);
            if (paper != null) {
                paper.setApproved(approved);
                paperDao.update(paper);
                mainHandler.post(() -> callback.onSuccess(null));
            } else {
                mainHandler.post(() -> callback.onError(new Exception("Paper not found")));
            }
        });
    }
}
