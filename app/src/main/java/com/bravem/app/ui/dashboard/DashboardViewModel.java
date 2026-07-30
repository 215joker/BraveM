package com.bravem.app.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bravem.app.data.AuthRepositoryImpl;
import com.bravem.app.data.PaperRepositoryImpl;
import com.bravem.app.data.ChatRepository;
import com.bravem.app.data.DataCallback;
import com.bravem.app.data.NotificationRepository;
import com.bravem.app.domain.repository.PaperRepository;
import com.bravem.app.domain.repository.UserRepository;
import com.bravem.app.domain.usecase.GetRecommendedPapersUseCase;
import com.bravem.app.model.PastPaper;
import com.bravem.app.utils.Resource;
import com.bravem.app.utils.SessionManager;

import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final PaperRepository paperRepository;
    private final NotificationRepository notificationRepository;
    private final ChatRepository chatRepository;
    private final SessionManager sessionManager;

    private final GetRecommendedPapersUseCase getRecommendedPapersUseCase;

    private final MutableLiveData<Resource<List<PastPaper>>> papersState = new MutableLiveData<>();
    private final MutableLiveData<Resource<com.bravem.app.domain.model.User>> userProfileState = new MutableLiveData<>();
    private final MutableLiveData<Integer> unreadNotificationsCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> unreadChatCount = new MutableLiveData<>(0);

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new AuthRepositoryImpl(application);
        this.paperRepository = new PaperRepositoryImpl(application);
        this.notificationRepository = new NotificationRepository(application);
        this.chatRepository = new ChatRepository(application);
        this.sessionManager = new SessionManager(application);

        this.getRecommendedPapersUseCase = new GetRecommendedPapersUseCase(paperRepository);
    }

    public LiveData<Resource<List<PastPaper>>> getPapersState() { return papersState; }
    public LiveData<Resource<com.bravem.app.domain.model.User>> getUserProfileState() { return userProfileState; }
    public LiveData<Integer> getUnreadNotificationsCount() { return unreadNotificationsCount; }
    public LiveData<Integer> getUnreadChatCount() { return unreadChatCount; }

    public void loadDashboardData() {
        String degreeId = sessionManager.getDegreeId();
        String intake = sessionManager.getIntake();

        papersState.setValue(Resource.loading(null));
        getRecommendedPapersUseCase.execute(degreeId, intake, new DataCallback<List<PastPaper>>() {
            @Override
            public void onSuccess(List<PastPaper> papers) {
                papersState.setValue(Resource.success(papers));
            }

            @Override
            public void onError(Exception e) {
                papersState.setValue(Resource.error(e.getMessage() != null ? e.getMessage() : "Failed to load papers", null));
            }
        });
    }

    public void fetchUserProfile() {
        userRepository.getCurrentUser(new DataCallback<>() {
            @Override
            public void onSuccess(com.bravem.app.domain.model.User user) {
                userProfileState.setValue(Resource.success(user));
            }

            @Override
            public void onError(Exception e) {
                userProfileState.setValue(Resource.error(e.getMessage(), null));
            }
        });
    }

    public void refreshBadges() {
        notificationRepository.getUnreadCount(new DataCallback<>() {
            @Override
            public void onSuccess(Integer count) {
                unreadNotificationsCount.setValue(count);
            }
            @Override
            public void onError(Exception e) {}
        });

        chatRepository.getUnreadChatCount(new DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                unreadChatCount.setValue(count);
            }
            @Override
            public void onError(Exception e) {}
        });
    }

    public void togglePin(String paperId) {
        paperRepository.togglePin(paperId, new DataCallback<>() {
            @Override
            public void onSuccess(Boolean isPinned) {
                loadDashboardData(); // Refresh list
            }
            @Override
            public void onError(Exception e) {}
        });
    }
}
