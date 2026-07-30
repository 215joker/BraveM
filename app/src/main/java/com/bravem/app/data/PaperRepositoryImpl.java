package com.bravem.app.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.source.PaperLocalDataSource;
import com.bravem.app.data.remote.source.FirebasePaperRemoteDataSource;
import com.bravem.app.data.remote.source.PaperRemoteDataSource;
import com.bravem.app.domain.repository.PaperRepository;
import com.bravem.app.model.PastPaper;
import com.bravem.app.utils.SessionManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaperRepositoryImpl implements PaperRepository {

    private final PaperRemoteDataSource remoteDataSource;
    private final PaperLocalDataSource localDataSource;
    private final SessionManager sessionManager;
    private final NotificationRepository notificationRepository;
    private final DatabaseReference papersRef;
    private final StorageReference storageRef;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Context context;

    public PaperRepositoryImpl(Context context) {
        this.context = context.getApplicationContext();
        this.remoteDataSource = new FirebasePaperRemoteDataSource();
        this.localDataSource = new PaperLocalDataSource(AppDatabase.getInstance(context).paperDao());
        this.sessionManager = new SessionManager(context);
        this.notificationRepository = new NotificationRepository(context);
        this.papersRef = FirebaseDatabase.getInstance().getReference("papers");
        this.storageRef = FirebaseStorage.getInstance().getReference().child("papers");
    }

    @Override
    public void fetchRecommendedPapers(String degreeId, String intake, int limit, DataCallback<List<PastPaper>> callback) {
        String uid = sessionManager.getUid();
        
        executor.execute(() -> {
            List<PastPaper> localPapers = localDataSource.getRecommended(degreeId, intake, uid);
            if (!localPapers.isEmpty()) {
                mainHandler.post(() -> callback.onSuccess(localPapers));
            }

            remoteDataSource.fetchAllPapers(new DataCallback<>() {
                @Override
                public void onSuccess(List<PastPaper> papers) {
                    executor.execute(() -> {
                        localDataSource.savePapers(papers);
                        List<PastPaper> freshResults = localDataSource.getRecommended(degreeId, intake, uid);
                        mainHandler.post(() -> callback.onSuccess(freshResults));
                    });
                }
                @Override
                public void onError(Exception e) {
                    if (localPapers.isEmpty()) {
                        mainHandler.post(() -> callback.onError(e));
                    }
                }
            });
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
                remoteDataSource.updatePaper(paper, null);
                mainHandler.post(() -> callback.onSuccess(newState));
            } else {
                mainHandler.post(() -> callback.onError(new Exception("Paper not found")));
            }
        });
    }

    @Override
    public void fetchPapersForDegree(String degreeId, int limit, DataCallback<List<PastPaper>> callback) {
        executor.execute(() -> {
            List<PastPaper> local = localDataSource.getByDegreeId(degreeId, sessionManager.getUid());
            if (!local.isEmpty()) {
                mainHandler.post(() -> callback.onSuccess(local));
            }
            remoteDataSource.fetchAllPapers(new DataCallback<>() {
                @Override
                public void onSuccess(List<PastPaper> results) {
                    executor.execute(() -> {
                        localDataSource.savePapers(results);
                        List<PastPaper> updated = localDataSource.getByDegreeId(degreeId, sessionManager.getUid());
                        mainHandler.post(() -> callback.onSuccess(updated));
                    });
                }
                @Override
                public void onError(Exception e) {
                    if (local.isEmpty()) mainHandler.post(() -> callback.onError(e));
                }
            });
        });
    }

    @Override
    public void fetchPapersByCourse(String courseId, DataCallback<List<PastPaper>> callback) {
        executor.execute(() -> {
            List<PastPaper> local = localDataSource.getByCourseId(courseId);
            if (!local.isEmpty()) {
                mainHandler.post(() -> callback.onSuccess(local));
            }
            remoteDataSource.fetchAllPapers(new DataCallback<>() {
                @Override
                public void onSuccess(List<PastPaper> results) {
                    executor.execute(() -> {
                        localDataSource.savePapers(results);
                        List<PastPaper> updated = localDataSource.getByCourseId(courseId);
                        mainHandler.post(() -> callback.onSuccess(updated));
                    });
                }
                @Override
                public void onError(Exception e) {
                    if (local.isEmpty()) mainHandler.post(() -> callback.onError(e));
                }
            });
        });
    }

    @Override
    public void searchPapers(String query, DataCallback<List<PastPaper>> callback) {
        executor.execute(() -> {
            List<PastPaper> local = localDataSource.search(query);
            mainHandler.post(() -> callback.onSuccess(local));
        });
    }

    @Override
    public void fetchPapersUploadedBy(String uid, DataCallback<List<PastPaper>> callback) {
        executor.execute(() -> {
            List<PastPaper> papers = localDataSource.getUploadedBy(uid);
            mainHandler.post(() -> callback.onSuccess(papers));
            
            remoteDataSource.fetchAllPapers(new DataCallback<>() {
                @Override
                public void onSuccess(List<PastPaper> results) {
                    executor.execute(() -> localDataSource.savePapers(results));
                }
                @Override
                public void onError(Exception e) {}
            });
        });
    }

    @Override
    public void fetchAllPapersForAdmin(DataCallback<List<PastPaper>> callback) {
        remoteDataSource.fetchAllPapers(new DataCallback<>() {
            @Override
            public void onSuccess(List<PastPaper> result) {
                executor.execute(() -> {
                    localDataSource.savePapers(result);
                    mainHandler.post(() -> callback.onSuccess(result));
                });
            }

            @Override
            public void onError(Exception e) {
                executor.execute(() -> {
                    List<PastPaper> localPapers = localDataSource.getAll();
                    mainHandler.post(() -> callback.onSuccess(localPapers));
                });
            }
        });
    }

    @Override
    public void deletePaper(String paperId, DataCallback<Void> callback) {
        executor.execute(() -> {
            PastPaper paper = localDataSource.getById(paperId);
            if (paper != null) {
                remoteDataSource.deletePaper(paperId, new DataCallback<>() {
                    @Override
                    public void onSuccess(Void result) {
                        executor.execute(() -> {
                            localDataSource.deletePaper(paper);
                            if (callback != null) mainHandler.post(() -> callback.onSuccess(null));
                        });
                    }
                    @Override
                    public void onError(Exception e) {
                        if (callback != null) mainHandler.post(() -> callback.onError(e));
                    }
                });
            }
        });
    }

    @Override
    public void setPaperApproved(String paperId, boolean approved, DataCallback<Void> callback) {
        executor.execute(() -> {
            PastPaper paper = localDataSource.getById(paperId);
            if (paper != null) {
                paper.setApproved(approved);
                remoteDataSource.updatePaper(paper, new DataCallback<>() {
                    @Override
                    public void onSuccess(Void result) {
                        executor.execute(() -> {
                            localDataSource.updatePaper(paper);
                            if (callback != null) mainHandler.post(() -> callback.onSuccess(null));
                        });
                    }
                    @Override
                    public void onError(Exception e) {
                        if (callback != null) mainHandler.post(() -> callback.onError(e));
                    }
                });
            }
        });
    }

    @Override
    public void uploadPaper(android.net.Uri fileUri, String originalFileName, android.net.Uri memoUri, String memoFileName, PastPaper paperMeta, UploadProgressListener progressListener, DataCallback<PastPaper> callback) {
        final String paperId = UUID.randomUUID().toString();
        final StorageReference fileRef = storageRef.child(paperId + "_question_" + originalFileName);

        fileRef.putFile(fileUri)
                .addOnProgressListener(snapshot -> {
                    if (progressListener != null) {
                        int progress = (int) (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        // If there's a memo, this is only the first half
                        if (memoUri != null) progress /= 2;
                        progressListener.onProgress(progress);
                    }
                })
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    paperMeta.setId(paperId);
                    paperMeta.setFileUrl(uri.toString());
                    paperMeta.setFileName(originalFileName);
                    paperMeta.setCreatedAt(System.currentTimeMillis());
                    paperMeta.setSynced(true);

                    if (memoUri != null) {
                        // Upload Memo
                        final StorageReference mRef = storageRef.child(paperId + "_memo_" + memoFileName);
                        mRef.putFile(memoUri)
                                .addOnProgressListener(mSnap -> {
                                    if (progressListener != null) {
                                        int p = (int) (100.0 * mSnap.getBytesTransferred() / mSnap.getTotalByteCount());
                                        progressListener.onProgress(50 + (p / 2));
                                    }
                                })
                                .addOnSuccessListener(mTask -> mRef.getDownloadUrl().addOnSuccessListener(mUri -> {
                                    paperMeta.setMemoUrl(mUri.toString());
                                    paperMeta.setMemoName(memoFileName);
                                    savePaperToDatabase(paperMeta, callback);
                                }))
                                .addOnFailureListener(e -> mainHandler.post(() -> callback.onError(e)));
                    } else {
                        savePaperToDatabase(paperMeta, callback);
                    }
                }))
                .addOnFailureListener(e -> mainHandler.post(() -> callback.onError(e)));
    }

    @Override
    public void uploadMemorandumOnly(String paperId, android.net.Uri memoUri, String memoFileName, UploadProgressListener progressListener, DataCallback<PastPaper> callback) {
        final StorageReference mRef = storageRef.child(paperId + "_memo_" + memoFileName);
        
        mRef.putFile(memoUri)
                .addOnProgressListener(snapshot -> {
                    if (progressListener != null) {
                        int progress = (int) (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        progressListener.onProgress(progress);
                    }
                })
                .addOnSuccessListener(taskSnapshot -> mRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    executor.execute(() -> {
                        PastPaper paper = localDataSource.getById(paperId);
                        if (paper != null) {
                            paper.setMemoUrl(uri.toString());
                            paper.setMemoName(memoFileName);
                            savePaperToDatabase(paper, callback);
                        } else {
                            // If not in local DB, fetch from remote then update
                            papersRef.child(paperId).get().addOnSuccessListener(snapshot -> {
                                PastPaper remotePaper = snapshot.getValue(PastPaper.class);
                                if (remotePaper != null) {
                                    remotePaper.setMemoUrl(uri.toString());
                                    remotePaper.setMemoName(memoFileName);
                                    savePaperToDatabase(remotePaper, callback);
                                } else {
                                    mainHandler.post(() -> callback.onError(new Exception("Paper not found")));
                                }
                            });
                        }
                    });
                }))
                .addOnFailureListener(e -> mainHandler.post(() -> callback.onError(e)));
    }

    private void savePaperToDatabase(PastPaper paperMeta, DataCallback<PastPaper> callback) {
        String paperId = paperMeta.getId();
        papersRef.child(paperId).setValue(paperMeta).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                executor.execute(() -> {
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
            } else {
                mainHandler.post(() -> callback.onError(task.getException()));
            }
        });
    }
}
