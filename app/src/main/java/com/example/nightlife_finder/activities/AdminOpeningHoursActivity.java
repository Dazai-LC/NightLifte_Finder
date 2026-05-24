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

public class AdminOpeningHoursActivity extends BaseActivity {

    private FirebaseFirestore db;
    private LinearLayout adminHoursList;
    private List<DocumentSnapshot> allPlaces = new ArrayList<>();
    private EditText edtSearchHours;
    private String currentFilter = "Tất cả";
    private String currentSearchText = "";

    private TextView filterAll, filterAllNight, filterLateNight, filterLocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#1A1A1F"));
        getWindow().setNavigationBarColor(Color.parseColor("#0F0F12"));
        setContentView(R.layout.activity_admin_opening_hours);

        db = FirebaseFirestore.getInstance();
        adminHoursList = findViewById(R.id.adminHoursList);
        edtSearchHours = findViewById(R.id.edtSearchHours);

        findViewById(R.id.btnBackHours).setOnClickListener(v -> finish());

        setupFilters();
        setupSearch();
        loadPlaces();
    }

    private void setupSearch() {
        if (edtSearchHours != null) {
            edtSearchHours.addTextChangedListener(new TextWatcher() {
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
        filterAllNight = findViewById(R.id.filterAllNight);
        filterLateNight = findViewById(R.id.filterLateNight);
        filterLocked = findViewById(R.id.filterLocked);

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
        if (filterAllNight != null) filterAllNight.setOnClickListener(filterListener);
        if (filterLateNight != null) filterLateNight.setOnClickListener(filterListener);
        if (filterLocked != null) filterLocked.setOnClickListener(filterListener);
    }

    private void resetFilters() {
        TextView[] filters = {filterAll, filterAllNight, filterLateNight, filterLocked};
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
        adminHoursList.removeAllViews();
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
            String otLower = openTime.toLowerCase();
            
            Boolean isActiveObj = doc.getBoolean("isActive");
            boolean isActive = isActiveObj == null ? true : isActiveObj;

            if (currentFilter.equals("Tất cả")) {
                matchFilter = true;
            } else if (currentFilter.equals("Mở cả đêm")) {
                if (otLower.contains("cả đêm") || otLower.contains("24")) {
                    matchFilter = true;
                }
            } else if (currentFilter.equals("Mở đến 02:00+")) {
                if (otLower.contains("02") || otLower.contains("03") || otLower.contains("04") || otLower.contains("05")) {
                    matchFilter = true;
                }
            } else if (currentFilter.equals("Tạm khóa")) {
                matchFilter = !isActive;
            }

            if (matchFilter) {
                adminHoursList.addView(createHourCard(doc));
            }
        }
    }

    private LinearLayout createHourCard(DocumentSnapshot doc) {
        String placeId = doc.getId();
        String name = doc.getString("name");
        String category = doc.getString("category");
        String address = doc.getString("address");
        String openTime = doc.getString("openTime");
        
        if (name == null) name = "Không có tên";
        if (category == null) category = "N/A";
        if (address == null) address = "N/A";
        if (openTime == null) openTime = "N/A";
        
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

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        TextView tvName = new TextView(this);
        tvName.setText(name);
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

        // Details
        TextView tvCat = new TextView(this);
        tvCat.setText("Loại: " + category);
        tvCat.setTextColor(Color.parseColor("#9CA0AA"));
        tvCat.setTextSize(13);
        tvCat.setPadding(0, 8, 0, 0);
        card.addView(tvCat);

        TextView tvAddr = new TextView(this);
        tvAddr.setText("Địa chỉ: " + address);
        tvAddr.setTextColor(Color.parseColor("#9CA0AA"));
        tvAddr.setTextSize(13);
        card.addView(tvAddr);

        TextView tvHours = new TextView(this);
        tvHours.setText("Giờ mở cửa: " + openTime);
        tvHours.setTextColor(Color.parseColor("#00BFFF"));
        tvHours.setTextSize(13);
        tvHours.setPadding(0, 4, 0, 16);
        card.addView(tvHours);

        // Buttons row
        LinearLayout btnLayout = new LinearLayout(this);
        btnLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView btnUpdate = createButton("Cập nhật", "#A020F0");
        btnUpdate.setOnClickListener(v -> showUpdateDialog(doc));

        TextView btnView = createButton("Xem", "#242430");
        btnView.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlaceDetailActivity.class);
            intent.putExtra("PLACE_ID", placeId);
            startActivity(intent);
        });

        btnLayout.addView(btnUpdate);
        btnLayout.addView(btnView);

        card.addView(btnLayout);

        return card;
    }

    private TextView createButton(String text, String bgColor) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(12);
        btn.setGravity(android.view.Gravity.CENTER);
        btn.setPadding(0, 24, 0, 24);
        
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        shape.setCornerRadius(16);
        shape.setColor(Color.parseColor(bgColor));
        btn.setBackground(shape);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        params.setMargins(0, 0, text.equals("Cập nhật") ? 16 : 0, 0);
        btn.setLayoutParams(params);

        return btn;
    }

    private void showUpdateDialog(DocumentSnapshot doc) {
        String placeId = doc.getId();
        String currentName = doc.getString("name");
        String currentOpenTime = doc.getString("openTime");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Giờ HĐ: " + (currentName != null ? currentName : "N/A"));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        EditText edtOpenTime = new EditText(this);
        edtOpenTime.setHint("Nhập giờ hoạt động mới");
        if (currentOpenTime != null) {
            edtOpenTime.setText(currentOpenTime);
        }
        layout.addView(edtOpenTime);

        // Quick options
        String[] options = {"Mở cả đêm", "Mở đến 01:30", "Mở đến 02:30", "Mở đến 03:00", "Mở đến 04:00"};
        for (String opt : options) {
            TextView tvOpt = new TextView(this);
            tvOpt.setText("+ " + opt);
            tvOpt.setTextColor(Color.parseColor("#A020F0"));
            tvOpt.setPadding(0, 16, 0, 16);
            tvOpt.setOnClickListener(v -> edtOpenTime.setText(opt));
            layout.addView(tvOpt);
        }

        builder.setView(layout);
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newTime = edtOpenTime.getText().toString().trim();
            if (newTime.isEmpty()) newTime = "Mở cả đêm";

            Map<String, Object> data = new HashMap<>();
            data.put("openTime", newTime);

            db.collection("places").document(placeId).set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã cập nhật giờ hoạt động", Toast.LENGTH_SHORT).show();
                    loadPlaces();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                });
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
}
