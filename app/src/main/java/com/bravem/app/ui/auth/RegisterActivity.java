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

public class RegisterActivity extends AppCompatActivity {

    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_EMAIL = "extra_email";
    public static final String EXTRA_PASSWORD = "extra_password";

    private TextInputLayout nameLayout, emailLayout, passwordLayout, confirmLayout;
    private TextInputEditText nameInput, emailInput, passwordInput, confirmInput, universityInput;
    private MaterialButton registerButton;
    private View loginLink;
    private CircularProgressIndicator progressIndicator;

    private AuthRepository authRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authRepository = new AuthRepository(this);
        sessionManager = new SessionManager(this);

        nameLayout = findViewById(R.id.layout_name);
        emailLayout = findViewById(R.id.layout_email);
        passwordLayout = findViewById(R.id.layout_password);
        confirmLayout = findViewById(R.id.layout_confirm_password);

        nameInput = findViewById(R.id.input_name);
        emailInput = findViewById(R.id.input_email);
        passwordInput = findViewById(R.id.input_password);
        confirmInput = findViewById(R.id.input_confirm_password);
        universityInput = findViewById(R.id.input_university);

        registerButton = findViewById(R.id.btn_register);
        loginLink = findViewById(R.id.link_login);
        progressIndicator = findViewById(R.id.progress_indicator);

        // Pre-populate university
        if (universityInput != null) {
            universityInput.setText(sessionManager.getUniversity());
        }

        registerButton.setOnClickListener(v -> attemptRegister());
        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }

    private void attemptRegister() {
        String name = textOf(nameInput);
        String email = textOf(emailInput);
        String password = textOf(passwordInput);
        String confirm = textOf(confirmInput);

        nameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmLayout.setError(null);

        boolean valid = true;
        if (TextUtils.isEmpty(name)) {
            nameLayout.setError(getString(R.string.error_required_field));
            valid = false;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError(getString(R.string.error_invalid_email));
            valid = false;
        }
        if (password.length() < 6) {
            passwordLayout.setError(getString(R.string.error_password_short));
            valid = false;
        }
        if (!password.equals(confirm)) {
            confirmLayout.setError(getString(R.string.error_passwords_dont_match));
            valid = false;
        }
        if (!valid) return;

        setLoading(true);

        // Check if user already exists before proceeding
        authRepository.checkUserExists(email, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean exists) {
                setLoading(false);
                if (exists) {
                    emailLayout.setError(getString(R.string.error_user_exists));
                } else {
                    Intent intent = new Intent(RegisterActivity.this, SelectDegreeActivity.class);
                    intent.putExtra(EXTRA_NAME, name);
                    intent.putExtra(EXTRA_EMAIL, email);
                    intent.putExtra(EXTRA_PASSWORD, password);
                    startActivity(intent);
                }
            }

            @Override
            public void onError(Exception e) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String textOf(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    private void setLoading(boolean loading) {
        progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!loading);
        registerButton.setText(loading ? "" : getString(R.string.register));
    }
}
