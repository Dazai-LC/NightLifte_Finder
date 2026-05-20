package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.nightlife_finder.R;
import com.example.nightlife_finder.utils.AppSettings;
import com.example.nightlife_finder.utils.ValidationUtils;

import java.io.File;

public class EditProfileActivity extends BaseActivity {

    private ImageView imgEditAvatar;

    private EditText inputName;
    private EditText inputBirthday;
    private EditText inputPhone;
    private EditText inputEmail;
    private EditText inputLocation;
    private EditText inputBio;

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
    // Load existing data into fields
    // -------------------------------------------------------
    private void loadData() {
        inputName.setText(AppSettings.getProfileName(this));
        inputBirthday.setText(AppSettings.getProfileBirthday(this));
        inputPhone.setText(AppSettings.getProfilePhone(this));
        inputEmail.setText(AppSettings.getProfileEmail(this));
        inputLocation.setText(AppSettings.getProfileLocation(this));
        inputBio.setText(AppSettings.getProfileBio(this));

        rowGender.setText(AppSettings.getProfileGender(this) + "                                      ›");

        // 2-step verification toggle
        switchTwoFactor.setChecked(AppSettings.isTwoFactorEnabled(this));

        loadAvatar();
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

        // Delete account (demo)
        findViewById(R.id.btnDeleteAccount).setOnClickListener(v ->
                showDeleteAccountConfirmation());
    }

    // -------------------------------------------------------
    // Save profile to SharedPreferences
    // -------------------------------------------------------
    private void saveProfile() {
        String name  = inputName.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();

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

        // Validate email (optional but validated if filled)
        if (!email.isEmpty() && !ValidationUtils.isValidEmail(email)) {
            inputEmail.setError("Định dạng email không hợp lệ");
            inputEmail.requestFocus();
            return;
        }

        // Persist all fields
        AppSettings.setProfileName(this, name);
        AppSettings.setProfileBirthday(this, inputBirthday.getText().toString().trim());
        AppSettings.setProfilePhone(this, phone);
        AppSettings.setProfileEmail(this, email);
        AppSettings.setProfileLocation(this, inputLocation.getText().toString().trim());
        AppSettings.setProfileBio(this, inputBio.getText().toString().trim());

        Toast.makeText(this, "✅ Đã lưu hồ sơ thành công", Toast.LENGTH_SHORT).show();
        finish();
    }

    // -------------------------------------------------------
    // Delete account – demo confirmation dialog
    // -------------------------------------------------------
    private void showDeleteAccountConfirmation() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Xóa tài khoản")
                .setMessage("Bạn có chắc muốn xóa tài khoản? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) ->
                        Toast.makeText(this, "Demo: Tài khoản đã được xóa.", Toast.LENGTH_SHORT).show())
                .setNegativeButton("Hủy", null)
                .show();
    }
}