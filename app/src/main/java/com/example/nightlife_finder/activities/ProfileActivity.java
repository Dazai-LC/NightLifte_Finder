package com.example.nightlife_finder.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.nightlife_finder.R;
import com.example.nightlife_finder.constants.FirebaseConstants;
import com.example.nightlife_finder.utils.AppSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.util.List;
import java.util.Set;

public class ProfileActivity extends BaseActivity {

    private static final String TAG = "ProfileActivity";

    private ImageView imgProfileAvatar;
    private TextView txtProfileName;
    private TextView txtProfileLocation;
    private Switch switchDarkMode;
    private TextView rowLanguage;

    // Stat chips
    private TextView chipFavorites;
    private TextView chipConversations;
    private TextView chipHistory;

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
        loadProfileFromFirestore();
        loadAvatar();
        loadStats();
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

        chipFavorites = findViewById(R.id.chipFavorites);
        chipConversations = findViewById(R.id.chipConversations);
        chipHistory = findViewById(R.id.chipHistory);
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

        loadProfileFromFirestore();
        loadAvatar();
        loadStats();
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

    // -------------------------------------------------------
    // Load real user data from FirebaseAuth + Firestore
    // -------------------------------------------------------
    private void loadProfileFromFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            txtProfileName.setText("Người dùng NightLife");
            txtProfileLocation.setText("Chưa cập nhật");
            updateLanguageRow();
            return;
        }

        // Set email-based fallback immediately
        String email = currentUser.getEmail();
        txtProfileName.setText("Người dùng NightLife");
        txtProfileLocation.setText("Chưa cập nhật");
        updateLanguageRow();

        // Load from Firestore
        String uid = currentUser.getUid();
        FirebaseFirestore.getInstance()
                .collection(FirebaseConstants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String displayName = doc.getString(FirebaseConstants.FIELD_DISPLAY_NAME);
                        String location = doc.getString(FirebaseConstants.FIELD_LOCATION);

                        if (displayName != null && !displayName.isEmpty()) {
                            txtProfileName.setText(displayName);
                        }
                        if (location != null && !location.isEmpty()) {
                            txtProfileLocation.setText(location);
                        }

                        // Cache to SharedPreferences
                        if (displayName != null) AppSettings.setProfileName(this, displayName);
                        if (location != null) AppSettings.setProfileLocation(this, location);

                        String phone = doc.getString(FirebaseConstants.FIELD_PHONE);
                        String bio = doc.getString(FirebaseConstants.FIELD_BIO);
                        if (phone != null) AppSettings.setProfilePhone(this, phone);
                        if (bio != null) AppSettings.setProfileBio(this, bio);
                        if (email != null) AppSettings.setProfileEmail(this, email);
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Failed to load profile from Firestore", e));
    }

    private void updateLanguageRow() {
        if (AppSettings.getLanguage(this).equals("vi")) {
            rowLanguage.setText("Ngôn ngữ                                      Tiếng Việt  ▾");
        } else {
            rowLanguage.setText("Language                                      English  ▾");
        }
    }

    // -------------------------------------------------------
    // Load real stats
    // -------------------------------------------------------
    private void loadStats() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            chipFavorites.setText("0 Yêu thích");
            chipConversations.setText("0 Trò chuyện");
            chipHistory.setText("0 Đã xem");
            return;
        }

        String uid = currentUser.getUid();

        // 1. Favorites count from Firestore users/{uid}.favorites
        FirebaseFirestore.getInstance()
                .collection(FirebaseConstants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    int favCount = 0;
                    if (doc.exists()) {
                        List<String> favorites = (List<String>) doc.get(FirebaseConstants.FIELD_FAVORITES);
                        if (favorites != null) {
                            favCount = favorites.size();
                        }
                    }
                    chipFavorites.setText(favCount + " Yêu thích");
                })
                .addOnFailureListener(e -> chipFavorites.setText("0 Yêu thích"));

        // 2. Conversations count from Firestore conversations where createdBy == uid
        FirebaseFirestore.getInstance()
                .collection(FirebaseConstants.COLLECTION_CONVERSATIONS)
                .whereEqualTo("createdBy", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int convCount = querySnapshot != null ? querySnapshot.size() : 0;
                    chipConversations.setText(convCount + " Trò chuyện");
                })
                .addOnFailureListener(e -> chipConversations.setText("0 Trò chuyện"));

        // 3. History count from SharedPreferences
        SharedPreferences histPrefs = getSharedPreferences("nightlife_history", MODE_PRIVATE);
        Set<String> recentIds = histPrefs.getStringSet("recent_place_ids", new java.util.HashSet<>());
        int historyCount = recentIds != null ? recentIds.size() : 0;
        chipHistory.setText(historyCount + " Đã xem");
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

    // -------------------------------------------------------
    // Button listeners
    // -------------------------------------------------------
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

        // --- Expansion features – AlertDialog thay vì Toast demo ---
        findViewById(R.id.cardNotification).setOnClickListener(v -> showExpansionDialog("Thông báo"));
        findViewById(R.id.cardVoucher).setOnClickListener(v -> showExpansionDialog("Voucher"));
        findViewById(R.id.cardSupport).setOnClickListener(v -> showExpansionDialog("Hỗ trợ"));
        findViewById(R.id.rowAddress).setOnClickListener(v -> showExpansionDialog("Địa chỉ của tôi"));
        findViewById(R.id.rowSocial).setOnClickListener(v -> showExpansionDialog("Liên kết mạng xã hội"));

        // --- Đổi mật khẩu thật ---
        findViewById(R.id.rowPassword).setOnClickListener(v -> handlePasswordReset());

        // --- Đăng xuất ---
        findViewById(R.id.rowLogout).setOnClickListener(v -> {
            new com.example.nightlife_finder.repositories.AuthRepository().logout();
            Toast.makeText(this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // -------------------------------------------------------
    // Đổi mật khẩu thật bằng sendPasswordResetEmail
    // -------------------------------------------------------
    private void handlePasswordReset() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null || currentUser.getEmail() == null || currentUser.getEmail().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy email tài khoản.", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = currentUser.getEmail();

        new AlertDialog.Builder(this)
                .setTitle("Đổi mật khẩu")
                .setMessage("Gửi email đặt lại mật khẩu đến:\n" + email + "?")
                .setPositiveButton("Gửi", (dialog, which) -> {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnSuccessListener(unused -> {
                                new AlertDialog.Builder(ProfileActivity.this)
                                        .setTitle("Thành công")
                                        .setMessage("Đã gửi email đặt lại mật khẩu đến " + email + ".\nVui lòng kiểm tra hộp thư.")
                                        .setPositiveButton("OK", null)
                                        .show();
                            })
                            .addOnFailureListener(e -> {
                                new AlertDialog.Builder(ProfileActivity.this)
                                        .setTitle("Lỗi")
                                        .setMessage("Không thể gửi email đặt lại mật khẩu: " + e.getMessage())
                                        .setPositiveButton("OK", null)
                                        .show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // -------------------------------------------------------
    // AlertDialog cho các chức năng mở rộng
    // -------------------------------------------------------
    private void showExpansionDialog(String featureName) {
        new AlertDialog.Builder(this)
                .setTitle(featureName)
                .setMessage("Chức năng \"" + featureName + "\" nằm trong phạm vi mở rộng.\n\nPhiên bản hiện tại tập trung vào tài khoản, yêu thích, lịch sử và trò chuyện.")
                .setPositiveButton("Đã hiểu", null)
                .show();
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