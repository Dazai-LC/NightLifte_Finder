package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nightlife_finder.R;

public class ChatActivity extends BaseActivity {

    private TextView chipAll;
    private TextView chipUnread;

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

    private int deleteWidthPx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSystemBars();
        setContentView(R.layout.activity_chat);

        deleteWidthPx = dpToPx(92);

        bindViews();
        setupFilterChips();
        setupChatItems();
        setupSwipeDelete();
        setupBottomNavigation();
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
    }

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

    private void setupChatItems() {
        chatBunBoFront.setOnClickListener(v -> openChatDetail("bunbo"));
        chatLauFront.setOnClickListener(v -> openChatDetail("lau"));
        chatBBQFront.setOnClickListener(v -> openChatDetail("bbq"));
        chatCheFront.setOnClickListener(v -> openChatDetail("che"));
        chatPizzaFront.setOnClickListener(v -> openChatDetail("pizza"));

        findViewById(R.id.btnNewChat).setOnClickListener(v -> {
            startActivity(new Intent(ChatActivity.this, NewChatActivity.class));
        });

        findViewById(R.id.btnNewChatTop).setOnClickListener(v -> {
            startActivity(new Intent(ChatActivity.this, NewChatActivity.class));
        });

        findViewById(R.id.btnSearchChat).setOnClickListener(v -> {
            Toast.makeText(this, "Nhập tên quán để tìm cuộc trò chuyện", Toast.LENGTH_SHORT).show();
        });
    }

    private void openChatDetail(String chatId) {
        Intent intent = new Intent(ChatActivity.this, ChatDetailActivity.class);
        intent.putExtra("CHAT_ID", chatId);
        startActivity(intent);
    }

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

                    if (Math.abs(diff) > 12) {
                        isDragging[0] = true;
                    }

                    if (diff < 0) {
                        float translateX = Math.max(diff, -deleteWidthPx);
                        frontView.setTranslationX(translateX);
                    }

                    if (diff > 0) {
                        frontView.setTranslationX(0);
                    }

                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    float finalDiff = event.getRawX() - downX[0];

                    if (!isDragging[0]) {
                        frontView.performClick();
                        return true;
                    }

                    if (finalDiff < -60) {
                        showDeleteThenSpringBack(frontView);
                    } else {
                        springBack(frontView);
                    }

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

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        findViewById(R.id.navMap).setOnClickListener(v -> {
            startActivity(new Intent(ChatActivity.this, MapActivity.class));
        });

        findViewById(R.id.navChat).setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.you_are_on_favorite), Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.navFavorite).setOnClickListener(v -> {
            startActivity(new Intent(this, FavoriteActivity.class));
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }
}