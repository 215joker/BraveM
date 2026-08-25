package com.bravem.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bravem.app.R;
import com.bravem.app.databinding.ActivityLoginBinding;
import com.bravem.app.domain.model.User;
import com.bravem.app.ui.admin.AdminDashboardActivity;
import com.bravem.app.ui.dashboard.DashboardActivity;
import com.bravem.app.ui.supervisor.SupervisorDashboardActivity;
import com.bravem.app.utils.Resource;
import com.bravem.app.utils.SessionManager;
import com.bravem.app.utils.UiUtils;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UiUtils.handleTopInset(binding.getRoot());
        UiUtils.handleBottomInset(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        sessionManager = new SessionManager(this);

        setupUI();
        observeViewModel();
    }

    private void setupUI() {
        // Pre-populate university
        binding.inputUniversity.setText(sessionManager.getUniversity());
        binding.inputUniversity.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectUniversityActivity.class);
            intent.putExtra("from_login", true);
            startActivity(intent);
        });

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.linkRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        binding.btnForgotPassword.setOnClickListener(v -> handleForgotPassword());

        binding.btnGoogle.setOnClickListener(v -> handleSocialLogin("Google"));
        binding.btnFacebook.setOnClickListener(v -> handleSocialLogin("Facebook"));
        binding.btnInstagram.setOnClickListener(v -> handleSocialLogin("Instagram"));
        binding.btnGithub.setOnClickListener(v -> handleSocialLogin("GitHub"));
    }

    private void observeViewModel() {
        viewModel.getLoginState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    routeUser(resource.data);
                    break;
                case ERROR:
                    setLoading(false);
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });

        viewModel.getResetPasswordState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    Toast.makeText(this, R.string.password_reset_sent, Toast.LENGTH_LONG).show();
                    break;
                case ERROR:
                    setLoading(false);
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void handleForgotPassword() {
        String email = binding.inputEmail.getText() != null ? binding.inputEmail.getText().toString().trim() : "";
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.setError(getString(R.string.error_invalid_email));
            return;
        }
        viewModel.forgotPassword(email);
    }

    private void handleSocialLogin(String provider) {
        Toast.makeText(this, provider + " login coming soon. Configure API keys first.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.inputUniversity.setText(sessionManager.getUniversity());
    }

    private void attemptLogin() {
        String email = binding.inputEmail.getText() != null ? binding.inputEmail.getText().toString().trim() : "";
        String password = binding.inputPassword.getText() != null ? binding.inputPassword.getText().toString().trim() : "";

        binding.layoutEmail.setError(null);
        binding.layoutPassword.setError(null);

        boolean valid = true;
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.setError(getString(R.string.error_invalid_email));
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            binding.layoutPassword.setError(getString(R.string.error_required_field));
            valid = false;
        }
        
        if (valid) {
            viewModel.login(email, password);
        }
    }

    private void routeUser(User user) {
        Intent intent;
        if (user.isSupervisor()) {
            intent = new Intent(LoginActivity.this, SupervisorDashboardActivity.class);
        } else if (user.isAdmin()) {
            intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
        } else if (user.getDegreeId() == null || user.getDegreeId().isEmpty()) {
            intent = new Intent(LoginActivity.this, SelectDegreeActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, DashboardActivity.class);
        }
        
        // Final sanity check for administrative intents
        if (intent.getComponent() != null) {
            String className = intent.getComponent().getClassName();
            if (className.contains("SupervisorDashboardActivity") && !user.isSupervisor()) {
                Toast.makeText(this, "Unauthorised: Supervisor access required", Toast.LENGTH_LONG).show();
                return;
            }
            if (className.contains("AdminDashboardActivity") && !user.isAdmin()) {
                Toast.makeText(this, "Unauthorised: Not an admin account", Toast.LENGTH_LONG).show();
                return;
            }
        }

        startActivity(intent);
        finishAffinity();
    }

    private void setLoading(boolean loading) {
        binding.progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!loading);
        binding.btnLogin.setText(loading ? "" : getString(R.string.login));
    }
}
