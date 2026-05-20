package com.example.nightlife_finder.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.example.nightlife_finder.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends BaseActivity {

    // Vị trí mặc định khi không lấy được GPS:
    // 60 QL1A, Thường Tín, Hà Nội, Việt Nam
    private static final double DEFAULT_LAT = 20.8712;
    private static final double DEFAULT_LON = 105.8643;

    private MapView osmMap;
    private FusedLocationProviderClient fusedLocationClient;

    private TextView placeEmoji, placeName, placeType, placeDistance, placeOpenTime;
    private TextView chipAll, chipFood, chipCafe, chipTea, chipStore, chipSupermarket;

    private double userLat = DEFAULT_LAT;
    private double userLon = DEFAULT_LON;

    private String currentFilter = "all";
    private boolean focusUserAfterRender = false;

    // true = đang dùng vị trí mặc định 60 QL1A
    // false = đã lấy được vị trí hiện tại thật
    private boolean isUsingDefaultLocation = true;

    private final List<DemoPlace> demoPlaces = new ArrayList<>();

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    loadCurrentLocation();
                } else {
                    useDefaultFallbackLocation("Bạn chưa cấp quyền vị trí");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Configuration.getInstance().setUserAgentValue("NightLifeFinderDemo/1.0");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        setupSystemBars();
        bindViews();
        setupMap();
        setupButtons();
        setupBottomNavigation();

        createHaNoiDemoPlaces();

        focusUserAfterRender = true;
        requestLocationPermissionFirst();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (osmMap != null) {
            osmMap.onResume();
        }

        applyGlobalUi();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (osmMap != null) {
            osmMap.onPause();
        }
    }

    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.parseColor("#0F0F12"));
        getWindow().setNavigationBarColor(Color.parseColor("#1A1A1F"));
    }

    private void bindViews() {
        osmMap = findViewById(R.id.osmMap);

        placeEmoji = findViewById(R.id.placeEmoji);
        placeName = findViewById(R.id.placeName);
        placeType = findViewById(R.id.placeType);
        placeDistance = findViewById(R.id.placeDistance);
        placeOpenTime = findViewById(R.id.placeOpenTime);

        chipAll = findViewById(R.id.chipAll);
        chipFood = findViewById(R.id.chipFood);
        chipCafe = findViewById(R.id.chipCafe);
        chipTea = findViewById(R.id.chipTea);
        chipStore = findViewById(R.id.chipStore);
        chipSupermarket = findViewById(R.id.chipSupermarket);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void setupMap() {
        osmMap.setTileSource(TileSourceFactory.MAPNIK);
        osmMap.setMultiTouchControls(true);

        GeoPoint haNoiCenter = new GeoPoint(21.0285, 105.8542);
        osmMap.getController().setZoom(11.7);
        osmMap.getController().setCenter(haNoiCenter);
    }

    private void setupButtons() {
        findViewById(R.id.btnMyLocation).setOnClickListener(v -> {
            focusUserAfterRender = true;
            requestLocationPermissionFirst();
        });

        findViewById(R.id.btnZoomIn).setOnClickListener(v -> {
            double zoom = osmMap.getZoomLevelDouble();

            if (zoom < 20) {
                osmMap.getController().zoomTo(zoom + 1);
            }
        });

        findViewById(R.id.btnZoomOut).setOnClickListener(v -> {
            double zoom = osmMap.getZoomLevelDouble();

            if (zoom > 3) {
                osmMap.getController().zoomTo(zoom - 1);
            }
        });

        findViewById(R.id.btnReloadPlaces).setOnClickListener(v -> {
            showPlacesByFilter(currentFilter);
            Toast.makeText(this, "Đã tải lại dữ liệu demo", Toast.LENGTH_SHORT).show();
        });

        chipAll.setOnClickListener(v -> chooseFilter("all", chipAll));
        chipFood.setOnClickListener(v -> chooseFilter("food", chipFood));
        chipCafe.setOnClickListener(v -> chooseFilter("cafe", chipCafe));
        chipTea.setOnClickListener(v -> chooseFilter("tea", chipTea));
        chipStore.setOnClickListener(v -> chooseFilter("store", chipStore));
        chipSupermarket.setOnClickListener(v -> chooseFilter("supermarket", chipSupermarket));
    }

    private void chooseFilter(String filter, TextView activeChip) {
        currentFilter = filter;
        setActiveChip(activeChip);
        showPlacesByFilter(filter);
    }

    private void requestLocationPermissionFirst() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            loadCurrentLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void loadCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            useDefaultFallbackLocation("Chưa cấp quyền vị trí");
            return;
        }

        updatePlaceInfo(
                "⌖",
                "Đang lấy vị trí hiện tại...",
                "GPS / Wi-Fi / mạng di động",
                "Vui lòng chờ vài giây",
                "Nếu GPS lỗi, app sẽ dùng vị trí 60 QL1A Thường Tín"
        );

        CancellationTokenSource tokenSource = new CancellationTokenSource();

        fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null && isValidVietnamLocation(location)) {
                        userLat = location.getLatitude();
                        userLon = location.getLongitude();

                        isUsingDefaultLocation = false;
                        focusUserAfterRender = true;
                        showPlacesByFilter(currentFilter);

                        updatePlaceInfo(
                                "📍",
                                "Vị trí hiện tại",
                                "GPS đã xác nhận vị trí của bạn",
                                "Tọa độ: " + String.format(Locale.US, "%.5f, %.5f", userLat, userLon),
                                "Đang hiển thị địa điểm gần khu vực của bạn"
                        );

                        Toast.makeText(this, "Đã lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
                    } else {
                        loadLastLocationFallback();
                    }
                })
                .addOnFailureListener(e -> loadLastLocationFallback());
    }

    private void loadLastLocationFallback() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            useDefaultFallbackLocation("Chưa cấp quyền vị trí");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null && isValidVietnamLocation(location)) {
                        userLat = location.getLatitude();
                        userLon = location.getLongitude();

                        isUsingDefaultLocation = false;
                        focusUserAfterRender = true;
                        showPlacesByFilter(currentFilter);

                        updatePlaceInfo(
                                "📍",
                                "Vị trí hiện tại",
                                "Đã lấy vị trí gần nhất của bạn",
                                "Tọa độ: " + String.format(Locale.US, "%.5f, %.5f", userLat, userLon),
                                "Đang hiển thị địa điểm gần khu vực của bạn"
                        );

                        Toast.makeText(this, "Đã lấy vị trí gần nhất", Toast.LENGTH_SHORT).show();
                    } else {
                        useDefaultFallbackLocation("GPS không hợp lệ, dùng vị trí mặc định");
                    }
                })
                .addOnFailureListener(e -> useDefaultFallbackLocation("Không lấy được GPS, dùng vị trí mặc định"));
    }

    private boolean isValidVietnamLocation(Location location) {
        if (location == null) {
            return false;
        }

        double lat = location.getLatitude();
        double lon = location.getLongitude();

        return lat >= 8.0 && lat <= 24.5 && lon >= 102.0 && lon <= 110.5;
    }

    private void useDefaultFallbackLocation(String reason) {
        userLat = DEFAULT_LAT;
        userLon = DEFAULT_LON;

        isUsingDefaultLocation = true;
        focusUserAfterRender = true;
        showPlacesByFilter(currentFilter);

        updatePlaceInfo(
                "📍",
                "Vị trí hiện tại",
                "60 QL1A, Thường Tín, Hà Nội",
                "Việt Nam",
                reason
        );

        Toast.makeText(
                this,
                "Không lấy được GPS, dùng vị trí 60 QL1A Thường Tín",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void zoomToUserLocation() {
        GeoPoint userPoint = new GeoPoint(userLat, userLon);

        osmMap.getController().animateTo(userPoint);
        osmMap.getController().zoomTo(16.5);
    }

    private void createHaNoiDemoPlaces() {
        demoPlaces.clear();

        // HOÀN KIẾM
        add("Bún Bò Cô Hoa Phố Cổ", "food", "Quán ăn / Bún bò", "Gần phố Hàng Bạc, Hoàn Kiếm, Hà Nội", "Mở 18:00 - 02:30", 21.0347, 105.8521, 92);
        add("Phở Gà Chú Tùng Hàng Đào", "food", "Quán ăn / Phở", "Khu Hàng Đào, Hoàn Kiếm, Hà Nội", "Mở 19:00 - 03:00", 21.0332, 105.8510, 94);
        add("Cà Phê Ven Hồ", "cafe", "Cafe / View hồ", "Gần Hồ Hoàn Kiếm, Hà Nội", "Mở 07:00 - 01:00", 21.0287, 105.8529, 88);
        add("Trà Sữa Mây Hồng Phố Cổ", "tea", "Trà sữa / Đồ uống", "Khu phố cổ Hoàn Kiếm, Hà Nội", "Mở 24/7", 21.0315, 105.8502, 100);
        add("Tiệm Tiện Lợi Minh Anh", "store", "Cửa hàng tiện lợi", "Phố Hàng Bạc, Hoàn Kiếm, Hà Nội", "Mở 24/7", 21.0340, 105.8537, 100);
        add("Chợ Mini Tràng Tiền", "supermarket", "Siêu thị mini", "Khu Tràng Tiền, Hoàn Kiếm, Hà Nội", "Mở 08:00 - 23:00", 21.0252, 105.8568, 62);

        // BA ĐÌNH
        add("Bún Chả Cô Hạnh Kim Mã", "food", "Quán ăn / Bún chả", "Khu Kim Mã, Ba Đình, Hà Nội", "Mở 10:00 - 23:00", 21.0311, 105.8181, 70);
        add("Lẩu Nhà Gió Giảng Võ", "food", "Quán ăn / Lẩu", "Khu Giảng Võ, Ba Đình, Hà Nội", "Mở 16:00 - 02:00", 21.0258, 105.8246, 86);
        add("Cà Phê Trúc Bạch Xưa", "cafe", "Cafe / Chill", "Gần hồ Trúc Bạch, Ba Đình, Hà Nội", "Mở 07:00 - 00:30", 21.0450, 105.8412, 82);
        add("Trà Chanh Góc Ngọc Hà", "tea", "Trà chanh / Đồ uống", "Khu Ngọc Hà, Ba Đình, Hà Nội", "Mở 15:00 - 01:30", 21.0372, 105.8282, 84);
        add("Siêu Thị Nhỏ Liễu Giai", "supermarket", "Siêu thị mini", "Khu Liễu Giai, Ba Đình, Hà Nội", "Mở 08:00 - 23:00", 21.0346, 105.8125, 60);

        // ĐỐNG ĐA
        add("Mì Vằn Thắn Gia Khang", "food", "Quán ăn / Mì", "Khu Thái Hà, Đống Đa, Hà Nội", "Mở 18:00 - 01:30", 21.0145, 105.8219, 83);
        add("Xôi Gà Cô Lan Chùa Bộc", "food", "Quán ăn / Xôi", "Khu Chùa Bộc, Đống Đa, Hà Nội", "Mở 20:00 - 03:00", 21.0079, 105.8282, 93);
        add("Cà Phê Góc Học Bài", "cafe", "Cafe / Học tập", "Khu Nguyễn Trãi, Đống Đa, Hà Nội", "Mở 08:00 - 00:00", 21.0024, 105.8208, 76);
        add("Trà Hoa Quả Nắng Mai", "tea", "Trà hoa quả / Đồ uống", "Khu Tây Sơn, Đống Đa, Hà Nội", "Mở 09:00 - 01:00", 21.0115, 105.8327, 84);
        add("Cửa Hàng Tiện Lợi An Nhiên", "store", "Cửa hàng tiện lợi", "Khu Thái Hà, Đống Đa, Hà Nội", "Mở 24/7", 21.0137, 105.8192, 100);

        // HAI BÀ TRƯNG
        add("Cơm Tấm Anh Ba Bạch Mai", "food", "Quán ăn / Cơm tấm", "Khu Bạch Mai, Hai Bà Trưng, Hà Nội", "Mở 17:00 - 02:00", 21.0016, 105.8508, 88);
        add("Phở Đêm Minh Khai", "food", "Quán ăn / Phở", "Khu Minh Khai, Hai Bà Trưng, Hà Nội", "Mở 19:00 - 03:30", 20.9962, 105.8604, 95);
        add("Cà Phê Sân Nhỏ", "cafe", "Cafe / Văn phòng", "Khu Đại Cồ Việt, Hai Bà Trưng, Hà Nội", "Mở 07:00 - 23:30", 21.0082, 105.8469, 66);
        add("Trà Sữa Bé Gấu Bạch Mai", "tea", "Trà sữa / Đồ uống", "Khu Bạch Mai, Hai Bà Trưng, Hà Nội", "Mở 09:00 - 00:30", 21.0004, 105.8483, 78);

        // CẦU GIẤY
        add("Quán Nướng Chú Mạnh", "food", "Quán ăn / Đồ nướng", "Khu Cầu Giấy, Hà Nội", "Mở 17:00 - 01:00", 21.0364, 105.7902, 84);
        add("Bún Bò Cô Mai Duy Tân", "food", "Quán ăn / Bún bò", "Khu Duy Tân, Cầu Giấy, Hà Nội", "Mở 18:00 - 02:00", 21.0305, 105.7849, 89);
        add("Cà Phê Lá Xanh", "cafe", "Cafe / Làm việc", "Khu Dịch Vọng, Cầu Giấy, Hà Nội", "Mở 07:00 - 01:00", 21.0330, 105.7938, 86);
        add("Trà Sữa Mộc Xuân Thủy", "tea", "Trà sữa / Đồ uống", "Khu Xuân Thủy, Cầu Giấy, Hà Nội", "Mở 09:00 - 00:30", 21.0377, 105.7825, 80);
        add("Siêu Thị Mini An Phát", "supermarket", "Siêu thị mini", "Khu Cầu Giấy, Hà Nội", "Mở 08:00 - 23:00", 21.0319, 105.7982, 60);

        // THANH XUÂN
        add("Lẩu Đêm Bếp Nhà", "food", "Quán ăn / Lẩu", "Khu Nguyễn Trãi, Thanh Xuân, Hà Nội", "Mở 16:00 - 02:00", 20.9955, 105.8062, 87);
        add("Cà Phê Gác Nhỏ", "cafe", "Cafe / Trung tâm thương mại", "Khu Royal City, Thanh Xuân, Hà Nội", "Mở 08:00 - 23:30", 21.0028, 105.8155, 68);
        add("Chợ Thực Phẩm Thanh Xuân", "supermarket", "Siêu thị thực phẩm", "Khu Thanh Xuân, Hà Nội", "Mở 07:00 - 00:00", 20.9896, 105.8121, 76);

        // HÀ ĐÔNG
        add("Bún Đêm Cô Thảo Văn Quán", "food", "Quán ăn / Bún", "Khu Văn Quán, Hà Đông, Hà Nội", "Mở 18:00 - 01:00", 20.9790, 105.7892, 80);
        add("Cà Phê Mỗ Lao Corner", "cafe", "Cafe / Khu đô thị", "Khu Mỗ Lao, Hà Đông, Hà Nội", "Mở 07:00 - 00:30", 20.9638, 105.7704, 78);
        add("Tiệm Tiện Lợi Hà Đông", "store", "Cửa hàng tiện lợi", "Khu Hà Đông, Hà Nội", "Mở 24/7", 20.9556, 105.7559, 100);
        add("Siêu Thị Nhỏ Văn Phú", "supermarket", "Siêu thị mini", "Khu Văn Phú, Hà Đông, Hà Nội", "Mở 08:00 - 23:00", 20.9700, 105.7607, 63);

        // TÂY HỒ
        add("Cà Phê Gió Hồ Tây", "cafe", "Cafe / View hồ", "Khu Hồ Tây, Hà Nội", "Mở 07:00 - 01:00", 21.0583, 105.8248, 87);
        add("Trà Chanh Ven Hồ", "tea", "Trà chanh / Đồ uống", "Khu Tây Hồ, Hà Nội", "Mở 15:00 - 02:00", 21.0645, 105.8120, 89);
        add("Pizza Đêm Nhà Gạch", "food", "Quán ăn / Pizza", "Khu Tây Hồ, Hà Nội", "Mở 11:00 - 00:30", 21.0701, 105.8345, 78);

        // NAM TỪ LIÊM
        add("Lẩu Mễ Trì Cô Hương", "food", "Quán ăn / Lẩu", "Khu Mễ Trì, Nam Từ Liêm, Hà Nội", "Mở 16:00 - 02:30", 21.0068, 105.7801, 90);
        add("Cà Phê Tòa Nhà", "cafe", "Cafe / Văn phòng", "Khu Keangnam, Nam Từ Liêm, Hà Nội", "Mở 07:00 - 00:00", 21.0179, 105.7841, 75);
        add("Mini Mart Mỹ Đình", "store", "Cửa hàng tiện lợi", "Khu Mỹ Đình, Nam Từ Liêm, Hà Nội", "Mở 24/7", 21.0284, 105.7707, 100);

        // LONG BIÊN
        add("Phở Đêm Bên Cầu", "food", "Quán ăn / Phở", "Khu Long Biên, Hà Nội", "Mở 18:00 - 02:00", 21.0431, 105.8903, 86);
        add("Cà Phê Bờ Sông", "cafe", "Cafe / Ven sông", "Khu Long Biên, Hà Nội", "Mở 07:00 - 00:00", 21.0532, 105.9101, 74);
        add("Chợ Thực Phẩm Long Biên", "supermarket", "Siêu thị thực phẩm", "Khu Long Biên, Hà Nội", "Mở 07:00 - 23:30", 21.0309, 105.8754, 68);

        // HOÀNG MAI
        add("Bún Ngan Cô Lý Linh Đàm", "food", "Quán ăn / Bún ngan", "Khu Linh Đàm, Hoàng Mai, Hà Nội", "Mở 18:00 - 02:00", 20.9702, 105.8352, 86);
        add("Trà Sữa Gấu Nâu Hoàng Mai", "tea", "Trà sữa / Đồ uống", "Khu Hoàng Mai, Hà Nội", "Mở 09:00 - 00:00", 20.9860, 105.8503, 74);
        add("Tiệm Tiện Lợi Giải Phóng", "store", "Cửa hàng tiện lợi", "Khu Giải Phóng, Hoàng Mai, Hà Nội", "Mở 24/7", 20.9801, 105.8651, 100);

        // ĐÔNG ANH
        add("Bún Đêm Vân Trì", "food", "Quán ăn / Bún", "Gần CTECH, Vân Trì, Đông Anh, Hà Nội", "Mở 17:00 - 00:30", 21.1392, 105.8486, 72);
        add("Cà Phê Sân Trường", "cafe", "Cafe / Sinh viên", "Gần CTECH, Đông Anh, Hà Nội", "Mở 07:00 - 23:00", 21.1405, 105.8464, 65);
        add("Trà Sữa Cổng Trường", "tea", "Trà sữa / Đồ uống", "Gần CTECH, Đông Anh, Hà Nội", "Mở 09:00 - 23:30", 21.1385, 105.8499, 70);
        add("Tạp Hóa Cô Nga", "store", "Cửa hàng tiện lợi", "Gần CTECH, Đông Anh, Hà Nội", "Mở 06:00 - 23:00", 21.1409, 105.8481, 58);

        // THANH TRÌ
        add("Phở Đêm Văn Điển", "food", "Quán ăn / Phở", "Khu thị trấn Văn Điển, Thanh Trì, Hà Nội", "Mở 18:00 - 02:00", 20.9462, 105.8468, 86);
        add("Bún Bò Cô Sáu Ngọc Hồi", "food", "Quán ăn / Bún bò", "Khu Ngọc Hồi, Thanh Trì, Hà Nội", "Mở 17:30 - 01:30", 20.9249, 105.8494, 84);
        add("Cơm Rang Tứ Hiệp", "food", "Quán ăn / Cơm rang", "Khu Tứ Hiệp, Thanh Trì, Hà Nội", "Mở 16:00 - 01:00", 20.9528, 105.8499, 80);
        add("Lẩu Nhà Cô Hằng", "food", "Quán ăn / Lẩu", "Khu Thanh Liệt, Thanh Trì, Hà Nội", "Mở 16:00 - 02:30", 20.9625, 105.8146, 88);
        add("Bánh Mì Chảo Yên Xá", "food", "Quán ăn / Bánh mì", "Khu Yên Xá, Thanh Trì, Hà Nội", "Mở 07:00 - 23:30", 20.9554, 105.8108, 68);
        add("Quán Nướng Đại Áng", "food", "Quán ăn / Đồ nướng", "Khu Đại Áng, Thanh Trì, Hà Nội", "Mở 17:00 - 01:30", 20.9218, 105.8355, 82);

        add("Cà Phê Gió Văn Điển", "cafe", "Cafe / Làm việc", "Khu Văn Điển, Thanh Trì, Hà Nội", "Mở 07:00 - 00:00", 20.9474, 105.8446, 74);
        add("Cà Phê Đường Ngọc Hồi", "cafe", "Cafe / Gặp bạn bè", "Khu Ngọc Hồi, Thanh Trì, Hà Nội", "Mở 07:00 - 23:30", 20.9288, 105.8509, 70);
        add("Coffee Đèn Vàng Tứ Hiệp", "cafe", "Cafe / Đồ uống đêm", "Khu Tứ Hiệp, Thanh Trì, Hà Nội", "Mở 08:00 - 01:00", 20.9546, 105.8480, 82);

        add("Trà Sữa Mây Trắng Văn Điển", "tea", "Trà sữa / Đồ uống", "Khu Văn Điển, Thanh Trì, Hà Nội", "Mở 24/7", 20.9451, 105.8479, 100);
        add("Trà Chanh Cây Bàng", "tea", "Trà chanh / Đồ uống", "Khu Ngọc Hồi, Thanh Trì, Hà Nội", "Mở 15:00 - 01:00", 20.9234, 105.8507, 83);
        add("Trà Hoa Quả Tứ Hiệp", "tea", "Trà hoa quả / Đồ uống", "Khu Tứ Hiệp, Thanh Trì, Hà Nội", "Mở 09:00 - 00:30", 20.9519, 105.8516, 78);

        add("Tiệm Tiện Lợi Văn Điển", "store", "Cửa hàng tiện lợi", "Khu Văn Điển, Thanh Trì, Hà Nội", "Mở 24/7", 20.9469, 105.8431, 100);
        add("Tạp Hóa Ngọc Hồi 24h", "store", "Cửa hàng tiện lợi", "Khu Ngọc Hồi, Thanh Trì, Hà Nội", "Mở 24/7", 20.9276, 105.8488, 100);
        add("Cửa Hàng Tứ Hiệp", "store", "Cửa hàng tiện lợi", "Khu Tứ Hiệp, Thanh Trì, Hà Nội", "Mở 06:00 - 23:30", 20.9532, 105.8522, 62);

        add("Siêu Thị Mini Văn Điển", "supermarket", "Siêu thị mini", "Khu Văn Điển, Thanh Trì, Hà Nội", "Mở 08:00 - 23:00", 20.9440, 105.8455, 64);
        add("Chợ Thực Phẩm Thanh Trì", "supermarket", "Siêu thị thực phẩm", "Khu Thanh Trì, Hà Nội", "Mở 07:00 - 23:30", 20.9507, 105.8384, 70);
        add("Siêu Thị Nhỏ Ngọc Hồi", "supermarket", "Siêu thị mini", "Khu Ngọc Hồi, Thanh Trì, Hà Nội", "Mở 08:00 - 22:30", 20.9258, 105.8526, 58);

        // THƯỜNG TÍN
        add("Phở Đêm Quốc Lộ 1A", "food", "Quán ăn / Phở", "Khu thị trấn Thường Tín, Hà Nội", "Mở 18:00 - 02:00", 20.8696, 105.8618, 86);
        add("Bún Bò Chị Hương Quất Động", "food", "Quán ăn / Bún bò", "Khu Quất Động, Thường Tín, Hà Nội", "Mở 17:30 - 01:00", 20.8876, 105.8537, 80);
        add("Cơm Rang Anh Tú", "food", "Quán ăn / Cơm rang", "Khu trung tâm Thường Tín, Hà Nội", "Mở 16:00 - 01:30", 20.8709, 105.8650, 84);
        add("Lẩu Khuya Hà Hồi", "food", "Quán ăn / Lẩu", "Khu Hà Hồi, Thường Tín, Hà Nội", "Mở 16:00 - 02:00", 20.8586, 105.8555, 85);
        add("Bánh Mì Chảo Ninh Sở", "food", "Quán ăn / Bánh mì", "Khu Ninh Sở, Thường Tín, Hà Nội", "Mở 07:00 - 23:00", 20.8396, 105.8751, 62);
        add("Quán Nướng Vân Tảo", "food", "Quán ăn / Đồ nướng", "Khu Vân Tảo, Thường Tín, Hà Nội", "Mở 17:00 - 01:00", 20.8915, 105.8693, 80);

        add("Cà Phê Phố Huyện", "cafe", "Cafe / Gặp bạn bè", "Khu thị trấn Thường Tín, Hà Nội", "Mở 07:00 - 00:00", 20.8718, 105.8625, 72);
        add("Cà Phê Quất Động", "cafe", "Cafe / Làm việc", "Khu Quất Động, Thường Tín, Hà Nội", "Mở 07:00 - 23:30", 20.8891, 105.8525, 66);
        add("Cà Phê Hà Hồi Đêm", "cafe", "Cafe / Đồ uống đêm", "Khu Hà Hồi, Thường Tín, Hà Nội", "Mở 08:00 - 00:30", 20.8599, 105.8568, 76);

        add("Trà Sữa Bông Mây", "tea", "Trà sữa / Đồ uống", "Khu trung tâm Thường Tín, Hà Nội", "Mở 24/7", 20.8701, 105.8604, 100);
        add("Trà Chanh Quất Động", "tea", "Trà chanh / Đồ uống", "Khu Quất Động, Thường Tín, Hà Nội", "Mở 15:00 - 01:00", 20.8864, 105.8556, 82);
        add("Trà Hoa Quả Ninh Sở", "tea", "Trà hoa quả / Đồ uống", "Khu Ninh Sở, Thường Tín, Hà Nội", "Mở 09:00 - 23:30", 20.8408, 105.8729, 65);

        add("Tiệm Tiện Lợi Quốc Lộ 1A", "store", "Cửa hàng tiện lợi", "Khu thị trấn Thường Tín, Hà Nội", "Mở 24/7", 20.8712, 105.8643, 100);
        add("Tạp Hóa Quất Động 24h", "store", "Cửa hàng tiện lợi", "Khu Quất Động, Thường Tín, Hà Nội", "Mở 24/7", 20.8883, 105.8549, 100);
        add("Cửa Hàng Hà Hồi", "store", "Cửa hàng tiện lợi", "Khu Hà Hồi, Thường Tín, Hà Nội", "Mở 06:00 - 23:00", 20.8578, 105.8576, 60);

        add("Siêu Thị Mini Thường Tín", "supermarket", "Siêu thị mini", "Khu trung tâm Thường Tín, Hà Nội", "Mở 08:00 - 23:00", 20.8690, 105.8637, 64);
        add("Chợ Thực Phẩm Quất Động", "supermarket", "Siêu thị thực phẩm", "Khu Quất Động, Thường Tín, Hà Nội", "Mở 07:00 - 23:30", 20.8869, 105.8515, 68);
        add("Siêu Thị Nhỏ Ninh Sở", "supermarket", "Siêu thị mini", "Khu Ninh Sở, Thường Tín, Hà Nội", "Mở 08:00 - 22:30", 20.8417, 105.8744, 56);
    }

    private void add(
            String name,
            String category,
            String type,
            String address,
            String openingHours,
            double latitude,
            double longitude,
            int nightScore
    ) {
        demoPlaces.add(new DemoPlace(
                name,
                category,
                type,
                address,
                openingHours,
                latitude,
                longitude,
                nightScore
        ));
    }

    private void showPlacesByFilter(String filter) {
        osmMap.getOverlays().clear();

        addMyLocationMarker();

        int count = 0;

        for (DemoPlace place : demoPlaces) {
            if (filter.equals("all") || place.category.equals(filter)) {
                addPlaceMarker(place);
                count++;
            }
        }

        osmMap.invalidate();

        if (focusUserAfterRender) {
            zoomToUserLocation();
            focusUserAfterRender = false;
        } else {
            if (filter.equals("all")) {
                osmMap.getController().animateTo(new GeoPoint(21.0350, 105.8350));
                osmMap.getController().zoomTo(11.7);
            } else {
                osmMap.getController().animateTo(new GeoPoint(21.0285, 105.8542));
                osmMap.getController().zoomTo(12.4);
            }
        }

        updatePlaceInfo(
                "✅",
                "Đang hiển thị " + count + " địa điểm",
                "Bộ lọc: " + getFilterName(filter),
                "Dữ liệu demo có địa chỉ, giờ mở cửa, điểm đi đêm",
                "Bấm marker để zoom tới địa điểm"
        );
    }

    private void addMyLocationMarker() {
        GeoPoint point = new GeoPoint(userLat, userLon);

        Marker marker = new Marker(osmMap);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        if (isUsingDefaultLocation) {
            marker.setTitle("Vị trí hiện tại");
            marker.setSnippet("60 QL1A, Thường Tín, Hà Nội, Việt Nam");
        } else {
            marker.setTitle("Vị trí hiện tại");
            marker.setSnippet("Vị trí của bạn đã được xác nhận bằng GPS");
        }

        osmMap.getOverlays().add(marker);
    }

    private void addPlaceMarker(DemoPlace place) {
        GeoPoint point = new GeoPoint(place.latitude, place.longitude);

        Marker marker = new Marker(osmMap);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(place.name);
        marker.setSnippet(place.type);

        marker.setOnMarkerClickListener((m, mapView) -> {
            zoomToPlace(place);
            m.showInfoWindow();
            return true;
        });

        osmMap.getOverlays().add(marker);
    }

    private void zoomToPlace(DemoPlace place) {
        GeoPoint point = new GeoPoint(place.latitude, place.longitude);

        osmMap.getController().animateTo(point);
        osmMap.getController().zoomTo(18.5);

        double distance = calculateDistance(place.latitude, place.longitude);

        String distanceText;

        if (distance >= 1000) {
            distanceText = String.format(Locale.US, "%.1f km từ bạn", distance / 1000.0);
        } else {
            distanceText = Math.round(distance) + " m từ bạn";
        }

        String nightText = "Điểm đi đêm: " + place.nightScore + "/100";

        if (place.nightScore >= 90) {
            nightText += " • Rất phù hợp";
        } else if (place.nightScore >= 75) {
            nightText += " • Phù hợp";
        } else {
            nightText += " • Bình thường";
        }

        updatePlaceInfo(
                getEmojiForCategory(place.category),
                place.name,
                place.type + " • " + distanceText,
                place.address,
                place.openingHours + " • " + nightText
        );
    }

    private double calculateDistance(double lat, double lon) {
        float[] results = new float[1];

        Location.distanceBetween(
                userLat,
                userLon,
                lat,
                lon,
                results
        );

        return results[0];
    }

    private void setActiveChip(TextView activeChip) {
        chipAll.setBackgroundResource(R.drawable.chip_bg);
        chipFood.setBackgroundResource(R.drawable.chip_bg);
        chipCafe.setBackgroundResource(R.drawable.chip_bg);
        chipTea.setBackgroundResource(R.drawable.chip_bg);
        chipStore.setBackgroundResource(R.drawable.chip_bg);
        chipSupermarket.setBackgroundResource(R.drawable.chip_bg);

        chipAll.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
        chipFood.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
        chipCafe.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
        chipTea.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
        chipStore.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
        chipSupermarket.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));

        activeChip.setBackgroundResource(R.drawable.chip_active_bg);
        activeChip.setTextColor(Color.WHITE);
    }

    private String getEmojiForCategory(String category) {
        switch (category) {
            case "food":
                return "🍜";
            case "cafe":
                return "☕";
            case "tea":
                return "🧋";
            case "store":
                return "🏪";
            case "supermarket":
                return "🛒";
            default:
                return "📍";
        }
    }

    private String getFilterName(String filter) {
        switch (filter) {
            case "food":
                return "Quán ăn";
            case "cafe":
                return "Cafe";
            case "tea":
                return "Trà sữa";
            case "store":
                return "Cửa hàng tiện lợi";
            case "supermarket":
                return "Siêu thị";
            default:
                return "Tất cả";
        }
    }

    private void updatePlaceInfo(String emoji, String name, String type, String distance, String openTime) {
        placeEmoji.setText(emoji);
        placeName.setText(name);
        placeType.setText(type);
        placeDistance.setText(distance);
        placeOpenTime.setText(openTime);
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(MapActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        findViewById(R.id.navMap).setOnClickListener(v ->
                Toast.makeText(this, "Bạn đang ở trang Bản đồ", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.navChat).setOnClickListener(v ->
                startActivity(new Intent(MapActivity.this, ChatActivity.class))
        );

        findViewById(R.id.navFavorite).setOnClickListener(v ->
                startActivity(new Intent(MapActivity.this, FavoriteActivity.class))
        );

        findViewById(R.id.navProfile).setOnClickListener(v ->
                startActivity(new Intent(MapActivity.this, ProfileActivity.class))
        );
    }

    private static class DemoPlace {
        String name;
        String category;
        String type;
        String address;
        String openingHours;
        double latitude;
        double longitude;
        int nightScore;

        DemoPlace(
                String name,
                String category,
                String type,
                String address,
                String openingHours,
                double latitude,
                double longitude,
                int nightScore
        ) {
            this.name = name;
            this.category = category;
            this.type = type;
            this.address = address;
            this.openingHours = openingHours;
            this.latitude = latitude;
            this.longitude = longitude;
            this.nightScore = nightScore;
        }
    }
}