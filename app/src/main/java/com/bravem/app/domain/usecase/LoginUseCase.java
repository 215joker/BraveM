package com.bravem.app.domain.usecase;

import com.bravem.app.data.DataCallback;
import com.bravem.app.domain.model.User;
import com.bravem.app.domain.repository.UserRepository;

public class LoginUseCase {
    private final UserRepository userRepository;

    public LoginUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(String email, String password, DataCallback<User> callback) {
        // Business Rule: Email and Password cannot be empty (already in UI, but good to have here too)
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Email and password are required"));
            return;
        }

        userRepository.login(email, password, callback);
    }
}
