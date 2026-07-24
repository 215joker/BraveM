package com.bravem.app.data;

import android.content.Context;
import android.net.Uri;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.PaperDao;
import com.bravem.app.model.PastPaper;
import com.bravem.app.model.User;
import com.bravem.app.utils.FileUtils;
import com.bravem.app.utils.NotificationHelper;
import com.bravem.app.utils.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaperRepository {

    private final Context context;
    private final PaperDao paperDao;
    private final NotificationRepository notificationRepository;
    private final DatabaseReference papersRef;
    private final StorageReference storageRef;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    public interface UploadProgressListener {
        void onProgress(int percent);
    }

    public PaperRepository(Context context) {
        this.context = context.getApplicationContext();
        this.paperDao = AppDatabase.getInstance(context).paperDao();
        this.notificationRepository = new NotificationRepository(context);
        this.papersRef = FirebaseDatabase.getInstance().getReference("papers");
        this.storageRef = FirebaseStorage.getInstance().getReference("papers");
    }

    public void uploadPaper(final Uri fileUri, final String originalFileName, final PastPaper paperMeta,
                             final UploadProgressListener progressListener,
                             final DataCallback<PastPaper> callback) {

        final String paperId = UUID.randomUUID().toString();
        final StorageReference fileRef = storageRef.child(paperId + "_" + originalFileName);

        fileRef.putFile(fileUri)
                .addOnProgressListener(snapshot -> {
                    if (progressListener != null) {
                        int progress = (int) (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        progressListener.onProgress(progress);
                    }
                })
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    paperMeta.setId(paperId);
                    paperMeta.setFileUrl(uri.toString());
                    paperMeta.setFileName(originalFileName);
                    paperMeta.setCreatedAt(System.currentTimeMillis());
                    paperMeta.setSynced(true);

                    // Save metadata to Realtime Database
                    papersRef.child(paperId).setValue(paperMeta).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            executor.execute(() -> {
                                paperDao.insert(paperMeta);
                                // Notifications...
                                String currentUid = new SessionManager(context).getUid();
                                notificationRepository.addNotification(
                                        "Upload Complete",
                                        "Paper '" + paperMeta.getTitle() + "' uploaded successfully.",
                                        "upload",
                                        currentUid,
                                        paperId
                                );
                                mainHandler.post(() -> {
                                    NotificationHelper.showNotification(context, "Upload Complete", "Paper '" + paperMeta.getTitle() + "' uploaded successfully.");
                                    callback.onSuccess(paperMeta);
                                });
                            });
                        } else {
                            mainHandler.post(() -> callback.onError(task.getException()));
                        }
                    });
                }))
                .addOnFailureListener(e -> mainHandler.post(() -> callback.onError(e)));
    }

    public void syncPapers(final DataCallback<Void> callback) {
        papersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                executor.execute(() -> {
                    for (DataSnapshot paperSnapshot : snapshot.getChildren()) {
                        PastPaper paper = paperSnapshot.getValue(PastPaper.class);
                        if (paper != null) {
                            paperDao.insert(paper);
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

    public void fetchPapersForCourse(final String courseId, final DataCallback<List<PastPaper>> callback) {
        syncPapers(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                executor.execute(() -> {
                    final List<PastPaper> papers = paperDao.getByCourseId(courseId);
                    mainHandler.post(() -> callback.onSuccess(papers));
                });
            }
            @Override
            public void onError(Exception e) {
                // Fallback to local
                executor.execute(() -> {
                    final List<PastPaper> papers = paperDao.getByCourseId(courseId);
                    mainHandler.post(() -> callback.onSuccess(papers));
                });
            }
        });
    }

    public void fetchPapersForDegree(final String degreeId, final int limit, final DataCallback<List<PastPaper>> callback) {
        syncPapers(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                final String currentUid = new SessionManager(context).getUid();
                executor.execute(() -> {
                    final List<PastPaper> papers = paperDao.getByDegreeId(degreeId, currentUid);
                    final List<PastPaper> resultList = (papers.size() > limit) ? papers.subList(0, limit) : papers;
                    mainHandler.post(() -> callback.onSuccess(resultList));
                });
            }
            @Override
            public void onError(Exception e) {
                executor.execute(() -> {
                    final List<PastPaper> papers = paperDao.getByDegreeId(degreeId, new SessionManager(context).getUid());
                    final List<PastPaper> resultList = (papers.size() > limit) ? papers.subList(0, limit) : papers;
                    mainHandler.post(() -> callback.onSuccess(resultList));
                });
            }
        });
    }

    public void fetchRecommendedPapers(final String degreeId, final String intake, final int limit, final DataCallback<List<PastPaper>> callback) {
        syncPapers(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                final String currentUid = new SessionManager(context).getUid();
                executor.execute(() -> {
                    final List<PastPaper> papersList;
                    if (intake != null && intake.contains(".")) {
                        List<PastPaper> temp;
                        try {
                            String[] parts = intake.split("\\.");
                            int year = Integer.parseInt(parts[0]);
                            String semNum = parts[1];
                            String semesterQuery = "Semester " + semNum;
                            temp = paperDao.getRecommended(degreeId, year, semesterQuery, currentUid);
                        } catch (Exception e) {
                            temp = paperDao.getByDegreeId(degreeId, currentUid);
                        }
                        papersList = temp;
                    } else {
                        papersList = paperDao.getByDegreeId(degreeId, currentUid);
                    }
                    final List<PastPaper> resultList = (papersList.size() > limit) ? papersList.subList(0, limit) : papersList;
                    mainHandler.post(() -> callback.onSuccess(resultList));
                });
            }
            @Override
            public void onError(Exception e) {
                fetchPapersForDegree(degreeId, limit, callback);
            }
        });
    }

    public void fetchRecentPapers(final int limit, final DataCallback<List<PastPaper>> callback) {
        syncPapers(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                executor.execute(() -> {
                    final List<PastPaper> papers = paperDao.getAll();
                    final List<PastPaper> resultList = (papers.size() > limit) ? papers.subList(0, limit) : papers;
                    mainHandler.post(() -> callback.onSuccess(resultList));
                });
            }
            @Override
            public void onError(Exception e) {
                executor.execute(() -> {
                    final List<PastPaper> papers = paperDao.getAll();
                    final List<PastPaper> resultList = (papers.size() > limit) ? papers.subList(0, limit) : papers;
                    mainHandler.post(() -> callback.onSuccess(resultList));
                });
            }
        });
    }

    public void fetchPapersUploadedBy(final String uid, final DataCallback<List<PastPaper>> callback) {
        executor.execute(() -> {
            final List<PastPaper> papers = paperDao.getByUploader(uid);
            mainHandler.post(() -> callback.onSuccess(papers));
        });
    }

    public void fetchAllPapersForAdmin(final DataCallback<List<PastPaper>> callback) {
        syncPapers(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                executor.execute(() -> {
                    final List<PastPaper> papers = paperDao.getAll();
                    mainHandler.post(() -> callback.onSuccess(papers));
                });
            }
            @Override
            public void onError(Exception e) {
                executor.execute(() -> {
                    final List<PastPaper> papers = paperDao.getAll();
                    mainHandler.post(() -> callback.onSuccess(papers));
                });
            }
        });
    }

    public void searchPapersByTitlePrefix(final String queryText, final DataCallback<List<PastPaper>> callback) {
        executor.execute(() -> {
            final List<PastPaper> papers = paperDao.search(queryText);
            mainHandler.post(() -> callback.onSuccess(papers));
        });
    }

    public void deletePaper(final String paperId, final DataCallback<Void> callback) {
        executor.execute(() -> {
            final PastPaper paper = paperDao.getById(paperId);
            if (paper != null) {
                papersRef.child(paperId).removeValue().addOnCompleteListener(task -> {
                    executor.execute(() -> {
                        paperDao.delete(paper);
                        mainHandler.post(() -> callback.onSuccess(null));
                    });
                });
            } else {
                mainHandler.post(() -> callback.onError(new Exception("Paper not found")));
            }
        });
    }

    public void fetchPinnedPapers(final DataCallback<List<PastPaper>> callback) {
        executor.execute(() -> {
            final List<PastPaper> papers = paperDao.getPinned();
            mainHandler.post(() -> callback.onSuccess(papers));
        });
    }

    public void togglePin(final String paperId, final DataCallback<Boolean> callback) {
        executor.execute(() -> {
            final PastPaper paper = paperDao.getById(paperId);
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

    public void setPaperApproved(final String paperId, final boolean approved, final DataCallback<Void> callback) {
        executor.execute(() -> {
            final PastPaper paper = paperDao.getById(paperId);
            if (paper != null) {
                paper.setApproved(approved);
                papersRef.child(paperId).child("approved").setValue(approved).addOnCompleteListener(task -> {
                    executor.execute(() -> {
                        paperDao.update(paper);
                        mainHandler.post(() -> callback.onSuccess(null));
                    });
                });
            } else {
                mainHandler.post(() -> callback.onError(new Exception("Paper not found")));
            }
        });
    }
}
