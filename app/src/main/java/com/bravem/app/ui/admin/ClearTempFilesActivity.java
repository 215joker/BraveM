package com.bravem.app.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.adapter.FileAdapter;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ClearTempFilesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FileAdapter adapter;
    private View emptyState;
    private MaterialButton btnDeleteSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clear_temp);

        recyclerView = findViewById(R.id.recycler_files);
        emptyState = findViewById(R.id.empty_state);
        btnDeleteSelected = findViewById(R.id.btn_delete_selected);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        adapter = new FileAdapter(count -> {
            btnDeleteSelected.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            btnDeleteSelected.setText("Delete (" + count + ")");
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnDeleteSelected.setOnClickListener(v -> confirmDeleteSelected());

        loadFiles();
    }

    private void loadFiles() {
        File cacheDir = getCacheDir();
        File[] filesArray = cacheDir.listFiles();
        List<File> filesList = new ArrayList<>();
        if (filesArray != null) {
            for (File f : filesArray) {
                if (f.isFile()) filesList.add(f);
            }
        }
        
        adapter.submitList(filesList);
        emptyState.setVisibility(filesList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void confirmDeleteSelected() {
        Set<File> selected = adapter.getSelectedFiles();
        com.bravem.app.utils.DialogUtils.showConfirmation(this,
                "Delete " + selected.size() + " files?",
                "This action cannot be undone.",
                getString(R.string.delete),
                () -> {
                    for (File f : selected) {
                        f.delete();
                    }
                    Toast.makeText(this, "Files deleted", Toast.LENGTH_SHORT).show();
                    loadFiles();
                });
    }
}
