package com.bravem.app.ui.dashboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
    private SwipeRefreshLayout swipeRefresh;
    private View emptyState;
    private MaterialButton browseButton;
    private View searchBar;
    private FloatingActionButton uploadFab;
    private View profileButton;
    private ImageView profileImage;

    private AuthRepository authRepository;
    private PaperRepository paperRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        authRepository = new AuthRepository(this);
        paperRepository = new PaperRepository(this);
        sessionManager = new SessionManager(this);

        welcomeText = findViewById(R.id.text_welcome);
        universityText = findViewById(R.id.text_university);
        degreeText = findViewById(R.id.text_degree);
        intakeText = findViewById(R.id.text_intake);
        recyclerRecent = findViewById(R.id.recycler_recent);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        emptyState = findViewById(R.id.empty_state);
        browseButton = findViewById(R.id.btn_browse_degrees);
        searchBar = findViewById(R.id.search_bar);
        uploadFab = findViewById(R.id.fab_upload);
        profileButton = findViewById(R.id.btn_profile);
        profileImage = findViewById(R.id.btn_profile);

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
        });

        recyclerRecent.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecent.setAdapter(recentAdapter);

        swipeRefresh.setOnRefreshListener(this::loadDashboardData);

        browseButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SelectDegreeActivity.class);
            intent.putExtra(SelectDegreeActivity.EXTRA_BROWSE_MODE, true);
            startActivity(intent);
        });

        searchBar.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, SearchActivity.class)));

        uploadFab.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, UploadPaperActivity.class)));

        profileButton.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class)));

        bindGreeting();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
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
                        profileImage.setPadding(0, 0, 0, 0);
                        profileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    } catch (Exception e) {
                        // If loading fails, keep default icon
                        profileImage.setImageResource(R.drawable.ic_person);
                        profileImage.setPadding(9, 9, 9, 9);
                    }
                }
            }
            @Override
            public void onError(Exception e) {}
        });
    }

    private String firstName(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 0 ? parts[0] : fullName;
    }

    private void loadDashboardData() {
        String degreeId = sessionManager.getDegreeId();
        if (degreeId == null) {
            swipeRefresh.setRefreshing(false);
            return;
        }

        swipeRefresh.setRefreshing(true);

        paperRepository.fetchPapersForDegree(degreeId, 20, new DataCallback<List<PastPaper>>() {
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
