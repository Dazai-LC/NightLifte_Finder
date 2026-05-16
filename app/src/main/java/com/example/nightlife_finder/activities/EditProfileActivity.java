package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.nightlife_finder.R;
import com.example.nightlife_finder.utils.AppSettings;

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

    private ActivityResultLauncher<String> pickAvatarLauncher;
    private ActivityResultLauncher<Intent> cropAvatarLauncher;

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

    private void bindViews() {
        imgEditAvatar = findViewById(R.id.imgEditAvatar);

        inputName = findViewById(R.id.inputName);
        inputBirthday = findViewById(R.id.inputBirthday);
        inputPhone = findViewById(R.id.inputPhone);
        inputEmail = findViewById(R.id.inputEmail);
        inputLocation = findViewById(R.id.inputLocation);
        inputBio = findViewById(R.id.inputBio);

        rowGender = findViewById(R.id.rowGender);
    }

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

    private void loadData() {
        inputName.setText(AppSettings.getProfileName(this));
        inputBirthday.setText(AppSettings.getProfileBirthday(this));
        inputPhone.setText(AppSettings.getProfilePhone(this));
        inputEmail.setText(AppSettings.getProfileEmail(this));
        inputLocation.setText(AppSettings.getProfileLocation(this));
        inputBio.setText(AppSettings.getProfileBio(this));

        rowGender.setText(AppSettings.getProfileGender(this) + "                                      ›");

        loadAvatar();
    }

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

    private void setupButtons() {
        findViewById(R.id.btnBackEditProfile).setOnClickListener(v -> finish());

        findViewById(R.id.btnChangeAvatar).setOnClickListener(v -> {
            pickAvatarLauncher.launch("image/*");
        });

        findViewById(R.id.txtChangeAvatar).setOnClickListener(v -> {
            pickAvatarLauncher.launch("image/*");
        });

        rowGender.setOnClickListener(v -> {
            String current = AppSettings.getProfileGender(this);

            if (current.equals("Nam")) {
                AppSettings.setProfileGender(this, "Nữ");
            } else {
                AppSettings.setProfileGender(this, "Nam");
            }

            rowGender.setText(AppSettings.getProfileGender(this) + "                                      ›");
        });

        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> {
            saveProfile();
        });

        findViewById(R.id.btnDeleteAccount).setOnClickListener(v -> {
            Toast.makeText(this, "Demo xóa tài khoản", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveProfile() {
        String name = inputName.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập họ và tên", Toast.LENGTH_SHORT).show();
            return;
        }

        AppSettings.setProfileName(this, name);
        AppSettings.setProfileBirthday(this, inputBirthday.getText().toString().trim());
        AppSettings.setProfilePhone(this, inputPhone.getText().toString().trim());
        AppSettings.setProfileEmail(this, inputEmail.getText().toString().trim());
        AppSettings.setProfileLocation(this, inputLocation.getText().toString().trim());
        AppSettings.setProfileBio(this, inputBio.getText().toString().trim());

        Toast.makeText(this, "Đã lưu hồ sơ", Toast.LENGTH_SHORT).show();
        finish();
    }
}