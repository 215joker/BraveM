package com.bravem.app.domain.usecase;

import com.bravem.app.data.DataCallback;
import com.bravem.app.domain.model.Degree;
import com.bravem.app.domain.repository.UserRepository;

public class UpdateProfileUseCase {
    private final UserRepository userRepository;

    public UpdateProfileUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(String uid, String name, String intake, Degree degree, DataCallback<Void> callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onError(new IllegalArgumentException("User ID is required"));
            return;
        }
        if (name == null || name.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Name cannot be empty"));
            return;
        }

        // Domain Rule: Intake should follow a specific format (e.g., Year.Semester)
        if (intake != null && !intake.isEmpty() && !intake.matches("\\d+\\.\\d+")) {
            callback.onError(new IllegalArgumentException("Intake must be in format 'Year.Semester' (e.g. 1.1)"));
            return;
        }

        userRepository.updateProfile(uid, name, intake, degree, callback);
    }
}
