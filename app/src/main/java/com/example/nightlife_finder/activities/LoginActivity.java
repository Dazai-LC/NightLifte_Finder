package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nightlife_finder.R;
import com.example.nightlife_finder.interfaces.OnAuthListener;
import com.example.nightlife_finder.repositories.AuthRepository;
import com.example.nightlife_finder.utils.AppSettings;
import com.example.nightlife_finder.utils.ValidationUtils;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends BaseActivity {

    private EditText etLoginEmail;
    private EditText etLoginPassword;
    private TextView btnLogin;
    private TextView btnGotoRegister;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSystemBars();
        setContentView(R.layout.activity_login);

        authRepository = new AuthRepository();

        bindViews();
        setupListeners();
    }

    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.parseColor("#0F0F12"));
        getWindow().setNavigationBarColor(Color.parseColor("#1A1A1F"));
    }

    private void bindViews() {
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGotoRegister = findViewById(R.id.btnGotoRegister);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());

        btnGotoRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void handleLogin() {
        String email = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        String emailError = ValidationUtils.getEmailError(email);
        if (emailError != null) {
            etLoginEmail.setError(emailError);
            etLoginEmail.requestFocus();
            return;
        }

        String passwordError = ValidationUtils.getPasswordError(password);
        if (passwordError != null) {
            etLoginPassword.setError(passwordError);
            etLoginPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");

        authRepository.login(email, password, new OnAuthListener() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Đăng nhập");
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                // Save email or username to local app settings
                String username = email.split("@")[0];
                AppSettings.setProfileName(LoginActivity.this, username);
                AppSettings.setProfileLocation(LoginActivity.this, "Hà Nội, Việt Nam");

                // Launch main screen
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Đăng nhập");
                Toast.makeText(LoginActivity.this, "Lỗi đăng nhập: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
