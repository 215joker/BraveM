package com.bravem.app.ui.community;

import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.adapter.MessageAdapter;
import com.bravem.app.data.ChatRepository;
import com.bravem.app.data.DataCallback;
import com.bravem.app.model.ChatMessage;
import com.bravem.app.model.Group;
import com.bravem.app.model.User;
import com.bravem.app.utils.SessionManager;
import com.bravem.app.utils.UiUtils;

import java.util.List;

public class GroupChatActivity extends AppCompatActivity implements MessageAdapter.OnMessageClickListener {
    public static final String EXTRA_GROUP = "extra_group";

    private Group group;
    private ChatRepository chatRepository;
    private SessionManager sessionManager;
    private MessageAdapter adapter;
    private RecyclerView recyclerView;
    private EditText etMessage;

    private ActivityResultLauncher<String> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat); // Reusing activity_chat layout

        UiUtils.handleTopInset(findViewById(R.id.app_bar));
        UiUtils.handleBottomInset(findViewById(R.id.chat_root));

        group = (Group) getIntent().getSerializableExtra(EXTRA_GROUP);
        if (group == null) {
            finish();
            return;
        }

        chatRepository = new ChatRepository(this);
        sessionManager = new SessionManager(this);

        ((TextView) findViewById(R.id.text_title)).setText(group.getName());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        etMessage = findViewById(R.id.et_message);
        recyclerView = findViewById(R.id.recycler_messages);
        adapter = new MessageAdapter(sessionManager.getUid(), this);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                recyclerView.postDelayed(() -> {
                    if (adapter.getItemCount() > 0) {
                        recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                    }
                }, 100);
            }
        });

        findViewById(R.id.btn_send).setOnClickListener(v -> sendMessage());
        findViewById(R.id.btn_video_call).setOnClickListener(v -> startVideoCall());
        
        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), this::handleFileSelected);
        findViewById(R.id.btn_attach).setOnClickListener(v -> filePickerLauncher.launch("*/*"));

        startListening();
    }

    private void startListening() {
        chatRepository.startListeningToGroup(group.getId(), new DataCallback<List<ChatMessage>>() {
            @Override
            public void onSuccess(List<ChatMessage> messages) {
                adapter.submitList(messages);
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }

            @Override
            public void onError(Exception e) {
                // Silently fail or log
            }
        });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        chatRepository.sendGroupMessage(group.getId(), text, null, null, new DataCallback<ChatMessage>() {
            @Override
            public void onSuccess(ChatMessage message) {
                etMessage.setText("");
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(GroupChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startVideoCall() {
        android.content.Intent intent = new android.content.Intent(this, StudyRoomActivity.class);
        intent.putExtra(StudyRoomActivity.EXTRA_CHANNEL_ID, group.getId());
        // For groups, we can pass a dummy user or handle it differently in StudyRoomActivity
        User dummyUser = new User();
        dummyUser.setFullName(group.getName());
        intent.putExtra(StudyRoomActivity.EXTRA_USER, dummyUser);
        startActivity(intent);
    }

    private void handleFileSelected(Uri uri) {
        if (uri == null) return;
        
        String type = getContentResolver().getType(uri);
        String label = type != null && type.contains("pdf") ? "PDF" : "Document";
        String originalName = com.bravem.app.utils.FileUtils.getFileName(this, uri);
        
        String localPath = com.bravem.app.utils.FileUtils.copyFileToInternalStorage(this, uri, originalName);
        
        if (localPath == null) {
            Toast.makeText(this, "Failed to process file", Toast.LENGTH_SHORT).show();
            return;
        }

        chatRepository.sendGroupMessage(group.getId(), "Shared a " + label, localPath, label, new DataCallback<ChatMessage>() {
            @Override
            public void onSuccess(ChatMessage message) {
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(GroupChatActivity.this, "Failed to share file", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAttachmentClick(ChatMessage message) {
        if (message.getAttachmentPath() != null) {
            String path = message.getAttachmentPath();
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            
            long downloadId = com.bravem.app.utils.FileUtils.downloadFile(this, path, fileName);
            if (downloadId != -1) {
                Toast.makeText(this, "Downloading " + fileName + "...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (chatRepository != null) {
            chatRepository.stopListening();
        }
        super.onDestroy();
    }
}
