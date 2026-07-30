package com.bravem.app.domain.usecase;

import com.bravem.app.data.DataCallback;
import com.bravem.app.domain.model.User;
import com.bravem.app.domain.repository.UserRepository;

public class RegisterUseCase {
    private final UserRepository userRepository;

    public RegisterUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(String name, String email, String password, DataCallback<User> callback) {
        if (name == null || name.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Name is required"));
            return;
        }
        if (email == null || email.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Email is required"));
            return;
        }
        if (password == null || password.length() < 6) {
            callback.onError(new IllegalArgumentException("Password must be at least 6 characters"));
            return;
        }

        userRepository.register(name, email, password, callback);
    }
}
