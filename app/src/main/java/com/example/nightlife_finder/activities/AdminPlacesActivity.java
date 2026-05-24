package com.example.nightlife_finder.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nightlife_finder.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminPlacesActivity extends BaseActivity {

    private FirebaseFirestore db;
    private LinearLayout adminPlacesList;
    private List<DocumentSnapshot> allPlaces = new ArrayList<>();
    private EditText edtSearchPlace;
    private String currentFilter = "Tất cả";
    private String currentSearchText = "";

    // View IDs
    private TextView filterAll, filterRestaurant, filterMart, filterOpen, filterClosed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#1A1A1F"));
        getWindow().setNavigationBarColor(Color.parseColor("#0F0F12"));
        setContentView(R.layout.activity_admin_places);

        db = FirebaseFirestore.getInstance();
        adminPlacesList = findViewById(R.id.adminPlacesList);
        edtSearchPlace = findViewById(R.id.edtSearchPlace);

        findViewById(R.id.btnBackPlaces).setOnClickListener(v -> finish());
        
        View btnAddPlace = findViewById(R.id.btnAddPlace);
        if (btnAddPlace != null) {
            btnAddPlace.setOnClickListener(v -> showPlaceDialog(null));
        }

        setupFilters();
        setupSearch();
        loadPlaces();
    }

    private void setupSearch() {
        if (edtSearchPlace != null) {
            edtSearchPlace.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchText = s.toString().toLowerCase();
                    applyFilters();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupFilters() {
        filterAll = findViewById(R.id.filterAll);
        filterRestaurant = findViewById(R.id.filterRestaurant);
        filterMart = findViewById(R.id.filterMart);
        filterOpen = findViewById(R.id.filterOpen);
        filterClosed = findViewById(R.id.filterClosed);

        View.OnClickListener filterListener = v -> {
            resetFilters();
            v.setBackgroundResource(R.drawable.admin_icon_bg);
            if (v instanceof TextView) {
                ((TextView) v).setTextColor(Color.WHITE);
                currentFilter = ((TextView) v).getText().toString();
            }
            applyFilters();
        };

        if (filterAll != null) filterAll.setOnClickListener(filterListener);
        if (filterRestaurant != null) filterRestaurant.setOnClickListener(filterListener);
        if (filterMart != null) filterMart.setOnClickListener(filterListener);
        if (filterOpen != null) filterOpen.setOnClickListener(filterListener);
        if (filterClosed != null) filterClosed.setOnClickListener(filterListener);
    }

    private void resetFilters() {
        TextView[] filters = {filterAll, filterRestaurant, filterMart, filterOpen, filterClosed};
        for (TextView tv : filters) {
            if (tv != null) {
                tv.setBackgroundResource(R.drawable.admin_card_bg);
                tv.setTextColor(Color.parseColor("#9CA0AA"));
            }
        }
    }

    private void loadPlaces() {
        db.collection("places").get().addOnSuccessListener(queryDocumentSnapshots -> {
            allPlaces.clear();
            allPlaces.addAll(queryDocumentSnapshots.getDocuments());
            applyFilters();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
        });
    }

    private void applyFilters() {
        adminPlacesList.removeAllViews();
        for (DocumentSnapshot doc : allPlaces) {
            String name = doc.getString("name");
            String category = doc.getString("category");
            String address = doc.getString("address");
            String openTime = doc.getString("openTime");
            
            if (name == null) name = "";
            if (category == null) category = "";
            if (address == null) address = "";
            if (openTime == null) openTime = "";

            boolean matchSearch = name.toLowerCase().contains(currentSearchText) ||
                    category.toLowerCase().contains(currentSearchText) ||
                    address.toLowerCase().contains(currentSearchText) ||
                    openTime.toLowerCase().contains(currentSearchText);

            if (!matchSearch) continue;

            boolean matchFilter = false;
            String catLower = category.toLowerCase();
            
            Boolean isActiveObj = doc.getBoolean("isActive");
            boolean isActive = isActiveObj == null ? true : isActiveObj;

            if (currentFilter.equals("Tất cả")) {
                matchFilter = true;
            } else if (currentFilter.equals("Quán ăn")) {
                if (catLower.contains("bún bò") || catLower.contains("lẩu") || catLower.contains("pizza") ||
                    catLower.contains("trà sữa") || catLower.contains("nướng") || catLower.contains("cơm") || 
                    catLower.contains("phở") || catLower.contains("quán ăn")) {
                    matchFilter = true;
                }
            } else if (currentFilter.equals("Siêu thị")) {
                if (catLower.contains("siêu thị") || catLower.contains("mini mart") || 
                    catLower.contains("store") || catLower.contains("cửa hàng")) {
                    matchFilter = true;
                }
            } else if (currentFilter.equals("Đang hiển thị")) {
                matchFilter = isActive;
            } else if (currentFilter.equals("Tạm khóa")) {
                matchFilter = !isActive;
            }

            if (matchFilter) {
                adminPlacesList.addView(createPlaceCard(doc));
            }
        }
    }

    private LinearLayout createPlaceCard(DocumentSnapshot doc) {
        String placeId = doc.getId();
        String name = doc.getString("name");
        String category = doc.getString("category");
        String address = doc.getString("address");
        String openTime = doc.getString("openTime");
        
        Boolean isActiveObj = doc.getBoolean("isActive");
        boolean isActive = isActiveObj == null ? true : isActiveObj;
        
        String status = isActive ? "Đang hiển thị" : "Tạm khóa";
        String statusColor = isActive ? "#00BFFF" : "#FF4444";

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.admin_module_bg);
        card.setPadding(32, 32, 32, 32);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 24);
        card.setLayoutParams(params);

        // Header (Name + Status)
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        TextView tvName = new TextView(this);
        tvName.setText(name != null ? name : "No name");
        tvName.setTextColor(Color.WHITE);
        tvName.setTextSize(16);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        header.addView(tvName);

        TextView tvStatus = new TextView(this);
        tvStatus.setText(status);
        tvStatus.setTextColor(Color.parseColor(statusColor));
        tvStatus.setTextSize(12);
        tvStatus.setTypeface(null, android.graphics.Typeface.BOLD);
        header.addView(tvStatus);
        card.addView(header);

        // Info
        TextView tvCat = new TextView(this);
        tvCat.setText("Loại: " + (category != null ? category : "N/A"));
        tvCat.setTextColor(Color.parseColor("#9CA0AA"));
        tvCat.setTextSize(13);
        tvCat.setPadding(0, 8, 0, 0);
        card.addView(tvCat);

        TextView tvAddr = new TextView(this);
        tvAddr.setText("Địa chỉ: " + (address != null ? address : "N/A"));
        tvAddr.setTextColor(Color.parseColor("#9CA0AA"));
        tvAddr.setTextSize(13);
        card.addView(tvAddr);

        TextView tvHours = new TextView(this);
        tvHours.setText("Giờ mở cửa: " + (openTime != null ? openTime : "N/A"));
        tvHours.setTextColor(Color.parseColor("#9CA0AA"));
        tvHours.setTextSize(13);
        card.addView(tvHours);

        // Buttons
        LinearLayout btnLayout = new LinearLayout(this);
        btnLayout.setOrientation(LinearLayout.HORIZONTAL);
        btnLayout.setPadding(0, 24, 0, 0);

        TextView btnEdit = createButton("Sửa", "#A020F0");
        TextView btnView = createButton("Xem", "#242430");
        TextView btnLock = createButton(isActive ? "Tạm khóa" : "Mở khóa", isActive ? "#FF4444" : "#4CAF50");

        btnEdit.setOnClickListener(v -> showPlaceDialog(doc));
        btnView.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlaceDetailActivity.class);
            intent.putExtra("PLACE_ID", placeId);
            startActivity(intent);
        });
        btnLock.setOnClickListener(v -> toggleLock(placeId, isActive));

        btnLayout.addView(btnEdit);
        btnLayout.addView(btnView);
        btnLayout.addView(btnLock);

        card.addView(btnLayout);

        return card;
    }

    private void toggleLock(String placeId, boolean currentActive) {
        db.collection("places").document(placeId)
            .update("isActive", !currentActive)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                loadPlaces();
            });
    }

    private TextView createButton(String text, String bgColor) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(12);
        btn.setGravity(android.view.Gravity.CENTER);
        
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        shape.setCornerRadius(16);
        shape.setColor(Color.parseColor(bgColor));
        btn.setBackground(shape);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 80, 1);
        params.setMargins(0, 0, text.contains("khóa") ? 0 : 16, 0);
        btn.setLayoutParams(params);

        return btn;
    }

    private void showPlaceDialog(DocumentSnapshot doc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        EditText edtName = new EditText(this); edtName.setHint("Name");
        EditText edtCategory = new EditText(this); edtCategory.setHint("Category");
        EditText edtAddress = new EditText(this); edtAddress.setHint("Address");
        EditText edtOpenTime = new EditText(this); edtOpenTime.setHint("Open Time");
        EditText edtImageUrl = new EditText(this); edtImageUrl.setHint("Image URL");
        EditText edtLat = new EditText(this); edtLat.setHint("Latitude");
        EditText edtLng = new EditText(this); edtLng.setHint("Longitude");

        layout.addView(edtName);
        layout.addView(edtCategory);
        layout.addView(edtAddress);
        layout.addView(edtOpenTime);
        layout.addView(edtImageUrl);
        layout.addView(edtLat);
        layout.addView(edtLng);

        if (doc != null) {
            edtName.setText(doc.getString("name"));
            edtCategory.setText(doc.getString("category"));
            edtAddress.setText(doc.getString("address"));
            edtOpenTime.setText(doc.getString("openTime"));
            edtImageUrl.setText(doc.getString("imageUrl"));
            Double lat = doc.getDouble("lat");
            Double lng = doc.getDouble("lng");
            if (lat != null) edtLat.setText(String.valueOf(lat));
            if (lng != null) edtLng.setText(String.valueOf(lng));
        }

        builder.setTitle(doc == null ? "Thêm địa điểm" : "Sửa địa điểm");
        builder.setView(layout);
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String name = edtName.getText().toString().trim();
            String cat = edtCategory.getText().toString().trim();
            String addr = edtAddress.getText().toString().trim();
            String time = edtOpenTime.getText().toString().trim();
            String img = edtImageUrl.getText().toString().trim();
            
            if (name.isEmpty() || cat.isEmpty() || addr.isEmpty()) {
                Toast.makeText(this, "Name, Category, Address không được rỗng", Toast.LENGTH_SHORT).show();
                return;
            }
            if (time.isEmpty()) time = "Mở cả đêm";
            if (img.isEmpty()) img = "diner";

            double latVal = 21.028511;
            double lngVal = 105.804817; // default Hanoi
            if (doc != null) {
                Double existingLat = doc.getDouble("lat");
                if (existingLat != null) latVal = existingLat;
                Double existingLng = doc.getDouble("lng");
                if (existingLng != null) lngVal = existingLng;
            }
            try {
                if (!edtLat.getText().toString().trim().isEmpty()) {
                    latVal = Double.parseDouble(edtLat.getText().toString().trim());
                }
            } catch (Exception e) {}
            try {
                if (!edtLng.getText().toString().trim().isEmpty()) {
                    lngVal = Double.parseDouble(edtLng.getText().toString().trim());
                }
            } catch (Exception e) {}

            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("category", cat);
            data.put("address", addr);
            data.put("openTime", time);
            data.put("imageUrl", img);
            data.put("lat", latVal);
            data.put("lng", lngVal);

            if (doc == null) {
                data.put("isActive", true);
                db.collection("places").add(data).addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Đã thêm địa điểm", Toast.LENGTH_SHORT).show();
                    loadPlaces();
                });
            } else {
                db.collection("places").document(doc.getId()).set(data, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đã cập nhật địa điểm", Toast.LENGTH_SHORT).show();
                        loadPlaces();
                    });
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
}
