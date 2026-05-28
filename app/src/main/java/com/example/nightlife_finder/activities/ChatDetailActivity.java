package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nightlife_finder.R;
import com.example.nightlife_finder.interfaces.OnChatActionListener;
import com.example.nightlife_finder.interfaces.OnMessageLoadedListener;
import com.example.nightlife_finder.models.ChatMessage;
import com.example.nightlife_finder.repositories.ChatRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatDetailActivity extends BaseActivity {

    // -------------------------------------------------------
    // Views – header
    // -------------------------------------------------------
    private TextView detailEmoji;
    private TextView detailName;
    private TextView detailStatus;

    // -------------------------------------------------------
    // Views – hardcode demo messages (dùng khi không có conversationId)
    // -------------------------------------------------------
    private TextView userMsg1;
    private TextView shopMsg1;
    private TextView userMsg2;
    private TextView shopMsg2;

    // -------------------------------------------------------
    // Views – input & scroll
    // -------------------------------------------------------
    private EditText messageInput;
    private ScrollView messageScroll;
    private LinearLayout messageContainer; // container bên trong ScrollView

    // -------------------------------------------------------
    // Data
    // -------------------------------------------------------
    private String conversationId;   // null nếu là demo
    private ChatRepository chatRepository;
    private com.example.nightlife_finder.models.Conversation currentConversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSystemBars();
        setContentView(R.layout.activity_chat_detail);

        chatRepository = new ChatRepository();

        bindViews();
        resolveIntent();
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
        messageScroll = findViewById(R.id.messageScroll);

        // Lấy LinearLayout bên trong ScrollView để append message mới
        if (messageScroll != null && messageScroll.getChildCount() > 0) {
            messageContainer = (LinearLayout) messageScroll.getChildAt(0);
        }
    }

    // -------------------------------------------------------
    // Phân tích Intent: ưu tiên CONVERSATION_ID (Firestore),
    // fallback CHAT_ID (demo hardcode)
    // -------------------------------------------------------
    private void resolveIntent() {
        conversationId = getIntent().getStringExtra("CONVERSATION_ID");

        if (conversationId != null && !conversationId.isEmpty()) {
            // Chế độ Firestore
            detailEmoji.setText("💬");
            detailName.setText("Cuộc trò chuyện");
            detailStatus.setText("Đang tải...");

            // Ẩn messages hardcode, dùng container để load từ Firestore
            hideHardcodedMessages();
            loadConversationMetadata();
            loadFirestoreMessages();
        } else {
            // Chế độ demo hardcode (giữ nguyên logic cũ)
            String chatId = getIntent().getStringExtra("CHAT_ID");
            if (chatId == null) chatId = "bunbo";
            setupDemoData(chatId);
        }
    }

    private void hideHardcodedMessages() {
        if (userMsg1 != null) userMsg1.setVisibility(android.view.View.GONE);
        if (shopMsg1 != null) shopMsg1.setVisibility(android.view.View.GONE);
        if (userMsg2 != null) userMsg2.setVisibility(android.view.View.GONE);
        if (shopMsg2 != null) shopMsg2.setVisibility(android.view.View.GONE);

        // Ẩn các TextView timestamp đi kèm
        TextView t;
        t = findViewById(R.id.userTime1); if (t != null) t.setVisibility(android.view.View.GONE);
        t = findViewById(R.id.shopTime1); if (t != null) t.setVisibility(android.view.View.GONE);
        t = findViewById(R.id.userTime2); if (t != null) t.setVisibility(android.view.View.GONE);
        t = findViewById(R.id.shopTime2); if (t != null) t.setVisibility(android.view.View.GONE);
        t = findViewById(R.id.timeLabel); if (t != null) t.setVisibility(android.view.View.GONE);
    }

    // -------------------------------------------------------
    // Load conversation metadata từ Firestore
    // -------------------------------------------------------
    private void loadConversationMetadata() {
        if (conversationId == null) return;
        com.google.firebase.firestore.FirebaseFirestore db = com.example.nightlife_finder.firebase.FirebaseManager.getInstance().getFirestore();
        db.collection(com.example.nightlife_finder.constants.FirebaseConstants.COLLECTION_CONVERSATIONS)
            .document(conversationId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    currentConversation = doc.toObject(com.example.nightlife_finder.models.Conversation.class);
                    if (currentConversation != null) {
                        updateHeaderWithMetadata();
                    }
                }
            });
    }

    private void updateHeaderWithMetadata() {
        if (currentConversation == null) return;

        // 1. Primary Text (detailName)
        String shopName = currentConversation.getShopName();
        if (shopName == null || shopName.isEmpty()) {
            shopName = currentConversation.getTitle();
        }
        if (shopName == null || shopName.isEmpty()) {
            shopName = "Cuộc trò chuyện";
        }
        detailName.setText(shopName);

        // 2. Avatar (detailEmoji)
        String avatarText = currentConversation.getShopAvatarText();
        if (avatarText == null || avatarText.isEmpty()) {
            avatarText = categoryToEmoji(currentConversation.getShopCategory());
        }
        detailEmoji.setText(avatarText);

        // 3. Subtitle (detailStatus)
        String cat = currentConversation.getShopCategory();
        String openTime = currentConversation.getOpenTime();
        if (cat != null && !cat.isEmpty() && openTime != null && !openTime.isEmpty()) {
            detailStatus.setText(cat + " • " + openTime);
            detailStatus.setTextColor(Color.parseColor("#8A8A95"));
        } else if (currentConversation.getAddress() != null && !currentConversation.getAddress().isEmpty() && !"Chưa có địa chỉ".equals(currentConversation.getAddress())) {
            detailStatus.setText(currentConversation.getAddress());
            detailStatus.setTextColor(Color.parseColor("#8A8A95"));
        } else {
            detailStatus.setText("Thông tin cập nhật...");
            detailStatus.setTextColor(Color.parseColor("#8A8A95"));
        }
    }

    private String categoryToEmoji(String category) {
        if (category == null) return "💬";
        switch (category.toLowerCase()) {
            case "pizza": return "🍕";
            case "lẩu": case "lau": return "🍲";
            case "trà sữa": case "che": return "🧋";
            case "nướng": case "bbq": return "🔥";
            case "cơm": case "diner": return "🍚";
            case "phở": case "bún bò": case "bunbo": return "🍜";
            default: return "💬";
        }
    }

    // -------------------------------------------------------
    // Load messages từ Firestore
    // -------------------------------------------------------
    private void loadFirestoreMessages() {
        if (conversationId == null || messageContainer == null) return;

        chatRepository.getMessages(conversationId, new OnMessageLoadedListener() {
            @Override
            public void onSuccess(List<ChatMessage> messages) {
                renderMessages(messages);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ChatDetailActivity.this,
                        "Lỗi tải tin nhắn: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderMessages(List<ChatMessage> messages) {
        if (messageContainer == null) return;

        // Xóa message cũ đã render (không xóa views hardcode vì đã gone)
        // Ta chỉ xóa views có tag "firestore_msg"
        for (int i = messageContainer.getChildCount() - 1; i >= 0; i--) {
            android.view.View child = messageContainer.getChildAt(i);
            if ("firestore_msg".equals(child.getTag())) {
                messageContainer.removeViewAt(i);
            }
        }

        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        String myUid = me != null ? me.getUid() : "";

        for (ChatMessage msg : messages) {
            boolean isMe = myUid.equals(msg.getSenderId());
            addMessageBubble(msg, isMe);
        }

        // Scroll xuống cuối
        messageScroll.post(() -> messageScroll.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void addMessageBubble(ChatMessage msg, boolean isMe) {
        if (messageContainer == null) return;

        // Bubble text
        TextView bubble = new TextView(this);
        bubble.setText(msg.getText());
        bubble.setTextColor(Color.WHITE);
        bubble.setTextSize(14);
        bubble.setTypeface(null, android.graphics.Typeface.BOLD);
        bubble.setPadding(dpToPx(14), dpToPx(10), dpToPx(14), dpToPx(10));
        bubble.setMaxWidth(dpToPx(285));
        bubble.setTag("firestore_msg");

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dpToPx(14);

        if (isMe) {
            bubble.setBackgroundResource(R.drawable.chat_bubble_user);
            params.gravity = Gravity.END;
        } else {
            bubble.setBackgroundResource(R.drawable.chat_bubble_shop);
            params.gravity = Gravity.START;
        }

        bubble.setLayoutParams(params);
        messageContainer.addView(bubble);

        // Timestamp nhỏ
        String timeStr = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(msg.getCreatedAt()));
        TextView timeView = new TextView(this);
        timeView.setText(timeStr);
        timeView.setTextColor(Color.parseColor("#8A8A95"));
        timeView.setTextSize(10);
        timeView.setTag("firestore_msg");

        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        timeParams.topMargin = dpToPx(4);
        timeParams.gravity = isMe ? Gravity.END : Gravity.START;
        timeView.setLayoutParams(timeParams);
        messageContainer.addView(timeView);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    // -------------------------------------------------------
    // Gửi tin nhắn mới (chỉ hoạt động khi có conversationId)
    // -------------------------------------------------------
    private void handleSendMessage() {
        if (messageInput == null) return;

        String text = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "Bạn chưa nhập tin nhắn", Toast.LENGTH_SHORT).show();
            return;
        }

        if (conversationId == null) {
            // Chế độ demo — chỉ toast
            Toast.makeText(this, "Đã gửi tin nhắn demo: " + text, Toast.LENGTH_SHORT).show();
            messageInput.setText("");
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        String email = currentUser.getEmail() != null ? currentUser.getEmail() : "";

        // Tạm thời hiển thị ngay trên UI trước khi Firestore confirm
        ChatMessage optimistic = new ChatMessage(
                "", conversationId, uid, email, text, System.currentTimeMillis()
        );
        addMessageBubble(optimistic, true);
        messageScroll.post(() -> messageScroll.fullScroll(ScrollView.FOCUS_DOWN));
        messageInput.setText("");

        chatRepository.sendMessage(conversationId, uid, email, text, new OnChatActionListener() {
            @Override
            public void onSuccess(String msgId) {
                // Reload để đồng bộ với Firestore (đơn giản, không realtime)
                loadFirestoreMessages();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ChatDetailActivity.this,
                        "Lỗi gửi tin nhắn: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // -------------------------------------------------------
    // Setup buttons
    // -------------------------------------------------------
    private void setupButtons() {
        // Nút back
        TextView back = findViewById(R.id.btnBackChat);
        if (back != null) back.setOnClickListener(v -> finish());

        // Nút gửi tin nhắn
        TextView sendBtn = findViewById(R.id.btnSendMessage);
        if (sendBtn != null) sendBtn.setOnClickListener(v -> handleSendMessage());

        // Quick actions
        safeClick(R.id.btnCallShop, "Tính năng mở rộng");
        safeClick(R.id.btnVideoShop, "Tính năng mở rộng");
        safeClick(R.id.btnBooking, "Tính năng mở rộng");
        safeClick(R.id.btnMenu, "Tính năng mở rộng");
        safeClick(R.id.btnCallQuick, "Tính năng mở rộng");

        android.view.View btnDir = findViewById(R.id.btnDirection);
        if (btnDir != null) {
            btnDir.setOnClickListener(v -> {
                if (conversationId != null) {
                    handleDirectionClick();
                } else {
                    Toast.makeText(this, "Demo chỉ đường tới quán", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void handleDirectionClick() {
        if (currentConversation != null) {
            String address = currentConversation.getAddress();
            String shopName = currentConversation.getShopName();
            if (shopName == null || shopName.isEmpty()) shopName = currentConversation.getTitle();

            if (address != null && !address.isEmpty() && !"Chưa có địa chỉ".equals(address)) {
                String query = shopName != null ? shopName + " " + address : address;
                android.net.Uri browserUri = android.net.Uri.parse("https://maps.google.com/?q=" + android.net.Uri.encode(query));
                startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
            } else if (shopName != null && !shopName.isEmpty() && !"Cuộc trò chuyện".equals(shopName)) {
                android.net.Uri browserUri = android.net.Uri.parse("https://maps.google.com/?q=" + android.net.Uri.encode(shopName));
                startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
            } else {
                Toast.makeText(this, "Chưa có dữ liệu chỉ đường cho cuộc trò chuyện này", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Chưa có dữ liệu chỉ đường", Toast.LENGTH_SHORT).show();
        }
    }

    private void safeClick(int viewId, String toastMsg) {
        android.view.View v = findViewById(viewId);
        if (v != null) v.setOnClickListener(x ->
                Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show());
    }

    // -------------------------------------------------------
    // Demo hardcode data (CHAT_ID fallback)
    // -------------------------------------------------------
    private void setupDemoData(String chatId) {
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
            default: // bunbo
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

    // -------------------------------------------------------
    // Bottom Navigation
    // -------------------------------------------------------
    private void setupBottomNavigation() {
        android.view.View navHome = findViewById(R.id.navHome);
        if (navHome != null) navHome.setOnClickListener(v -> {
            Intent intent = new Intent(ChatDetailActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        android.view.View navMap = findViewById(R.id.navMap);
        if (navMap != null) navMap.setOnClickListener(v ->
                startActivity(new Intent(ChatDetailActivity.this, MapActivity.class)));

        android.view.View navChat = findViewById(R.id.navChat);
        if (navChat != null) navChat.setOnClickListener(v -> {
            Intent intent = new Intent(ChatDetailActivity.this, ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        android.view.View navFav = findViewById(R.id.navFavorite);
        if (navFav != null) navFav.setOnClickListener(v ->
                startActivity(new Intent(this, FavoriteActivity.class)));

        android.view.View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) navProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }
}