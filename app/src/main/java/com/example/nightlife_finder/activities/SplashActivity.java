package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nightlife_finder.R;
import com.example.nightlife_finder.repositories.AuthRepository;

public class SplashActivity extends AppCompatActivity {

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        authRepository = new AuthRepository();

        android.view.View llContent = findViewById(R.id.llCenterContent);
        if (llContent != null) {
            // Apply a premium fade-in animation to the entire centered content
            Animation anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            anim.setDuration(1200);
            llContent.startAnimation(anim);
        }

        // Delay for 2.5 seconds, then transition depending on login status
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (authRepository.isLoggedIn()) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            // Apply a smooth premium transition animation
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2500);
    }
}
