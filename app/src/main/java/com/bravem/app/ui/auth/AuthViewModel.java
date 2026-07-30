package com.bravem.app.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bravem.app.data.AuthRepositoryImpl;
import com.bravem.app.data.DataCallback;
import com.bravem.app.domain.model.User;
import com.bravem.app.domain.repository.UserRepository;
import com.bravem.app.domain.usecase.LoginUseCase;
import com.bravem.app.domain.usecase.RegisterUseCase;
import com.bravem.app.utils.Resource;

public class AuthViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;
    private final MutableLiveData<Resource<User>> loginState = new MutableLiveData<>();
    private final MutableLiveData<Resource<User>> registerState = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> resetPasswordState = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        // In a real project with DI, these would be injected
        this.userRepository = new AuthRepositoryImpl(application);
        this.loginUseCase = new LoginUseCase(userRepository);
        this.registerUseCase = new RegisterUseCase(userRepository);
    }

    public LiveData<Resource<User>> getLoginState() {
        return loginState;
    }

    public LiveData<Resource<User>> getRegisterState() {
        return registerState;
    }

    public LiveData<Resource<Void>> getResetPasswordState() {
        return resetPasswordState;
    }

    public void login(String email, String password) {
        loginState.setValue(Resource.loading(null));
        loginUseCase.execute(email, password, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                loginState.setValue(Resource.success(user));
            }

            @Override
            public void onError(Exception e) {
                loginState.setValue(Resource.error(e.getMessage() != null ? e.getMessage() : "Login failed", null));
            }
        });
    }

    public void register(String name, String email, String password) {
        registerState.setValue(Resource.loading(null));
        registerUseCase.execute(name, email, password, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                registerState.setValue(Resource.success(user));
            }

            @Override
            public void onError(Exception e) {
                registerState.setValue(Resource.error(e.getMessage() != null ? e.getMessage() : "Registration failed", null));
            }
        });
    }

    public void forgotPassword(String email) {
        resetPasswordState.setValue(Resource.loading(null));
        userRepository.forgotPassword(email, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                resetPasswordState.setValue(Resource.success(null));
            }

            @Override
            public void onError(Exception e) {
                resetPasswordState.setValue(Resource.error(e.getMessage() != null ? e.getMessage() : "Reset failed", null));
            }
        });
    }
}
