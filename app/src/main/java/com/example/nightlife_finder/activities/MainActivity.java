package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.nightlife_finder.R;
import com.example.nightlife_finder.utils.AppSettings;

import java.io.File;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSystemBars();
        setContentView(R.layout.activity_main);

        loadHomeAvatar();
        setupHomeClicks();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyGlobalUi();
        loadHomeAvatar();
    }

    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.parseColor("#0F0F12"));
        getWindow().setNavigationBarColor(Color.parseColor("#1A1A1F"));
    }

    private void loadHomeAvatar() {
        ImageView imgHomeAvatar = findViewById(R.id.imgHomeAvatar);

        if (imgHomeAvatar == null) {
            return;
        }

        String avatarPath = AppSettings.getAvatarPath(this);

        if (avatarPath != null && !avatarPath.isEmpty()) {
            File file = new File(avatarPath);

            if (file.exists()) {
                imgHomeAvatar.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                return;
            }
        }

        imgHomeAvatar.setImageResource(R.drawable.logo);
    }

    private void setupHomeClicks() {

        // PHỞ ĐÊM
        bindClickIfExists("cardPhoDem", v -> openMapToPlace(
                "Phở Gà 24H",
                "food",
                "Quán ăn / Phở gà",
                "Khu trung tâm Thường Tín, Hà Nội",
                "Mở 18:00 - 03:00",
                20.8712,
                105.8643,
                92
        ));

        // BÁNH MÌ CHẢO
        bindClickIfExists("cardBanhMiChao", v -> openMapToPlace(
                "Bánh Mì Chảo Cô Ba",
                "food",
                "Quán ăn / Bánh mì chảo",
                "Khu thị trấn Thường Tín, Hà Nội",
                "Mở 17:00 - 01:30",
                20.8705,
                105.8628,
                88
        ));

        // CƠM RANG KHUYA
        bindClickIfExists("cardComRang", v -> openMapToPlace(
                "Cơm Rang Dưa bò Cô Thanh",
                "food",
                "Quán ăn / Cơm rang",
                "Gần Quốc lộ 1A, Thường Tín, Hà Nội",
                "Mở 18:00 - 02:00",
                20.8724,
                105.8661,
                86
        ));

        // LẨU BÒ ĐÊM
        bindClickIfExists("cardLauBoDem", v -> openMapToPlace(
                "Lẩu Bò Neon",
                "food",
                "Quán ăn / Lẩu bò",
                "Khu Vân Tảo, Thường Tín, Hà Nội",
                "Mở 17:00 - 03:30",
                20.8915,
                105.8693,
                94
        ));

        // BẠCH TUỘC NƯỚNG
        bindClickIfExists("cardBachTuocNuong", v -> openMapToPlace(
                "Bạch Tuộc Nướng",
                "food",
                "Quán ăn / Hải sản nướng",
                "Khu trung tâm Thường Tín, Hà Nội",
                "Mở 18:00 - 01:00",
                20.8732,
                105.8619,
                90
        ));

        // BURGER ĐÊM
        bindClickIfExists("cardBurgerDem", v -> openMapToPlace(
                "Burger Velocity",
                "food",
                "Quán ăn / Burger",
                "Gần ga Thường Tín, Hà Nội",
                "Mở 10:00 - 00:30",
                20.8698,
                105.8604,
                82
        ));

        // CHÁO SƯỜN
        bindClickIfExists("cardChaoSuon", v -> openMapToPlace(
                "Cháo Sườn Starlight",
                "food",
                "Quán ăn / Cháo sườn",
                "Khu thị trấn Thường Tín, Hà Nội",
                "Mở 17:00 - 02:00",
                20.8718,
                105.8625,
                89
        ));

        // PIZZA ĐÊM
        bindClickIfExists("cardPizzaDem", v -> openMapToPlace(
                "Pizza Crimson",
                "food",
                "Quán ăn / Pizza",
                "Khu trung tâm Thường Tín, Hà Nội",
                "Mở 10:00 - 00:30",
                20.8704,
                105.8661,
                80
        ));

        // SUSHI KHUYA
        bindClickIfExists("cardSushiKhuya", v -> openMapToPlace(
                "Sushi Tokyo",
                "food",
                "Quán ăn / Sushi",
                "Khu trung tâm Thường Tín, Hà Nội",
                "Mở 17:00 - 00:30",
                20.8724,
                105.8628,
                84
        ));

        // LẨU ĐÊM NEON
        bindClickIfExists("cardLauDemNeon", v -> openMapToPlace(
                "Lẩu Đêm Neon",
                "food",
                "Quán ăn / Lẩu đêm",
                "Khu Vân Tảo, Thường Tín, Hà Nội",
                "Mở 17:00 - 03:30",
                20.8915,
                105.8693,
                95
        ));
        // MÌ CAY ĐÊM
        bindClickIfExists("cardMiCay", v -> openMapToPlace(
                "Mì Cay Seoul",
                "food",
                "Quán ăn / Mì cay",
                "Khu trung tâm Thường Tín, Hà Nội",
                "Mở 17:00 - 02:00",
                20.8729,
                105.8648,
                91
        ));

// BÚN ĐẬU KHUYA
        bindClickIfExists("cardBunDau", v -> openMapToPlace(
                "Bún Đậu ",
                "food",
                "Quán ăn / Bún đậu",
                "Khu thị trấn Thường Tín, Hà Nội",
                "Mở 16:00 - 00:30",
                20.8710,
                105.8617,
                84
        ));

// XÔI GÀ ĐÊM
        bindClickIfExists("cardXoiDem", v -> openMapToPlace(
                "Xôi Gà ",
                "food",
                "Quán ăn / Xôi gà",
                "Gần Quốc lộ 1A, Thường Tín, Hà Nội",
                "Mở 20:00 - 04:00",
                20.8694,
                105.8649,
                93
        ));

// GÀ RÁN KHUYA
        bindClickIfExists("cardGaRan", v -> openMapToPlace(
                "Gà Rán ",
                "food",
                "Quán ăn / Gà rán",
                "Khu trung tâm Thường Tín, Hà Nội",
                "Mở 10:00 - 01:00",
                20.8708,
                105.8668,
                85
        ));

// MÌ XÀO BÒ
        bindClickIfExists("cardMiXaoBo", v -> openMapToPlace(
                "Mì Xào Bò Thanh Nghị",
                "food",
                "Quán ăn / Mì xào",
                "Khu phố ga Thường Tín, Hà Nội",
                "Mở 17:00 - 01:30",
                20.8689,
                105.8609,
                87
        ));

// BÁNH TRÁNG TRỘN
        bindClickIfExists("cardBanhTrang", v -> openMapToPlace(
                "Bánh Tráng Trộn",
                "food",
                "Ăn vặt / Bánh tráng",
                "Khu trung tâm Thường Tín, Hà Nội",
                "Mở 15:00 - 01:00",
                20.8721,
                105.8631,
                83
        ));

// BÚN BÒ ĐÊM
        bindClickIfExists("cardBunBo", v -> openMapToPlace(
                "Bún Bò Huế",
                "food",
                "Quán ăn / Bún bò",
                "Khu Hà Hồi, Thường Tín, Hà Nội",
                "Mở 18:00 - 02:00",
                20.8586,
                105.8555,
                88
        ));

// TRÀ CHANH ĐÊM
        bindClickIfExists("cardTraChanh", v -> openMapToPlace(
                "Lola Tea",
                "tea",
                "Trà chanh / Đồ uống đêm",
                "Khu Quất Động, Thường Tín, Hà Nội",
                "Mở 15:00 - 01:30",
                20.8864,
                105.8556,
                82
        ));

// ỐC ĐÊM SỐT CAY
        bindClickIfExists("cardOcDem", v -> openMapToPlace(
                "Ốc Sốt Cay",
                "food",
                "Quán ăn / Hải sản ốc",
                "Khu trung tâm Thường Tín, Hà Nội",
                "Mở 17:00 - 02:30",
                20.8735,
                105.8657,
                90
        ));

// CƠM TẤM ĐÊM
        bindClickIfExists("cardComTam", v -> openMapToPlace(
                "Cơm Tấm",
                "food",
                "Quán ăn / Cơm tấm",
                "Gần 60 QL1A, Thường Tín, Hà Nội",
                "Mở 18:00 - 01:00",
                20.8702,
                105.8639,
                86
        ));
    }

    private void bindClickIfExists(String idName, View.OnClickListener listener) {
        int id = getResources().getIdentifier(idName, "id", getPackageName());

        if (id == 0) {
            return;
        }

        View view = findViewById(id);

        if (view == null) {
            return;
        }

        view.setClickable(true);
        view.setFocusable(true);
        view.setOnClickListener(listener);
    }

    private void openMapToPlace(
            String name,
            String category,
            String type,
            String address,
            String openingHours,
            double latitude,
            double longitude,
            int nightScore
    ) {
        Intent intent = new Intent(MainActivity.this, MapActivity.class);

        intent.putExtra("target_name", name);
        intent.putExtra("target_category", category);
        intent.putExtra("target_type", type);
        intent.putExtra("target_address", address);
        intent.putExtra("target_opening_hours", openingHours);
        intent.putExtra("target_latitude", latitude);
        intent.putExtra("target_longitude", longitude);
        intent.putExtra("target_night_score", nightScore);

        startActivity(intent);
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Toast.makeText(this, "Bạn đang ở Trang chủ", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.navMap).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MapActivity.class));
        });

        findViewById(R.id.navChat).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ChatActivity.class));
        });

        findViewById(R.id.navFavorite).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, FavoriteActivity.class));
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });
    }
}