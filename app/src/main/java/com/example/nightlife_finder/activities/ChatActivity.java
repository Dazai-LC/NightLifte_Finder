package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nightlife_finder.R;
import com.example.nightlife_finder.interfaces.OnConversationLoadedListener;
import com.example.nightlife_finder.models.Conversation;
import com.example.nightlife_finder.repositories.ChatRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends BaseActivity {

    private TextView chipAll;
    private TextView chipUnread;

    // Các chat item tĩnh (hardcode demo, vẫn giữ để UI không trống ngay)
    private View chatBunBoRow;
    private View chatLauRow;
    private View chatBBQRow;
    private View chatCheRow;
    private View chatPizzaRow;

    private View chatBunBoFront;
    private View chatLauFront;
    private View chatBBQFront;
    private View chatCheFront;
    private View chatPizzaFront;

    // Container để append conversation từ Firestore
    private LinearLayout chatListContainer;

    private int deleteWidthPx;

    private ChatRepository chatRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSystemBars();
        setContentView(R.layout.activity_chat);

        deleteWidthPx = dpToPx(92);
        chatRepository = new ChatRepository();

        bindViews();
        setupFilterChips();
        setupChatItems();
        setupSwipeDelete();
        setupBottomNavigation();

        // Load conversations từ Firestore
        loadFirestoreConversations();
    }

    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.parseColor("#0F0F12"));
        getWindow().setNavigationBarColor(Color.parseColor("#1A1A1F"));
    }

    private void bindViews() {
        chipAll = findViewById(R.id.chipAll);
        chipUnread = findViewById(R.id.chipUnread);

        chatBunBoRow = findViewById(R.id.chatBunBoRow);
        chatLauRow = findViewById(R.id.chatLauRow);
        chatBBQRow = findViewById(R.id.chatBBQRow);
        chatCheRow = findViewById(R.id.chatCheRow);
        chatPizzaRow = findViewById(R.id.chatPizzaRow);

        chatBunBoFront = findViewById(R.id.chatBunBoFront);
        chatLauFront = findViewById(R.id.chatLauFront);
        chatBBQFront = findViewById(R.id.chatBBQFront);
        chatCheFront = findViewById(R.id.chatCheFront);
        chatPizzaFront = findViewById(R.id.chatPizzaFront);

        chatListContainer = findViewById(R.id.chatListContainer);
    }

    // -------------------------------------------------------
    // Load conversations từ Firestore và thêm vào cuối list
    // -------------------------------------------------------
    private void loadFirestoreConversations() {
        chatRepository.getConversations(new OnConversationLoadedListener() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                if (conversations.isEmpty()) return;

                for (Conversation conv : conversations) {
                    View item = buildConversationRow(conv);
                    chatListContainer.addView(item);
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ChatActivity.this,
                        "Không tải được hội thoại: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Tạo một row đơn giản cho conversation từ Firestore.
     * Dùng LinearLayout programmatic để không cần layout mới.
     */
    private View buildConversationRow(Conversation conv) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setBackgroundColor(Color.parseColor("#1A1A27"));
        row.setPadding(dpToPx(20), dpToPx(14), dpToPx(20), dpToPx(14));

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(76)
        );
        rowParams.setMargins(0, 1, 0, 0);
        row.setLayoutParams(rowParams);

        // Avatar emoji
        TextView avatar = new TextView(this);
        avatar.setText("💬");
        avatar.setTextSize(24);
        avatar.setGravity(android.view.Gravity.CENTER);
        avatar.setBackgroundResource(R.drawable.marker_purple_bg);
        row.addView(avatar, new LinearLayout.LayoutParams(dpToPx(48), dpToPx(48)));

        // Info column
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dpToPx(12), 0, 0, 0);

        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(info, infoParams);

        TextView titleView = new TextView(this);
        titleView.setText(conv.getTitle() != null ? conv.getTitle() : "(Không có tiêu đề)");
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(15);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setSingleLine(true);
        titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        info.addView(titleView);

        String lastMsg = conv.getLastMessage();
        if (lastMsg == null || lastMsg.isEmpty()) lastMsg = "Bắt đầu cuộc trò chuyện...";
        TextView lastMsgView = new TextView(this);
        lastMsgView.setText(lastMsg);
        lastMsgView.setTextColor(Color.parseColor("#9CA0AA"));
        lastMsgView.setTextSize(13);
        lastMsgView.setSingleLine(true);
        lastMsgView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        lastMsgView.setPadding(0, dpToPx(3), 0, 0);
        info.addView(lastMsgView);

        // Thời gian
        String timeStr = "";
        if (conv.getLastMessageAt() > 0) {
            timeStr = new SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(new Date(conv.getLastMessageAt()));
        }
        TextView timeView = new TextView(this);
        timeView.setText(timeStr);
        timeView.setTextColor(Color.parseColor("#A020F0"));
        timeView.setTextSize(12);
        row.addView(timeView);

        // Click mở ChatDetailActivity với conversationId
        final String convId = conv.getId();
        row.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, ChatDetailActivity.class);
            intent.putExtra("CONVERSATION_ID", convId);
            startActivity(intent);
        });

        return row;
    }

    // -------------------------------------------------------
    // Filter chips
    // -------------------------------------------------------
    private void setupFilterChips() {
        chipAll.setOnClickListener(v -> {
            setActiveChip(chipAll);
            showAllChats();
            Toast.makeText(this, "Hiển thị tất cả cuộc trò chuyện", Toast.LENGTH_SHORT).show();
        });

        chipUnread.setOnClickListener(v -> {
            setActiveChip(chipUnread);
            showUnreadChats();
            Toast.makeText(this, "Đang lọc tin chưa đọc", Toast.LENGTH_SHORT).show();
        });
    }

    private void setActiveChip(TextView activeChip) {
        chipAll.setBackgroundResource(R.drawable.chip_bg);
        chipUnread.setBackgroundResource(R.drawable.chip_bg);
        activeChip.setBackgroundResource(R.drawable.chip_active_bg);
    }

    private void showAllChats() {
        chatBunBoRow.setVisibility(View.VISIBLE);
        chatLauRow.setVisibility(View.VISIBLE);
        chatBBQRow.setVisibility(View.VISIBLE);
        chatCheRow.setVisibility(View.VISIBLE);
        chatPizzaRow.setVisibility(View.VISIBLE);
        resetAllSwipePositions();
    }

    private void showUnreadChats() {
        chatBunBoRow.setVisibility(View.VISIBLE);
        chatLauRow.setVisibility(View.VISIBLE);
        chatBBQRow.setVisibility(View.GONE);
        chatCheRow.setVisibility(View.GONE);
        chatPizzaRow.setVisibility(View.GONE);
        resetAllSwipePositions();
    }

    private void resetAllSwipePositions() {
        chatBunBoFront.setTranslationX(0);
        chatLauFront.setTranslationX(0);
        chatBBQFront.setTranslationX(0);
        chatCheFront.setTranslationX(0);
        chatPizzaFront.setTranslationX(0);
    }

    // -------------------------------------------------------
    // Chat items (hardcode demo)
    // -------------------------------------------------------
    private void setupChatItems() {
        chatBunBoFront.setOnClickListener(v -> openDemoChat("bunbo"));
        chatLauFront.setOnClickListener(v -> openDemoChat("lau"));
        chatBBQFront.setOnClickListener(v -> openDemoChat("bbq"));
        chatCheFront.setOnClickListener(v -> openDemoChat("che"));
        chatPizzaFront.setOnClickListener(v -> openDemoChat("pizza"));

        findViewById(R.id.btnNewChat).setOnClickListener(v ->
                startActivity(new Intent(ChatActivity.this, NewChatActivity.class)));

        findViewById(R.id.btnNewChatTop).setOnClickListener(v ->
                startActivity(new Intent(ChatActivity.this, NewChatActivity.class)));

        findViewById(R.id.btnSearchChat).setOnClickListener(v ->
                Toast.makeText(this, "Nhập tên quán để tìm cuộc trò chuyện", Toast.LENGTH_SHORT).show());
    }

    private void openDemoChat(String chatId) {
        Intent intent = new Intent(ChatActivity.this, ChatDetailActivity.class);
        intent.putExtra("CHAT_ID", chatId);
        startActivity(intent);
    }

    // -------------------------------------------------------
    // Swipe to delete (giữ nguyên)
    // -------------------------------------------------------
    private void setupSwipeDelete() {
        setupSwipeForItem(chatBunBoRow, chatBunBoFront, findViewById(R.id.deleteBunBo));
        setupSwipeForItem(chatLauRow, chatLauFront, findViewById(R.id.deleteLau));
        setupSwipeForItem(chatBBQRow, chatBBQFront, findViewById(R.id.deleteBBQ));
        setupSwipeForItem(chatCheRow, chatCheFront, findViewById(R.id.deleteChe));
        setupSwipeForItem(chatPizzaRow, chatPizzaFront, findViewById(R.id.deletePizza));
    }

    private void setupSwipeForItem(View row, View frontView, View deleteButton) {
        final float[] downX = new float[1];
        final boolean[] isDragging = new boolean[1];

        frontView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX[0] = event.getRawX();
                    isDragging[0] = false;
                    frontView.animate().cancel();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float diff = event.getRawX() - downX[0];
                    if (Math.abs(diff) > 12) isDragging[0] = true;
                    if (diff < 0) frontView.setTranslationX(Math.max(diff, -deleteWidthPx));
                    if (diff > 0) frontView.setTranslationX(0);
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    float finalDiff = event.getRawX() - downX[0];
                    if (!isDragging[0]) {
                        frontView.performClick();
                        return true;
                    }
                    if (finalDiff < -60) showDeleteThenSpringBack(frontView);
                    else springBack(frontView);
                    return true;
            }
            return false;
        });

        deleteButton.setOnClickListener(v -> {
            row.animate()
                    .alpha(0f)
                    .translationX(-row.getWidth())
                    .setDuration(220)
                    .withEndAction(() -> {
                        row.setVisibility(View.GONE);
                        row.setAlpha(1f);
                        row.setTranslationX(0f);
                    })
                    .start();
            Toast.makeText(this, "Đã xóa tin nhắn", Toast.LENGTH_SHORT).show();
        });
    }

    private void showDeleteThenSpringBack(View frontView) {
        frontView.animate()
                .translationX(-deleteWidthPx)
                .setDuration(120)
                .withEndAction(() -> frontView.postDelayed(() -> springBack(frontView), 450))
                .start();
    }

    private void springBack(View frontView) {
        frontView.animate()
                .translationX(0)
                .setDuration(420)
                .setInterpolator(new OvershootInterpolator(1.8f))
                .start();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    // -------------------------------------------------------
    // Bottom Navigation
    // -------------------------------------------------------
    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        findViewById(R.id.navMap).setOnClickListener(v ->
                startActivity(new Intent(ChatActivity.this, MapActivity.class)));

        findViewById(R.id.navChat).setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.you_are_on_favorite), Toast.LENGTH_SHORT).show());

        findViewById(R.id.navFavorite).setOnClickListener(v ->
                startActivity(new Intent(this, FavoriteActivity.class)));

        findViewById(R.id.navProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }
}