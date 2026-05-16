package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;



import com.example.nightlife_finder.R;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.example.nightlife_finder.utils.AppSettings;

import java.io.File;

public class MainActivity extends BaseActivity {
    @Override
    protected void onResume() {
        super.onResume();
        applyGlobalUi();
    }

    private void loadHomeAvatar() {
        ImageView imgHomeAvatar = findViewById(R.id.imgHomeAvatar);

        if (imgHomeAvatar == null) return;

        String avatarPath = AppSettings.getAvatarPath(this);

        if (avatarPath != null && !avatarPath.isEmpty()) {
            File file = new File(avatarPath);

            if (file.exists()) {
                imgHomeAvatar.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                return;
            }
        }

        imgHomeAvatar.setImageResource(R.drawable.logo);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSystemBars();
        setContentView(R.layout.activity_main);

        setupBottomNavigation();
    }

    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.parseColor("#0F0F12"));
        getWindow().setNavigationBarColor(Color.parseColor("#1A1A1F"));
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Toast.makeText(this, "Bạn đang ở Trang chủ", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.navMap).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MapActivity.class));
        });

        findViewById(R.id.navChat).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ChatActivity.class));
        });

        findViewById(R.id.navFavorite).setOnClickListener(v -> {
            startActivity(new Intent(this, FavoriteActivity.class));
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }
}