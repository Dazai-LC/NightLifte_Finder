package com.example.nightlife_finder.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.nightlife_finder.R;
import com.example.nightlife_finder.constants.FirebaseConstants;
import com.example.nightlife_finder.utils.AppSettings;
import com.example.nightlife_finder.utils.ValidationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends BaseActivity {

    private static final String TAG = "EditProfileActivity";

    private ImageView imgEditAvatar;

    private EditText inputName;
    private EditText inputBirthday;
    private EditText inputPhone;
    private EditText inputEmail;
    private EditText inputLocation;
    private EditText inputBio;
    private EditText inputFacebook;
    private EditText inputInstagram;
    private EditText inputZalo;

    private TextView rowGender;
    private Switch   switchTwoFactor;

    private ActivityResultLauncher<String>  pickAvatarLauncher;
    private ActivityResultLauncher<Intent>  cropAvatarLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.parseColor("#0F0F12"));
        getWindow().setNavigationBarColor(Color.parseColor("#1A1A1F"));

        setContentView(R.layout.activity_edit_profile);

        bindViews();
        setupAvatarPickers();
        loadData();
        setupButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAvatar();
    }

    // -------------------------------------------------------
    // Bind views
    // -------------------------------------------------------
    private void bindViews() {
        imgEditAvatar   = findViewById(R.id.imgEditAvatar);

        inputName       = findViewById(R.id.inputName);
        inputBirthday   = findViewById(R.id.inputBirthday);
        inputPhone      = findViewById(R.id.inputPhone);
        inputEmail      = findViewById(R.id.inputEmail);
        inputLocation   = findViewById(R.id.inputLocation);
        inputBio        = findViewById(R.id.inputBio);
        inputFacebook   = findViewById(R.id.inputFacebook);
        inputInstagram  = findViewById(R.id.inputInstagram);
        inputZalo       = findViewById(R.id.inputZalo);

        rowGender       = findViewById(R.id.rowGender);
        switchTwoFactor = findViewById(R.id.switchTwoFactor);
    }

    // -------------------------------------------------------
    // Avatar pickers
    // -------------------------------------------------------
    private void setupAvatarPickers() {
        pickAvatarLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Intent intent = new Intent(EditProfileActivity.this, AvatarCropActivity.class);
                        intent.putExtra(AvatarCropActivity.EXTRA_IMAGE_URI, uri.toString());
                        cropAvatarLauncher.launch(intent);
                    }
                }
        );

        cropAvatarLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> loadAvatar()
        );
    }

    // -------------------------------------------------------
    // Load existing data – Firestore first, SharedPreferences fallback
    // -------------------------------------------------------
    private void loadData() {
        // Set email from FirebaseAuth (read-only)
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            inputEmail.setText(currentUser.getEmail());
        }
        inputEmail.setEnabled(false); // Email không cho sửa
        inputEmail.setAlpha(0.6f);

        // Load local fallback values first (instant display)
        inputName.setText(AppSettings.getProfileName(this));
        inputBirthday.setText(AppSettings.getProfileBirthday(this));
        inputPhone.setText(AppSettings.getProfilePhone(this));
        inputLocation.setText(AppSettings.getProfileLocation(this));
        inputBio.setText(AppSettings.getProfileBio(this));

        rowGender.setText(AppSettings.getProfileGender(this) + "                                      ›");

        // 2-step verification toggle
        switchTwoFactor.setChecked(AppSettings.isTwoFactorEnabled(this));

        loadAvatar();

        // Now override with Firestore data (source of truth)
        if (currentUser != null) {
            String uid = currentUser.getUid();
            FirebaseFirestore.getInstance()
                    .collection(FirebaseConstants.COLLECTION_USERS)
                    .document(uid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String displayName = doc.getString(FirebaseConstants.FIELD_DISPLAY_NAME);
                            String phone = doc.getString(FirebaseConstants.FIELD_PHONE);
                            String location = doc.getString(FirebaseConstants.FIELD_LOCATION);
                            String bio = doc.getString(FirebaseConstants.FIELD_BIO);
                            String facebook = doc.getString(FirebaseConstants.FIELD_FACEBOOK_URL);
                            String instagram = doc.getString(FirebaseConstants.FIELD_INSTAGRAM_URL);
                            String zalo = doc.getString(FirebaseConstants.FIELD_ZALO_CONTACT);

                            if (displayName != null && !displayName.isEmpty()) {
                                inputName.setText(displayName);
                            }
                            if (phone != null && !phone.isEmpty()) {
                                inputPhone.setText(phone);
                            }
                            if (location != null && !location.isEmpty()) {
                                inputLocation.setText(location);
                            }
                            if (bio != null && !bio.isEmpty()) {
                                inputBio.setText(bio);
                            }
                            if (facebook != null && !facebook.isEmpty()) {
                                inputFacebook.setText(facebook);
                            }
                            if (instagram != null && !instagram.isEmpty()) {
                                inputInstagram.setText(instagram);
                            }
                            if (zalo != null && !zalo.isEmpty()) {
                                inputZalo.setText(zalo);
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Log.w(TAG, "Failed to load profile from Firestore", e));
        }
    }

    // -------------------------------------------------------
    // Load avatar from local storage
    // -------------------------------------------------------
    private void loadAvatar() {
        String avatarPath = AppSettings.getAvatarPath(this);

        if (avatarPath != null && !avatarPath.isEmpty()) {
            File file = new File(avatarPath);

            if (file.exists()) {
                imgEditAvatar.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                return;
            }
        }

        imgEditAvatar.setImageResource(R.drawable.logo);
    }

    // -------------------------------------------------------
    // Setup button listeners
    // -------------------------------------------------------
    private void setupButtons() {
        // Back
        findViewById(R.id.btnBackEditProfile).setOnClickListener(v -> finish());

        // Change avatar – camera button
        findViewById(R.id.btnChangeAvatar).setOnClickListener(v ->
                pickAvatarLauncher.launch("image/*"));

        // Change avatar – text link
        findViewById(R.id.txtChangeAvatar).setOnClickListener(v ->
                pickAvatarLauncher.launch("image/*"));

        // Gender toggle (cycle Nam → Nữ → Khác → Nam …)
        rowGender.setOnClickListener(v -> {
            String current = AppSettings.getProfileGender(this);
            String next;

            switch (current) {
                case "Nam":   next = "Nữ";   break;
                case "Nữ":   next = "Khác"; break;
                default:     next = "Nam";  break;
            }

            AppSettings.setProfileGender(this, next);
            rowGender.setText(next + "                                      ›");
        });

        // 2-step verification switch
        switchTwoFactor.setOnCheckedChangeListener((button, isChecked) -> {
            AppSettings.setTwoFactorEnabled(this, isChecked);
            String msg = isChecked
                    ? "Đã bật xác minh 2 bước"
                    : "Đã tắt xác minh 2 bước";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // Save
        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> saveProfile());

        // Delete account – safe warning only
        findViewById(R.id.btnDeleteAccount).setOnClickListener(v ->
                showDeleteAccountConfirmation());
    }

    // -------------------------------------------------------
    // Save profile to Firestore (merge) + SharedPreferences cache
    // -------------------------------------------------------
    private void saveProfile() {
        String name     = inputName.getText().toString().trim();
        String phone    = inputPhone.getText().toString().trim();
        String location = inputLocation.getText().toString().trim();
        String bio      = inputBio.getText().toString().trim();

        // Validate name
        if (ValidationUtils.isEmpty(name)) {
            inputName.setError("Vui lòng nhập họ và tên");
            inputName.requestFocus();
            return;
        }

        // Validate phone (optional but validated if filled)
        if (!phone.isEmpty() && !ValidationUtils.isValidPhoneNumber(phone)) {
            inputPhone.setError("Số điện thoại không hợp lệ (10 chữ số, bắt đầu 0)");
            inputPhone.requestFocus();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy tài khoản đăng nhập.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();

        // Build map for Firestore merge – chỉ ghi các field profile, không ghi đè favorites
        Map<String, Object> profileData = new HashMap<>();
        profileData.put(FirebaseConstants.FIELD_DISPLAY_NAME, name);
        profileData.put(FirebaseConstants.FIELD_PHONE, phone);
        profileData.put(FirebaseConstants.FIELD_LOCATION, location);
        profileData.put(FirebaseConstants.FIELD_BIO, bio);
        profileData.put(FirebaseConstants.FIELD_FACEBOOK_URL, inputFacebook.getText().toString().trim());
        profileData.put(FirebaseConstants.FIELD_INSTAGRAM_URL, inputInstagram.getText().toString().trim());
        profileData.put(FirebaseConstants.FIELD_ZALO_CONTACT, inputZalo.getText().toString().trim());

        FirebaseFirestore.getInstance()
                .collection(FirebaseConstants.COLLECTION_USERS)
                .document(uid)
                .set(profileData, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    // Cache to SharedPreferences
                    AppSettings.setProfileName(EditProfileActivity.this, name);
                    AppSettings.setProfilePhone(EditProfileActivity.this, phone);
                    AppSettings.setProfileLocation(EditProfileActivity.this, location);
                    AppSettings.setProfileBio(EditProfileActivity.this, bio);

                    // Also persist birthday locally (not in Firestore for now)
                    AppSettings.setProfileBirthday(EditProfileActivity.this,
                            inputBirthday.getText().toString().trim());

                    Toast.makeText(EditProfileActivity.this,
                            "✅ Đã lưu hồ sơ thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save profile to Firestore", e);
                    Toast.makeText(EditProfileActivity.this,
                            "Lỗi lưu hồ sơ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // -------------------------------------------------------
    // Delete account – safe warning, no real deletion
    // -------------------------------------------------------
    private void showDeleteAccountConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa tài khoản")
                .setMessage("Xóa tài khoản thật chưa được bật trong bản demo để tránh mất dữ liệu.\n\nNếu bạn cần xóa tài khoản, vui lòng liên hệ đội ngũ phát triển.")
                .setPositiveButton("Đã hiểu", null)
                .show();
    }
}