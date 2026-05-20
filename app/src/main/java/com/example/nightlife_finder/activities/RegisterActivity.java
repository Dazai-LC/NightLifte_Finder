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
import com.example.nightlife_finder.utils.ValidationUtils;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends BaseActivity {

    private EditText etRegisterEmail;
    private EditText etRegisterPassword;
    private EditText etRegisterConfirmPassword;
    private TextView btnRegister;
    private TextView btnGotoLogin;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSystemBars();
        setContentView(R.layout.activity_register);

        authRepository = new AuthRepository();

        bindViews();
        setupListeners();
    }

    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.parseColor("#0F0F12"));
        getWindow().setNavigationBarColor(Color.parseColor("#1A1A1F"));
    }

    private void bindViews() {
        etRegisterEmail = findViewById(R.id.etRegisterEmail);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        etRegisterConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnGotoLogin = findViewById(R.id.btnGotoLogin);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> handleRegister());

        btnGotoLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void handleRegister() {
        String email = etRegisterEmail.getText().toString().trim();
        String password = etRegisterPassword.getText().toString().trim();
        String confirmPassword = etRegisterConfirmPassword.getText().toString().trim();

        String emailError = ValidationUtils.getEmailError(email);
        if (emailError != null) {
            etRegisterEmail.setError(emailError);
            etRegisterEmail.requestFocus();
            return;
        }

        String passwordError = ValidationUtils.getPasswordError(password);
        if (passwordError != null) {
            etRegisterPassword.setError(passwordError);
            etRegisterPassword.requestFocus();
            return;
        }

        String confirmPasswordError = ValidationUtils.getConfirmPasswordError(password, confirmPassword);
        if (confirmPasswordError != null) {
            etRegisterConfirmPassword.setError(confirmPasswordError);
            etRegisterConfirmPassword.requestFocus();
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Đang đăng ký...");

        authRepository.register(email, password, new OnAuthListener() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Đăng ký tài khoản");
                Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Hãy đăng nhập.", Toast.LENGTH_LONG).show();

                // Redirect to Login screen
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Đăng ký tài khoản");
                Toast.makeText(RegisterActivity.this, "Lỗi đăng ký: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
