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

import java.util.List;

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
        SessionManager sessionManager = new SessionManager(context);

        try {
            // 1. Sync Users to remote
            List<User> unsyncedUsers = userDao.getUnsynced();
            for (User user : unsyncedUsers) {
                // simulate success:
                user.setSynced(true);
                userDao.update(user);
            }

            // 2. Sync Papers to remote
            List<PastPaper> unsyncedPapers = paperDao.getUnsynced();
            for (PastPaper paper : unsyncedPapers) {
                // simulate success:
                paper.setSynced(true);
                paperDao.update(paper);
            }

            // 3. Simulate fetching new papers from server
            // In a real app, we'd check for papers matching the user's degree/intake
            // and show a notification if there are any NEW ones.
            // String degreeId = sessionManager.getDegreeId();
            // List<PastPaper> newPapers = api.fetchNewPapers(degreeId, sessionManager.getLastChecked());
            // if (!newPapers.isEmpty()) {
            //     NotificationHelper.showNotification(context, "New Papers Available", 
            //         "New papers have been uploaded for your degree.");
            // }

            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}
