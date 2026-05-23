package com.example.nightlife_finder.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nightlife_finder.R;

public class AdminReviewsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#1A1A1F"));
        getWindow().setNavigationBarColor(Color.parseColor("#0F0F12"));
        setContentView(R.layout.activity_admin_reviews);

        findViewById(R.id.btnBackReviews).setOnClickListener(v -> finish());

        LinearLayout adminReviewsList = findViewById(R.id.adminReviewsList);

        adminReviewsList.addView(createReviewCard("User A", "Bún bò Đêm Phố Cổ", "⭐⭐⭐⭐⭐", "Đồ ăn ngon, phục vụ nhanh", "1 giờ trước", "Hiển thị", "#00BFFF"));
        adminReviewsList.addView(createReviewCard("User B", "Mini Mart Đêm Hà Nội", "⭐⭐⭐⭐", "Mở muộn rất tiện", "3 giờ trước", "Chờ duyệt", "#FFB800"));
        adminReviewsList.addView(createReviewCard("User C", "Trà sữa 24h Phố Cổ", "⭐⭐⭐", "Không gian ổn, giá hợp lý", "1 ngày trước", "Đã ẩn", "#FF4444"));
    }

    private LinearLayout createReviewCard(String user, String place, String stars, String content, String time, String status, String statusColor) {
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

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        
        TextView tvUser = new TextView(this);
        tvUser.setText(user);
        tvUser.setTextColor(Color.WHITE);
        tvUser.setTextSize(15);
        tvUser.setTypeface(null, android.graphics.Typeface.BOLD);
        tvUser.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        header.addView(tvUser);

        TextView tvStatus = new TextView(this);
        tvStatus.setText(status);
        tvStatus.setTextColor(Color.parseColor(statusColor));
        tvStatus.setTextSize(12);
        tvStatus.setTypeface(null, android.graphics.Typeface.BOLD);
        header.addView(tvStatus);
        
        card.addView(header);

        // Place and Stars
        TextView tvPlace = new TextView(this);
        tvPlace.setText("Tại: " + place);
        tvPlace.setTextColor(Color.parseColor("#9CA0AA"));
        tvPlace.setTextSize(13);
        tvPlace.setPadding(0, 4, 0, 4);
        card.addView(tvPlace);

        TextView tvStars = new TextView(this);
        tvStars.setText(stars);
        tvStars.setTextSize(12);
        tvStars.setPadding(0, 0, 0, 8);
        card.addView(tvStars);

        // Content
        TextView tvContent = new TextView(this);
        tvContent.setText("\"" + content + "\"");
        tvContent.setTextColor(Color.WHITE);
        tvContent.setTextSize(14);
        tvContent.setPadding(0, 0, 0, 8);
        card.addView(tvContent);

        // Time
        TextView tvTime = new TextView(this);
        tvTime.setText(time);
        tvTime.setTextColor(Color.parseColor("#9CA0AA"));
        tvTime.setTextSize(12);
        card.addView(tvTime);

        // Buttons
        LinearLayout btnLayout = new LinearLayout(this);
        btnLayout.setOrientation(LinearLayout.HORIZONTAL);
        btnLayout.setPadding(0, 24, 0, 0);

        btnLayout.addView(createButton("Duyệt", "#00BFFF"));
        btnLayout.addView(createButton("Ẩn", "#FF4444"));
        btnLayout.addView(createButton("Xem", "#242430"));

        card.addView(btnLayout);

        return card;
    }

    private TextView createButton(String text, String bgColor) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(12);
        btn.setGravity(android.view.Gravity.CENTER);
        
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        shape.setCornerRadius(16);
        shape.setColor(Color.parseColor(bgColor));
        btn.setBackground(shape);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 80, 1);
        params.setMargins(0, 0, text.equals("Xem") ? 0 : 16, 0);
        btn.setLayoutParams(params);
        
        btn.setOnClickListener(v -> Toast.makeText(this, "Demo: " + text, Toast.LENGTH_SHORT).show());

        return btn;
    }
}
