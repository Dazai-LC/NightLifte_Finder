package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nightlife_finder.R;
import com.example.nightlife_finder.constants.FirebaseConstants;
import com.example.nightlife_finder.firebase.FirebaseManager;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FavoriteActivity extends BaseActivity {

    private TextView tabFavorite;
    private TextView tabHistory;
    private TextView txtFavoriteSubtitle;
    private TextView btnViewMode;

    private View favoriteScroll;
    private View historyScroll;

    private LinearLayout favoriteList;
    private LinearLayout historyList;

    private boolean compactMode = false;
    /** Category filter: rỗng = tất cả, không rỗng = lọc theo category đó */
    private String currentFilter = "";
    private TextView btnFilter; // giữ ref để cập nhật text

    private final List<Place> favoritePlaces = new ArrayList<>();
    private final List<Place> historyPlaces = new ArrayList<>();

    private static class Place {
        String title;
        String status;
        String statusColor;
        String[] tags;
        String meta;
        String crowdText;
        int crowd;
        int imageRes;
        boolean available;
        // Dữ liệu place thật từ Firestore
        String placeId;
        String category;
        String address;
        String openTime;
        String imageUrl;
        Double lat;
        Double lng;

        Place(String title, String status, String statusColor, String[] tags,
              String meta, String crowdText, int crowd, int imageRes,
              boolean available,
              String placeId, String category, String address, String openTime,
              String imageUrl, Double lat, Double lng) {
            this.title = title;
            this.status = status;
            this.statusColor = statusColor;
            this.tags = tags;
            this.meta = meta;
            this.crowdText = crowdText;
            this.crowd = crowd;
            this.imageRes = imageRes;
            this.available = available;
            this.placeId = placeId;
            this.category = category;
            this.address = address;
            this.openTime = openTime;
            this.imageUrl = imageUrl;
            this.lat = lat;
            this.lng = lng;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSystemBars();
        setContentView(R.layout.activity_favorite);

        bindViews();
        prepareData();
        setupTabs();
        setupTopButtons();
        setupBottomNavigation();

        renderFavorites();
        // History chỉ load khi người dùng bấm tab
    }

    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.parseColor("#0F0F12"));
        getWindow().setNavigationBarColor(Color.parseColor("#1A1A1F"));
    }

    private void bindViews() {
        tabFavorite = findViewById(R.id.tabFavorite);
        tabHistory = findViewById(R.id.tabHistory);
        txtFavoriteSubtitle = findViewById(R.id.txtFavoriteSubtitle);
        btnViewMode = findViewById(R.id.btnViewMode);
        btnFilter = findViewById(R.id.btnFilterFavorites);

        favoriteScroll = findViewById(R.id.favoriteScroll);
        historyScroll = findViewById(R.id.historyScroll);

        favoriteList = findViewById(R.id.favoriteList);
        historyList = findViewById(R.id.historyList);
    }

    private void prepareData() {
        favoritePlaces.clear();

        com.google.firebase.auth.FirebaseUser user = new com.example.nightlife_finder.repositories.AuthRepository().getCurrentUser();
        if (user != null) {
            new com.example.nightlife_finder.repositories.FavoriteRepository().getFavorites(user.getUid(), new com.example.nightlife_finder.interfaces.OnPlaceLoadedListener() {
                @Override
                public void onSuccess(List<com.example.nightlife_finder.models.Place> places) {
                    favoritePlaces.clear();
                    for (com.example.nightlife_finder.models.Place p : places) {
                        String status = "🟢 Còn chỗ";
                        String color = "#32CD32";
                        int crowd = 25;
                        boolean available = true;
                        if (p.getName().contains("Lẩu")) {
                            status = "🔴 Đông";
                            color = "#FF4D5A";
                            crowd = 80;
                            available = false;
                        } else if (p.getName().contains("Nhậu") || p.getName().contains("bbq")) {
                            status = "🟡 Đang đông";
                            color = "#FFB84D";
                            crowd = 60;
                            available = false;
                        }

                        String[] tags = new String[]{p.getCategory(), "⭐ 4.7", "🌙 Khuya"};

                        int imgRes = R.drawable.bar;
                        if ("bunbo".equals(p.getImageUrl())) {
                            imgRes = R.drawable.burger;
                        } else if ("lau".equals(p.getImageUrl())) {
                            imgRes = R.drawable.sushi;
                        } else if ("pizza".equals(p.getImageUrl())) {
                            imgRes = R.drawable.pizza;
                        } else if ("diner".equals(p.getImageUrl())) {
                            imgRes = R.drawable.diner;
                        } else if ("bbq".equals(p.getImageUrl()) || "bar".equals(p.getImageUrl())) {
                            imgRes = R.drawable.bar;
                        }

                        favoritePlaces.add(new Place(
                                p.getName(),
                                status,
                                color,
                                tags,
                                "⭐ 4.8   •   📍 1.2 km   •   ⏱ " + p.getOpenTime(),
                                crowd + " %",
                                crowd,
                                imgRes,
                                available,
                                p.getId(),
                                p.getCategory(),
                                p.getAddress(),
                                p.getOpenTime(),
                                p.getImageUrl(),
                                p.getLat(),
                                p.getLng()
                        ));
                    }
                    renderFavorites();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(FavoriteActivity.this, "Lỗi tải yêu thích: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Không còn hardcode history – load từ SharedPreferences khi cần
        historyPlaces.clear();
    }

    private void setupTabs() {
        tabFavorite.setOnClickListener(v -> showFavoriteTab());
        tabHistory.setOnClickListener(v -> showHistoryTab());
    }

    private void showFavoriteTab() {
        favoriteScroll.setVisibility(View.VISIBLE);
        historyScroll.setVisibility(View.GONE);

        tabFavorite.setTextColor(Color.WHITE);
        tabHistory.setTextColor(Color.parseColor("#9CA0AA"));

        tabFavorite.setBackgroundResource(R.drawable.favorite_tab_active_bg);
        tabHistory.setBackgroundResource(0);

        txtFavoriteSubtitle.setText("12 địa điểm đã lưu");
    }

    private void showHistoryTab() {
        favoriteScroll.setVisibility(View.GONE);
        historyScroll.setVisibility(View.VISIBLE);

        tabFavorite.setTextColor(Color.parseColor("#9CA0AA"));
        tabHistory.setTextColor(Color.WHITE);

        tabFavorite.setBackgroundResource(0);
        tabHistory.setBackgroundResource(R.drawable.favorite_tab_active_bg);

        // Load history từ SharedPreferences mỗi lần mở tab
        loadHistoryFromPrefs();
    }

    private void setupTopButtons() {
        // Nút lọc – hiển thị AlertDialog chọn category
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> showFilterDialog());
        }

        // Nút toggle card / danh sách gọn
        if (btnViewMode != null) {
            btnViewMode.setText("⊞");
            btnViewMode.setOnClickListener(v -> {
                compactMode = !compactMode;
                if (compactMode) {
                    btnViewMode.setText("☰");
                    Toast.makeText(this, "Chế độ: Danh sách gọn", Toast.LENGTH_SHORT).show();
                } else {
                    btnViewMode.setText("⊞");
                    Toast.makeText(this, "Chế độ: Thẻ lớn", Toast.LENGTH_SHORT).show();
                }
                renderFavorites();
                renderHistory();
            });
        }
    }

    // -------------------------------------------------------
    // Dialog chọn bộ lọc category
    // -------------------------------------------------------
    private void showFilterDialog() {
        final String[] labels = {
                "Tất cả", "Bún bò", "Lẩu", "Pizza", "Trà sữa", "Nướng", "Cơm", "Phở"
        };
        final String[] values = {
                "", "Bún bò", "Lẩu", "Pizza", "Trà sữa", "Nướng", "Cơm", "Phở"
        };
        int currentIndex = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(currentFilter)) { currentIndex = i; break; }
        }
        new AlertDialog.Builder(this)
                .setTitle("Lọc theo loại đồ ăn")
                .setSingleChoiceItems(labels, currentIndex, (dialog, which) -> {
                    currentFilter = values[which];
                    if (btnFilter != null) {
                        btnFilter.setText(currentFilter.isEmpty() ? "🔽 Lọc" : "🔽 " + currentFilter);
                    }
                    dialog.dismiss();
                    renderFavorites();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void renderFavorites() {
        favoriteList.removeAllViews();

        String header = currentFilter.isEmpty() ? "ĐÃ LƯU GẦN ĐÂY" : "ĐÃ LƯU – " + currentFilter.toUpperCase();
        addSectionHeader(favoriteList, header, "Xem cả");

        int count = 0;

        for (Place place : favoritePlaces) {
            // Lọc theo category nếu đã chọn filter
            if (!currentFilter.isEmpty()) {
                String cat = place.category != null ? place.category : "";
                if (!cat.equalsIgnoreCase(currentFilter)) continue;
            }

            favoriteList.addView(createFavoriteCard(place));
            count++;
        }

        if (count == 0) {
            if (favoritePlaces.isEmpty()) {
                addEmptyText(favoriteList, "👋 Chưa có địa điểm yêu thích nào.\nHãy bấm ♥ trên trang chi tiết quán!");
            } else {
                addEmptyText(favoriteList, "🔍 Không có địa điểm nào thuộc loại \"" + currentFilter + "\".");
            }
        }

        // Cập nhật subtitle
        txtFavoriteSubtitle.setText(favoritePlaces.size() + " địa điểm đã lưu");
    }

    private void renderHistory() {
        historyList.removeAllViews();
        addSectionHeader(historyList, "MỚN GẦN ĐÂY", "");

        if (historyPlaces.isEmpty()) {
            addEmptyText(historyList,
                    "📍 Lịch sử xem địa điểm sẽ được ghi nhận\nkhi bạn mở trang chi tiết quán.");
            txtFavoriteSubtitle.setText("Chưa có lịch sử");
            return;
        }

        for (Place place : historyPlaces) {
            historyList.addView(createHistoryCard(place));
        }
        txtFavoriteSubtitle.setText(historyPlaces.size() + " quán đã xem gần đây");
    }

    // -------------------------------------------------------
    // Load lịch sử từ SharedPreferences và query Firestore
    // -------------------------------------------------------
    private void loadHistoryFromPrefs() {
        historyPlaces.clear();
        renderHistory(); // hiển empty state trước

        SharedPreferences prefs = getSharedPreferences("nightlife_history", MODE_PRIVATE);
        Set<String> recentIds = prefs.getStringSet("recent_place_ids", new java.util.HashSet<>());

        if (recentIds == null || recentIds.isEmpty()) {
            txtFavoriteSubtitle.setText("Chưa có lịch sử");
            return;
        }

        // Load từng place từ Firestore
        com.google.firebase.firestore.FirebaseFirestore db = FirebaseManager.getInstance().getFirestore();
        final int total = recentIds.size();
        final int[] loaded = {0};

        for (String pid : recentIds) {
            db.collection(FirebaseConstants.COLLECTION_PLACES)
                    .document(pid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        loaded[0]++;
                        if (doc.exists()) {
                            com.example.nightlife_finder.models.Place p = doc.toObject(com.example.nightlife_finder.models.Place.class);
                            if (p != null) {
                                if (p.getId() == null || p.getId().isEmpty()) p.setId(doc.getId());

                                int imgRes = R.drawable.bar;
                                if ("bunbo".equals(p.getImageUrl()))      imgRes = R.drawable.burger;
                                else if ("lau".equals(p.getImageUrl()))  imgRes = R.drawable.sushi;
                                else if ("pizza".equals(p.getImageUrl())) imgRes = R.drawable.pizza;
                                else if ("diner".equals(p.getImageUrl())) imgRes = R.drawable.diner;

                                historyPlaces.add(new Place(
                                        p.getName(),
                                        p.getCategory() != null ? p.getCategory() : "",
                                        "#9CA0AA",
                                        new String[]{p.getCategory() != null ? p.getCategory() : ""},
                                        p.getAddress() != null ? p.getAddress() : "",
                                        "", 0, imgRes, true,
                                        p.getId(), p.getCategory(), p.getAddress(),
                                        p.getOpenTime(), p.getImageUrl(), p.getLat(), p.getLng()
                                ));
                            }
                        }
                        if (loaded[0] >= total) {
                            renderHistory();
                        }
                    })
                    .addOnFailureListener(e -> {
                        loaded[0]++;
                        if (loaded[0] >= total) renderHistory();
                    });
        }
    }

    private void addSectionHeader(LinearLayout parent, String title, String action) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        parent.addView(row, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(42)
        ));

        TextView left = new TextView(this);
        left.setText(title);
        left.setTextColor(Color.parseColor("#9CA0AA"));
        left.setTextSize(12);
        left.setTypeface(null, android.graphics.Typeface.BOLD);

        row.addView(left, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));

        if (!action.isEmpty()) {
            TextView right = new TextView(this);
            right.setText(action);
            right.setTextColor(Color.parseColor("#A020F0"));
            right.setTextSize(13);
            right.setTypeface(null, android.graphics.Typeface.BOLD);
            right.setOnClickListener(v -> Toast.makeText(this, "Demo xem tất cả", Toast.LENGTH_SHORT).show());

            row.addView(right);
        }
    }

    private MaterialCardView createFavoriteCard(Place place) {
        MaterialCardView card = new MaterialCardView(this);
        card.setRadius(dp(18));
        card.setStrokeWidth(dp(1));
        card.setStrokeColor(Color.parseColor("#4B167C"));
        card.setCardBackgroundColor(Color.parseColor("#1A1A1F"));
        card.setCardElevation(0);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                compactMode ? dp(250) : dp(380)
        );
        cardParams.setMargins(0, 0, 0, dp(18));
        card.setLayoutParams(cardParams);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        card.addView(root);

        FrameLayout imageArea = new FrameLayout(this);
        root.addView(imageArea, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                compactMode ? dp(90) : dp(150)
        ));

        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setImageResource(place.imageRes);
        imageArea.addView(image, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        View overlay = new View(this);
        overlay.setBackgroundColor(Color.parseColor("#55000000"));
        imageArea.addView(overlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        TextView status = new TextView(this);
        status.setText(place.status);
        status.setTextColor(Color.WHITE);
        status.setTextSize(12);
        status.setTypeface(null, android.graphics.Typeface.BOLD);
        status.setGravity(Gravity.CENTER);
        status.setPadding(dp(12), 0, dp(12), 0);
        status.setBackgroundResource(R.drawable.favorite_status_bg);

        FrameLayout.LayoutParams statusParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                dp(30)
        );
        statusParams.leftMargin = dp(14);
        statusParams.topMargin = dp(12);
        imageArea.addView(status, statusParams);

        TextView heart = new TextView(this);
        heart.setText("❤");
        heart.setTextColor(Color.parseColor("#FF304F"));
        heart.setTextSize(22);
        heart.setGravity(Gravity.CENTER);
        heart.setBackgroundResource(R.drawable.favorite_heart_bg);
        heart.setOnClickListener(v -> Toast.makeText(this, "Đã lưu " + place.title, Toast.LENGTH_SHORT).show());

        FrameLayout.LayoutParams heartParams = new FrameLayout.LayoutParams(dp(44), dp(44));
        heartParams.gravity = Gravity.END;
        heartParams.topMargin = dp(12);
        heartParams.rightMargin = dp(14);
        imageArea.addView(heart, heartParams);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(14), dp(16), dp(12));

        root.addView(content, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        TextView title = new TextView(this);
        title.setText(place.title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(19);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        content.addView(title);

        LinearLayout tagRow = new LinearLayout(this);
        tagRow.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams tagRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(34)
        );
        tagRowParams.topMargin = dp(8);
        content.addView(tagRow, tagRowParams);

        for (String tag : place.tags) {
            TextView tagView = new TextView(this);
            tagView.setText(tag);
            tagView.setTextColor(Color.WHITE);
            tagView.setTextSize(12);
            tagView.setGravity(Gravity.CENTER);
            tagView.setPadding(dp(12), 0, dp(12), 0);
            tagView.setBackgroundResource(R.drawable.favorite_tag_bg);

            LinearLayout.LayoutParams tagParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dp(28)
            );
            tagParams.rightMargin = dp(8);
            tagRow.addView(tagView, tagParams);
        }

        TextView meta = new TextView(this);
        meta.setText(place.meta);
        meta.setTextColor(Color.parseColor("#CDD0DA"));
        meta.setTextSize(13);
        content.addView(meta);

        LinearLayout crowdRow = new LinearLayout(this);
        crowdRow.setOrientation(LinearLayout.HORIZONTAL);
        crowdRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams crowdRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(28)
        );
        crowdRowParams.topMargin = dp(8);
        content.addView(crowdRow, crowdRowParams);

        TextView crowdLabel = new TextView(this);
        crowdLabel.setText("Mức độ đông đúc");
        crowdLabel.setTextColor(Color.parseColor("#8A8A95"));
        crowdLabel.setTextSize(12);
        crowdRow.addView(crowdLabel, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView crowdValue = new TextView(this);
        crowdValue.setText(place.crowdText);
        crowdValue.setTextColor(Color.parseColor("#CDD0DA"));
        crowdValue.setTextSize(12);
        crowdRow.addView(crowdValue);

        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(place.crowd);
        progressBar.setProgressTintList(ColorStateList.valueOf(getCrowdColor(place.crowd)));
        progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2D2D3A")));
        content.addView(progressBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(6)
        ));

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams buttonRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48)
        );
        buttonRowParams.topMargin = dp(12);
        content.addView(buttonRow, buttonRowParams);

        TextView directionBtn = createOutlineButton("⌖  Chỉ đường");
        directionBtn.setOnClickListener(v -> openDirections(place));

        TextView chatBtn = createFilledButton("▱  Hộp thoại");
        chatBtn.setOnClickListener(v -> openNewChatForPlace(place));

        buttonRow.addView(directionBtn, new LinearLayout.LayoutParams(0, dp(42), 1));

        Space space = new Space(this);
        buttonRow.addView(space, new LinearLayout.LayoutParams(dp(10), 1));

        buttonRow.addView(chatBtn, new LinearLayout.LayoutParams(0, dp(42), 1));

        return card;
    }

    private MaterialCardView createHistoryCard(Place place) {
        MaterialCardView card = new MaterialCardView(this);
        card.setRadius(dp(18));
        card.setStrokeWidth(dp(1));
        card.setStrokeColor(Color.parseColor("#2D2D3A"));
        card.setCardBackgroundColor(Color.parseColor("#1A1A1F"));
        card.setCardElevation(0);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                compactMode ? dp(120) : dp(150)
        );
        cardParams.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(cardParams);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.addView(row);

        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setImageResource(place.imageRes);
        row.addView(image, new LinearLayout.LayoutParams(
                compactMode ? dp(62) : dp(76),
                compactMode ? dp(62) : dp(76)
        ));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(14), 0, 0, 0);

        row.addView(info, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView title = new TextView(this);
        title.setText(place.title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(17);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        info.addView(title);

        TextView status = new TextView(this);
        status.setText(place.status);
        status.setTextColor(Color.parseColor("#8A8A95"));
        status.setTextSize(12);
        info.addView(status);

        TextView meta = new TextView(this);
        meta.setText(place.meta);
        meta.setTextColor(Color.parseColor("#FFD84D"));
        meta.setTextSize(12);
        info.addView(meta);

        TextView chat = createFilledButton("Nhắn");
        chat.setOnClickListener(v -> openNewChatForPlace(place));
        row.addView(chat, new LinearLayout.LayoutParams(dp(72), dp(36)));

        return card;
    }

    private TextView createOutlineButton(String text) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextColor(Color.parseColor("#A020F0"));
        btn.setTextSize(13);
        btn.setTypeface(null, android.graphics.Typeface.BOLD);
        btn.setGravity(Gravity.CENTER);
        btn.setBackgroundResource(R.drawable.quick_action_bg);
        return btn;
    }

    private TextView createFilledButton(String text) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(13);
        btn.setTypeface(null, android.graphics.Typeface.BOLD);
        btn.setGravity(Gravity.CENTER);
        btn.setBackgroundResource(R.drawable.favorite_filled_btn);
        return btn;
    }

    private void addEmptyText(LinearLayout parent, String message) {
        TextView text = new TextView(this);
        text.setText(message);
        text.setTextColor(Color.parseColor("#9CA0AA"));
        text.setTextSize(14);
        text.setGravity(Gravity.CENTER);
        parent.addView(text, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(120)
        ));
    }

    private int getCrowdColor(int crowd) {
        if (crowd <= 35) {
            return Color.parseColor("#32CD32");
        }

        if (crowd <= 65) {
            return Color.parseColor("#FFB84D");
        }

        return Color.parseColor("#FF4D5A");
    }

    // -------------------------------------------------------
    // Mở Google Maps Intent để chỉ đường đến place
    // -------------------------------------------------------
    private void openDirections(Place place) {
        if (place.lat == null || place.lng == null || (place.lat == 0 && place.lng == 0)) {
            // Không có tọa độ – fallback tìm kiếm theo địa chỉ
            String query = place.address != null ? place.address : place.title;
            Uri browserUri = Uri.parse("https://maps.google.com/?q=" + Uri.encode(query));
            startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
            return;
        }
        double lat = place.lat;
        double lng = place.lng;
        String label = Uri.encode(place.title != null ? place.title : "Địa điểm");
        Uri gmmUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(" + label + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Uri browserUri = Uri.parse("https://maps.google.com/?q=" + lat + "," + lng);
            startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
        }
    }

    // -------------------------------------------------------
    // Mở NewChatActivity với dữ liệu place thật
    // -------------------------------------------------------
    private void openNewChatForPlace(Place place) {
        Intent intent = new Intent(FavoriteActivity.this, NewChatActivity.class);
        intent.putExtra("PLACE_ID",        place.placeId);
        intent.putExtra("PLACE_NAME",      place.title);
        intent.putExtra("PLACE_CATEGORY",  place.category);
        intent.putExtra("PLACE_ADDRESS",   place.address);
        intent.putExtra("PLACE_OPEN_TIME", place.openTime);
        intent.putExtra("PLACE_IMAGE_URL", place.imageUrl);
        if (place.lat != null) intent.putExtra("PLACE_LAT", place.lat);
        if (place.lng != null) intent.putExtra("PLACE_LNG", place.lng);
        startActivity(intent);
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(FavoriteActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        findViewById(R.id.navMap).setOnClickListener(v -> {
            startActivity(new Intent(FavoriteActivity.this, MapActivity.class));
        });

        findViewById(R.id.navChat).setOnClickListener(v -> {
            startActivity(new Intent(FavoriteActivity.this, ChatActivity.class));
        });

        findViewById(R.id.navFavorite).setOnClickListener(v -> {
            Toast.makeText(this, "Bạn đang ở trang Đã lưu", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}