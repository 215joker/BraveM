package com.bravem.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private final List<File> files = new ArrayList<>();
    private final Set<File> selectedFiles = new HashSet<>();
    private final OnSelectionChangeListener selectionListener;

    public interface OnSelectionChangeListener {
        void onSelectionChanged(int count);
    }

    public FileAdapter(OnSelectionChangeListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    public void submitList(List<File> newFiles) {
        files.clear();
        files.addAll(newFiles);
        selectedFiles.clear();
        notifyDataSetChanged();
    }

    public Set<File> getSelectedFiles() {
        return selectedFiles;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_temp_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        holder.bind(files.get(position));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    class FileViewHolder extends RecyclerView.ViewHolder {
        private final TextView fileName;
        private final TextView fileSize;
        private final CheckBox checkBox;

        FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.text_file_name);
            fileSize = itemView.findViewById(R.id.text_file_size);
            checkBox = itemView.findViewById(R.id.checkbox_select);
        }

        void bind(File file) {
            fileName.setText(file.getName());
            fileSize.setText(FileUtils.humanReadableSize(file.length()));
            
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(selectedFiles.contains(file));
            
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) selectedFiles.add(file);
                else selectedFiles.remove(file);
                selectionListener.onSelectionChanged(selectedFiles.size());
            });

            itemView.setOnClickListener(v -> checkBox.toggle());
        }
    }
}
