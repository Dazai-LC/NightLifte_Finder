package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nightlife_finder.R;

public class NewChatActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSystemBars();
        setContentView(R.layout.activity_new_chat);

        setupButtons();
    }

    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.parseColor("#0F0F12"));
        getWindow().setNavigationBarColor(Color.parseColor("#0F0F12"));
    }

    private void setupButtons() {
        findViewById(R.id.btnCancelNewChat).setOnClickListener(v -> {
            finish();
        });

        findViewById(R.id.newChatBunBo).setOnClickListener(v -> {
            openChatDetail("bunbo");
        });

        findViewById(R.id.newChatLau).setOnClickListener(v -> {
            openChatDetail("lau");
        });

        findViewById(R.id.newChatBBQ).setOnClickListener(v -> {
            openChatDetail("bbq");
        });

        findViewById(R.id.newChatChe).setOnClickListener(v -> {
            openChatDetail("che");
        });

        findViewById(R.id.newChatPizza).setOnClickListener(v -> {
            openChatDetail("pizza");
        });
    }

    private void openChatDetail(String chatId) {
        Toast.makeText(this, "Đang mở cuộc trò chuyện mới", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(NewChatActivity.this, ChatDetailActivity.class);
        intent.putExtra("CHAT_ID", chatId);
        startActivity(intent);
    }
}