package com.bravem.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.domain.model.Degree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Displays a selectable list of Degree programmes.
 * Supports grouping by School (for student selection) or plain list (for Admin).
 */
public class DegreeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public interface OnDegreeClickListener {
        void onDegreeClick(Degree degree);
    }

    public interface OnDegreeActionListener {
        void onEdit(Degree degree);
        void onDelete(Degree degree);
    }

    private final List<Object> displayList = new ArrayList<>();
    private final List<Degree> allDegrees = new ArrayList<>();
    private final Map<String, List<Degree>> groupedDegrees = new TreeMap<>();
    private final Map<String, Boolean> expandedSchools = new HashMap<>();
    
    private final OnDegreeClickListener clickListener;
    private OnDegreeActionListener actionListener;
    private String selectedDegreeId;
    private boolean isGroupedMode = true;

    public DegreeAdapter(OnDegreeClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setActionListener(OnDegreeActionListener actionListener) {
        this.actionListener = actionListener;
        this.isGroupedMode = false; // Admin mode usually doesn't need grouping
    }

    public void setGroupedMode(boolean groupedMode) {
        this.isGroupedMode = groupedMode;
    }

    public void submitList(List<Degree> newDegrees) {
        submitList(newDegrees, false);
    }

    public void submitList(List<Degree> newDegrees, boolean expandAll) {
        this.allDegrees.clear();
        this.allDegrees.addAll(newDegrees);
        if (expandAll) {
            for (Degree d : newDegrees) {
                if (d.getDescription() != null) expandedSchools.put(d.getDescription(), true);
            }
        }
        filter("");
    }

    public void filter(String query) {
        List<Degree> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Degree d : allDegrees) {
            if (d.getName().toLowerCase().contains(lowerQuery) ||
                    (d.getUniversity() != null && d.getUniversity().toLowerCase().contains(lowerQuery)) ||
                    (d.getDescription() != null && d.getDescription().toLowerCase().contains(lowerQuery))) {
                filtered.add(d);
            }
        }

        if (isGroupedMode) {
            groupedDegrees.clear();
            for (Degree degree : filtered) {
                String school = degree.getDescription() != null ? degree.getDescription() : "Other Programs";
                if (!groupedDegrees.containsKey(school)) {
                    groupedDegrees.put(school, new ArrayList<>());
                }
                List<Degree> list = groupedDegrees.get(school);
                if (list != null) list.add(degree);
            }
            updateDisplayList();
        } else {
            displayList.clear();
            displayList.addAll(filtered);
            notifyDataSetChanged();
        }
    }

    private void updateDisplayList() {
        displayList.clear();
        for (String school : groupedDegrees.keySet()) {
            displayList.add(school);
            if (Boolean.TRUE.equals(expandedSchools.get(school))) {
                List<Degree> degrees = groupedDegrees.get(school);
                if (degrees != null) displayList.addAll(degrees);
            }
        }
        notifyDataSetChanged();
    }

    public void setSelectedDegreeId(String degreeId) {
        this.selectedDegreeId = degreeId;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return (isGroupedMode && displayList.get(position) instanceof String) ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_school_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_degree, parent, false);
            return new DegreeViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = displayList.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((String) item);
        } else {
            ((DegreeViewHolder) holder).bind((Degree) item);
        }
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView schoolName;
        private final ImageView arrow;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            schoolName = itemView.findViewById(R.id.text_school_name);
            arrow = itemView.findViewById(R.id.image_arrow);
        }

        void bind(String school) {
            schoolName.setText(school);
            boolean isExpanded = Boolean.TRUE.equals(expandedSchools.get(school));
            arrow.setRotation(isExpanded ? 180 : 0);

            itemView.setOnClickListener(v -> {
                expandedSchools.put(school, !isExpanded);
                updateDisplayList();
            });
        }
    }

    class DegreeViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView;
        private final TextView descriptionView;
        private final View adminActions;
        private final View editButton;
        private final View deleteButton;

        DegreeViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.text_degree_name);
            descriptionView = itemView.findViewById(R.id.text_degree_description);
            adminActions = itemView.findViewById(R.id.layout_admin_actions);
            editButton = itemView.findViewById(R.id.btn_edit);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }

        void bind(Degree degree) {
            nameView.setText(degree.getName());
            
            if (!isGroupedMode && degree.getDescription() != null && !degree.getDescription().isEmpty()) {
                descriptionView.setVisibility(View.VISIBLE);
                descriptionView.setText(degree.getDescription());
            } else {
                descriptionView.setVisibility(View.GONE);
            }

            boolean isSelected = degree.getId() != null && degree.getId().equals(selectedDegreeId);
            itemView.setSelected(isSelected);
            itemView.setBackgroundResource(isSelected
                    ? R.drawable.bg_card_selected
                    : R.drawable.bg_card_default);

            itemView.setOnClickListener(v -> clickListener.onDegreeClick(degree));

            if (actionListener != null) {
                adminActions.setVisibility(View.VISIBLE);
                editButton.setOnClickListener(v -> actionListener.onEdit(degree));
                deleteButton.setOnClickListener(v -> actionListener.onDelete(degree));
            } else {
                adminActions.setVisibility(View.GONE);
            }
        }
    }
}
