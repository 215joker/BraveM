package com.bravem.app.ui.dashboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.adapter.PastPaperAdapter;
import com.bravem.app.data.AuthRepository;
import com.bravem.app.data.DataCallback;
import com.bravem.app.data.PaperRepository;
import com.bravem.app.model.PastPaper;
import com.bravem.app.model.User;
import com.bravem.app.ui.auth.LoginActivity;
import com.bravem.app.ui.papers.PaperViewerActivity;
import com.bravem.app.utils.FileUtils;
import com.bravem.app.utils.SessionManager;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameView, uniView, degreeView, intakeView;
    private ImageView profileImage;
    private RecyclerView recyclerUploads;
    private PastPaperAdapter adapter;
    private View emptyState;
    private View backButton, logoutButton, editImageButton, editNameButton;
    private View settingTheme, settingAbout, settingContact;

    private AuthRepository authRepository;
    private PaperRepository paperRepository;
    private SessionManager sessionManager;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        authRepository = new AuthRepository(this);
        paperRepository = new PaperRepository(this);
        sessionManager = new SessionManager(this);

        nameView = findViewById(R.id.text_profile_name);
        uniView = findViewById(R.id.text_profile_uni);
        degreeView = findViewById(R.id.text_profile_degree);
        intakeView = findViewById(R.id.text_profile_intake);
        profileImage = findViewById(R.id.img_profile);
        
        recyclerUploads = findViewById(R.id.recycler_my_uploads);
        emptyState = findViewById(R.id.empty_state);
        
        backButton = findViewById(R.id.btn_back);
        logoutButton = findViewById(R.id.btn_logout);
        editImageButton = findViewById(R.id.btn_edit_image);
        editNameButton = findViewById(R.id.btn_edit_name);

        settingTheme = findViewById(R.id.setting_theme);
        settingAbout = findViewById(R.id.setting_about);
        settingContact = findViewById(R.id.setting_contact);

        backButton.setOnClickListener(v -> finish());
        logoutButton.setOnClickListener(v -> logout());

        editNameButton.setOnClickListener(v -> showEditNameDialog());
        editImageButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        settingTheme.setOnClickListener(v -> showThemeDialog());
        settingAbout.setOnClickListener(v -> showAboutDialog());
        settingContact.setOnClickListener(v -> showContactDialog());

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        updateProfilePicture(uri);
                    }
                }
        );

        setupProfileData();
        setupRecyclerView();
        loadMyUploads();
    }

    private void setupProfileData() {
        nameView.setText(sessionManager.getFullName());
        uniView.setText(sessionManager.getUniversity());
        degreeView.setText(sessionManager.getDegreeName() != null
                ? sessionManager.getDegreeName() : getString(R.string.select_degree));
        intakeView.setText(sessionManager.getIntake() != null
                ? getString(R.string.intake_label, sessionManager.getIntake()) : "");
        
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
                        profileImage.setPadding(0, 0, 0, 0); // Remove padding for image
                        profileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    } catch (Exception e) {
                        profileImage.setImageResource(R.drawable.ic_person);
                        profileImage.setPadding(13, 13, 13, 13);
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onError(Exception e) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new PastPaperAdapter(new PastPaperAdapter.OnPaperActionListener() {
            @Override
            public void onPaperClick(PastPaper paper) {
                Intent intent = new Intent(ProfileActivity.this, PaperViewerActivity.class);
                intent.putExtra(PaperViewerActivity.EXTRA_PAPER, paper);
                startActivity(intent);
            }

            @Override
            public void onDownloadClick(PastPaper paper) {
                if (paper.getFileUrl() == null) return;
                FileUtils.downloadFile(ProfileActivity.this, paper.getFileUrl(),
                        paper.getFileName() != null ? paper.getFileName() : paper.getTitle());
                Toast.makeText(ProfileActivity.this, R.string.downloading, Toast.LENGTH_SHORT).show();
            }
        });

        recyclerUploads.setLayoutManager(new LinearLayoutManager(this));
        recyclerUploads.setAdapter(adapter);
    }

    private void showEditNameDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null);
        EditText input = view.findViewById(R.id.edit_text_input);
        input.setText(sessionManager.getFullName());
        input.setHint(R.string.full_name);

        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_name)
                .setView(view)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        updateProfile(newName, null);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showThemeDialog() {
        String[] options = {
                getString(R.string.theme_light),
                getString(R.string.theme_dark),
                getString(R.string.theme_system)
        };

        int currentTheme = sessionManager.getTheme();
        int checkedItem = 0; // Default to Light
        if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) checkedItem = 1;
        else if (currentTheme == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) checkedItem = 2;

        new AlertDialog.Builder(this)
                .setTitle(R.string.choose_theme)
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    int mode = AppCompatDelegate.MODE_NIGHT_NO;
                    if (which == 1) mode = AppCompatDelegate.MODE_NIGHT_YES;
                    else if (which == 2) mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

                    sessionManager.setTheme(mode);
                    AppCompatDelegate.setDefaultNightMode(mode);
                    dialog.dismiss();
                })
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.about_title)
                .setMessage(R.string.about_desc)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void showContactDialog() {
        String[] options = {
                getString(R.string.contact_email),
                getString(R.string.contact_call),
                getString(R.string.contact_whatsapp)
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.contact_title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) contactEmail();
                    else if (which == 1) contactCall();
                    else if (which == 2) contactWhatsApp();
                })
                .show();
    }

    private void contactEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:mutiziwabravely@gmail.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "BraveM Support Request");
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void contactCall() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:00260762033632"));
        startActivity(intent);
    }

    private void contactWhatsApp() {
        String url = "https://api.whatsapp.com/send?phone=260762033632";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfilePicture(Uri uri) {
        String fileName = FileUtils.getFileName(this, uri);
        String localPath = FileUtils.copyFileToInternalStorage(this, uri, fileName);
        if (localPath != null) {
            updateProfile(sessionManager.getFullName(), localPath);
        } else {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfile(String name, String imageUri) {
        String uid = sessionManager.getUid();
        authRepository.updateUserProfile(uid, name, imageUri, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                nameView.setText(name);
                if (imageUri != null) {
                    if (imageUri.startsWith("/")) {
                        profileImage.setImageURI(Uri.fromFile(new java.io.File(imageUri)));
                    } else {
                        profileImage.setImageURI(Uri.parse(imageUri));
                    }
                    profileImage.setPadding(0, 0, 0, 0);
                    profileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
                Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ProfileActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMyUploads() {
        String uid = sessionManager.getUid();
        if (uid == null) return;

        paperRepository.fetchPapersUploadedBy(uid, new DataCallback<List<PastPaper>>() {
            @Override
            public void onSuccess(List<PastPaper> papers) {
                adapter.submitList(papers);
                emptyState.setVisibility(papers.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ProfileActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        authRepository.logout();
        sessionManager.clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
