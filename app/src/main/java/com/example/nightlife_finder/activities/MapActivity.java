package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nightlife_finder.R;

public class MapActivity extends BaseActivity {

    private boolean is3DMode = true;
    private float zoomScale = 1f;

    private View mapCanvas;
    private TextView txtToggle3D;

    private TextView placeEmoji;
    private TextView placeName;
    private TextView placeInfo;
    private TextView placeRoute;

    private TextView chipBunBo;
    private TextView chipNuong;
    private TextView chipLau;
    private TextView chipTraSua;
    private TextView chipPizza;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSystemBars();
        setContentView(R.layout.activity_map);

        bindViews();
        setupMapButtons();
        setupMarkers();
        setupCategoryChips();
        setupBottomNavigation();

        applyMapTransform();
    }

    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.parseColor("#0F0F12"));
        getWindow().setNavigationBarColor(Color.parseColor("#1A1A1F"));
    }

    private void bindViews() {
        mapCanvas = findViewById(R.id.mapCanvas);
        txtToggle3D = findViewById(R.id.txtToggle3D);

        placeEmoji = findViewById(R.id.placeEmoji);
        placeName = findViewById(R.id.placeName);
        placeInfo = findViewById(R.id.placeInfo);
        placeRoute = findViewById(R.id.placeRoute);

        chipBunBo = findViewById(R.id.chipBunBo);
        chipNuong = findViewById(R.id.chipNuong);
        chipLau = findViewById(R.id.chipLau);
        chipTraSua = findViewById(R.id.chipTraSua);
        chipPizza = findViewById(R.id.chipPizza);
    }

    private void setupMapButtons() {
        findViewById(R.id.btnMyLocation).setOnClickListener(v -> {
            zoomScale = 1f;
            is3DMode = true;
            txtToggle3D.setText("3D");
            applyMapTransform();

            updatePlaceInfo(
                    "◎",
                    "Vị trí của bạn",
                    "Hồ Hoàn Kiếm • Hà Nội",
                    "Bạn đang ở khu vực trung tâm Hà Nội"
            );

            Toast.makeText(this, "Đã quay về vị trí Hồ Hoàn Kiếm, Hà Nội", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnZoomIn).setOnClickListener(v -> {
            if (zoomScale < 1.25f) {
                zoomScale += 0.08f;
                applyMapTransform();
            }
        });

        findViewById(R.id.btnZoomOut).setOnClickListener(v -> {
            if (zoomScale > 0.85f) {
                zoomScale -= 0.08f;
                applyMapTransform();
            }
        });

        findViewById(R.id.btnToggle3D).setOnClickListener(v -> {
            is3DMode = !is3DMode;

            if (is3DMode) {
                txtToggle3D.setText("3D");
                Toast.makeText(this, "Đã bật chế độ 3D demo", Toast.LENGTH_SHORT).show();
            } else {
                txtToggle3D.setText("2D");
                Toast.makeText(this, "Đã bật chế độ 2D demo", Toast.LENGTH_SHORT).show();
            }

            applyMapTransform();
        });
    }

    private void applyMapTransform() {
        mapCanvas.setScaleX(zoomScale);

        if (is3DMode) {
            mapCanvas.setRotationX(12f);
            mapCanvas.setScaleY(zoomScale * 0.94f);
        } else {
            mapCanvas.setRotationX(0f);
            mapCanvas.setScaleY(zoomScale);
        }
    }

    private void setupMarkers() {
        findViewById(R.id.markerBunBo).setOnClickListener(v -> {
            setActiveChip(chipBunBo);
            updatePlaceInfo(
                    "🍜",
                    "Bún bò Đêm Phố Cổ",
                    "Mở đến 02:30 • 0,5 km",
                    "AI gợi ý: đường nhanh nhất khoảng 5 phút"
            );
        });

        findViewById(R.id.markerNuong).setOnClickListener(v -> {
            setActiveChip(chipNuong);
            updatePlaceInfo(
                    "🍢",
                    "Đồ nướng Hàng Bạc",
                    "Mở đến 01:30 • 0,8 km",
                    "AI gợi ý: phù hợp đi nhóm 3-5 người"
            );
        });

        findViewById(R.id.markerLau).setOnClickListener(v -> {
            setActiveChip(chipLau);
            updatePlaceInfo(
                    "🍲",
                    "Lẩu khuya Hoàn Kiếm",
                    "Mở đến 03:00 • 1,1 km",
                    "AI gợi ý: đang còn bàn, nên đi ngay"
            );
        });

        findViewById(R.id.markerPizza).setOnClickListener(v -> {
            setActiveChip(chipPizza);
            updatePlaceInfo(
                    "🍕",
                    "Pizza Midnight Hà Nội",
                    "Mở đến 04:00 • 1,4 km",
                    "AI gợi ý: có ưu đãi mua 1 tặng 1"
            );
        });

        findViewById(R.id.markerTea).setOnClickListener(v -> {
            setActiveChip(chipTraSua);
            updatePlaceInfo(
                    "🧋",
                    "Trà sữa 24h Phố Cổ",
                    "Mở cả đêm • 0,6 km",
                    "AI gợi ý: thích hợp mua mang đi"
            );
        });
    }

    private void setupCategoryChips() {
        chipBunBo.setOnClickListener(v -> {
            setActiveChip(chipBunBo);
            updatePlaceInfo(
                    "🍜",
                    "Bún bò Đêm Phố Cổ",
                    "Mở đến 02:30 • 0,5 km",
                    "AI gợi ý: đường nhanh nhất khoảng 5 phút"
            );
            Toast.makeText(this, "Đang lọc: Bún bò", Toast.LENGTH_SHORT).show();
        });

        chipNuong.setOnClickListener(v -> {
            setActiveChip(chipNuong);
            updatePlaceInfo(
                    "🍢",
                    "Đồ nướng Hàng Bạc",
                    "Mở đến 01:30 • 0,8 km",
                    "AI gợi ý: phù hợp đi nhóm 3-5 người"
            );
            Toast.makeText(this, "Đang lọc: Đồ nướng", Toast.LENGTH_SHORT).show();
        });

        chipLau.setOnClickListener(v -> {
            setActiveChip(chipLau);
            updatePlaceInfo(
                    "🍲",
                    "Lẩu khuya Hoàn Kiếm",
                    "Mở đến 03:00 • 1,1 km",
                    "AI gợi ý: đang còn bàn, nên đi ngay"
            );
            Toast.makeText(this, "Đang lọc: Lẩu", Toast.LENGTH_SHORT).show();
        });

        chipTraSua.setOnClickListener(v -> {
            setActiveChip(chipTraSua);
            updatePlaceInfo(
                    "🧋",
                    "Trà sữa 24h Phố Cổ",
                    "Mở cả đêm • 0,6 km",
                    "AI gợi ý: thích hợp mua mang đi"
            );
            Toast.makeText(this, "Đang lọc: Trà sữa", Toast.LENGTH_SHORT).show();
        });

        chipPizza.setOnClickListener(v -> {
            setActiveChip(chipPizza);
            updatePlaceInfo(
                    "🍕",
                    "Pizza Midnight Hà Nội",
                    "Mở đến 04:00 • 1,4 km",
                    "AI gợi ý: có ưu đãi mua 1 tặng 1"
            );
            Toast.makeText(this, "Đang lọc: Pizza", Toast.LENGTH_SHORT).show();
        });
    }

    private void setActiveChip(TextView activeChip) {
        chipBunBo.setBackgroundResource(R.drawable.chip_bg);
        chipNuong.setBackgroundResource(R.drawable.chip_bg);
        chipLau.setBackgroundResource(R.drawable.chip_bg);
        chipTraSua.setBackgroundResource(R.drawable.chip_bg);
        chipPizza.setBackgroundResource(R.drawable.chip_bg);

        activeChip.setBackgroundResource(R.drawable.chip_active_bg);
    }

    private void updatePlaceInfo(String emoji, String name, String info, String route) {
        placeEmoji.setText(emoji);
        placeName.setText(name);
        placeInfo.setText(info);
        placeRoute.setText(route);
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(MapActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        findViewById(R.id.navMap).setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.you_are_on_map), Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.navChat).setOnClickListener(v -> {
            startActivity(new Intent(MapActivity.this, ChatActivity.class));
        });

        findViewById(R.id.navFavorite).setOnClickListener(v -> {
            startActivity(new Intent(MapActivity.this, FavoriteActivity.class));
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(MapActivity.this, ProfileActivity.class));
        });
    }
}