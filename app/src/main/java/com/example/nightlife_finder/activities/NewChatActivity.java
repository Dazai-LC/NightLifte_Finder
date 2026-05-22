package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nightlife_finder.R;
import com.example.nightlife_finder.interfaces.OnChatActionListener;
import com.example.nightlife_finder.repositories.ChatRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NewChatActivity extends BaseActivity {

    private EditText searchShopInput;
    private EditText firstMessageInput;

    private ChatRepository chatRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSystemBars();
        setContentView(R.layout.activity_new_chat);

        chatRepository = new ChatRepository();

        bindViews();
        setupButtons();
    }

    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.parseColor("#0F0F12"));
        getWindow().setNavigationBarColor(Color.parseColor("#0F0F12"));
    }

    private void bindViews() {
        searchShopInput = findViewById(R.id.searchShopInput);
        firstMessageInput = findViewById(R.id.firstMessageInput);
    }

    private void setupButtons() {
        // Nút Hủy
        findViewById(R.id.btnCancelNewChat).setOnClickListener(v -> finish());

        // Nút Tạo cuộc trò chuyện mới (Firestore)
        findViewById(R.id.btnCreateConversation).setOnClickListener(v -> handleCreateConversation());

        // Các shop mẫu vẫn giữ để demo nhanh (mở trực tiếp ChatDetailActivity với chatId hardcode)
        findViewById(R.id.newChatBunBo).setOnClickListener(v -> openDemoChat("bunbo", "Bún Bò Huế 24h"));
        findViewById(R.id.newChatLau).setOnClickListener(v -> openDemoChat("lau", "Lẩu Thái Nửa Đêm"));
        findViewById(R.id.newChatBBQ).setOnClickListener(v -> openDemoChat("bbq", "Nhậu và Có BBQ"));
        findViewById(R.id.newChatChe).setOnClickListener(v -> openDemoChat("che", "Chè Khuya Hà Nội"));
        findViewById(R.id.newChatPizza).setOnClickListener(v -> openDemoChat("pizza", "Pizza Midnight"));
    }

    // -------------------------------------------------------
    // Tạo conversation mới lưu Firestore
    // -------------------------------------------------------
    private void handleCreateConversation() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = searchShopInput.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            searchShopInput.setError("Vui lòng nhập tiêu đề");
            searchShopInput.requestFocus();
            return;
        }

        String firstMsg = firstMessageInput != null
                ? firstMessageInput.getText().toString().trim()
                : "";

        String uid = currentUser.getUid();
        String email = currentUser.getEmail() != null ? currentUser.getEmail() : "";

        Toast.makeText(this, "Đang tạo cuộc trò chuyện...", Toast.LENGTH_SHORT).show();

        chatRepository.createConversation(title, uid, email, firstMsg, new OnChatActionListener() {
            @Override
            public void onSuccess(String conversationId) {
                openConversation(conversationId);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(NewChatActivity.this,
                        "Lỗi tạo cuộc trò chuyện: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // -------------------------------------------------------
    // Mở ChatDetailActivity với conversationId từ Firestore
    // -------------------------------------------------------
    private void openConversation(String conversationId) {
        Intent intent = new Intent(NewChatActivity.this, ChatDetailActivity.class);
        intent.putExtra("CONVERSATION_ID", conversationId);
        startActivity(intent);
        finish();
    }

    // -------------------------------------------------------
    // Mở ChatDetailActivity với chatId hardcode (demo)
    // -------------------------------------------------------
    private void openDemoChat(String chatId, String shopName) {
        Toast.makeText(this, "Đang mở cuộc trò chuyện demo: " + shopName, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(NewChatActivity.this, ChatDetailActivity.class);
        intent.putExtra("CHAT_ID", chatId);
        startActivity(intent);
    }
}