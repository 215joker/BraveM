package com.bravem.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.utils.SessionManager;
import com.bravem.app.utils.UiUtils;

import java.util.Arrays;
import java.util.List;

public class SelectUniversityActivity extends AppCompatActivity {

    private static final List<String> UNIVERSITIES = Arrays.asList(
            "Eden University",
            "University of Zambia (UNZA)",
            "Copperbelt University (CBU)",
            "Mulungushi University",
            "Kwame Nkrumah University",
            "Chalimbana University",
            "Robert Makasa University",
            "Levy Mwanawasa Medical University",
            "Palabana University",
            "Paul Mambo University (PMU)",
            "Cavendish University Zambia (CUZ)",
            "Texila American University Zambia (TAU-Z)",
            "Lusaka Apex Medical University (LAMU)",
            "Zambian Open University (ZAOU)",
            "ZCAS University",
            "Information and Communications University (ICU)",
            "Rusangu University (RU)",
            "DMI-St. Eugene University (DMISEU)"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_university);

        UiUtils.handleTopInset(findViewById(R.id.layout_header));
        UiUtils.handleBottomInset(findViewById(R.id.recycler_universities));

        RecyclerView recyclerView = findViewById(R.id.recycler_universities);
        // Use 2 columns for the grid
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        
        SessionManager sessionManager = new SessionManager(this);

        recyclerView.setAdapter(new RecyclerView.Adapter<UniViewHolder>() {
            @NonNull
            @Override
            public UniViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_university_grid, parent, false);
                return new UniViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull UniViewHolder holder, int position) {
                String uni = UNIVERSITIES.get(position);
                holder.text.setText(uni);
                holder.itemView.setOnClickListener(v -> {
                    sessionManager.setUniversity(uni);
                    boolean fromLogin = getIntent().getBooleanExtra("from_login", false);
                    boolean fromRegister = getIntent().getBooleanExtra("from_register", false);
                    
                    if (fromLogin || fromRegister) {
                        finish();
                    } else {
                        startActivity(new Intent(SelectUniversityActivity.this, LoginActivity.class));
                        finish();
                    }
                });
            }

            @Override
            public int getItemCount() {
                return UNIVERSITIES.size();
            }
        });
    }

    static class UniViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        UniViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text_university_name);
        }
    }
}
