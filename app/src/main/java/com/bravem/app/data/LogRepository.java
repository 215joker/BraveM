package com.bravem.app.data;

import android.content.Context;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.SearchLogDao;
import com.bravem.app.model.SearchLog;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogRepository {

    private final SearchLogDao searchLogDao;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    public LogRepository(Context context) {
        this.searchLogDao = AppDatabase.getInstance(context).searchLogDao();
    }

    public void logSearch(String query, String userEmail) {
        executor.execute(() -> {
            SearchLog log = new SearchLog(query, userEmail, System.currentTimeMillis());
            searchLogDao.insert(log);
        });
    }

    public void fetchAllLogs(DataCallback<List<SearchLog>> callback) {
        executor.execute(() -> {
            List<SearchLog> logs = searchLogDao.getAll();
            mainHandler.post(() -> callback.onSuccess(logs));
        });
    }

    public void clearLogs(DataCallback<Void> callback) {
        executor.execute(() -> {
            searchLogDao.deleteAll();
            mainHandler.post(() -> callback.onSuccess(null));
        });
    }
}
