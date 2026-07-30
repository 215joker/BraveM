package com.bravem.app.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bravem.app.R;
import com.bravem.app.adapter.PastPaperAdapter;
import com.bravem.app.databinding.ActivityProfileBinding;
import com.bravem.app.databinding.DialogEditProfileBinding;
import com.bravem.app.domain.model.Degree;
import com.bravem.app.domain.model.User;
import com.bravem.app.ui.auth.LoginActivity;
import com.bravem.app.ui.auth.SelectUniversityActivity;
import com.bravem.app.ui.dashboard.ProfileViewModel;
import com.bravem.app.ui.papers.PaperViewerActivity;
import com.bravem.app.utils.FileUtils;
import com.bravem.app.utils.SessionManager;
import com.bravem.app.utils.UiUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private ProfileViewModel viewModel;
    private PastPaperAdapter adapter;
    private SessionManager sessionManager;
    private ActivityResultLauncher<String> imagePickerLauncher;

    private List<Degree> allDegrees = new ArrayList<>();
    private Degree chosenDegree;
    private com.bravem.app.model.PastPaper paperForMemo;

    private final androidx.activity.result.ActivityResultLauncher<Intent> memoPickerLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null && paperForMemo != null) {
                        uploadMemoOnly(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        sessionManager = new SessionManager(this);

        setupInsets();
        setupListeners();
        setupRecyclerView();
        observeViewModel();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> { if (uri != null) viewModel.updateProfilePicture(uri); }
        );

        viewModel.fetchUserProfile();
        viewModel.loadMyUploads();
    }

    private void setupInsets() {
        UiUtils.handleTopInset(binding.appBar);
        UiUtils.handleBottomInset(binding.getRoot());
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnLogout.setOnClickListener(v -> showLogoutConfirmation());
        binding.btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        binding.btnEditImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        binding.settingTheme.setOnClickListener(v -> showThemeDialog());
        binding.settingAbout.setOnClickListener(v -> showAboutDialog());
        binding.settingContact.setOnClickListener(v -> showContactDialog());
    }

    private void setupRecyclerView() {
        adapter = new PastPaperAdapter(new PastPaperAdapter.OnPaperActionListener() {
            @Override
            public void onPaperClick(com.bravem.app.model.PastPaper paper) {
                showPaperSelectionDialog(paper);
            }

            @Override
            public void onDownloadClick(com.bravem.app.model.PastPaper paper) {
                if (paper.getFileUrl() == null) return;
                FileUtils.downloadFile(ProfileActivity.this, paper.getFileUrl(),
                        paper.getFileName() != null ? paper.getFileName() : paper.getTitle());
                Toast.makeText(ProfileActivity.this, R.string.downloading, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPinClick(com.bravem.app.model.PastPaper paper) {
                viewModel.togglePin(paper.getId());
            }
        });

        binding.recyclerMyUploads.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerMyUploads.setAdapter(adapter);
        binding.recyclerMyUploads.setNestedScrollingEnabled(false);
    }

    private void showPaperSelectionDialog(com.bravem.app.model.PastPaper paper) {
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
        
        com.bravem.app.domain.repository.PaperRepository paperRepo = new com.bravem.app.data.PaperRepositoryImpl(this);
        paperRepo.uploadMemorandumOnly(paperForMemo.getId(), uri, fileName, null, new com.bravem.app.data.DataCallback<com.bravem.app.model.PastPaper>() {
            @Override
            public void onSuccess(com.bravem.app.model.PastPaper result) {
                runOnUiThread(() -> {
                    Toast.makeText(ProfileActivity.this, "Memorandum uploaded successfully!", Toast.LENGTH_LONG).show();
                    viewModel.loadMyUploads();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void openPaperViewer(com.bravem.app.model.PastPaper paper, boolean isMemo) {
        Intent intent = new Intent(this, PaperViewerActivity.class);
        intent.putExtra(PaperViewerActivity.EXTRA_PAPER, paper);
        intent.putExtra("is_memo", isMemo);
        startActivity(intent);
    }

    private void observeViewModel() {
        viewModel.getUserProfileState().observe(this, resource -> {
            if (resource != null && resource.data != null) {
                updateUI(resource.data);
            }
        });

        viewModel.getMyUploadsState().observe(this, resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    adapter.submitList(resource.data);
                    binding.emptyState.setVisibility(resource.data == null || resource.data.isEmpty() ? View.VISIBLE : View.GONE);
                    break;
                case ERROR:
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getUpdateState().observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == com.bravem.app.utils.Resource.Status.SUCCESS) {
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
            } else if (resource.status == com.bravem.app.utils.Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getDeletionState().observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == com.bravem.app.utils.Resource.Status.SUCCESS) {
                Toast.makeText(this, "Account deletion requested", Toast.LENGTH_LONG).show();
                logoutAndGoToUniversitySelect();
            }
        });
    }

    private void updateUI(User user) {
        binding.textProfileName.setText(user.getFullName());
        binding.textProfileUni.setText(user.getUniversity());
        binding.textProfileDegree.setText(user.getDegreeName());
        binding.textProfileIntake.setText(getString(R.string.intake_label, user.getIntake()));

        if (user.getProfilePicture() != null) {
            try {
                String path = user.getProfilePicture();
                if (path.startsWith("/")) {
                    binding.imgProfile.setImageURI(Uri.fromFile(new File(path)));
                } else {
                    binding.imgProfile.setImageURI(Uri.parse(path));
                }
                binding.imgProfile.setImageTintList(null);
                binding.imgProfile.setPadding(0, 0, 0, 0);
                binding.imgProfile.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                setPlaceholderImage();
            }
        } else {
            setPlaceholderImage();
        }
    }

    private void setPlaceholderImage() {
        binding.imgProfile.setImageResource(R.drawable.ic_person);
        binding.imgProfile.setImageTintList(android.content.res.ColorStateList.valueOf(
                androidx.core.content.ContextCompat.getColor(this, R.color.text_on_ink)));
        binding.imgProfile.setPadding(13, 13, 13, 13);
    }

    private void showEditProfileDialog() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this, R.style.Theme_BraveM_BottomSheetDialog);
        com.bravem.app.databinding.DialogEditProfileBinding dialogBinding = com.bravem.app.databinding.DialogEditProfileBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        
        dialogBinding.editName.setText(sessionManager.getFullName());
        dialogBinding.editIntake.setText(sessionManager.getIntake());
        dialogBinding.editDegree.setText(sessionManager.getDegreeName(), false);

        viewModel.getDegreesState().observe(this, resource -> {
            if (resource != null && resource.data != null) {
                allDegrees = resource.data;
                List<String> names = new ArrayList<>();
                for (Degree d : allDegrees) names.add(d.getName());
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1, names);
                dialogBinding.editDegree.setAdapter(arrayAdapter);
                dialogBinding.editDegree.setOnItemClickListener((parent, v, position, id) -> chosenDegree = allDegrees.get(position));
            }
        });
        viewModel.fetchDegrees();

        dialogBinding.btnSave.setOnClickListener(v -> {
            String newName = dialogBinding.editName.getText().toString().trim();
            String newIntake = dialogBinding.editIntake.getText().toString().trim();
            if (newName.isEmpty()) {
                dialogBinding.editName.setError(getString(R.string.error_required_field));
                return;
            }
            viewModel.updateProfile(newName, newIntake, chosenDegree);
            dialog.dismiss();
        });

        dialogBinding.btnDeleteAccount.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteConfirmation();
        });

        dialog.show();
    }

    private void showDeleteConfirmation() {
        com.bravem.app.utils.DialogUtils.showConfirmation(this,
                "Delete Account",
                "Are you sure you want to delete your account? Your details will be permanently removed after 30 days.",
                "Delete",
                () -> viewModel.requestDeletion());
    }

    private void showLogoutConfirmation() {
        com.bravem.app.utils.DialogUtils.showConfirmation(this,
                getString(R.string.logout),
                "Are you sure you want to logout?",
                getString(R.string.logout),
                this::logout);
    }

    private void logout() {
        viewModel.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void logoutAndGoToUniversitySelect() {
        viewModel.logout();
        Intent intent = new Intent(this, SelectUniversityActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showThemeDialog() {
        String[] options = {getString(R.string.theme_light), getString(R.string.theme_dark), getString(R.string.theme_system)};
        // No icons for now, but we can pass null
        com.bravem.app.utils.DialogUtils.showOptions(this, getString(R.string.choose_theme), options, null, which -> {
            int mode = which == 1 ? AppCompatDelegate.MODE_NIGHT_YES : (which == 2 ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM : AppCompatDelegate.MODE_NIGHT_NO);
            sessionManager.setTheme(mode);
            AppCompatDelegate.setDefaultNightMode(mode);
        });
    }

    private void showAboutDialog() {
        com.bravem.app.utils.DialogUtils.showConfirmation(this,
                getString(R.string.about_title),
                getString(R.string.about_desc),
                getString(R.string.ok),
                null);
    }

    private void showContactDialog() {
        String[] options = {getString(R.string.contact_email), getString(R.string.contact_call), getString(R.string.in_app_chat)};
        int[] icons = {R.drawable.ic_google, R.drawable.ic_call_end, R.drawable.ic_chat};
        com.bravem.app.utils.DialogUtils.showOptions(this, getString(R.string.contact_title), options, icons, which -> {
            if (which == 0) contactEmail();
            else if (which == 1) contactCall();
            else if (which == 2) contactAdmin();
        });
    }

    private void contactEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:mutiziwabravely@gmail.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "BraveM Support Request");
        try { startActivity(intent); } catch (Exception e) { Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show(); }
    }

    private void contactCall() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:00260762033632"));
        startActivity(intent);
    }

    private void contactAdmin() {
        Toast.makeText(this, "Connecting to support chat...", Toast.LENGTH_SHORT).show();
    }
}
