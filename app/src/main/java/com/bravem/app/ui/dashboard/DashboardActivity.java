package com.bravem.app.ui.dashboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bravem.app.R;
import com.bravem.app.adapter.PastPaperAdapter;
import com.bravem.app.databinding.ActivityDashboardBinding;
import com.bravem.app.model.PastPaper;
import com.bravem.app.domain.model.User;
import com.bravem.app.ui.notifications.NotificationActivity;
import com.bravem.app.ui.papers.PaperViewerActivity;
import com.bravem.app.ui.papers.SearchActivity;
import com.bravem.app.ui.profile.ProfileActivity;
import com.bravem.app.ui.upload.UploadPaperActivity;
import com.bravem.app.utils.FileUtils;
import com.bravem.app.utils.SessionManager;
import com.bravem.app.utils.UiUtils;

import java.io.File;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private DashboardViewModel viewModel;
    private PastPaperAdapter recentAdapter;
    private SessionManager sessionManager;

    private com.bravem.app.domain.repository.PaperRepository paperRepository;
    private PastPaper paperForMemo;
    private final androidx.activity.result.ActivityResultLauncher<Intent> memoPickerLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null && paperForMemo != null) {
                        uploadMemoOnly(uri);
                    }
                }
            });

    private androidx.activity.result.ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        sessionManager = new SessionManager(this);
        paperRepository = new com.bravem.app.data.PaperRepositoryImpl(this);

        setupInsets();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        observeViewModel();
        
        requestNotificationPermission();
    }

    private void setupInsets() {
        UiUtils.handleTopInset(binding.appBar);
        UiUtils.handleFabBottomInset(binding.fabUpload, 24);
        
        // Chat FAB container (it's inside a FrameLayout in XML)
        View chatFabContainer = (View) binding.fabChat.getParent();
        UiUtils.handleFabBottomInset(chatFabContainer, 100);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupRecyclerView() {
        recentAdapter = new PastPaperAdapter(new PastPaperAdapter.OnPaperActionListener() {
            @Override
            public void onPaperClick(PastPaper paper) {
                showPaperSelectionDialog(paper);
            }

            @Override
            public void onDownloadClick(PastPaper paper) {
                downloadPaper(paper);
            }

            @Override
            public void onPinClick(PastPaper paper) {
                viewModel.togglePin(paper.getId());
            }
        });

        binding.recyclerRecent.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerRecent.setAdapter(recentAdapter);
        // Disable nested scrolling as it's inside NestedScrollView
        binding.recyclerRecent.setNestedScrollingEnabled(false);
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadDashboardData());

        binding.searchBar.setOnClickListener(v ->
                startActivity(new Intent(this, SearchActivity.class)));

        binding.fabUpload.setOnClickListener(v ->
                startActivity(new Intent(this, UploadPaperActivity.class)));

        binding.fabChat.setOnClickListener(v ->
                startActivity(new Intent(this, com.bravem.app.ui.community.CommunityActivity.class)));

        binding.btnProfile.setOnClickListener(v -> showProfileOptions());
    }

    private void observeViewModel() {
        viewModel.getPapersState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    binding.swipeRefresh.setRefreshing(true);
                    break;
                case SUCCESS:
                    binding.swipeRefresh.setRefreshing(false);
                    recentAdapter.submitList(resource.data);
                    binding.emptyState.setVisibility(resource.data == null || resource.data.isEmpty() ? View.VISIBLE : View.GONE);
                    binding.recyclerRecent.setVisibility(resource.data != null && !resource.data.isEmpty() ? View.VISIBLE : View.GONE);
                    break;
                case ERROR:
                    binding.swipeRefresh.setRefreshing(false);
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getUserProfileState().observe(this, resource -> {
            if (resource != null && resource.data != null) {
                updateProfileUI(resource.data);
            }
        });

        viewModel.getUnreadNotificationsCount().observe(this, count -> {
            if (count != null && count > 0) {
                binding.profileNotificationBadge.setVisibility(View.VISIBLE);
                binding.textNotificationCount.setVisibility(View.VISIBLE);
                binding.textNotificationCount.setText(String.valueOf(count));
            } else {
                binding.profileNotificationBadge.setVisibility(View.GONE);
                binding.textNotificationCount.setVisibility(View.GONE);
            }
        });

        viewModel.getUnreadChatCount().observe(this, count -> {
            if (count != null && count > 0) {
                binding.textChatBadge.setVisibility(View.VISIBLE);
                binding.textChatBadge.setText(String.valueOf(count));
            } else {
                binding.textChatBadge.setVisibility(View.GONE);
            }
        });
    }

    private void updateProfileUI(User user) {
        if (user.getProfilePicture() != null) {
            try {
                String path = user.getProfilePicture();
                if (path.startsWith("/")) {
                    binding.btnProfile.setImageURI(Uri.fromFile(new File(path)));
                } else {
                    binding.btnProfile.setImageURI(Uri.parse(path));
                }
                binding.btnProfile.setImageTintList(null);
                binding.btnProfile.setPadding(0, 0, 0, 0);
                binding.btnProfile.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                setPlaceholderImage();
            }
        } else {
            setPlaceholderImage();
        }
    }

    private void setPlaceholderImage() {
        binding.btnProfile.setImageResource(R.drawable.ic_person);
        binding.btnProfile.setImageTintList(android.content.res.ColorStateList.valueOf(
                androidx.core.content.ContextCompat.getColor(this, R.color.text_on_ink)));
        binding.btnProfile.setPadding(9, 9, 9, 9);
    }

    private void showProfileOptions() {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this, R.style.Theme_BraveM_BottomSheetDialog);

        com.bravem.app.databinding.LayoutProfileBottomSheetBinding sheetBinding =
                com.bravem.app.databinding.LayoutProfileBottomSheetBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        // Populate header
        User user = viewModel.getUserProfileState().getValue() != null ? viewModel.getUserProfileState().getValue().data : null;
        if (user != null) {
            sheetBinding.sheetUserName.setText(user.getFullName());
            sheetBinding.sheetUserEmail.setText(user.getEmail());
            if (user.getProfilePicture() != null) {
                try {
                    String path = user.getProfilePicture();
                    if (path.startsWith("/")) {
                        sheetBinding.sheetProfileImage.setImageURI(Uri.fromFile(new File(path)));
                    } else {
                        sheetBinding.sheetProfileImage.setImageURI(Uri.parse(path));
                    }
                    sheetBinding.sheetProfileImage.setImageTintList(null);
                    sheetBinding.sheetProfileImage.setPadding(0, 0, 0, 0);
                } catch (Exception ignored) {}
            }
        } else {
            sheetBinding.sheetUserName.setText(sessionManager.getFullName());
            sheetBinding.sheetUserEmail.setText(sessionManager.getEmail());
        }

        // Listeners
        sheetBinding.btnViewProfile.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            startActivity(new Intent(this, ProfileActivity.class));
        });

        sheetBinding.btnNotifications.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            startActivity(new Intent(this, NotificationActivity.class));
        });

        sheetBinding.btnSettings.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            startActivity(new Intent(this, ProfileActivity.class));
        });

        if ("admin".equals(sessionManager.getRole())) {
            sheetBinding.btnAdmin.setVisibility(View.VISIBLE);
            sheetBinding.btnAdmin.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                startActivity(new Intent(this, com.bravem.app.ui.admin.AdminDashboardActivity.class));
            });
        }

        sheetBinding.btnLogout.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            showLogoutConfirmation();
        });

        bottomSheetDialog.show();
    }

    private void showLogoutConfirmation() {
        com.bravem.app.utils.DialogUtils.showConfirmation(this, 
                getString(R.string.logout), 
                "Are you sure you want to logout?", 
                getString(R.string.logout), 
                () -> {
                    sessionManager.clear();
                    Intent intent = new Intent(this, com.bravem.app.ui.auth.LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
    }

    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher = registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
                    isGranted -> {});
            
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindGreeting();
        viewModel.loadDashboardData();
        viewModel.fetchUserProfile();
        viewModel.refreshBadges();
    }

    private void bindGreeting() {
        String name = sessionManager.getFullName();
        String welcome = getString(R.string.welcome_prefix) + " " + (name != null ? firstName(name) : "User");
        binding.textWelcome.setText(welcome);
        
        binding.textUniversity.setText(sessionManager.getUniversity() != null 
                ? sessionManager.getUniversity() 
                : "No University Selected");
                
        binding.textDegree.setText(sessionManager.getDegreeName() != null
                ? sessionManager.getDegreeName()
                : getString(R.string.select_degree));

        binding.textIntake.setText(sessionManager.getIntake() != null
                ? getString(R.string.intake_label, sessionManager.getIntake())
                : "");
    }

    private String firstName(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 0 ? parts[0] : fullName;
    }

    private void showPaperSelectionDialog(PastPaper paper) {
        boolean hasMemo = paper.getMemoUrl() != null;
        String[] options = hasMemo 
                ? new String[]{"View Question Paper", "View Memorandum"} 
                : new String[]{"View Question Paper", "Upload Memorandum (Missing)"};
        int[] icons = {R.drawable.ic_document, hasMemo ? R.drawable.ic_document : R.drawable.ic_upload};

        com.bravem.app.utils.DialogUtils.showOptions(this, paper.getTitle(), options, icons, index -> {
            if (index == 0) {
                openPaperViewer(paper, false);
            } else {
                if (hasMemo) {
                    openPaperViewer(paper, true);
                } else {
                    paperForMemo = paper;
                    launchMemoPicker();
                }
            }
        });
    }

    private void launchMemoPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        memoPickerLauncher.launch(Intent.createChooser(intent, "Select Memorandum (PDF)"));
    }

    private void uploadMemoOnly(Uri uri) {
        String fileName = FileUtils.getFileName(this, uri);
        Toast.makeText(this, "Uploading memorandum...", Toast.LENGTH_SHORT).show();
        
        paperRepository.uploadMemorandumOnly(paperForMemo.getId(), uri, fileName, null, new com.bravem.app.data.DataCallback<PastPaper>() {
            @Override
            public void onSuccess(PastPaper result) {
                runOnUiThread(() -> {
                    Toast.makeText(DashboardActivity.this, "Memorandum uploaded successfully!", Toast.LENGTH_LONG).show();
                    viewModel.loadDashboardData();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> Toast.makeText(DashboardActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void openPaperViewer(PastPaper paper, boolean isMemo) {
        Intent intent = new Intent(this, PaperViewerActivity.class);
        intent.putExtra(PaperViewerActivity.EXTRA_PAPER, paper);
        intent.putExtra("is_memo", isMemo);
        startActivity(intent);
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
