package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nightlife_finder.R;

public class ChatDetailActivity extends BaseActivity {

    private TextView detailEmoji;
    private TextView detailName;
    private TextView detailStatus;

    private TextView userMsg1;
    private TextView shopMsg1;
    private TextView userMsg2;
    private TextView shopMsg2;

    private EditText messageInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSystemBars();
        setContentView(R.layout.activity_chat_detail);

        bindViews();
        setupData();
        setupButtons();
        setupBottomNavigation();
    }

    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.parseColor("#1A1A1F"));
        getWindow().setNavigationBarColor(Color.parseColor("#1A1A1F"));
    }

    private void bindViews() {
        detailEmoji = findViewById(R.id.detailEmoji);
        detailName = findViewById(R.id.detailName);
        detailStatus = findViewById(R.id.detailStatus);

        userMsg1 = findViewById(R.id.userMsg1);
        shopMsg1 = findViewById(R.id.shopMsg1);
        userMsg2 = findViewById(R.id.userMsg2);
        shopMsg2 = findViewById(R.id.shopMsg2);

        messageInput = findViewById(R.id.messageInput);
    }

    private void setupData() {
        String chatId = getIntent().getStringExtra("CHAT_ID");

        if (chatId == null) {
            chatId = "bunbo";
        }

        switch (chatId) {
            case "lau":
                detailEmoji.setText("🍲");
                detailName.setText("Lẩu Thái Nửa Đêm");
                detailStatus.setText("● Đang mở");

                userMsg1.setText("Cho mình hỏi lẩu còn nhận khách không?");
                shopMsg1.setText("Dạ còn anh nhé, quán mở tới 3 giờ sáng 😊");
                userMsg2.setText("Menu có set hải sản không ạ?");
                shopMsg2.setText("Dạ có set hải sản cay Thái, đang giảm 15% hôm nay ạ.");
                break;

            case "bbq":
                detailEmoji.setText("🍢");
                detailName.setText("Nhậu và Có BBQ");
                detailStatus.setText("● Đang mở");

                userMsg1.setText("Quán còn bàn cho 4 người không?");
                shopMsg1.setText("Dạ còn bàn ngoài trời và trong nhà anh nhé.");
                userMsg2.setText("Có món nướng nào bán chạy nhất?");
                shopMsg2.setText("Best seller là xiên bò phô mai và ba chỉ nướng sốt cay ạ.");
                break;

            case "che":
                detailEmoji.setText("🍧");
                detailName.setText("Chè Khuya Hà Nội");
                detailStatus.setText("● Đang mở");

                userMsg1.setText("Chè khuya còn bán không bạn?");
                shopMsg1.setText("Dạ quán còn bán tới 1 giờ sáng anh nhé.");
                userMsg2.setText("Món nào đặc biệt hôm nay?");
                shopMsg2.setText("Hôm nay có chè khúc bạch và chè sầu riêng đặc biệt ạ.");
                break;

            case "pizza":
                detailEmoji.setText("🍕");
                detailName.setText("Pizza Midnight");
                detailStatus.setText("● Đang mở");

                userMsg1.setText("Pizza còn ship quanh Hoàn Kiếm không?");
                shopMsg1.setText("Dạ có anh nhé, khu Hoàn Kiếm khoảng 15 phút ạ.");
                userMsg2.setText("Có ưu đãi gì không?");
                shopMsg2.setText("Dạ đang có mua 1 tặng 1 size M tới 12 giờ đêm ạ.");
                break;

            default:
                detailEmoji.setText("🍜");
                detailName.setText("Bún Bò Huế 24h");
                detailStatus.setText("● Đang mở");

                userMsg1.setText("Cho mình hỏi quán còn chỗ không?");
                shopMsg1.setText("Xin chào! Quán còn 3 bàn trống anh ơi 😊");
                userMsg2.setText("Menu có món gì ngon không ạ?");
                shopMsg2.setText("Hiện tại quán còn chỗ trống, mở tới 2:00 AM nhé!");
                break;
        }
    }

    private void setupButtons() {
        findViewById(R.id.btnBackChat).setOnClickListener(v -> finish());

        findViewById(R.id.btnCallShop).setOnClickListener(v -> {
            Toast.makeText(this, "Demo gọi điện cho quán", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnVideoShop).setOnClickListener(v -> {
            Toast.makeText(this, "Demo gọi video cho quán", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnBooking).setOnClickListener(v -> {
            Toast.makeText(this, "Demo đặt bàn thành công", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnMenu).setOnClickListener(v -> {
            Toast.makeText(this, "Demo mở menu quán", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnDirection).setOnClickListener(v -> {
            Toast.makeText(this, "Demo chỉ đường tới quán", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnCallQuick).setOnClickListener(v -> {
            Toast.makeText(this, "Demo gọi nhanh cho quán", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnSendMessage).setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();

            if (text.isEmpty()) {
                Toast.makeText(this, "Bạn chưa nhập tin nhắn", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đã gửi tin nhắn demo: " + text, Toast.LENGTH_SHORT).show();
                messageInput.setText("");
            }
        });
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(ChatDetailActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        findViewById(R.id.navMap).setOnClickListener(v -> {
            startActivity(new Intent(ChatDetailActivity.this, MapActivity.class));
        });

        findViewById(R.id.navChat).setOnClickListener(v -> {
            Intent intent = new Intent(ChatDetailActivity.this, ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        findViewById(R.id.navFavorite).setOnClickListener(v -> {
            startActivity(new Intent(this, FavoriteActivity.class));
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }
}