package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nightlife_finder.R;
import com.example.nightlife_finder.constants.FirebaseConstants;
import com.example.nightlife_finder.firebase.FirebaseManager;
import com.example.nightlife_finder.models.Place;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * SearchResultsActivity
 * - Load toàn bộ collection "places" từ Firestore một lần.
 * - Lọc local theo query (name / category / address / openTime).
 * - Filter chips: Tất cả / Bún bò / Lẩu / Pizza / Trà sữa / Nướng.
 * - Bấm item mở PlaceDetailActivity.
 */
public class SearchResultsActivity extends BaseActivity {

    public static final String EXTRA_SEARCH_QUERY = "SEARCH_QUERY";

    // -------------------------------------------------------
    // Views
    // -------------------------------------------------------
    private TextView btnBack;
    private EditText etSearchQuery;
    private LinearLayout resultContainer;
    private TextView txtResultCount;

    private TextView chipAll, chipBunBo, chipLau, chipPizza, chipTraSua, chipNuong;
    private TextView activeChip;

    // -------------------------------------------------------
    // Data
    // -------------------------------------------------------
    private final List<Place> allPlaces = new ArrayList<>();
    private String currentQuery = "";
    private String currentCategory = ""; // "" = tất cả

    private FirebaseFirestore db;

    // -------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSystemBars();
        setContentView(R.layout.activity_search_results);

        db = FirebaseManager.getInstance().getFirestore();

        bindViews();
        setupBackButton();
        setupChips();
        setupSearchInput();

        // Nhận query từ Home nếu có
        String incomingQuery = getIntent().getStringExtra(EXTRA_SEARCH_QUERY);
        if (incomingQuery != null && !incomingQuery.isEmpty()) {
            currentQuery = incomingQuery.trim();
            etSearchQuery.setText(currentQuery);
            etSearchQuery.setSelection(currentQuery.length());
        }

        loadPlacesFromFirestore();
    }

    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.parseColor("#0F0F12"));
        getWindow().setNavigationBarColor(Color.parseColor("#1A1A1F"));
    }

    // -------------------------------------------------------
    // Bind views
    // -------------------------------------------------------
    private void bindViews() {
        btnBack         = findViewById(R.id.btnSearchBack);
        etSearchQuery   = findViewById(R.id.etSearchQuery);
        resultContainer = findViewById(R.id.resultContainer);
        txtResultCount  = findViewById(R.id.txtResultCount);

        chipAll    = findViewById(R.id.chipAll);
        chipBunBo  = findViewById(R.id.chipBunBo);
        chipLau    = findViewById(R.id.chipLau);
        chipPizza  = findViewById(R.id.chipPizza);
        chipTraSua = findViewById(R.id.chipTraSua);
        chipNuong  = findViewById(R.id.chipNuong);

        activeChip = chipAll;
    }

    // -------------------------------------------------------
    // Back button
    // -------------------------------------------------------
    private void setupBackButton() {
        btnBack.setOnClickListener(v -> finish());
    }

    // -------------------------------------------------------
    // Search input listener
    // -------------------------------------------------------
    private void setupSearchInput() {
        // Lọc live khi gõ
        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().trim();
                applyFilter();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Bấm Search trên keyboard
        etSearchQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                currentQuery = etSearchQuery.getText().toString().trim();
                applyFilter();
                return true;
            }
            return false;
        });
    }

    // -------------------------------------------------------
    // Filter chips
    // -------------------------------------------------------
    private void setupChips() {
        chipAll.setOnClickListener(v    -> selectChip(chipAll, ""));
        chipBunBo.setOnClickListener(v  -> selectChip(chipBunBo, "Bún bò"));
        chipLau.setOnClickListener(v    -> selectChip(chipLau, "Lẩu"));
        chipPizza.setOnClickListener(v  -> selectChip(chipPizza, "Pizza"));
        chipTraSua.setOnClickListener(v -> selectChip(chipTraSua, "Trà sữa"));
        chipNuong.setOnClickListener(v  -> selectChip(chipNuong, "Nướng"));
    }

    private void selectChip(TextView chip, String category) {
        // Reset chip cũ
        activeChip.setBackgroundResource(R.drawable.chip_bg);
        activeChip.setTextColor(Color.parseColor("#AAAAAA"));

        // Active chip mới
        chip.setBackgroundResource(R.drawable.chip_active_bg);
        chip.setTextColor(Color.WHITE);
        activeChip = chip;

        currentCategory = category;
        applyFilter();
    }

    // -------------------------------------------------------
    // Load từ Firestore (một lần duy nhất)
    // -------------------------------------------------------
    private void loadPlacesFromFirestore() {
        txtResultCount.setText("Đang tải...");
        db.collection(FirebaseConstants.COLLECTION_PLACES)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allPlaces.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Place place = doc.toObject(Place.class);
                        // @DocumentId được set tự động khi toObject(), nhưng set thêm để chắc
                        if (place.getId() == null || place.getId().isEmpty()) {
                            place.setId(doc.getId());
                        }
                        allPlaces.add(place);
                    }
                    applyFilter();
                })
                .addOnFailureListener(e -> {
                    txtResultCount.setText("Lỗi tải dữ liệu");
                    Toast.makeText(this, "Không thể tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // -------------------------------------------------------
    // Lọc local theo query + category
    // -------------------------------------------------------
    private void applyFilter() {
        List<Place> filtered = new ArrayList<>();
        String queryLower = currentQuery.toLowerCase();

        for (Place p : allPlaces) {
            // --- Filter theo category chip ---
            if (!currentCategory.isEmpty()) {
                String cat = p.getCategory() != null ? p.getCategory() : "";
                if (!cat.equalsIgnoreCase(currentCategory)) continue;
            }

            // --- Filter theo text query ---
            if (!queryLower.isEmpty()) {
                boolean match = contains(p.getName(), queryLower)
                        || contains(p.getCategory(), queryLower)
                        || contains(p.getAddress(), queryLower)
                        || contains(p.getOpenTime(), queryLower);
                if (!match) continue;
            }

            filtered.add(p);
        }

        renderResults(filtered);
    }

    private boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    // -------------------------------------------------------
    // Render danh sách kết quả vào LinearLayout
    // -------------------------------------------------------
    private void renderResults(List<Place> places) {
        resultContainer.removeAllViews();

        // Cập nhật label số kết quả
        int count = places.size();
        if (currentQuery.isEmpty() && currentCategory.isEmpty()) {
            txtResultCount.setText(count + " địa điểm");
        } else {
            txtResultCount.setText("Tìm thấy " + count + " kết quả");
        }

        if (count == 0) {
            TextView empty = makeEmptyView();
            resultContainer.addView(empty);
            return;
        }

        for (Place place : places) {
            android.view.View card = makePlaceCard(place);
            resultContainer.addView(card);
        }
    }

    // -------------------------------------------------------
    // Tạo card cho mỗi địa điểm (programmatic)
    // -------------------------------------------------------
    private android.view.View makePlaceCard(Place place) {
        // Outer card (MaterialCardView không dùng được dễ dàng programmatically — dùng CardView)
        androidx.cardview.widget.CardView card = new androidx.cardview.widget.CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        card.setLayoutParams(cardParams);
        card.setRadius(dpToPx(18));
        card.setCardElevation(0);
        card.setCardBackgroundColor(Color.parseColor("#1A1A1F"));

        // Inner row
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14));

        // Emoji avatar
        TextView emoji = new TextView(this);
        emoji.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(54), dpToPx(54)));
        emoji.setGravity(Gravity.CENTER);
        emoji.setTextSize(28);
        emoji.setText(categoryToEmoji(place.getCategory(), place.getImageUrl()));
        emoji.setBackgroundColor(Color.parseColor("#232330"));
        ((LinearLayout.LayoutParams) emoji.getLayoutParams()).setMarginEnd(dpToPx(14));

        // Text column
        LinearLayout col = new LinearLayout(this);
        LinearLayout.LayoutParams colParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        col.setLayoutParams(colParams);
        col.setOrientation(LinearLayout.VERTICAL);

        // Name
        TextView name = new TextView(this);
        name.setText(place.getName() != null ? place.getName() : "—");
        name.setTextColor(Color.WHITE);
        name.setTextSize(15);
        name.setTypeface(null, android.graphics.Typeface.BOLD);

        // Category + address
        TextView sub = new TextView(this);
        String subText = (place.getCategory() != null ? place.getCategory() : "")
                + (place.getAddress() != null ? "  •  " + place.getAddress() : "");
        sub.setText(subText);
        sub.setTextColor(Color.parseColor("#9CA0AA"));
        sub.setTextSize(12);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        subParams.topMargin = dpToPx(3);
        sub.setLayoutParams(subParams);

        // Open time
        TextView openTime = new TextView(this);
        openTime.setText(place.getOpenTime() != null ? "🕐 " + place.getOpenTime() : "");
        openTime.setTextColor(Color.parseColor("#32CD32"));
        openTime.setTextSize(12);
        LinearLayout.LayoutParams otParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        otParams.topMargin = dpToPx(4);
        openTime.setLayoutParams(otParams);

        col.addView(name);
        col.addView(sub);
        col.addView(openTime);

        row.addView(emoji);
        row.addView(col);
        card.addView(row);

        // Click → PlaceDetailActivity
        String placeId = place.getId();
        card.setOnClickListener(v -> {
            if (placeId == null || placeId.isEmpty()) {
                Toast.makeText(this, "Không tìm thấy ID địa điểm", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(SearchResultsActivity.this, PlaceDetailActivity.class);
            intent.putExtra(PlaceDetailActivity.EXTRA_PLACE_ID, placeId);
            startActivity(intent);
        });

        // Ripple feedback
        android.util.TypedValue outValue = new android.util.TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        row.setForeground(getDrawable(outValue.resourceId));
        row.setClickable(false); // click trên card, không phải row

        return card;
    }

    // -------------------------------------------------------
    // Empty state view
    // -------------------------------------------------------
    private TextView makeEmptyView() {
        TextView tv = new TextView(this);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(180)));
        tv.setGravity(Gravity.CENTER);
        tv.setText("😕\nKhông tìm thấy kết quả");
        tv.setTextColor(Color.parseColor("#6A6A75"));
        tv.setTextSize(15);
        tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        return tv;
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------
    private String categoryToEmoji(String category, String imageUrl) {
        String key = (imageUrl != null && !imageUrl.isEmpty()) ? imageUrl : category;
        if (key == null) return "🍽️";
        switch (key.toLowerCase()) {
            case "bunbo":   case "bún bò":  return "🍜";
            case "bbq":     case "nướng":   return "🍢";
            case "lau":     case "lẩu":     return "🍲";
            case "pizza":                   return "🍕";
            case "che":     case "trà sữa": return "🧋";
            case "bar":                     return "🍺";
            case "diner":   case "cơm":     return "🍚";
            case "phở":                     return "🍜";
            default:                        return "🍽️";
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
