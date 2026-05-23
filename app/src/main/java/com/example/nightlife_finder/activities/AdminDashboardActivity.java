package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.nightlife_finder.R;

public class AdminDashboardActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#0F0F12"));
        getWindow().setNavigationBarColor(Color.parseColor("#1A1A1F"));

        setContentView(R.layout.activity_admin_dashboard);

        findViewById(R.id.btnBackDashboard).setOnClickListener(v -> finish());

        LinearLayout cardModulePlaces = findViewById(R.id.cardModulePlaces);
        LinearLayout cardModuleHours = findViewById(R.id.cardModuleHours);
        LinearLayout cardModuleImages = findViewById(R.id.cardModuleImages);
        LinearLayout cardModuleReviews = findViewById(R.id.cardModuleReviews);

        cardModulePlaces.setOnClickListener(v -> startActivity(new Intent(this, AdminPlacesActivity.class)));
        cardModuleHours.setOnClickListener(v -> startActivity(new Intent(this, AdminOpeningHoursActivity.class)));
        cardModuleImages.setOnClickListener(v -> startActivity(new Intent(this, AdminImagesActivity.class)));
        cardModuleReviews.setOnClickListener(v -> startActivity(new Intent(this, AdminReviewsActivity.class)));
    }
}
