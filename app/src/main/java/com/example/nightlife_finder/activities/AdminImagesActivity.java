package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.nightlife_finder.R;
import com.example.nightlife_finder.constants.FirebaseConstants;
import com.example.nightlife_finder.firebase.FirebaseManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * AdminImagesActivity – Quản lý imageUrl của các địa điểm trong Firestore.
 * Load collection "places", hiển thị danh sách thật, cho phép sửa imageUrl.
 * Dùng set(..., SetOptions.merge()) để đảm bảo không ghi đè field khác.
 * Không upload Firebase Storage. Không chọn ảnh từ máy.
 */
public class AdminImagesActivity extends BaseActivity {

    private static final String TAG = "AdminImagesActivity";

    private LinearLayout adminImagesList;
    private ProgressBar loadingBar;
    private TextView tvEmpty;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#1A1A1F"));
        getWindow().setNavigationBarColor(Color.parseColor("#0F0F12"));
        setContentView(R.layout.activity_admin_images);

        db = FirebaseManager.getInstance().getFirestore();

        adminImagesList = findViewById(R.id.adminImagesList);
        loadingBar      = findViewById(R.id.loadingBarImages);
        tvEmpty         = findViewById(R.id.tvEmptyImages);

        findViewById(R.id.btnBackImages).setOnClickListener(v -> finish());

        // Nút "+" ẩn: chức năng thêm ảnh không được yêu cầu
        View btnAdd = findViewById(R.id.btnAddImage);
        if (btnAdd != null) btnAdd.setVisibility(View.GONE);

        loadPlaces();
    }

    // -------------------------------------------------------
    // Load danh sách địa điểm từ Firestore
    // -------------------------------------------------------
    private void loadPlaces() {
        showLoading(true);
        adminImagesList.removeAllViews();
        tvEmpty.setVisibility(View.GONE);

        db.collection(FirebaseConstants.COLLECTION_PLACES)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    showLoading(false);

                    int count = 0;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        // Bỏ qua địa điểm isActive == false
                        // Nếu field isActive không tồn tại → coi là active
                        Boolean isActive = doc.getBoolean("isActive");
                        if (Boolean.FALSE.equals(isActive)) continue;

                        String placeId  = doc.getId();
                        String name     = doc.getString(FirebaseConstants.FIELD_NAME);
                        String category = doc.getString(FirebaseConstants.FIELD_CATEGORY);
                        String address  = doc.getString(FirebaseConstants.FIELD_ADDRESS);
                        String imageUrl = doc.getString(FirebaseConstants.FIELD_IMAGE_URL);

                        // Validate placeId ngay lúc load
                        if (placeId == null || placeId.isEmpty()) {
                            Log.w(TAG, "Bỏ qua document thiếu ID: " + name);
                            continue;
                        }

                        adminImagesList.addView(
                                createImageCard(placeId, name, category, address, imageUrl)
                        );
                        count++;
                    }

                    if (count == 0) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Lỗi load places", e);
                    Toast.makeText(this,
                            "Lỗi tải dữ liệu: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // -------------------------------------------------------
    // Tạo card cho mỗi địa điểm
    // -------------------------------------------------------
    private LinearLayout createImageCard(String placeId, String name,
                                         String category, String address,
                                         String imageUrl) {
        // --- Outer card ---
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundResource(R.drawable.admin_module_bg);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(16));
        card.setLayoutParams(cardParams);

        // --- Thumbnail ---
        ImageView imgThumb = new ImageView(this);
        imgThumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgThumb.setBackgroundColor(Color.parseColor("#242430"));
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(dp(90), dp(90));
        imgParams.setMargins(0, 0, dp(12), 0);
        imgThumb.setLayoutParams(imgParams);
        loadThumbnail(imgThumb, imageUrl);
        card.addView(imgThumb);

        // --- Info column ---
        LinearLayout infoCol = new LinearLayout(this);
        infoCol.setOrientation(LinearLayout.VERTICAL);
        infoCol.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        // Name
        TextView tvName = new TextView(this);
        tvName.setText(name != null ? name : "—");
        tvName.setTextColor(Color.WHITE);
        tvName.setTextSize(14);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        tvName.setMaxLines(1);
        tvName.setEllipsize(TextUtils.TruncateAt.END);
        infoCol.addView(tvName);

        // Category
        TextView tvCat = new TextView(this);
        tvCat.setText(category != null ? category : "—");
        tvCat.setTextColor(Color.parseColor("#9CA0AA"));
        tvCat.setTextSize(12);
        tvCat.setPadding(0, dp(2), 0, 0);
        infoCol.addView(tvCat);

        // Address (nếu có)
        if (address != null && !address.isEmpty()) {
            TextView tvAddr = new TextView(this);
            tvAddr.setText(address);
            tvAddr.setTextColor(Color.parseColor("#6B7080"));
            tvAddr.setTextSize(11);
            tvAddr.setPadding(0, dp(2), 0, 0);
            tvAddr.setMaxLines(1);
            tvAddr.setEllipsize(TextUtils.TruncateAt.END);
            infoCol.addView(tvAddr);
        }

        // imageUrl preview label
        String urlLabel = (imageUrl != null && !imageUrl.isEmpty()) ? imageUrl : "(chưa có)";
        TextView tvUrl = new TextView(this);
        tvUrl.setText("🖼 " + urlLabel);
        tvUrl.setTextColor(Color.parseColor("#A020F0"));
        tvUrl.setTextSize(11);
        tvUrl.setPadding(0, dp(4), 0, dp(8));
        tvUrl.setMaxLines(1);
        tvUrl.setEllipsize(TextUtils.TruncateAt.END);
        infoCol.addView(tvUrl);

        // Button row
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView btnEdit = makeChip("✏ Sửa imageUrl", "#00BFFF", 0);
        btnEdit.setOnClickListener(v ->
                showEditDialog(placeId, name, imgThumb, tvUrl));
        btnRow.addView(btnEdit);

        TextView btnView = makeChip("👁 Xem", "#2C2C38", dp(10));
        btnView.setOnClickListener(v -> openPlaceDetail(placeId));
        btnRow.addView(btnView);

        infoCol.addView(btnRow);
        card.addView(infoCol);

        return card;
    }

    // -------------------------------------------------------
    // Load thumbnail: key → drawable, URL → Glide, null/khác → placeholder
    // -------------------------------------------------------
    private void loadThumbnail(ImageView iv, String imageUrl) {
        int drawableRes = keyToDrawable(imageUrl);
        if (drawableRes != 0) {
            iv.setImageResource(drawableRes);
        } else if (imageUrl != null &&
                (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.diner)
                    .error(R.drawable.diner)
                    .centerCrop()
                    .into(iv);
        } else {
            iv.setImageResource(R.drawable.diner);
        }
    }

    private int keyToDrawable(String key) {
        if (key == null) return 0;
        switch (key.toLowerCase()) {
            case "bar":    return R.drawable.bar;
            case "burger": return R.drawable.burger;
            case "pizza":  return R.drawable.pizza;
            case "diner":  return R.drawable.diner;
            case "sushi":  return R.drawable.sushi;
            default:       return 0;
        }
    }

    // -------------------------------------------------------
    // Dialog sửa imageUrl
    // Đọc imageUrl hiện tại từ Firestore trước khi mở dialog
    // để đảm bảo luôn hiện giá trị mới nhất, không dùng cache local.
    // -------------------------------------------------------
    private void showEditDialog(String placeId, String placeName,
                                ImageView thumbToUpdate,
                                TextView urlLabelToUpdate) {
        // Validate placeId trước khi mở dialog
        if (placeId == null || placeId.isEmpty()) {
            Log.e(TAG, "showEditDialog: placeId null/rỗng cho place: " + placeName);
            Toast.makeText(this, "Không tìm thấy ID địa điểm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Đọc imageUrl hiện tại từ Firestore để prefill EditText
        db.collection(FirebaseConstants.COLLECTION_PLACES)
                .document(placeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String currentUrl = null;
                    if (documentSnapshot.exists()) {
                        currentUrl = documentSnapshot.getString(FirebaseConstants.FIELD_IMAGE_URL);
                    }
                    openEditDialog(placeId, placeName, currentUrl, thumbToUpdate, urlLabelToUpdate);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Không đọc được document, mở dialog với giá trị rỗng", e);
                    // Vẫn mở dialog dù không đọc được – không block người dùng
                    openEditDialog(placeId, placeName, null, thumbToUpdate, urlLabelToUpdate);
                });
    }

    /** Mở AlertDialog thật sau khi đã có currentUrl */
    private void openEditDialog(String placeId, String placeName,
                                String currentUrl,
                                ImageView thumbToUpdate,
                                TextView urlLabelToUpdate) {
        // Root layout của dialog
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(16), dp(24), dp(16));

        // Hint
        TextView tvHint = new TextView(this);
        tvHint.setText("Nhập URL ảnh hoặc key local:");
        tvHint.setTextColor(Color.parseColor("#9CA0AA"));
        tvHint.setTextSize(12);
        root.addView(tvHint);

        // EditText nhập imageUrl – prefill bằng giá trị Firestore hiện tại
        EditText edtImageUrl = new EditText(this);
        edtImageUrl.setText(currentUrl != null ? currentUrl : "");
        edtImageUrl.setHint("pizza, bar, diner, burger, sushi hoặc https://...");
        LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        etParams.setMargins(0, dp(8), 0, dp(12));
        edtImageUrl.setLayoutParams(etParams);
        root.addView(edtImageUrl);

        // Quick choice label
        TextView tvQuick = new TextView(this);
        tvQuick.setText("Chọn nhanh:");
        tvQuick.setTextColor(Color.parseColor("#9CA0AA"));
        tvQuick.setTextSize(11);
        root.addView(tvQuick);

        // Quick choice chips
        LinearLayout quickRow = new LinearLayout(this);
        quickRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams qrParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        qrParams.setMargins(0, dp(6), 0, 0);
        quickRow.setLayoutParams(qrParams);

        String[] keys = {"pizza", "bar", "diner", "burger", "sushi"};
        for (int i = 0; i < keys.length; i++) {
            final String k = keys[i];
            TextView chip = makeChip(k, "#2C2C38", i == 0 ? 0 : dp(8));
            chip.setOnClickListener(v -> edtImageUrl.setText(k));
            quickRow.addView(chip);
        }
        root.addView(quickRow);

        // Build AlertDialog – KHÔNG set background transparent
        // để hệ thống render button Lưu/Hủy đúng cách
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Cập nhật hình ảnh")
                .setView(root)
                .setPositiveButton("Lưu", null)   // null → không auto-dismiss
                .setNegativeButton("Hủy", null)
                .create();

        // Gắn listener SAU khi show() để button đã được inflate và render
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String value = edtImageUrl.getText().toString().trim();
            if (value.isEmpty()) {
                value = "diner";
            }
            Log.d(TAG, "Lưu imageUrl: placeId=" + placeId + ", newUrl=" + value);
            // Disable nút để tránh bấm nhiều lần
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            updatePlaceImageUrl(placeId, value, thumbToUpdate, urlLabelToUpdate, dialog);
        });
    }

    // -------------------------------------------------------
    // Update Firestore dùng set(..., SetOptions.merge())
    // Đảm bảo chỉ ghi đúng field imageUrl, không xóa field khác.
    // -------------------------------------------------------
    private void updatePlaceImageUrl(String placeId, String newUrl,
                                     ImageView thumbToUpdate,
                                     TextView urlLabelToUpdate,
                                     AlertDialog dialog) {
        // Validate lần cuối trước khi gọi Firestore
        if (placeId == null || placeId.isEmpty()) {
            Log.e(TAG, "updatePlaceImageUrl: placeId null/rỗng, bỏ qua update");
            Toast.makeText(this, "Không tìm thấy ID địa điểm", Toast.LENGTH_SHORT).show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("imageUrl", newUrl);   // Dùng tên field literal để chắc chắn đúng

        Log.d(TAG, "Ghi Firestore: places/" + placeId + " → imageUrl=" + newUrl);

        db.collection(FirebaseConstants.COLLECTION_PLACES)
                .document(placeId)
                .set(updates, SetOptions.merge())          // merge: không ghi đè field khác
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Firestore update thành công: " + placeId);
                    dialog.dismiss();
                    Toast.makeText(this, "Đã cập nhật hình ảnh", Toast.LENGTH_SHORT).show();
                    // Reload toàn bộ danh sách để UI đồng bộ với Firestore thật
                    loadPlaces();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore update thất bại: " + placeId, e);
                    // Re-enable nút để người dùng thử lại
                    if (dialog.isShowing()) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    }
                    Toast.makeText(this,
                            "Lỗi cập nhật: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // -------------------------------------------------------
    // Mở PlaceDetailActivity để xem trước
    // -------------------------------------------------------
    private void openPlaceDetail(String placeId) {
        Intent intent = new Intent(this, PlaceDetailActivity.class);
        intent.putExtra(PlaceDetailActivity.EXTRA_PLACE_ID, placeId);
        startActivity(intent);
    }

    // -------------------------------------------------------
    // UI helpers
    // -------------------------------------------------------
    private void showLoading(boolean show) {
        loadingBar.setVisibility(show ? View.VISIBLE : View.GONE);
        tvEmpty.setVisibility(View.GONE);
    }

    /** Tạo chip/button nhỏ dạng TextView */
    private TextView makeChip(String text, String bgHex, int marginStartPx) {
        TextView chip = new TextView(this);
        chip.setText(text);
        chip.setTextColor(Color.WHITE);
        chip.setTextSize(11);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(12), dp(6), dp(12), dp(6));

        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        shape.setCornerRadius(dp(10));
        shape.setColor(Color.parseColor(bgHex));
        chip.setBackground(shape);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(marginStartPx, 0, 0, 0);
        chip.setLayoutParams(p);
        return chip;
    }

    /** dp → px */
    private int dp(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
