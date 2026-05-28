package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nightlife_finder.R;
import com.example.nightlife_finder.adapters.PlaceAdapter;
import com.example.nightlife_finder.constants.FirebaseConstants;
import com.example.nightlife_finder.firebase.FirebaseManager;
import com.example.nightlife_finder.models.Place;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

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

    // -------------------------------------------------------
    // Firestore places section
    // -------------------------------------------------------
    private RecyclerView rvPlaces;
    private TextView txtPlacesLoading;
    private TextView txtPlacesEmpty;
    private TextView txtPlacesError;
    private PlaceAdapter placeAdapter;
    private FirebaseFirestore db;

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

        // Firestore instance
        db = FirebaseManager.getInstance().getFirestore();

        bindTimerViews();
        setupFlashDealClicks();
        setupHotPlaceClicks();
        setupNearYouClicks();
        setupAiSuggestionClicks();
        setupLateNightClicks();
        setupSearchBar();
        setupBottomNavigation();
        setupPlacesRecyclerView(); // chỉ setup RecyclerView, không load data

        startCountdownTimers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload places mỗi khi quay lại Home (từ PlaceDetail, Admin, v.v.)
        loadPlacesFromFirestore();
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
        // Deal 1 – Bún bò Đêm Phố Cổ → place_1
        if (findViewById(R.id.dealCard1) != null) {
            findViewById(R.id.dealCard1).setOnClickListener(v -> openPlaceDetail("place_1"));
        }
        // Deal 2 – Lẩu khuya Hoàn Kiếm → place_3
        if (findViewById(R.id.dealCard2) != null) {
            findViewById(R.id.dealCard2).setOnClickListener(v -> openPlaceDetail("place_3"));
        }
        // Deal 3 – Pizza Midnight Hà Nội → place_4
        if (findViewById(R.id.dealCard3) != null) {
            findViewById(R.id.dealCard3).setOnClickListener(v -> openPlaceDetail("place_4"));
        }
        // Deal 4 – Đồ nướng Hàng Bạc → place_2
        if (findViewById(R.id.dealCard4) != null) {
            findViewById(R.id.dealCard4).setOnClickListener(v -> openPlaceDetail("place_2"));
        }
    }

    // -------------------------------------------------------
    // Hot places this week
    // -------------------------------------------------------
    private void setupHotPlaceClicks() {
        // cardHot1 – Bạch tuộc Neon → place_6
        if (findViewById(R.id.cardHot1) != null) {
            findViewById(R.id.cardHot1).setOnClickListener(v -> openPlaceDetail("place_6"));
        }
        // cardHot2 – Cơm Tấm Đêm Hà Nội → place_9
        if (findViewById(R.id.cardHot2) != null) {
            findViewById(R.id.cardHot2).setOnClickListener(v -> openPlaceDetail("place_9"));
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
        // cardNear1 – Quán ăn Starlight → place_7
        if (findViewById(R.id.cardNear1) != null) {
            findViewById(R.id.cardNear1).setOnClickListener(v -> openPlaceDetail("place_7"));
        }
        // cardNear2 – Pizza lát đỏ thẫm → place_8
        if (findViewById(R.id.cardNear2) != null) {
            findViewById(R.id.cardNear2).setOnClickListener(v -> openPlaceDetail("place_8"));
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
        // cardAiSuggest – Trà sữa 24h Phố Cổ → place_5
        if (findViewById(R.id.cardAiSuggest) != null) {
            findViewById(R.id.cardAiSuggest).setOnClickListener(v ->
                openPlaceDetail("place_5"));
        }
    }

    // -------------------------------------------------------
    // Late-night places
    // -------------------------------------------------------
    private void setupLateNightClicks() {
        // cardLateNight – Phở Gà Đêm Hàng Bạc → place_10
        if (findViewById(R.id.cardLateNight) != null) {
            findViewById(R.id.cardLateNight).setOnClickListener(v -> openPlaceDetail("place_10"));
        }
    }

    // -------------------------------------------------------
    // Search bar → mở SearchResultsActivity
    // -------------------------------------------------------
    private void setupSearchBar() {
        EditText et = findViewById(R.id.etHomeSearch);
        if (et == null) return;

        // Bấm Enter / nút Search trên keyboard
        et.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                openSearchResults(et.getText().toString().trim());
                return true;
            }
            return false;
        });

        // Bấm vào ô tìm kiếm cũng mở luôn (UX nhanh)
        et.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) openSearchResults(et.getText().toString().trim());
        });
    }

    /** Mở SearchResultsActivity với query (có thể rỗng) */
    private void openSearchResults(String query) {
        Intent intent = new Intent(MainActivity.this, SearchResultsActivity.class);
        intent.putExtra(SearchResultsActivity.EXTRA_SEARCH_QUERY, query);
        startActivity(intent);
    }

    // -------------------------------------------------------
    // Firestore places section – setup UI (gọi 1 lần trong onCreate)
    // -------------------------------------------------------
    private void setupPlacesRecyclerView() {
        rvPlaces         = findViewById(R.id.rvPlaces);
        txtPlacesLoading = findViewById(R.id.txtPlacesLoading);
        txtPlacesEmpty   = findViewById(R.id.txtPlacesEmpty);
        txtPlacesError   = findViewById(R.id.txtPlacesError);

        if (rvPlaces == null) return; // layout không có RecyclerView thì bỏ qua

        // Setup RecyclerView một lần duy nhất
        placeAdapter = new PlaceAdapter(this);
        rvPlaces.setLayoutManager(new LinearLayoutManager(this));
        rvPlaces.setAdapter(placeAdapter);

        // Click item → mở PlaceDetailActivity với documentId thật
        placeAdapter.setOnPlaceClickListener(placeId -> openPlaceDetail(placeId));
    }

    // -------------------------------------------------------
    // Load places từ Firestore – gọi trong onResume để luôn cập nhật
    // -------------------------------------------------------
    private void loadPlacesFromFirestore() {
        // Guard: adapter chưa được setup (rvPlaces null) thì bỏ qua
        if (placeAdapter == null || rvPlaces == null) return;

        // Hiển thị trạng thái loading
        showPlacesState(PlacesState.LOADING);

        // Load collection "places" từ Firestore
        db.collection(FirebaseConstants.COLLECTION_PLACES)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Place> activeList = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // Lọc isActive:
                        //   - isActive == false  → bỏ qua (tạm khóa)
                        //   - isActive == true hoặc field không tồn tại → hiển thị
                        Boolean isActive = doc.getBoolean("isActive");
                        if (Boolean.FALSE.equals(isActive)) {
                            continue;
                        }

                        // Map document sang Place object
                        Place place = doc.toObject(Place.class);
                        if (place != null) {
                            // @DocumentId thường tự fill, nhưng fallback thủ công
                            if (place.getId() == null || place.getId().isEmpty()) {
                                place.setId(doc.getId());
                            }
                            activeList.add(place);
                        }
                    }

                    // setPlaces() đã clear list cũ trước khi add mới (tránh duplicate)
                    if (activeList.isEmpty()) {
                        placeAdapter.setPlaces(null); // clear adapter
                        showPlacesState(PlacesState.EMPTY);
                    } else {
                        placeAdapter.setPlaces(activeList);
                        showPlacesState(PlacesState.LOADED);
                    }
                })
                .addOnFailureListener(e -> {
                    String errMsg = "⚠️ Không tải được địa điểm: " + e.getMessage();
                    if (txtPlacesError != null) {
                        txtPlacesError.setText(errMsg);
                    }
                    showPlacesState(PlacesState.ERROR);
                    Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show();
                });
    }

    /** Trạng thái hiển thị của section địa điểm */
    private enum PlacesState { LOADING, LOADED, EMPTY, ERROR }

    private void showPlacesState(PlacesState state) {
        if (txtPlacesLoading == null) return;
        txtPlacesLoading.setVisibility(state == PlacesState.LOADING ? View.VISIBLE : View.GONE);
        txtPlacesEmpty  .setVisibility(state == PlacesState.EMPTY   ? View.VISIBLE : View.GONE);
        txtPlacesError  .setVisibility(state == PlacesState.ERROR   ? View.VISIBLE : View.GONE);
        rvPlaces        .setVisibility(state == PlacesState.LOADED  ? View.VISIBLE : View.GONE);
    }

    // -------------------------------------------------------
    // Open PlaceDetailActivity with a Firestore documentId
    // -------------------------------------------------------
    /** Mở PlaceDetailActivity với placeId (documentId thật từ Firestore) */
    private void openPlaceDetail(String placeId) {
        Intent intent = new Intent(MainActivity.this, PlaceDetailActivity.class);
        intent.putExtra(PlaceDetailActivity.EXTRA_PLACE_ID, placeId);
        startActivity(intent);
    }

    /** Mở ChatDetailActivity với chatId (demo cũ – giữ lại để không break) */
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