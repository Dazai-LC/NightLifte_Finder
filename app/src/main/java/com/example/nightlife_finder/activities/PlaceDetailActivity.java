package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nightlife_finder.R;
import com.example.nightlife_finder.constants.FirebaseConstants;
import com.example.nightlife_finder.firebase.FirebaseManager;
import com.example.nightlife_finder.models.Place;
import com.example.nightlife_finder.repositories.FavoriteRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * PlaceDetailActivity – Màn chi tiết địa điểm.
 * Nhận PLACE_ID qua Intent extra, load từ Firestore collection "places".
 * Có nút Yêu thích (toggle) và nút Chỉ đường (Google Maps Intent).
 */
public class PlaceDetailActivity extends BaseActivity {

    public static final String EXTRA_PLACE_ID = "PLACE_ID";

    private TextView placeName;
    private TextView placeCategory;
    private TextView placeAddress;
    private TextView placeOpenTime;
    private TextView placeHeroEmoji;
    private ImageView placeHeroImage;
    private TextView btnFavorite;
    private TextView btnDirections;
    private TextView btnBack;
    private TextView txtInactiveWarning;

    private FirebaseFirestore db;
    private FavoriteRepository favoriteRepository;

    private String placeId;
    private Place currentPlace;
    private boolean isFavorited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSystemBars();
        setContentView(R.layout.activity_place_detail);

        db = FirebaseManager.getInstance().getFirestore();
        favoriteRepository = new FavoriteRepository();

        placeId = getIntent().getStringExtra(EXTRA_PLACE_ID);
        if (placeId == null || placeId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy địa điểm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupButtons();
        loadPlaceDetail();
    }

    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.parseColor("#0F0F12"));
        getWindow().setNavigationBarColor(Color.parseColor("#0F0F12"));
    }

    private void bindViews() {
        placeName       = findViewById(R.id.placeName);
        placeCategory   = findViewById(R.id.placeCategory);
        placeAddress    = findViewById(R.id.placeAddress);
        placeOpenTime   = findViewById(R.id.placeOpenTime);
        placeHeroEmoji  = findViewById(R.id.placeHeroEmoji);
        placeHeroImage  = findViewById(R.id.placeHeroImage);
        btnFavorite     = findViewById(R.id.btnFavorite);
        btnDirections   = findViewById(R.id.btnDirections);
        btnBack         = findViewById(R.id.btnBack);
        txtInactiveWarning = findViewById(R.id.txtInactiveWarning);
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> finish());

        btnFavorite.setOnClickListener(v -> toggleFavorite());

        btnDirections.setOnClickListener(v -> openDirections());
    }

    // -------------------------------------------------------
    // Load place data từ Firestore
    // -------------------------------------------------------
    private void loadPlaceDetail() {
        db.collection(FirebaseConstants.COLLECTION_PLACES)
                .document(placeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "Địa điểm không tồn tại", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    Place place = documentSnapshot.toObject(Place.class);
                    if (place != null) {
                        currentPlace = place;
                        renderPlace(place);
                        
                        if (Boolean.FALSE.equals(documentSnapshot.getBoolean("isActive"))) {
                            txtInactiveWarning.setVisibility(android.view.View.VISIBLE);
                            btnFavorite.setVisibility(android.view.View.GONE);
                        } else {
                            checkFavoriteStatus();
                        }
                        
                        saveToHistory(placeId); // ghi lịch sử
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Lưu placeId vào SharedPreferences (tối đa 10 địa điểm gần đây).
     * Dùng LinkedHashSet để giữ thứ tự và tránh trùng lặp.
     */
    private void saveToHistory(String pid) {
        android.content.SharedPreferences prefs =
                getSharedPreferences("nightlife_history", MODE_PRIVATE);
        java.util.Set<String> existing =
                prefs.getStringSet("recent_place_ids", new java.util.LinkedHashSet<>());

        // Tạo LinkedHashSet mới để có thể thêm và giới hạn 10
        java.util.LinkedHashSet<String> updated = new java.util.LinkedHashSet<>();
        updated.add(pid); // thêm mới nhất lên đầu
        for (String id : existing) {
            if (updated.size() >= 10) break;
            updated.add(id);
        }

        prefs.edit().putStringSet("recent_place_ids", updated).apply();
    }


    private void renderPlace(Place place) {
        // Name
        String name = place.getName() != null ? place.getName() : "Địa điểm";
        placeName.setText(name);

        // Category chip
        String cat = place.getCategory() != null ? place.getCategory() : "Ẩm thực";
        placeCategory.setText(cat);

        // Address
        placeAddress.setText(place.getAddress() != null ? place.getAddress() : "Chưa có địa chỉ");

        // Open time
        String ot = place.getOpenTime() != null ? place.getOpenTime() : "Chưa rõ";
        placeOpenTime.setText(ot);

        // Hero image / emoji
        String imageUrl = place.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // imageUrl là emoji (vì DatabaseSeeder dùng emoji string như "bunbo", "bbq"...)
            // Nếu là URL thực, dùng Glide/Picasso. Ở đây dùng emoji fallback
            placeHeroEmoji.setText(categoryToEmoji(imageUrl));
        } else {
            placeHeroEmoji.setText(categoryToEmoji(cat));
        }
    }

    /** Chuyển category hoặc imageUrl sang emoji đại diện */
    private String categoryToEmoji(String key) {
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

    // -------------------------------------------------------
    // Kiểm tra trạng thái yêu thích
    // -------------------------------------------------------
    private void checkFavoriteStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        favoriteRepository.isFavorite(user.getUid(), placeId, result -> {
            isFavorited = result;
            updateFavoriteButton();
        });
    }

    private void updateFavoriteButton() {
        if (btnFavorite == null) return;
        if (isFavorited) {
            btnFavorite.setText("♥  Đã thích");
            btnFavorite.setTextColor(Color.parseColor("#FF4444"));
            btnFavorite.setBackgroundResource(R.drawable.chip_active_bg);
        } else {
            btnFavorite.setText("♡  Yêu thích");
            btnFavorite.setTextColor(Color.WHITE);
            btnFavorite.setBackgroundResource(R.drawable.chip_bg);
        }
    }

    // -------------------------------------------------------
    // Toggle favorite
    // -------------------------------------------------------
    private void toggleFavorite() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = user.getUid();
        if (isFavorited) {
            removeFavoriteCall(uid);
        } else {
            addFavoriteCall(uid);
        }
    }

    private void addFavoriteCall(String uid) {
        favoriteRepository.addFavorite(uid, placeId, new com.example.nightlife_finder.interfaces.OnFavoriteListener() {
            @Override public void onSuccess() {
                isFavorited = true;
                updateFavoriteButton();
                Toast.makeText(PlaceDetailActivity.this, "Đã thêm vào yêu thích ♥", Toast.LENGTH_SHORT).show();
            }
            @Override public void onError(String error) {
                Toast.makeText(PlaceDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeFavoriteCall(String uid) {
        favoriteRepository.removeFavorite(uid, placeId, new com.example.nightlife_finder.interfaces.OnFavoriteListener() {
            @Override public void onSuccess() {
                isFavorited = false;
                updateFavoriteButton();
                Toast.makeText(PlaceDetailActivity.this, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
            }
            @Override public void onError(String error) {
                Toast.makeText(PlaceDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // -------------------------------------------------------
    // Chỉ đường bằng Google Maps Intent
    // -------------------------------------------------------
    private void openDirections() {
        if (currentPlace == null) {
            Toast.makeText(this, "Chưa tải xong dữ liệu địa điểm", Toast.LENGTH_SHORT).show();
            return;
        }

        Double lat = currentPlace.getLat();
        Double lng = currentPlace.getLng();

        if (lat == null || lng == null || (lat == 0 && lng == 0)) {
            Toast.makeText(this, "Địa điểm chưa có tọa độ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Google Maps Intent: geo:lat,lng?q=lat,lng(label)
        String label = Uri.encode(currentPlace.getName() != null ? currentPlace.getName() : "Địa điểm");
        Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(" + label + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Fallback: mở trình duyệt với Google Maps URL
            Uri browserUri = Uri.parse("https://maps.google.com/?q=" + lat + "," + lng);
            startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
        }
    }
}
