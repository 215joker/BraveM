package com.bravem.app.ui.library;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bravem.app.R;
import com.bravem.app.databinding.ActivityLibraryLoginBinding;
import com.bravem.app.utils.SessionManager;
import com.bravem.app.utils.UiUtils;

public class LibraryLoginActivity extends AppCompatActivity {

    private ActivityLibraryLoginBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        binding = ActivityLibraryLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UiUtils.handleTopInset(binding.getRoot());

        sessionManager = new SessionManager(this);
        String university = sessionManager.getUniversity();
        if (university == null) university = "University";

        binding.textLibrarySubtitle.setText(getString(R.string.library_login_subtitle, university));

        binding.btnLogin.setOnClickListener(v -> {
            String studentNum = binding.etStudentNumber.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (studentNum.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            } else {
                // Simulate login success and proceed to LibraryActivity
                startActivity(new Intent(this, LibraryActivity.class));
                finish();
            }
        });

        binding.btnBack.setOnClickListener(v -> finish());
    }
}
