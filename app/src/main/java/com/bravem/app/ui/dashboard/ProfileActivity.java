package com.bravem.app.ui.dashboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.bravem.app.model.Degree;
import com.bravem.app.model.PastPaper;
import com.bravem.app.model.User;
import com.bravem.app.ui.auth.LoginActivity;
import com.bravem.app.ui.auth.SelectUniversityActivity;
import com.bravem.app.ui.papers.PaperViewerActivity;
import com.bravem.app.utils.FileUtils;
import com.bravem.app.utils.SessionManager;
import com.bravem.app.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameView, uniView, degreeView, intakeView;
    private ImageView profileImage;
    private RecyclerView recyclerUploads;
    private PastPaperAdapter adapter;
    private View emptyState;
    private View backButton, logoutButton, editImageButton, editProfileButton;
    private View settingTheme, settingAbout, settingContact;

    private AuthRepository authRepository;
    private PaperRepository paperRepository;
    private com.bravem.app.data.DegreeRepository degreeRepository;
    private SessionManager sessionManager;

    private ActivityResultLauncher<String> imagePickerLauncher;

    private List<Degree> allDegrees = new ArrayList<>();
    private Degree chosenDegree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        UiUtils.handleTopInset(findViewById(R.id.app_bar));
        UiUtils.handleBottomInset(findViewById(android.R.id.content));

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        updateProfilePicture(uri);
                    }
                }
        );

        authRepository = new AuthRepository(this);
        paperRepository = new PaperRepository(this);
        degreeRepository = new com.bravem.app.data.DegreeRepository(this);
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
        editProfileButton = findViewById(R.id.btn_edit_profile);

        settingTheme = findViewById(R.id.setting_theme);
        settingAbout = findViewById(R.id.setting_about);
        settingContact = findViewById(R.id.setting_contact);

        backButton.setOnClickListener(v -> finish());
        logoutButton.setOnClickListener(v -> logout());

        editProfileButton.setOnClickListener(v -> showEditProfileDialog());
        editImageButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        settingTheme.setOnClickListener(v -> showThemeDialog());
        settingAbout.setOnClickListener(v -> showAboutDialog());
        settingContact.setOnClickListener(v -> showContactDialog());

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
                        profileImage.setImageTintList(null); // Clear tint for actual photo
                        profileImage.setPadding(0, 0, 0, 0); // Remove padding for image
                        profileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    } catch (Exception e) {
                        setPlaceholderImage();
                        e.printStackTrace();
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
        profileImage.setPadding(13, 13, 13, 13);
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

            @Override
            public void onPinClick(PastPaper paper) {
                togglePin(paper);
            }
        });

        recyclerUploads.setLayoutManager(new LinearLayoutManager(this));
        recyclerUploads.setAdapter(adapter);
    }

    private void showEditProfileDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        EditText nameInput = view.findViewById(R.id.edit_name);
        EditText intakeInput = view.findViewById(R.id.edit_intake);
        AutoCompleteTextView degreeDropdown = view.findViewById(R.id.edit_degree);
        View deleteBtn = view.findViewById(R.id.btn_delete_account);

        nameInput.setText(sessionManager.getFullName());
        intakeInput.setText(sessionManager.getIntake());
        degreeDropdown.setText(sessionManager.getDegreeName(), false);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        // Load Degrees for dropdown
        degreeRepository.fetchDegreesByUniversity(sessionManager.getUniversity(), new DataCallback<List<Degree>>() {
            @Override
            public void onSuccess(List<Degree> degrees) {
                allDegrees = degrees;
                List<String> names = new ArrayList<>();
                for (Degree d : degrees) names.add(d.getName());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(ProfileActivity.this,
                        android.R.layout.simple_list_item_1, names);
                degreeDropdown.setAdapter(adapter);

                degreeDropdown.setOnItemClickListener((parent, v, position, id) -> {
                    chosenDegree = allDegrees.get(position);
                });
            }
            @Override
            public void onError(Exception e) {}
        });

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String newName = nameInput.getText().toString().trim();
                String newIntake = intakeInput.getText().toString().trim();
                
                if (newName.isEmpty()) {
                    nameInput.setError(getString(R.string.error_required_field));
                    return;
                }

                updateFullProfile(newName, newIntake, chosenDegree);
                dialog.dismiss();
            });
        });

        deleteBtn.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteConfirmation();
        });

        dialog.show();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? Your details will be permanently removed after 30 days. You will receive a confirmation email within 72 hours.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteAccount() {
        String uid = sessionManager.getUid();
        authRepository.requestAccountDeletion(uid, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(ProfileActivity.this, "Account deletion requested. Check your email within 72 hours.", Toast.LENGTH_LONG).show();
                logoutAndGoToUniversitySelect();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ProfileActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutAndGoToUniversitySelect() {
        authRepository.logout();
        sessionManager.clear();
        Intent intent = new Intent(this, SelectUniversityActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void updateFullProfile(String name, String intake, Degree degree) {
        String uid = sessionManager.getUid();
        
        // Update Name and potentially image (null here means no change)
        authRepository.updateUserProfile(uid, name, null, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                nameView.setText(name);
                
                // Update Intake and Degree
                String dId = degree != null ? degree.getId() : sessionManager.getDegreeId();
                String dName = degree != null ? degree.getName() : sessionManager.getDegreeName();
                
                authRepository.updateUserDegree(uid, dId, dName, intake, new DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        intakeView.setText(getString(R.string.intake_label, intake));
                        degreeView.setText(dName);
                        Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onError(Exception e) {}
                });
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(ProfileActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            }
        });
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
                getString(R.string.in_app_chat)
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.contact_title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) contactEmail();
                    else if (which == 1) contactCall();
                    else if (which == 2) contactAdmin();
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

    private void contactAdmin() {
        authRepository.fetchUserProfile("admin_uid", new DataCallback<User>() {
            @Override
            public void onSuccess(User admin) {
                Intent intent = new Intent(ProfileActivity.this, com.bravem.app.ui.community.ChatActivity.class);
                intent.putExtra(com.bravem.app.ui.community.ChatActivity.EXTRA_USER, admin);
                startActivity(intent);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ProfileActivity.this, "Admin profile not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void togglePin(PastPaper paper) {
        paperRepository.togglePin(paper.getId(), new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isPinned) {
                loadMyUploads();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfilePicture(Uri uri) {
        String fileName = FileUtils.getFileName(this, uri);
        String localPath = FileUtils.copyFileToInternalStorage(this, uri, fileName);
        if (localPath != null) {
            String uid = sessionManager.getUid();
            authRepository.updateUserProfile(uid, sessionManager.getFullName(), localPath, new DataCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    profileImage.setImageURI(Uri.fromFile(new java.io.File(localPath)));
                    profileImage.setImageTintList(null); // Clear tint
                    profileImage.setPadding(0, 0, 0, 0);
                    profileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onError(Exception e) {
                    Toast.makeText(ProfileActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
        }
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
