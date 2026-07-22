package com.bravem.app.data.sync;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.PaperDao;
import com.bravem.app.data.local.UserDao;
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
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        try {
            // 1. Sync Users to Realtime Database
            List<User> unsyncedUsers = userDao.getUnsynced();
            if (!unsyncedUsers.isEmpty()) {
                Map<String, Object> childUpdates = new HashMap<>();
                for (User user : unsyncedUsers) {
                    childUpdates.put("/users/" + user.getUid(), user);
                }
                Tasks.await(database.updateChildren(childUpdates));
                for (User user : unsyncedUsers) {
                    user.setSynced(true);
                    userDao.update(user);
                }
            }

            // 2. Sync Papers to Realtime Database
            List<PastPaper> unsyncedPapers = paperDao.getUnsynced();
            if (!unsyncedPapers.isEmpty()) {
                Map<String, Object> childUpdates = new HashMap<>();
                for (PastPaper paper : unsyncedPapers) {
                    childUpdates.put("/papers/" + paper.getId(), paper);
                }
                Tasks.await(database.updateChildren(childUpdates));
                for (PastPaper paper : unsyncedPapers) {
                    paper.setSynced(true);
                    paperDao.update(paper);
                }
            }

            return Result.success();
        } catch (ExecutionException | InterruptedException e) {
            return Result.retry();
        } catch (Exception e) {
            return Result.failure();
        }
    }
}
