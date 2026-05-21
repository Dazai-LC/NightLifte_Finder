package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.nightlife_finder.R;
import com.example.nightlife_finder.utils.AppSettings;

import java.io.File;

public class ProfileActivity extends BaseActivity {

    private ImageView imgProfileAvatar;
    private TextView txtProfileName;
    private TextView txtProfileLocation;
    private Switch switchDarkMode;
    private TextView rowLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSystemBars();
        setContentView(R.layout.activity_profile);

        bindViews();
        setupProfileValues();
        setupProfileButtons();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileInfo();
        loadAvatar();
        applyGlobalUi();
    }

    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.parseColor("#0F0F12"));
        getWindow().setNavigationBarColor(Color.parseColor("#1A1A1F"));
    }

    private void bindViews() {
        imgProfileAvatar = findViewById(R.id.imgProfileAvatar);
        txtProfileName = findViewById(R.id.txtProfileName);
        txtProfileLocation = findViewById(R.id.txtProfileLocation);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        rowLanguage = findViewById(R.id.rowLanguage);
    }

    private void setupProfileValues() {
        switchDarkMode.setChecked(AppSettings.isDarkMode(this));

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppSettings.setDarkMode(ProfileActivity.this, isChecked);

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            restartApp();
        });

        rowLanguage.setOnClickListener(v -> showLanguagePopup());

        loadProfileInfo();
        loadAvatar();
    }

    private void showLanguagePopup() {
        PopupMenu popupMenu = new PopupMenu(this, rowLanguage);

        popupMenu.getMenu().add("Tiếng Việt");
        popupMenu.getMenu().add("English");

        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();

            if (title.equals("Tiếng Việt")) {
                AppSettings.setLanguage(ProfileActivity.this, "vi");
            } else {
                AppSettings.setLanguage(ProfileActivity.this, "en");
            }

            restartApp();
            return true;
        });

        popupMenu.show();
    }

    private void loadProfileInfo() {
        txtProfileName.setText(AppSettings.getProfileName(this));
        txtProfileLocation.setText(AppSettings.getProfileLocation(this));

        if (AppSettings.getLanguage(this).equals("vi")) {
            rowLanguage.setText("Ngôn ngữ                                      Tiếng Việt  ▾");
        } else {
            rowLanguage.setText("Language                                      English  ▾");
        }
    }

    private void loadAvatar() {
        String avatarPath = AppSettings.getAvatarPath(this);

        if (avatarPath != null && !avatarPath.isEmpty()) {
            File file = new File(avatarPath);

            if (file.exists()) {
                imgProfileAvatar.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                return;
            }
        }

        imgProfileAvatar.setImageResource(R.drawable.logo);
    }

    private void setupProfileButtons() {
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        });

        findViewById(R.id.profileAvatarCard).setOnClickListener(v -> {
            Toast.makeText(this, "Vào Chỉnh sửa hồ sơ để đổi ảnh đại diện", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.cardHistory).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, FavoriteActivity.class));
        });

        findViewById(R.id.cardNotification).setOnClickListener(v -> {
            Toast.makeText(this, "Demo mở thông báo", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.cardVoucher).setOnClickListener(v -> {
            Toast.makeText(this, "Demo mở voucher", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.cardSupport).setOnClickListener(v -> {
            Toast.makeText(this, "Demo mở hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.rowAddress).setOnClickListener(v -> {
            Toast.makeText(this, "Demo địa chỉ của tôi", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.rowPassword).setOnClickListener(v -> {
            Toast.makeText(this, "Demo đổi mật khẩu", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.rowSocial).setOnClickListener(v -> {
            Toast.makeText(this, "Demo liên kết mạng xã hội", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.rowLogout).setOnClickListener(v -> {
            new com.example.nightlife_finder.repositories.AuthRepository().logout();
            Toast.makeText(this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void restartApp() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        findViewById(R.id.navMap).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, MapActivity.class));
        });

        findViewById(R.id.navChat).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, ChatActivity.class));
        });

        findViewById(R.id.navFavorite).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, FavoriteActivity.class));
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            Toast.makeText(this, "Bạn đang ở trang Hồ sơ", Toast.LENGTH_SHORT).show();
        });
    }
}