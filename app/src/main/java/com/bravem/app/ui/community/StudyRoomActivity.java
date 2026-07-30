package com.bravem.app.ui.community;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bravem.app.R;
import com.bravem.app.data.CommunityRepository;
import com.bravem.app.data.DataCallback;
import com.bravem.app.data.NotificationRepository;
import com.bravem.app.data.VideoCallRepository;
import com.bravem.app.domain.model.User;
import com.bravem.app.utils.SessionManager;
import com.bravem.app.utils.UiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.ScreenCaptureParameters;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;

public class StudyRoomActivity extends AppCompatActivity {

    public static final String EXTRA_CHANNEL_ID = "extra_channel_id";
    public static final String EXTRA_USER = "extra_user";
    private static final int PERMISSION_REQ_ID = 22;
    private static final String TEMP_TOKEN = "007eJxTYJD0eR2gXVp8eJvmBPaXlx4m/Hu/4KDgwnQBgY9iWpLRN8oUGEwsjC0t0kzT0owsE00SjUwTUwxNk42SUi0MjUyMDVNMF3dlZDUEMjJc0/nFwMTACIYgPhuDU1FiWaovA1QUJMbCYGhgYAgAj94huA==";
    private static final int RTC_UID = 0; // 0 means let Agora assign

    private String channelId;
    private User remoteUser;
    private VideoCallRepository videoCallRepository;
    private FrameLayout localContainer;
    private GridLayout remoteVideoGrid;
    private View remotePlaceholder;
    private FrameLayout fullScreenContainer;
    private boolean isMuted = false;
    private boolean isVideoDisabled = false;
    private boolean isScreenSharing = false;

    private final Map<Integer, View> remoteViews = new HashMap<>();
    private final Map<Integer, String> uidToUserUidMap = new HashMap<>();
    private Integer focusedUid = null;

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            runOnUiThread(() -> Toast.makeText(StudyRoomActivity.this, "Joined Channel: " + channel, Toast.LENGTH_SHORT).show());
        }

        @Override
        public void onUserInfoUpdated(int uid, io.agora.rtc2.UserInfo userInfo) {
            runOnUiThread(() -> {
                if (userInfo != null && userInfo.userAccount != null) {
                    uidToUserUidMap.put(uid, userInfo.userAccount);
                }
            });
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(() -> {
                remotePlaceholder.setVisibility(View.GONE);
                setupRemoteVideo(uid);
            });
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            runOnUiThread(() -> removeRemoteVideo(uid));
        }

        @Override
        public void onError(int err) {
            Log.e("StudyRoomActivity", "Agora Error: " + err);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Use standard window behavior to ensure status bar is visible
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_room);

        // Ensure status bar is visible and icons are light (since background is dark)
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        channelId = getIntent().getStringExtra(EXTRA_CHANNEL_ID);
        remoteUser = (User) getIntent().getSerializableExtra(EXTRA_USER);
        
        if (channelId == null) {
            finish();
            return;
        }

        localContainer = findViewById(R.id.local_video_view_container);
        remoteVideoGrid = findViewById(R.id.remote_video_grid);
        remotePlaceholder = findViewById(R.id.remote_placeholder);
        fullScreenContainer = findViewById(R.id.full_screen_video_container);

        if (remoteUser != null) {
            ((TextView) findViewById(R.id.text_remote_name)).setText(remoteUser.getFullName());
        }

        findViewById(R.id.btn_end_call).setOnClickListener(v -> finish());
        findViewById(R.id.btn_mute).setOnClickListener(v -> toggleMute());
        findViewById(R.id.btn_switch_camera).setOnClickListener(v -> switchCamera());
        findViewById(R.id.btn_video_toggle).setOnClickListener(v -> toggleVideo());
        findViewById(R.id.btn_screen_share).setOnClickListener(v -> toggleScreenShare());
        findViewById(R.id.btn_add_friend).setOnClickListener(v -> showInviteDialog());

        videoCallRepository = new VideoCallRepository(this);
        setupDraggableLocalVideo();

        if (checkSelfPermission()) {
            initAgoraAndJoin();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA}, PERMISSION_REQ_ID);
        }
    }

    private boolean checkSelfPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initAgoraAndJoin();
            } else {
                finish();
            }
        }
    }

    private void initAgoraAndJoin() {
        videoCallRepository.initEngine(mRtcEventHandler);
        if (videoCallRepository.getEngine() != null) {
            setupLocalVideo();
            SessionManager session = new SessionManager(this);
            videoCallRepository.getEngine().joinChannelWithUserAccount(TEMP_TOKEN, "BraveM", session.getUid());
        }
    }

    private void setupLocalVideo() {
        SurfaceView surfaceView = new SurfaceView(this);
        surfaceView.setZOrderMediaOverlay(true);
        localContainer.removeAllViews();
        localContainer.addView(surfaceView);
        videoCallRepository.getEngine().setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
        videoCallRepository.startPreview();
    }

    private void setupRemoteVideo(int uid) {
        if (remoteViews.containsKey(uid)) return;

        SurfaceView surfaceView = new SurfaceView(this);
        remoteViews.put(uid, surfaceView);
        
        surfaceView.setOnClickListener(v -> toggleFullScreen(uid));
        
        updateRemoteVideoGrid();
        videoCallRepository.getEngine().setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
    }

    private void removeRemoteVideo(int uid) {
        remoteViews.remove(uid);
        if (focusedUid != null && focusedUid == uid) {
            focusedUid = null;
            fullScreenContainer.setVisibility(View.GONE);
            remoteVideoGrid.setVisibility(View.VISIBLE);
        }
        updateRemoteVideoGrid();
        if (remoteViews.isEmpty()) {
            remotePlaceholder.setVisibility(View.VISIBLE);
        }
    }

    private void updateRemoteVideoGrid() {
        remoteVideoGrid.removeAllViews();
        int count = remoteViews.size();
        if (count == 0) return;

        remoteVideoGrid.setColumnCount(count > 1 ? 2 : 1);
        remoteVideoGrid.setRowCount(count > 2 ? 2 : 1);

        for (View view : remoteViews.values()) {
            if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
            }
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            view.setLayoutParams(params);
            remoteVideoGrid.addView(view);
        }
    }

    private void toggleFullScreen(int uid) {
        if (focusedUid != null && focusedUid == uid) {
            // Unfocus
            focusedUid = null;
            fullScreenContainer.removeAllViews();
            fullScreenContainer.setVisibility(View.GONE);
            remoteVideoGrid.setVisibility(View.VISIBLE);
            updateRemoteVideoGrid();
        } else {
            // Focus
            focusedUid = uid;
            View view = remoteViews.get(uid);
            if (view != null) {
                if (view.getParent() != null) {
                    ((ViewGroup) view.getParent()).removeView(view);
                }
                remoteVideoGrid.setVisibility(View.GONE);
                fullScreenContainer.setVisibility(View.VISIBLE);
                fullScreenContainer.removeAllViews();
                fullScreenContainer.addView(view);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupDraggableLocalVideo() {
        View localCard = findViewById(R.id.local_video_card);
        localCard.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;
                        View parent = (View) view.getParent();
                        newX = Math.max(0, Math.min(newX, parent.getWidth() - view.getWidth()));
                        newY = Math.max(0, Math.min(newY, parent.getHeight() - view.getHeight()));
                        view.setX(newX);
                        view.setY(newY);
                        break;
                    default: return false;
                }
                return true;
            }
        });
    }

    private void toggleMute() {
        isMuted = !isMuted;
        videoCallRepository.getEngine().muteLocalAudioStream(isMuted);
        com.google.android.material.button.MaterialButton btn = findViewById(R.id.btn_mute);
        btn.setIconResource(isMuted ? R.drawable.ic_mic_off : R.drawable.ic_mic);
        btn.setBackgroundColor(ContextCompat.getColor(this, isMuted ? R.color.error_600 : R.color.ink_500));
    }

    private void switchCamera() {
        videoCallRepository.getEngine().switchCamera();
    }

    private void toggleVideo() {
        isVideoDisabled = !isVideoDisabled;
        videoCallRepository.getEngine().muteLocalVideoStream(isVideoDisabled);
        findViewById(R.id.local_video_card).setVisibility(isVideoDisabled ? View.GONE : View.VISIBLE);
        com.google.android.material.button.MaterialButton btn = findViewById(R.id.btn_video_toggle);
        btn.setIconResource(isVideoDisabled ? R.drawable.ic_video_off : R.drawable.ic_video);
        btn.setBackgroundColor(ContextCompat.getColor(this, isVideoDisabled ? R.color.error_600 : R.color.ink_500));
    }

    private void toggleScreenShare() {
        if (!isScreenSharing) {
            startScreenShare();
        } else {
            stopScreenShare();
        }
    }

    private void startScreenShare() {
        if (videoCallRepository.getEngine() == null) return;

        ScreenCaptureParameters params = new ScreenCaptureParameters();
        params.captureVideo = true;
        params.videoCaptureParameters = new ScreenCaptureParameters.VideoCaptureParameters();
        params.videoCaptureParameters.width = 720;
        params.videoCaptureParameters.height = 1280;
        params.videoCaptureParameters.framerate = 15;
        
        params.captureAudio = true;
        params.audioCaptureParameters = new ScreenCaptureParameters.AudioCaptureParameters();
        params.audioCaptureParameters.captureSignalVolume = 100;
        
        videoCallRepository.getEngine().setScreenCaptureScenario(Constants.ScreenScenarioType.SCREEN_SCENARIO_DOCUMENT);
        int result = videoCallRepository.getEngine().startScreenCapture(params);
        
        if (result == 0) {
            io.agora.rtc2.ChannelMediaOptions options = new io.agora.rtc2.ChannelMediaOptions();
            options.publishCameraTrack = false;
            options.publishScreenCaptureVideo = true;
            options.publishScreenCaptureAudio = true;
            videoCallRepository.getEngine().updateChannelMediaOptions(options);
            
            isScreenSharing = true;
            findViewById(R.id.btn_screen_share).setBackgroundColor(ContextCompat.getColor(this, R.color.amber_600));
            findViewById(R.id.local_video_card).setVisibility(View.GONE);
        } else {
            Toast.makeText(this, "Failed to start screen share", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopScreenShare() {
        videoCallRepository.getEngine().stopScreenCapture();
        videoCallRepository.getEngine().updateChannelMediaOptions(new io.agora.rtc2.ChannelMediaOptions() {{
            publishCameraTrack = true;
            publishScreenCaptureVideo = false;
            publishScreenCaptureAudio = false;
        }});
        
        isScreenSharing = false;
        findViewById(R.id.btn_screen_share).setBackgroundColor(ContextCompat.getColor(this, R.color.ink_500));
        if (!isVideoDisabled) findViewById(R.id.local_video_card).setVisibility(View.VISIBLE);
    }

    private void showInviteDialog() {
        CommunityRepository communityRepo = new CommunityRepository(this);
        communityRepo.getFriends(new DataCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> friends) {
                if (friends.isEmpty()) {
                    Toast.makeText(StudyRoomActivity.this, "No friends to invite", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                showInviteFriendsBottomSheet(friends);
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(StudyRoomActivity.this, "Failed to load friends", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showInviteFriendsBottomSheet(List<User> friends) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_invite_friends, null);
        androidx.recyclerview.widget.RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_invite_friends);
        View btnDone = dialogView.findViewById(R.id.btn_done);

        java.util.Set<String> inCallUserIds = new java.util.HashSet<>(uidToUserUidMap.values());
        SessionManager session = new SessionManager(this);
        inCallUserIds.add(session.getUid());

        com.bravem.app.adapter.InviteFriendAdapter adapter = new com.bravem.app.adapter.InviteFriendAdapter(
                friends, inCallUserIds, this::inviteFriend);
        
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        dialog.setContentView(dialogView);
        
        // Semi-circular corners for the bottom sheet
        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            bottomSheet.setBackgroundResource(R.drawable.bg_card_default);
            bottomSheet.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    getResources().getColor(R.color.parchment_50)));
        }

        btnDone.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void inviteFriend(User friend) {
        NotificationRepository notifRepo = new NotificationRepository(this);
        SessionManager session = new SessionManager(this);
        notifRepo.addNotification(
                "Study Room Invite",
                session.getFullName() + " invited you to join a study room.",
                "video_call_invite",
                friend.getUid(),
                channelId
        );
        Toast.makeText(this, "Invite sent to " + friend.getFullName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoCallRepository != null) {
            videoCallRepository.leaveChannel();
            videoCallRepository.destroy();
        }
    }
}
