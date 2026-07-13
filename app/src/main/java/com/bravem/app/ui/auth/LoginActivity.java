package com.bravem.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bravem.app.R;
import com.bravem.app.data.AuthRepository;
import com.bravem.app.data.DataCallback;
import com.bravem.app.model.User;
import com.bravem.app.ui.admin.AdminDashboardActivity;
import com.bravem.app.ui.dashboard.DashboardActivity;
import com.bravem.app.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailInput, passwordInput, universityInput;
    private MaterialButton loginButton;
    private View registerLink;
    private CircularProgressIndicator progressIndicator;

    private AuthRepository authRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authRepository = new AuthRepository(this);
        sessionManager = new SessionManager(this);

        emailLayout = findViewById(R.id.layout_email);
        passwordLayout = findViewById(R.id.layout_password);
        emailInput = findViewById(R.id.input_email);
        passwordInput = findViewById(R.id.input_password);
        universityInput = findViewById(R.id.input_university);
        loginButton = findViewById(R.id.btn_login);
        registerLink = findViewById(R.id.link_register);
        progressIndicator = findViewById(R.id.progress_indicator);

        // Pre-populate university
        if (universityInput != null) {
            universityInput.setText(sessionManager.getUniversity());
        }

        loginButton.setOnClickListener(v -> attemptLogin());
        registerLink.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString().trim() : "";

        emailLayout.setError(null);
        passwordLayout.setError(null);

        boolean valid = true;
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError(getString(R.string.error_invalid_email));
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(getString(R.string.error_required_field));
            valid = false;
        }
        if (!valid) return;

        setLoading(true);

        authRepository.login(email, password, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                setLoading(false);
                routeUser(user);
            }

            @Override
            public void onError(Exception e) {
                setLoading(false);
                Toast.makeText(LoginActivity.this,
                        e.getMessage() != null ? e.getMessage() : getString(R.string.error_generic),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void routeUser(User user) {
        Intent intent;
        if (user.isAdmin()) {
            intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
        } else if (user.getDegreeId() == null) {
            intent = new Intent(LoginActivity.this, SelectDegreeActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, DashboardActivity.class);
        }
        startActivity(intent);
        finishAffinity();
    }

    private void setLoading(boolean loading) {
        progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loading);
        loginButton.setText(loading ? "" : getString(R.string.login));
    }
}
