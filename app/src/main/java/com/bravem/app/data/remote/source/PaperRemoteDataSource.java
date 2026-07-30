package com.bravem.app.data.remote.source;

import com.bravem.app.data.DataCallback;
import com.bravem.app.model.PastPaper;

import java.util.List;

public interface PaperRemoteDataSource {
    void fetchAllPapers(DataCallback<List<PastPaper>> callback);
    void updatePaper(PastPaper paper, DataCallback<Void> callback);
    void deletePaper(String paperId, DataCallback<Void> callback);
}
