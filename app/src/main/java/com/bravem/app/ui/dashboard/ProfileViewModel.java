package com.bravem.app.ui.dashboard;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bravem.app.data.AuthRepositoryImpl;
import com.bravem.app.data.DataCallback;
import com.bravem.app.data.DegreeRepositoryImpl;
import com.bravem.app.data.PaperRepositoryImpl;
import com.bravem.app.domain.model.Degree;
import com.bravem.app.domain.model.User;
import com.bravem.app.domain.repository.DegreeRepository;
import com.bravem.app.domain.repository.UserRepository;
import com.bravem.app.domain.usecase.UpdateProfileUseCase;
import com.bravem.app.model.PastPaper;
import com.bravem.app.utils.FileUtils;
import com.bravem.app.utils.Resource;
import com.bravem.app.utils.SessionManager;

import java.util.List;

public class ProfileViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final com.bravem.app.domain.repository.PaperRepository paperRepository;
    private final DegreeRepository degreeRepository;
    private final SessionManager sessionManager;

    private final UpdateProfileUseCase updateProfileUseCase;

    private final MutableLiveData<Resource<User>> userProfileState = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<PastPaper>>> myUploadsState = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<Degree>>> degreesState = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> updateState = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> deletionState = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new AuthRepositoryImpl(application);
        this.paperRepository = new PaperRepositoryImpl(application);
        this.degreeRepository = new DegreeRepositoryImpl(application);
        this.sessionManager = new SessionManager(application);
        
        this.updateProfileUseCase = new UpdateProfileUseCase(userRepository);
    }

    public LiveData<Resource<User>> getUserProfileState() { return userProfileState; }
    public LiveData<Resource<List<PastPaper>>> getMyUploadsState() { return myUploadsState; }
    public LiveData<Resource<List<Degree>>> getDegreesState() { return degreesState; }
    public LiveData<Resource<Void>> getUpdateState() { return updateState; }
    public LiveData<Resource<Void>> getDeletionState() { return deletionState; }

    public void fetchUserProfile() {
        userRepository.getCurrentUser(new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                userProfileState.setValue(Resource.success(user));
            }
            @Override
            public void onError(Exception e) {
                userProfileState.setValue(Resource.error(e.getMessage(), null));
            }
        });
    }

    public void loadMyUploads() {
        String uid = sessionManager.getUid();
        if (uid == null) return;

        myUploadsState.setValue(Resource.loading(null));
        paperRepository.fetchPapersUploadedBy(uid, new DataCallback<List<PastPaper>>() {
            @Override
            public void onSuccess(List<PastPaper> papers) {
                myUploadsState.setValue(Resource.success(papers));
            }
            @Override
            public void onError(Exception e) {
                myUploadsState.setValue(Resource.error(e.getMessage(), null));
            }
        });
    }

    public void fetchDegrees() {
        degreeRepository.fetchDegreesByUniversity(sessionManager.getUniversity(), new DataCallback<List<Degree>>() {
            @Override
            public void onSuccess(List<Degree> degrees) {
                degreesState.setValue(Resource.success(degrees));
            }
            @Override
            public void onError(Exception e) {
                degreesState.setValue(Resource.error(e.getMessage(), null));
            }
        });
    }

    public void updateProfile(String name, String intake, Degree degree) {
        String uid = sessionManager.getUid();
        updateState.setValue(Resource.loading(null));

        updateProfileUseCase.execute(uid, name, intake, degree, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                updateState.setValue(Resource.success(null));
                fetchUserProfile(); // Refresh
            }
            @Override
            public void onError(Exception e) {
                updateState.setValue(Resource.error(e.getMessage(), null));
            }
        });
    }

    public void updateProfilePicture(Uri uri) {
        String fileName = FileUtils.getFileName(getApplication(), uri);
        String localPath = FileUtils.copyFileToInternalStorage(getApplication(), uri, fileName);
        if (localPath != null) {
            String uid = sessionManager.getUid();
            updateState.setValue(Resource.loading(null));
            userRepository.updateProfilePicture(uid, localPath, new DataCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    updateState.setValue(Resource.success(null));
                    fetchUserProfile();
                }
                @Override
                public void onError(Exception e) {
                    updateState.setValue(Resource.error(e.getMessage(), null));
                }
            });
        }
    }

    public void requestDeletion() {
        deletionState.setValue(Resource.loading(null));
        userRepository.requestAccountDeletion(sessionManager.getUid(), new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                deletionState.setValue(Resource.success(null));
            }
            @Override
            public void onError(Exception e) {
                deletionState.setValue(Resource.error(e.getMessage(), null));
            }
        });
    }

    public void togglePin(String paperId) {
        paperRepository.togglePin(paperId, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isPinned) {
                loadMyUploads();
            }
            @Override
            public void onError(Exception e) {}
        });
    }

    public void logout() {
        userRepository.logout();
    }
}
