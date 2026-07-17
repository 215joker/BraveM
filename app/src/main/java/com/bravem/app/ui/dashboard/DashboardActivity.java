package com.bravem.app.ui.dashboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bravem.app.R;
import com.bravem.app.adapter.PastPaperAdapter;
import com.bravem.app.data.AuthRepository;
import com.bravem.app.data.DataCallback;
import com.bravem.app.data.PaperRepository;
import com.bravem.app.model.PastPaper;
import com.bravem.app.model.User;
import com.bravem.app.ui.auth.SelectDegreeActivity;
import com.bravem.app.ui.papers.PaperViewerActivity;
import com.bravem.app.ui.papers.SearchActivity;
import com.bravem.app.ui.upload.UploadPaperActivity;
import com.bravem.app.utils.FileUtils;
import com.bravem.app.utils.SessionManager;
import com.bravem.app.utils.UiUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * The student's main hub: greeting, their degree programme, recently added
 * papers relevant to that degree, and quick links to browse, search, and upload.
 */
public class DashboardActivity extends AppCompatActivity {

    private TextView welcomeText;
    private TextView universityText;
    private TextView degreeText;
    private TextView intakeText;
    private RecyclerView recyclerRecent;
    private PastPaperAdapter recentAdapter;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh;
    private View emptyState;
    private View searchBar;
    private FloatingActionButton uploadFab;
    private FloatingActionButton chatFab;
    private TextView chatBadge;
    private View profileButton;
    private ImageView profileImage;
    private View profileBadge;
    private TextView notificationCountText;

    private AuthRepository authRepository;
    private PaperRepository paperRepository;
    private com.bravem.app.data.NotificationRepository notificationRepository;
    private SessionManager sessionManager;

    private androidx.activity.result.ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        UiUtils.handleTopInset(findViewById(R.id.app_bar));
        UiUtils.handleFabBottomInset(findViewById(R.id.fab_upload), 24);
        
        View chatFabContainer = findViewById(R.id.fab_chat).getParent() instanceof View ? (View) findViewById(R.id.fab_chat).getParent() : findViewById(R.id.fab_chat);
        UiUtils.handleFabBottomInset(chatFabContainer, 100);

        requestPermissionLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
                isGranted -> {});

        authRepository = new AuthRepository(this);
        paperRepository = new PaperRepository(this);
        notificationRepository = new com.bravem.app.data.NotificationRepository(this);
        sessionManager = new SessionManager(this);

        welcomeText = findViewById(R.id.text_welcome);
        universityText = findViewById(R.id.text_university);
        degreeText = findViewById(R.id.text_degree);
        intakeText = findViewById(R.id.text_intake);
        recyclerRecent = findViewById(R.id.recycler_recent);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        emptyState = findViewById(R.id.empty_state);
        searchBar = findViewById(R.id.search_bar);
        uploadFab = findViewById(R.id.fab_upload);
        chatFab = findViewById(R.id.fab_chat);
        chatBadge = findViewById(R.id.text_chat_badge);
        profileButton = findViewById(R.id.btn_profile);
        profileImage = (ImageView) profileButton;
        profileBadge = findViewById(R.id.profile_notification_badge);
        notificationCountText = findViewById(R.id.text_notification_count);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        recentAdapter = new PastPaperAdapter(new PastPaperAdapter.OnPaperActionListener() {
            @Override
            public void onPaperClick(PastPaper paper) {
                Intent intent = new Intent(DashboardActivity.this, PaperViewerActivity.class);
                intent.putExtra(PaperViewerActivity.EXTRA_PAPER, paper);
                startActivity(intent);
            }

            @Override
            public void onDownloadClick(PastPaper paper) {
                downloadPaper(paper);
            }

            @Override
            public void onPinClick(PastPaper paper) {
                togglePin(paper);
            }
        });

        recyclerRecent.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecent.setAdapter(recentAdapter);

        swipeRefresh.setOnRefreshListener(this::loadDashboardData);

        searchBar.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, SearchActivity.class)));

        uploadFab.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, UploadPaperActivity.class)));

        chatFab.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, com.bravem.app.ui.community.CommunityActivity.class)));

        profileButton.setOnClickListener(v -> {
            String[] options = {"View Notifications", "View Profile"};
            new AlertDialog.Builder(this)
                    .setTitle("Account Options")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            startActivity(new Intent(DashboardActivity.this, NotificationActivity.class));
                        } else {
                            startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
                        }
                    })
                    .show();
        });

        bindGreeting();
        requestNotificationPermission();
    }

    private void updateNotificationBadge() {
        notificationRepository.getUnreadCount(new DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                if (count > 0) {
                    profileBadge.setVisibility(View.VISIBLE);
                    notificationCountText.setVisibility(View.VISIBLE);
                    notificationCountText.setText(String.valueOf(count));
                } else {
                    profileBadge.setVisibility(View.GONE);
                    notificationCountText.setVisibility(View.GONE);
                }
            }
            @Override
            public void onError(Exception e) {}
        });
    }

    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindGreeting();
        loadDashboardData();
        updateNotificationBadge();
        updateChatBadge();
    }

    private void updateChatBadge() {
        new com.bravem.app.data.ChatRepository(this).getUnreadChatCount(new DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                if (count > 0) {
                    chatBadge.setVisibility(View.VISIBLE);
                    chatBadge.setText(String.valueOf(count));
                } else {
                    chatBadge.setVisibility(View.GONE);
                }
            }
            @Override
            public void onError(Exception e) {}
        });
    }

    private void bindGreeting() {
        String name = sessionManager.getFullName();
        String welcome = getString(R.string.welcome_prefix) + " " + (name != null ? firstName(name) : "User");
        welcomeText.setText(welcome);
        
        universityText.setText(sessionManager.getUniversity() != null 
                ? sessionManager.getUniversity() 
                : "No University Selected");
                
        degreeText.setText(sessionManager.getDegreeName() != null
                ? sessionManager.getDegreeName()
                : getString(R.string.select_degree));

        intakeText.setText(sessionManager.getIntake() != null
                ? getString(R.string.intake_label, sessionManager.getIntake())
                : "");

        loadProfilePicture();
    }

    private void loadProfilePicture() {
        authRepository.fetchCurrentUserProfile(new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null && user.getProfilePicture() != null) {
                    try {
                        String path = user.getProfilePicture();
                        if (path.startsWith("/")) {
                            profileImage.setImageURI(Uri.fromFile(new java.io.File(path)));
                        } else {
                            profileImage.setImageURI(Uri.parse(path));
                        }
                        profileImage.setImageTintList(null); // Clear tint for actual photo
                        profileImage.setPadding(0, 0, 0, 0);
                        profileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    } catch (Exception e) {
                        setPlaceholderImage();
                    }
                } else {
                    setPlaceholderImage();
                }
            }
            @Override
            public void onError(Exception e) {
                setPlaceholderImage();
            }
        });
    }

    private void setPlaceholderImage() {
        profileImage.setImageResource(R.drawable.ic_person);
        profileImage.setImageTintList(android.content.res.ColorStateList.valueOf(
                androidx.core.content.ContextCompat.getColor(this, R.color.text_on_ink)));
        profileImage.setPadding(9, 9, 9, 9);
    }

    private String firstName(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 0 ? parts[0] : fullName;
    }

    private void loadDashboardData() {
        String degreeId = sessionManager.getDegreeId();
        String intake = sessionManager.getIntake();
        
        if (degreeId == null) {
            swipeRefresh.setRefreshing(false);
            return;
        }

        swipeRefresh.setRefreshing(true);

        paperRepository.fetchRecommendedPapers(degreeId, intake, 20, new DataCallback<List<PastPaper>>() {
            @Override
            public void onSuccess(List<PastPaper> papers) {
                swipeRefresh.setRefreshing(false);
                recentAdapter.submitList(papers);
                emptyState.setVisibility(papers.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerRecent.setVisibility(papers.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onError(Exception e) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(DashboardActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void togglePin(PastPaper paper) {
        paperRepository.togglePin(paper.getId(), new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isPinned) {
                loadDashboardData();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(DashboardActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadPaper(PastPaper paper) {
        if (paper.getFileUrl() == null) {
            Toast.makeText(this, R.string.download_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        FileUtils.downloadFile(this, paper.getFileUrl(),
                paper.getFileName() != null ? paper.getFileName() : paper.getTitle());
        Toast.makeText(this, R.string.downloading, Toast.LENGTH_SHORT).show();
    }
}
