package com.bravem.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.data.DataCallback;
import com.bravem.app.data.DegreeRepository;
import com.bravem.app.model.Degree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UniversityAdapter extends RecyclerView.Adapter<UniversityAdapter.UniViewHolder> {

    private List<String> universities = new ArrayList<>();
    private List<String> allUniversities = new ArrayList<>();
    private final DegreeRepository degreeRepository;
    private final Map<String, List<Degree>> cachedDegrees = new HashMap<>();
    private final Map<String, Boolean> expandedState = new HashMap<>();

    public UniversityAdapter(DegreeRepository degreeRepository) {
        this.degreeRepository = degreeRepository;
    }

    public void submitList(List<String> list) {
        this.allUniversities = new ArrayList<>(list);
        this.universities = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        universities.clear();
        if (query.isEmpty()) {
            universities.addAll(allUniversities);
        } else {
            for (String u : allUniversities) {
                if (u.toLowerCase().contains(query.toLowerCase())) {
                    universities.add(u);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UniViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_university_expandable, parent, false);
        return new UniViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UniViewHolder holder, int position) {
        holder.bind(universities.get(position));
    }

    @Override
    public int getItemCount() {
        return universities.size();
    }

    class UniViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView;
        private final ImageView arrow;
        private final LinearLayout degreesLayout;
        private final View header;

        public UniViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.text_uni_name);
            arrow = itemView.findViewById(R.id.image_arrow);
            degreesLayout = itemView.findViewById(R.id.layout_degrees);
            header = itemView.findViewById(R.id.layout_header);
        }

        public void bind(String university) {
            nameView.setText(university);
            
            boolean isExpanded = expandedState.containsKey(university) && Boolean.TRUE.equals(expandedState.get(university));
            degreesLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            arrow.setRotation(isExpanded ? 90 : 0);

            header.setOnClickListener(v -> {
                boolean newState = !isExpanded;
                expandedState.put(university, newState);
                
                int pos = getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                if (newState && !cachedDegrees.containsKey(university)) {
                    degreeRepository.fetchDegreesByUniversity(university, new DataCallback<List<Degree>>() {
                        @Override
                        public void onSuccess(List<Degree> result) {
                            cachedDegrees.put(university, result);
                            notifyItemChanged(pos);
                        }

                        @Override
                        public void onError(Exception e) {}
                    });
                } else {
                    notifyItemChanged(pos);
                }
            });

            if (isExpanded && cachedDegrees.containsKey(university)) {
                populateDegrees(cachedDegrees.get(university));
            }
        }

        private void populateDegrees(List<Degree> degrees) {
            degreesLayout.removeAllViews();
            if (degrees == null || degrees.isEmpty()) {
                TextView tv = new TextView(itemView.getContext());
                tv.setText("No degrees found");
                tv.setPadding(0, 8, 0, 8);
                tv.setTextColor(itemView.getContext().getColor(R.color.text_secondary));
                degreesLayout.addView(tv);
                return;
            }

            for (Degree degree : degrees) {
                TextView tv = new TextView(itemView.getContext());
                tv.setText("• " + degree.getName());
                tv.setPadding(0, 8, 0, 8);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                tv.setTextColor(itemView.getContext().getColor(R.color.text_primary));
                degreesLayout.addView(tv);
            }
        }
    }
}
