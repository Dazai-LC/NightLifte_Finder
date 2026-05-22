package com.example.nightlife_finder.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nightlife_finder.R;
import com.example.nightlife_finder.interfaces.OnChatActionListener;
import com.example.nightlife_finder.interfaces.OnConversationLoadedListener;
import com.example.nightlife_finder.models.Conversation;
import com.example.nightlife_finder.repositories.ChatRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends BaseActivity {

    private TextView chipAll;
    private TextView chipUnread;

    private View chatBunBoRow, chatLauRow, chatBBQRow, chatCheRow, chatPizzaRow;
    private View chatBunBoFront, chatLauFront, chatBBQFront, chatCheFront, chatPizzaFront;

    private LinearLayout chatListContainer;
    private EditText chatSearchInput;
    private int deleteWidthPx;
    private ChatRepository chatRepository;

    private final List<Conversation> allConversations = new ArrayList<>();
    private static final int HARDCODE_ITEM_COUNT = 5;

    // Chip state: "all" hoặc "unread"
    private String activeChipMode = "all";

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
        setupSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadConversations();
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
        chatSearchInput = findViewById(R.id.chatSearchInput);
    }

    // -------------------------------------------------------
    // Search
    // -------------------------------------------------------
    private void setupSearch() {
        if (chatSearchInput == null) return;
        chatSearchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyCurrentFilter();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        View btnSearch = findViewById(R.id.btnSearchChat);
        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> {
                chatSearchInput.requestFocus();
                chatSearchInput.setFocusableInTouchMode(true);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(chatSearchInput, InputMethodManager.SHOW_IMPLICIT);
            });
        }
    }

    /**
     * Áp dụng cả chip mode (all/unread) và search query cùng lúc.
     */
    private void applyCurrentFilter() {
        String query = chatSearchInput != null ? chatSearchInput.getText().toString().trim() : "";
        List<Conversation> base = "unread".equals(activeChipMode)
                ? filterUnread(allConversations)
                : new ArrayList<>(allConversations);

        if (TextUtils.isEmpty(query)) {
            renderConversationRows(base);
            return;
        }
        String lower = query.toLowerCase(Locale.getDefault());
        List<Conversation> filtered = new ArrayList<>();
        for (Conversation conv : base) {
            if (matchesQuery(conv, lower)) filtered.add(conv);
        }
        renderConversationRows(filtered);
    }

    private List<Conversation> filterUnread(List<Conversation> source) {
        List<Conversation> result = new ArrayList<>();
        for (Conversation c : source) {
            if (!c.isRead()) result.add(c);
        }
        return result;
    }

    private boolean matchesQuery(Conversation conv, String lower) {
        return contains(conv.getTitle(), lower)
                || contains(conv.getLastMessage(), lower)
                || contains(conv.getCreatedByEmail(), lower)
                || contains(conv.getShopName(), lower)
                || contains(conv.getShopCategory(), lower)
                || contains(conv.getAddress(), lower);
    }

    private boolean contains(String field, String lower) {
        return field != null && field.toLowerCase(Locale.getDefault()).contains(lower);
    }

    // -------------------------------------------------------
    // Load conversations
    // -------------------------------------------------------
    private void loadConversations() {
        chatRepository.getConversations(new OnConversationLoadedListener() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                allConversations.clear();
                allConversations.addAll(conversations);
                applyCurrentFilter();
            }
            @Override
            public void onError(String error) {
                Toast.makeText(ChatActivity.this, "Không tải được hội thoại: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderConversationRows(List<Conversation> list) {
        int total = chatListContainer.getChildCount();
        if (total > HARDCODE_ITEM_COUNT) {
            chatListContainer.removeViews(HARDCODE_ITEM_COUNT, total - HARDCODE_ITEM_COUNT);
        }
        for (Conversation conv : list) {
            chatListContainer.addView(buildConversationRow(conv));
        }
    }

    // -------------------------------------------------------
    // Build row
    // -------------------------------------------------------
    private View buildConversationRow(Conversation conv) {
        boolean unread = !conv.isRead();

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        // Nền hơi sáng hơn nếu chưa đọc
        row.setBackgroundColor(Color.parseColor(unread ? "#1E1E30" : "#1A1A27"));
        row.setPadding(dpToPx(16), dpToPx(12), dpToPx(12), dpToPx(12));
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, dpToPx(1), 0, 0);
        row.setLayoutParams(rowParams);

        // Avatar
        String avatarText = conv.getShopAvatarText();
        if (avatarText == null || avatarText.isEmpty()) avatarText = "💬";
        TextView avatar = new TextView(this);
        avatar.setText(avatarText);
        avatar.setTextSize(22);
        avatar.setGravity(android.view.Gravity.CENTER);
        avatar.setBackgroundResource(R.drawable.marker_purple_bg);
        row.addView(avatar, new LinearLayout.LayoutParams(dpToPx(46), dpToPx(46)));

        // Info column
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dpToPx(12), 0, 0, 0);
        row.addView(info, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        // Title — bold hơn nếu chưa đọc
        TextView titleView = new TextView(this);
        String displayTitle = conv.getTitle() != null ? conv.getTitle() : "(Không có tiêu đề)";
        titleView.setText(displayTitle);
        titleView.setTextColor(unread ? Color.WHITE : Color.parseColor("#D0D0D8"));
        titleView.setTextSize(15);
        titleView.setTypeface(null, unread ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        titleView.setSingleLine(true);
        titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        info.addView(titleView);

        // Badge "Chưa đọc" nhỏ
        if (unread) {
            TextView badge = new TextView(this);
            badge.setText("● Chưa đọc");
            badge.setTextColor(Color.parseColor("#A020F0"));
            badge.setTextSize(10);
            badge.setPadding(0, dpToPx(1), 0, 0);
            info.addView(badge);
        }

        // Last message
        String lastMsg = conv.getLastMessage();
        if (lastMsg == null || lastMsg.isEmpty()) lastMsg = "Bắt đầu cuộc trò chuyện...";
        TextView lastMsgView = new TextView(this);
        lastMsgView.setText(lastMsg);
        lastMsgView.setTextColor(Color.parseColor(unread ? "#C0C0CC" : "#9CA0AA"));
        lastMsgView.setTextSize(12);
        lastMsgView.setSingleLine(true);
        lastMsgView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        lastMsgView.setPadding(0, dpToPx(2), 0, 0);
        info.addView(lastMsgView);

        // Address nếu có
        if (!TextUtils.isEmpty(conv.getAddress()) && !"Chưa có địa chỉ".equals(conv.getAddress())) {
            TextView addrView = new TextView(this);
            addrView.setText("📍 " + conv.getAddress());
            addrView.setTextColor(Color.parseColor("#6B6B75"));
            addrView.setTextSize(11);
            addrView.setSingleLine(true);
            addrView.setEllipsize(android.text.TextUtils.TruncateAt.END);
            addrView.setPadding(0, dpToPx(2), 0, 0);
            info.addView(addrView);
        }

        // Runtime status
        boolean openNow = isOpenNow(conv.getOpenTime(), conv.getCloseTime());
        TextView statusView = new TextView(this);
        statusView.setText(openNow ? "● Đang mở" : "● Đã đóng");
        statusView.setTextColor(Color.parseColor(openNow ? "#32CD32" : "#FF4444"));
        statusView.setTextSize(11);
        statusView.setPadding(0, dpToPx(2), 0, 0);
        info.addView(statusView);

        // Right column: thời gian + nút ✎ sửa + nút × xóa
        LinearLayout rightCol = new LinearLayout(this);
        rightCol.setOrientation(LinearLayout.VERTICAL);
        rightCol.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        rightCol.setPadding(dpToPx(8), 0, 0, 0);
        row.addView(rightCol, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // Thời gian
        String timeStr = "";
        if (conv.getLastMessageAt() > 0) {
            timeStr = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(conv.getLastMessageAt()));
        }
        TextView timeView = new TextView(this);
        timeView.setText(timeStr);
        timeView.setTextColor(Color.parseColor("#A020F0"));
        timeView.setTextSize(11);
        timeView.setGravity(android.view.Gravity.CENTER);
        rightCol.addView(timeView);

        // Nút sửa ✎
        final String convId = conv.getId();
        TextView editBtn = new TextView(this);
        editBtn.setText("✎");
        editBtn.setTextColor(Color.parseColor("#A020F0"));
        editBtn.setTextSize(16);
        editBtn.setGravity(android.view.Gravity.CENTER);
        editBtn.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), 0);
        rightCol.addView(editBtn, new LinearLayout.LayoutParams(dpToPx(32), dpToPx(28)));
        editBtn.setOnClickListener(v -> showEditDialog(conv));

        // Nút xóa ×
        TextView deleteBtn = new TextView(this);
        deleteBtn.setText("×");
        deleteBtn.setTextColor(Color.parseColor("#FF4444"));
        deleteBtn.setTextSize(20);
        deleteBtn.setGravity(android.view.Gravity.CENTER);
        deleteBtn.setPadding(dpToPx(4), dpToPx(2), dpToPx(2), 0);
        rightCol.addView(deleteBtn, new LinearLayout.LayoutParams(dpToPx(32), dpToPx(30)));
        deleteBtn.setOnClickListener(v -> showDeleteConfirmDialog(convId, displayTitle, row));

        // Click row → mark as read + mở ChatDetailActivity
        row.setOnClickListener(v -> {
            // Mark as read local + Firestore
            conv.setRead(true);
            chatRepository.markAsRead(convId);
            Intent intent = new Intent(ChatActivity.this, ChatDetailActivity.class);
            intent.putExtra("CONVERSATION_ID", convId);
            startActivity(intent);
        });

        return row;
    }

    // -------------------------------------------------------
    // Sửa conversation — AlertDialog với EditText fields
    // -------------------------------------------------------
    private void showEditDialog(Conversation conv) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dpToPx(20), dpToPx(12), dpToPx(20), 0);

        EditText etTitle    = makeEditField(layout, "Tiêu đề", conv.getTitle());
        EditText etShopName = makeEditField(layout, "Tên quán", conv.getShopName());
        EditText etAvatar   = makeEditField(layout, "Emoji avatar", conv.getShopAvatarText());
        EditText etCategory = makeEditField(layout, "Loại quán", conv.getShopCategory());
        EditText etAddress  = makeEditField(layout, "Địa chỉ", conv.getAddress());
        EditText etOpen     = makeEditField(layout, "Giờ mở (HH:mm)", conv.getOpenTime());
        EditText etClose    = makeEditField(layout, "Giờ đóng (HH:mm)", conv.getCloseTime());

        new AlertDialog.Builder(this)
                .setTitle("Sửa cuộc trò chuyện")
                .setView(layout)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    Map<String, Object> updates = new HashMap<>();
                    putIfNotEmpty(updates, "title",         etTitle.getText().toString().trim(),    "Cuộc trò chuyện mới");
                    putIfNotEmpty(updates, "shopName",      etShopName.getText().toString().trim(), conv.getTitle());
                    putIfNotEmpty(updates, "shopAvatarText",etAvatar.getText().toString().trim(),   "💬");
                    putIfNotEmpty(updates, "shopCategory",  etCategory.getText().toString().trim(), "general");
                    putIfNotEmpty(updates, "address",       etAddress.getText().toString().trim(),  "Chưa có địa chỉ");
                    putIfNotEmpty(updates, "openTime",      etOpen.getText().toString().trim(),     "18:00");
                    putIfNotEmpty(updates, "closeTime",     etClose.getText().toString().trim(),    "02:30");

                    chatRepository.updateConversation(conv.getId(), updates, new OnChatActionListener() {
                        @Override public void onSuccess(String id) {
                            Toast.makeText(ChatActivity.this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                            loadConversations();
                        }
                        @Override public void onError(String error) {
                            Toast.makeText(ChatActivity.this, "Lỗi cập nhật: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /** Tạo EditText có hint và giá trị mặc định, thêm vào container. */
    private EditText makeEditField(LinearLayout container, String hint, String value) {
        EditText et = new EditText(this);
        et.setHint(hint);
        et.setText(value != null ? value : "");
        et.setTextColor(Color.BLACK);
        et.setSingleLine(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dpToPx(8));
        et.setLayoutParams(lp);
        container.addView(et);
        return et;
    }

    /** Đưa value vào map; nếu rỗng thì dùng fallback. */
    private void putIfNotEmpty(Map<String, Object> map, String key, String value, String fallback) {
        map.put(key, TextUtils.isEmpty(value) ? fallback : value);
    }

    // -------------------------------------------------------
    // Xóa conversation
    // -------------------------------------------------------
    private void showDeleteConfirmDialog(String convId, String convTitle, View rowView) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa cuộc trò chuyện")
                .setMessage("Bạn có chắc muốn xóa \"" + convTitle + "\" không?\nHành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) ->
                        rowView.animate().alpha(0f).translationX(-rowView.getWidth()).setDuration(220)
                                .withEndAction(() -> {
                                    chatListContainer.removeView(rowView);
                                    deleteConversationFromFirestore(convId);
                                }).start())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteConversationFromFirestore(String convId) {
        chatRepository.deleteConversation(convId, new OnChatActionListener() {
            @Override public void onSuccess(String id) {
                allConversations.removeIf(c -> convId.equals(c.getId()));
                Toast.makeText(ChatActivity.this, "Đã xóa cuộc trò chuyện", Toast.LENGTH_SHORT).show();
            }
            @Override public void onError(String error) {
                Toast.makeText(ChatActivity.this, "Xóa thất bại: " + error, Toast.LENGTH_SHORT).show();
                loadConversations();
            }
        });
    }

    // -------------------------------------------------------
    // Runtime open/close status
    // -------------------------------------------------------
    private boolean isOpenNow(String openTime, String closeTime) {
        if (openTime == null || closeTime == null) return false;
        try {
            int openMin  = toMinutes(openTime);
            int closeMin = toMinutes(closeTime);
            Calendar cal = Calendar.getInstance();
            int nowMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
            if (openMin <= closeMin) return nowMin >= openMin && nowMin <= closeMin;
            else return nowMin >= openMin || nowMin <= closeMin;
        } catch (Exception e) { return false; }
    }

    private int toMinutes(String hhmm) {
        if (hhmm == null || !hhmm.contains(":")) return -1;
        String[] parts = hhmm.split(":");
        return Integer.parseInt(parts[0].trim()) * 60 + Integer.parseInt(parts[1].trim());
    }

    // -------------------------------------------------------
    // Filter chips — kết hợp với Firestore isRead
    // -------------------------------------------------------
    private void setupFilterChips() {
        chipAll.setOnClickListener(v -> {
            activeChipMode = "all";
            setActiveChip(chipAll);
            // Hiện lại hardcode items
            chatBunBoRow.setVisibility(View.VISIBLE);
            chatLauRow.setVisibility(View.VISIBLE);
            chatBBQRow.setVisibility(View.VISIBLE);
            chatCheRow.setVisibility(View.VISIBLE);
            chatPizzaRow.setVisibility(View.VISIBLE);
            resetAllSwipePositions();
            applyCurrentFilter();
        });

        chipUnread.setOnClickListener(v -> {
            activeChipMode = "unread";
            setActiveChip(chipUnread);
            // Ẩn hardcode items (không có isRead tracking)
            chatBunBoRow.setVisibility(View.GONE);
            chatLauRow.setVisibility(View.GONE);
            chatBBQRow.setVisibility(View.GONE);
            chatCheRow.setVisibility(View.GONE);
            chatPizzaRow.setVisibility(View.GONE);
            applyCurrentFilter();
        });
    }

    private void setActiveChip(TextView activeChip) {
        chipAll.setBackgroundResource(R.drawable.chip_bg);
        chipUnread.setBackgroundResource(R.drawable.chip_bg);
        activeChip.setBackgroundResource(R.drawable.chip_active_bg);
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
    }

    private void openDemoChat(String chatId) {
        Intent intent = new Intent(ChatActivity.this, ChatDetailActivity.class);
        intent.putExtra("CHAT_ID", chatId);
        startActivity(intent);
    }

    // -------------------------------------------------------
    // Swipe to delete (hardcode items)
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
                    downX[0] = event.getRawX(); isDragging[0] = false;
                    frontView.animate().cancel(); return true;
                case MotionEvent.ACTION_MOVE:
                    float diff = event.getRawX() - downX[0];
                    if (Math.abs(diff) > 12) isDragging[0] = true;
                    if (diff < 0) frontView.setTranslationX(Math.max(diff, -deleteWidthPx));
                    if (diff > 0) frontView.setTranslationX(0);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    float finalDiff = event.getRawX() - downX[0];
                    if (!isDragging[0]) { frontView.performClick(); return true; }
                    if (finalDiff < -60) showDeleteThenSpringBack(frontView);
                    else springBack(frontView);
                    return true;
            }
            return false;
        });

        deleteButton.setOnClickListener(v -> {
            row.animate().alpha(0f).translationX(-row.getWidth()).setDuration(220)
                    .withEndAction(() -> { row.setVisibility(View.GONE); row.setAlpha(1f); row.setTranslationX(0f); })
                    .start();
            Toast.makeText(this, "Đã xóa tin nhắn", Toast.LENGTH_SHORT).show();
        });
    }

    private void showDeleteThenSpringBack(View frontView) {
        frontView.animate().translationX(-deleteWidthPx).setDuration(120)
                .withEndAction(() -> frontView.postDelayed(() -> springBack(frontView), 450)).start();
    }

    private void springBack(View frontView) {
        frontView.animate().translationX(0).setDuration(420)
                .setInterpolator(new OvershootInterpolator(1.8f)).start();
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