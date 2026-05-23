package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nightlife_finder.R;
import com.example.nightlife_finder.interfaces.OnChatActionListener;
import com.example.nightlife_finder.repositories.ChatRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NewChatActivity extends BaseActivity {

    private EditText searchShopInput;
    private EditText firstMessageInput;
    private EditText shopAvatarInput;
    private EditText shopCategoryInput;
    private EditText shopAddressInput;
    private EditText shopOpenTimeInput;
    private EditText shopCloseTimeInput;

    private ChatRepository chatRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSystemBars();
        setContentView(R.layout.activity_new_chat);

        chatRepository = new ChatRepository();

        bindViews();
        prefillFromIntent();
        setupButtons();
    }

    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.parseColor("#0F0F12"));
        getWindow().setNavigationBarColor(Color.parseColor("#0F0F12"));
    }

    private void bindViews() {
        searchShopInput   = findViewById(R.id.searchShopInput);
        firstMessageInput = findViewById(R.id.firstMessageInput);
        shopAvatarInput   = findViewById(R.id.shopAvatarInput);
        shopCategoryInput = findViewById(R.id.shopCategoryInput);
        shopAddressInput  = findViewById(R.id.shopAddressInput);
        shopOpenTimeInput = findViewById(R.id.shopOpenTimeInput);
        shopCloseTimeInput = findViewById(R.id.shopCloseTimeInput);
    }

    private void setupButtons() {
        // Nút Hủy
        findViewById(R.id.btnCancelNewChat).setOnClickListener(v -> finish());

        // Nút Tạo cuộc trò chuyện mới (Firestore)
        findViewById(R.id.btnCreateConversation).setOnClickListener(v -> handleCreateConversation());

        // Các shop mẫu vẫn giữ để demo nhanh (mở trực tiếp ChatDetailActivity với chatId hardcode)
        findViewById(R.id.newChatBunBo).setOnClickListener(v -> openDemoChat("bunbo", "Bún Bò Huế 24h"));
        findViewById(R.id.newChatLau).setOnClickListener(v -> openDemoChat("lau", "Lẩu Thái Nửa Đêm"));
        findViewById(R.id.newChatBBQ).setOnClickListener(v -> openDemoChat("bbq", "Nhậu và Có BBQ"));
        findViewById(R.id.newChatChe).setOnClickListener(v -> openDemoChat("che", "Chè Khuya Hà Nội"));
        findViewById(R.id.newChatPizza).setOnClickListener(v -> openDemoChat("pizza", "Pizza Midnight"));
    }

    // -------------------------------------------------------
    // Prefill form từ dữ liệu place (nếu được mở từ FavoriteActivity)
    // -------------------------------------------------------
    private void prefillFromIntent() {
        Intent intent = getIntent();
        String placeName    = intent.getStringExtra("PLACE_NAME");
        if (placeName == null || placeName.isEmpty()) return; // mở thủ công bình thường

        String placeCategory = intent.getStringExtra("PLACE_CATEGORY");
        String placeAddress  = intent.getStringExtra("PLACE_ADDRESS");
        String placeOpenTime = intent.getStringExtra("PLACE_OPEN_TIME");
        String placeImageUrl = intent.getStringExtra("PLACE_IMAGE_URL");

        // Điền tên quán
        if (searchShopInput != null)   searchShopInput.setText(placeName);

        // Điền category
        if (shopCategoryInput != null && placeCategory != null)
            shopCategoryInput.setText(placeCategory);

        // Điền địa chỉ
        if (shopAddressInput != null && placeAddress != null)
            shopAddressInput.setText(placeAddress);

        // Điền openTime: cố gắng parse giờ, nếu không parse được thì giữ nguyên chuỗi
        if (shopOpenTimeInput != null && placeOpenTime != null) {
            String openTimeVal = parseOpenTime(placeOpenTime);
            shopOpenTimeInput.setText(openTimeVal);
        }

        // Emoji avatar phù hợp với category
        if (shopAvatarInput != null)
            shopAvatarInput.setText(categoryToEmoji(placeImageUrl, placeCategory));
    }

    /**
     * Cố gắng trích xuất HH:MM từ chuỗi openTime kiểu "Mở đến 02:30".
     * Nếu không match thì trả về chuỗi gốc.
     */
    private String parseOpenTime(String raw) {
        if (raw == null) return "18:00";
        // Tìm pattern HH:MM trong chuỗi
        java.util.regex.Matcher m =
                java.util.regex.Pattern.compile("(\\d{1,2}:\\d{2})").matcher(raw);
        return m.find() ? m.group(1) : raw;
    }

    /** Chuyển imageUrl hoặc category sang emoji đại diện */
    private String categoryToEmoji(String imageUrl, String category) {
        String key = (imageUrl != null && !imageUrl.isEmpty()) ? imageUrl : category;
        if (key == null) return "💬";
        switch (key.toLowerCase()) {
            case "bunbo":   case "bún bò":  return "🍜";
            case "bbq":     case "nướng":   return "🌢";
            case "lau":     case "lẩu":     return "🍲";
            case "pizza":                   return "🍕";
            case "che":     case "trà sữa": return "🧋";
            case "bar":                     return "🍺";
            case "diner":   case "cơm":     return "🍚";
            case "phở":                     return "🍜";
            default:                        return "💬";
        }
    }

    // -------------------------------------------------------
    // Tạo conversation mới lưu Firestore
    // -------------------------------------------------------
    private void handleCreateConversation() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tin nhắn đầu tiên – bắt buộc
        String firstMsg = getText(firstMessageInput);
        if (TextUtils.isEmpty(firstMsg)) {
            Toast.makeText(this, "Vui lòng nhập tin nhắn đầu tiên", Toast.LENGTH_SHORT).show();
            if (firstMessageInput != null) firstMessageInput.requestFocus();
            return;
        }

        // Tên quán / tiêu đề – dùng default nếu rỗng
        String title = getText(searchShopInput);
        if (TextUtils.isEmpty(title)) title = "Cuộc trò chuyện mới";

        // Các field tùy chọn – dùng default nếu rỗng
        String avatarText = getText(shopAvatarInput);
        if (TextUtils.isEmpty(avatarText)) avatarText = "💬";

        String category = getText(shopCategoryInput);
        if (TextUtils.isEmpty(category)) category = "general";

        String address = getText(shopAddressInput);
        if (TextUtils.isEmpty(address)) address = "Chưa có địa chỉ";

        String openTime = getText(shopOpenTimeInput);
        if (TextUtils.isEmpty(openTime)) openTime = "18:00";

        String closeTime = getText(shopCloseTimeInput);
        if (TextUtils.isEmpty(closeTime)) closeTime = "02:30";

        // Lấy shopName = title nếu không có field riêng
        final String shopName = title;

        String uid   = currentUser.getUid();
        String email = currentUser.getEmail() != null ? currentUser.getEmail() : "";

        Toast.makeText(this, "Đang tạo cuộc trò chuyện...", Toast.LENGTH_SHORT).show();

        chatRepository.createConversation(
                title, uid, email, firstMsg,
                shopName, avatarText, category, address, openTime, closeTime,
                new OnChatActionListener() {
                    @Override
                    public void onSuccess(String conversationId) {
                        openConversation(conversationId);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(NewChatActivity.this,
                                "Lỗi tạo cuộc trò chuyện: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /** Lấy text đã trim từ EditText, trả về "" nếu null */
    private String getText(EditText et) {
        if (et == null) return "";
        return et.getText().toString().trim();
    }

    // -------------------------------------------------------
    // Mở ChatDetailActivity với conversationId từ Firestore
    // -------------------------------------------------------
    private void openConversation(String conversationId) {
        Intent intent = new Intent(NewChatActivity.this, ChatDetailActivity.class);
        intent.putExtra("CONVERSATION_ID", conversationId);
        startActivity(intent);
        finish();
    }

    // -------------------------------------------------------
    // Mở ChatDetailActivity với chatId hardcode (demo)
    // -------------------------------------------------------
    private void openDemoChat(String chatId, String shopName) {
        Toast.makeText(this, "Đang mở cuộc trò chuyện demo: " + shopName, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(NewChatActivity.this, ChatDetailActivity.class);
        intent.putExtra("CHAT_ID", chatId);
        startActivity(intent);
    }
}