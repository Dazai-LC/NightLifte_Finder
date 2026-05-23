package com.example.nightlife_finder.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nightlife_finder.R;

public class AdminImagesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#1A1A1F"));
        getWindow().setNavigationBarColor(Color.parseColor("#0F0F12"));
        setContentView(R.layout.activity_admin_images);

        findViewById(R.id.btnBackImages).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddImage).setOnClickListener(v -> 
            Toast.makeText(this, "Demo: Mở thư viện thêm ảnh", Toast.LENGTH_SHORT).show()
        );

        LinearLayout adminImagesList = findViewById(R.id.adminImagesList);

        adminImagesList.addView(createImageCard("Bún bò Đêm Phố Cổ", "Ảnh bìa"));
        adminImagesList.addView(createImageCard("Lẩu khuya Hoàn Kiếm", "Ảnh không gian"));
        adminImagesList.addView(createImageCard("Mini Mart Đêm Hà Nội", "Ảnh mặt tiền"));
    }

    private LinearLayout createImageCard(String placeName, String imageType) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundResource(R.drawable.admin_module_bg);
        card.setPadding(24, 24, 24, 24);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 24);
        card.setLayoutParams(params);

        // Placeholder Image
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.logo); // Using logo as placeholder
        imageView.setBackgroundColor(Color.parseColor("#242430"));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(160, 160);
        imageView.setLayoutParams(imgParams);
        card.addView(imageView);

        // Info layout
        LinearLayout infoLayout = new LinearLayout(this);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        infoLayout.setPadding(24, 0, 0, 0);
        infoLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvName = new TextView(this);
        tvName.setText(placeName);
        tvName.setTextColor(Color.WHITE);
        tvName.setTextSize(15);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        infoLayout.addView(tvName);

        TextView tvType = new TextView(this);
        tvType.setText("Loại: " + imageType);
        tvType.setTextColor(Color.parseColor("#9CA0AA"));
        tvType.setTextSize(13);
        tvType.setPadding(0, 4, 0, 16);
        infoLayout.addView(tvType);

        // Buttons layout
        LinearLayout btnLayout = new LinearLayout(this);
        btnLayout.setOrientation(LinearLayout.HORIZONTAL);

        btnLayout.addView(createButton("Đổi", "#00BFFF", 0));
        btnLayout.addView(createButton("Xóa", "#FF4444", 16));
        btnLayout.addView(createButton("Xem", "#242430", 16));

        infoLayout.addView(btnLayout);
        card.addView(infoLayout);

        return card;
    }

    private TextView createButton(String text, String bgColor, int marginStart) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(11);
        btn.setGravity(android.view.Gravity.CENTER);
        btn.setPadding(16, 8, 16, 8);
        
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        shape.setCornerRadius(12);
        shape.setColor(Color.parseColor(bgColor));
        btn.setBackground(shape);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(marginStart, 0, 0, 0);
        btn.setLayoutParams(params);
        
        btn.setOnClickListener(v -> Toast.makeText(this, "Demo: " + text, Toast.LENGTH_SHORT).show());

        return btn;
    }
}
