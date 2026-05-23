package com.example.nightlife_finder.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nightlife_finder.R;

public class AdminPlacesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#1A1A1F"));
        getWindow().setNavigationBarColor(Color.parseColor("#0F0F12"));
        setContentView(R.layout.activity_admin_places);

        findViewById(R.id.btnBackPlaces).setOnClickListener(v -> finish());

        LinearLayout adminPlacesList = findViewById(R.id.adminPlacesList);

        // Add Mock Data
        adminPlacesList.addView(createPlaceCard("Bún bò Đêm Phố Cổ", "Quán ăn", "Hàng Buồm, Hoàn Kiếm", "18:00 - 04:00", "Đang mở", "#00BFFF"));
        adminPlacesList.addView(createPlaceCard("Mini Mart Đêm Hà Nội", "Siêu thị", "Tôn Đức Thắng, Đống Đa", "00:00 - 24:00", "Đang mở", "#00BFFF"));
        adminPlacesList.addView(createPlaceCard("Trà sữa 24h Phố Cổ", "Đồ uống", "Tràng Tiền, Hoàn Kiếm", "08:00 - 23:00", "Đã đóng", "#FFB800"));
        adminPlacesList.addView(createPlaceCard("Phở Gà Đêm Hàng Bạc", "Quán ăn", "Hàng Bạc, Hoàn Kiếm", "20:00 - 03:00", "Đang mở", "#00BFFF"));
    }

    private LinearLayout createPlaceCard(String name, String category, String address, String hours, String status, String statusColor) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.admin_module_bg);
        card.setPadding(32, 32, 32, 32);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 24);
        card.setLayoutParams(params);

        // Header (Name + Status)
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextColor(Color.WHITE);
        tvName.setTextSize(16);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        header.addView(tvName);

        TextView tvStatus = new TextView(this);
        tvStatus.setText(status);
        tvStatus.setTextColor(Color.parseColor(statusColor));
        tvStatus.setTextSize(12);
        tvStatus.setTypeface(null, android.graphics.Typeface.BOLD);
        header.addView(tvStatus);
        card.addView(header);

        // Info
        TextView tvCat = new TextView(this);
        tvCat.setText("Loại: " + category);
        tvCat.setTextColor(Color.parseColor("#9CA0AA"));
        tvCat.setTextSize(13);
        tvCat.setPadding(0, 8, 0, 0);
        card.addView(tvCat);

        TextView tvAddr = new TextView(this);
        tvAddr.setText("Địa chỉ: " + address);
        tvAddr.setTextColor(Color.parseColor("#9CA0AA"));
        tvAddr.setTextSize(13);
        card.addView(tvAddr);

        TextView tvHours = new TextView(this);
        tvHours.setText("Giờ mở cửa: " + hours);
        tvHours.setTextColor(Color.parseColor("#9CA0AA"));
        tvHours.setTextSize(13);
        card.addView(tvHours);

        // Buttons
        LinearLayout btnLayout = new LinearLayout(this);
        btnLayout.setOrientation(LinearLayout.HORIZONTAL);
        btnLayout.setPadding(0, 24, 0, 0);

        btnLayout.addView(createButton("Sửa", "#A020F0"));
        btnLayout.addView(createButton("Xem", "#242430"));
        btnLayout.addView(createButton("Tạm khóa", "#FF4444"));

        card.addView(btnLayout);

        return card;
    }

    private TextView createButton(String text, String bgColor) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(12);
        btn.setGravity(android.view.Gravity.CENTER);
        
        // Simple shape
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        shape.setCornerRadius(16);
        shape.setColor(Color.parseColor(bgColor));
        btn.setBackground(shape);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 80, 1);
        params.setMargins(0, 0, text.equals("Tạm khóa") ? 0 : 16, 0);
        btn.setLayoutParams(params);
        
        btn.setOnClickListener(v -> Toast.makeText(this, "Demo: " + text, Toast.LENGTH_SHORT).show());

        return btn;
    }
}
