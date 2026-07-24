package com.bravem.app.data.sync;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.CourseDao;
import com.bravem.app.data.local.DegreeDao;
import com.bravem.app.data.local.PaperDao;
import com.bravem.app.data.local.UserDao;
import com.bravem.app.model.Course;
import com.bravem.app.model.Degree;
import com.bravem.app.model.PastPaper;
import com.bravem.app.model.User;
import com.bravem.app.utils.NotificationHelper;
import com.bravem.app.utils.SessionManager;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(context);
        UserDao userDao = db.userDao();
        PaperDao paperDao = db.paperDao();
        DegreeDao degreeDao = db.degreeDao();
        CourseDao courseDao = db.courseDao();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        try {
            SessionManager session = new SessionManager(context);
            String currentUid = session.getUid();

            // 0. Update current user's version locally and sync it
            if (currentUid != null) {
                User currentUser = userDao.getByUid(currentUid);
                if (currentUser != null) {
                    boolean changed = false;
                    if (!"1.2.3".equals(currentUser.getAppVersion())) {
                        currentUser.setAppVersion("1.2.3");
                        changed = true;
                    }
                    if (changed) {
                        currentUser.setSynced(false);
                        userDao.update(currentUser);
                    }
                    
                    // Explicitly sync current user to avoid "Permission denied" on others
                    database.child("users").child(currentUid).setValue(currentUser);
                    currentUser.setSynced(true);
                    userDao.update(currentUser);
                }
            }

            // 1. Sync other local data if needed (Papers, Degrees, Courses)
            // Note: We use individual paths or ensure the user has permission for the root if using updateChildren
            
            // Sync Papers uploaded by THIS user
            if (currentUid != null) {
                List<PastPaper> unsyncedPapers = paperDao.getUnsynced();
                for (PastPaper paper : unsyncedPapers) {
                    if (currentUid.equals(paper.getUploadedByUid())) {
                        Tasks.await(database.child("papers").child(paper.getId()).setValue(paper));
                        paper.setSynced(true);
                        paperDao.update(paper);
                    }
                }
            }

            // 2. Update Global Metadata (Only if user is logged in)
            if (currentUid != null) {
                database.child("app_metadata").child("latest_version").setValue("1.2.3");
                database.child("app_metadata").child("min_version").setValue("1.2.0");
            }

            return Result.success();
        } catch (ExecutionException | InterruptedException e) {
            return Result.retry();
        } catch (Exception e) {
            return Result.failure();
        }
    }
}
