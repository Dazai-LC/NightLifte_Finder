package com.example.nightlife_finder.activities;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nightlife_finder.R;

public class AdminOpeningHoursActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#1A1A1F"));
        getWindow().setNavigationBarColor(Color.parseColor("#0F0F12"));
        setContentView(R.layout.activity_admin_opening_hours);

        findViewById(R.id.btnBackHours).setOnClickListener(v -> finish());

        LinearLayout adminHoursList = findViewById(R.id.adminHoursList);

        adminHoursList.addView(createHourCard("Bún bò Đêm Phố Cổ", "18:00 - 04:00"));
        adminHoursList.addView(createHourCard("Mini Mart Đêm Hà Nội", "00:00 - 24:00"));
        adminHoursList.addView(createHourCard("Lẩu khuya Hoàn Kiếm", "19:00 - 05:00"));
    }

    private LinearLayout createHourCard(String name, String currentHours) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundResource(R.drawable.admin_module_bg);
        card.setPadding(32, 32, 32, 32);
        card.setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 24);
        card.setLayoutParams(params);

        LinearLayout infoLayout = new LinearLayout(this);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        infoLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextColor(Color.WHITE);
        tvName.setTextSize(16);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        infoLayout.addView(tvName);

        TextView tvHours = new TextView(this);
        tvHours.setText("Đang mở: " + currentHours);
        tvHours.setTextColor(Color.parseColor("#00BFFF"));
        tvHours.setTextSize(13);
        tvHours.setPadding(0, 8, 0, 0);
        infoLayout.addView(tvHours);

        card.addView(infoLayout);

        TextView btnUpdate = new TextView(this);
        btnUpdate.setText("Cập nhật");
        btnUpdate.setTextColor(Color.WHITE);
        btnUpdate.setTextSize(12);
        btnUpdate.setPadding(32, 16, 32, 16);
        
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        shape.setCornerRadius(16);
        shape.setColor(Color.parseColor("#A020F0"));
        btnUpdate.setBackground(shape);

        btnUpdate.setOnClickListener(v -> showUpdateDialog(name));

        card.addView(btnUpdate);

        return card;
    }

    private void showUpdateDialog(String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cập nhật giờ: " + name);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        EditText etOpen = new EditText(this);
        etOpen.setHint("Giờ mở (VD: 18:00)");
        layout.addView(etOpen);

        EditText etClose = new EditText(this);
        etClose.setHint("Giờ đóng (VD: 04:00)");
        layout.addView(etClose);

        builder.setView(layout);
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            Toast.makeText(this, "Đã cập nhật (Demo)", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
}
