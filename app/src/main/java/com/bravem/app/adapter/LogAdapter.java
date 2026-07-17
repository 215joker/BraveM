package com.bravem.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.model.SearchLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

    private final List<SearchLog> logs = new ArrayList<>();
    private final List<SearchLog> allLogs = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());

    public void submitList(List<SearchLog> newLogs) {
        allLogs.clear();
        allLogs.addAll(newLogs);
        logs.clear();
        logs.addAll(newLogs);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        logs.clear();
        if (query.isEmpty()) {
            logs.addAll(allLogs);
        } else {
            String lower = query.toLowerCase();
            for (SearchLog log : allLogs) {
                if (log.getQuery().toLowerCase().contains(lower) || 
                    log.getUserEmail().toLowerCase().contains(lower)) {
                    logs.add(log);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        holder.bind(logs.get(position));
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    class LogViewHolder extends RecyclerView.ViewHolder {
        private final TextView queryView;
        private final TextView userView;
        private final TextView timeView;

        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            queryView = itemView.findViewById(R.id.text_query);
            userView = itemView.findViewById(R.id.text_user);
            timeView = itemView.findViewById(R.id.text_time);
        }

        void bind(SearchLog log) {
            queryView.setText("\"" + log.getQuery() + "\"");
            userView.setText(log.getUserEmail());
            timeView.setText(dateFormat.format(new Date(log.getTimestamp())));
        }
    }
}
