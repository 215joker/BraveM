package com.bravem.app.data.remote.source;

import com.bravem.app.data.DataCallback;
import com.bravem.app.model.PastPaper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebasePaperRemoteDataSource implements PaperRemoteDataSource {

    private final DatabaseReference papersRef;

    public FirebasePaperRemoteDataSource() {
        this.papersRef = FirebaseDatabase.getInstance().getReference("papers");
    }

    @Override
    public void fetchAllPapers(DataCallback<List<PastPaper>> callback) {
        papersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<PastPaper> papers = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    PastPaper paper = postSnapshot.getValue(PastPaper.class);
                    if (paper != null) papers.add(paper);
                }
                callback.onSuccess(papers);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    @Override
    public void updatePaper(PastPaper paper, DataCallback<Void> callback) {
        papersRef.child(paper.getId()).setValue(paper).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(null);
            } else {
                callback.onError(task.getException());
            }
        });
    }

    @Override
    public void deletePaper(String paperId, DataCallback<Void> callback) {
        papersRef.child(paperId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(null);
            } else {
                callback.onError(task.getException());
            }
        });
    }
}
