package com.bravem.app.ui.community;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.bravem.app.model.User;
import com.bravem.app.utils.SessionManager;
import com.bravem.app.utils.UiUtils;

import java.util.List;

public class ChatActivity extends AppCompatActivity implements MessageAdapter.OnMessageClickListener {
    public static final String EXTRA_USER = "extra_user";

    private User otherUser;
    private ChatRepository chatRepository;
    private SessionManager sessionManager;
    private MessageAdapter adapter;
    private RecyclerView recyclerView;
    private EditText etMessage;
    private Handler pollHandler = new Handler(Looper.getMainLooper());
    private Runnable pollRunnable;

    private ActivityResultLauncher<String> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        UiUtils.handleTopInset(findViewById(R.id.layout_header));
        UiUtils.handleBottomInset(findViewById(android.R.id.content));

        otherUser = (User) getIntent().getSerializableExtra(EXTRA_USER);
        if (otherUser == null) {
            finish();
            return;
        }

        chatRepository = new ChatRepository(this);
        sessionManager = new SessionManager(this);

        ((TextView) findViewById(R.id.text_title)).setText(otherUser.getFullName());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        etMessage = findViewById(R.id.et_message);
        recyclerView = findViewById(R.id.recycler_messages);
        adapter = new MessageAdapter(sessionManager.getUid(), this);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btn_send).setOnClickListener(v -> sendMessage());
        
        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), this::handleFileSelected);
        findViewById(R.id.btn_attach).setOnClickListener(v -> filePickerLauncher.launch("*/*"));

        loadHistory();
        startPolling();
    }

    private void loadHistory() {
        chatRepository.getChatHistory(otherUser.getUid(), new DataCallback<List<ChatMessage>>() {
            @Override
            public void onSuccess(List<ChatMessage> messages) {
                adapter.submitList(messages);
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                chatRepository.markAsRead(otherUser.getUid());
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ChatActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        chatRepository.sendMessage(otherUser.getUid(), text, null, null, new DataCallback<ChatMessage>() {
            @Override
            public void onSuccess(ChatMessage message) {
                adapter.addMessage(message);
                etMessage.setText("");
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFileSelected(Uri uri) {
        if (uri == null) return;
        
        String type = getContentResolver().getType(uri);
        String label = type != null && type.contains("pdf") ? "PDF" : "Document";
        String originalName = com.bravem.app.utils.FileUtils.getFileName(this, uri);
        
        // Copy to internal storage so other user (in this local shared DB) can access it as a file path
        String localPath = com.bravem.app.utils.FileUtils.copyFileToInternalStorage(this, uri, originalName);
        
        if (localPath == null) {
            Toast.makeText(this, "Failed to process file", Toast.LENGTH_SHORT).show();
            return;
        }

        chatRepository.sendMessage(otherUser.getUid(), "Shared a " + label, localPath, label, new DataCallback<ChatMessage>() {
            @Override
            public void onSuccess(ChatMessage message) {
                adapter.addMessage(message);
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ChatActivity.this, "Failed to share file", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startPolling() {
        pollRunnable = new Runnable() {
            @Override
            public void run() {
                loadHistory();
                pollHandler.postDelayed(this, 3000); // Poll every 3 seconds
            }
        };
        pollHandler.postDelayed(pollRunnable, 3000);
    }

    @Override
    public void onAttachmentClick(ChatMessage message) {
        if (message.getAttachmentPath() != null) {
            String path = message.getAttachmentPath();
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            
            // If it's the UUID-prefixed name, it already has the extension
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
        super.onDestroy();
        pollHandler.removeCallbacks(pollRunnable);
    }
}
