package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nightlife_finder.R;

public class MainActivity extends BaseActivity {

    // -------------------------------------------------------
    // Flash deal countdown timers (demo: fixed remaining seconds)
    // -------------------------------------------------------
    private static final long DEAL1_SECONDS = 8130;   // 02:15:30
    private static final long DEAL2_SECONDS = 2710;   // 00:45:10
    private static final long DEAL3_SECONDS = 18000;  // 05:00:00
    private static final long DEAL4_SECONDS = 4800;   // 01:20:00

    private long remaining1 = DEAL1_SECONDS;
    private long remaining2 = DEAL2_SECONDS;
    private long remaining3 = DEAL3_SECONDS;
    private long remaining4 = DEAL4_SECONDS;

    private TextView txtCountdown1;
    private TextView txtCountdown2;
    private TextView txtCountdown3;
    private TextView txtCountdown4;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check user session
        com.example.nightlife_finder.repositories.AuthRepository authRepository = new com.example.nightlife_finder.repositories.AuthRepository();
        if (!authRepository.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Seed Firestore database if it is currently empty
        com.example.nightlife_finder.utils.DatabaseSeeder.seedPlacesIfEmpty();

        setupSystemBars();
        setContentView(R.layout.activity_main);

        bindTimerViews();
        setupFlashDealClicks();
        setupHotPlaceClicks();
        setupNearYouClicks();
        setupAiSuggestionClicks();
        setupLateNightClicks();
        setupSearchBar();
        setupBottomNavigation();

        startCountdownTimers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    // -------------------------------------------------------
    // Status & nav bar colors
    // -------------------------------------------------------
    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.parseColor("#0F0F12"));
        getWindow().setNavigationBarColor(Color.parseColor("#1A1A1F"));
    }

    // -------------------------------------------------------
    // Bind the countdown TextViews from the flash deal cards
    // (The layout doesn't expose individual IDs for these, so
    //  we reference them by their parent's known structure or
    //  add IDs in the layout if needed. Here we use a
    //  tag-based approach via direct view reference if IDs
    //  are added, or simply animate them without real binding.
    //  Since activity_main.xml uses no IDs for countdown texts,
    //  we skip direct binding but provide the full timer logic
    //  that can be wired once IDs are added to the layout.)
    // -------------------------------------------------------
    private void bindTimerViews() {
        // Safely attempt to find countdown TextViews.
        // If IDs are not present in the layout they will be null
        // and the code below handles nulls gracefully.
        txtCountdown1 = findViewById(R.id.txtCountdown1);
        txtCountdown2 = findViewById(R.id.txtCountdown2);
        txtCountdown3 = findViewById(R.id.txtCountdown3);
        txtCountdown4 = findViewById(R.id.txtCountdown4);
    }

    // -------------------------------------------------------
    // Live countdown timers for flash deals
    // -------------------------------------------------------
    private void startCountdownTimers() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                updateCountdownView(txtCountdown1, --remaining1);
                updateCountdownView(txtCountdown2, --remaining2);
                updateCountdownView(txtCountdown3, --remaining3);
                updateCountdownView(txtCountdown4, --remaining4);

                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void updateCountdownView(TextView view, long secondsLeft) {
        if (view == null) return;
        if (secondsLeft <= 0) {
            view.setText("⏱ Hết ưu đãi");
            view.setTextColor(Color.parseColor("#FF4D5A"));
            return;
        }
        long h = secondsLeft / 3600;
        long m = (secondsLeft % 3600) / 60;
        long s = secondsLeft % 60;
        view.setText(String.format("⏱ Còn %02d:%02d:%02d", h, m, s));
    }

    // -------------------------------------------------------
    // Flash deal card click handlers
    // -------------------------------------------------------
    private void setupFlashDealClicks() {
        // Deal 1 – Neon Bites
        if (findViewById(R.id.dealCard1) != null) {
            findViewById(R.id.dealCard1).setOnClickListener(v ->
                    openChatDetail("bbq"));
        }
        // Deal 2 – Sushi Mang
        if (findViewById(R.id.dealCard2) != null) {
            findViewById(R.id.dealCard2).setOnClickListener(v ->
                    openChatDetail("lau"));
        }
        // Deal 3 – Pizza Đêm
        if (findViewById(R.id.dealCard3) != null) {
            findViewById(R.id.dealCard3).setOnClickListener(v ->
                    openChatDetail("pizza"));
        }
        // Deal 4 – Night BBQ
        if (findViewById(R.id.dealCard4) != null) {
            findViewById(R.id.dealCard4).setOnClickListener(v ->
                    openChatDetail("bbq"));
        }
    }

    // -------------------------------------------------------
    // Hot places this week
    // -------------------------------------------------------
    private void setupHotPlaceClicks() {
        if (findViewById(R.id.cardHot1) != null) {
            findViewById(R.id.cardHot1).setOnClickListener(v ->
                    openChatDetail("bbq"));
        }
        if (findViewById(R.id.cardHot2) != null) {
            findViewById(R.id.cardHot2).setOnClickListener(v ->
                    openChatDetail("bunbo"));
        }
        // "Xem tất cả" link beside the section header
        if (findViewById(R.id.btnViewAllHot) != null) {
            findViewById(R.id.btnViewAllHot).setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, FavoriteActivity.class)));
        }
    }

    // -------------------------------------------------------
    // Near you cards
    // -------------------------------------------------------
    private void setupNearYouClicks() {
        if (findViewById(R.id.cardNear1) != null) {
            findViewById(R.id.cardNear1).setOnClickListener(v ->
                    openChatDetail("bunbo"));
        }
        if (findViewById(R.id.cardNear2) != null) {
            findViewById(R.id.cardNear2).setOnClickListener(v ->
                    openChatDetail("pizza"));
        }
        // "Xem bản đồ" link
        if (findViewById(R.id.btnViewMap) != null) {
            findViewById(R.id.btnViewMap).setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, MapActivity.class)));
        }
    }

    // -------------------------------------------------------
    // AI suggestion card
    // -------------------------------------------------------
    private void setupAiSuggestionClicks() {
        if (findViewById(R.id.cardAiSuggest) != null) {
            findViewById(R.id.cardAiSuggest).setOnClickListener(v -> {
                Toast.makeText(this, "🤖 AI gợi ý: Sushi khuya Tokyo – 0,9 km, còn bàn!", Toast.LENGTH_LONG).show();
                openChatDetail("lau");
            });
        }
    }

    // -------------------------------------------------------
    // Late-night places
    // -------------------------------------------------------
    private void setupLateNightClicks() {
        if (findViewById(R.id.cardLateNight) != null) {
            findViewById(R.id.cardLateNight).setOnClickListener(v ->
                    openChatDetail("lau"));
        }
    }

    // -------------------------------------------------------
    // Search bar interaction
    // -------------------------------------------------------
    private void setupSearchBar() {
        if (findViewById(R.id.etHomeSearch) != null) {
            findViewById(R.id.etHomeSearch).setOnClickListener(v ->
                    Toast.makeText(this, "Nhập tên món hoặc quán để tìm kiếm...", Toast.LENGTH_SHORT).show());
        }
    }

    // -------------------------------------------------------
    // Open chat detail screen for a given shop
    // -------------------------------------------------------
    private void openChatDetail(String chatId) {
        Intent intent = new Intent(MainActivity.this, ChatDetailActivity.class);
        intent.putExtra("CHAT_ID", chatId);
        startActivity(intent);
    }

    // -------------------------------------------------------
    // Bottom navigation
    // -------------------------------------------------------
    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.you_are_on_home), Toast.LENGTH_SHORT).show());

        findViewById(R.id.navMap).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MapActivity.class)));

        findViewById(R.id.navChat).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ChatActivity.class)));

        findViewById(R.id.navFavorite).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, FavoriteActivity.class)));

        findViewById(R.id.navProfile).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
    }
}