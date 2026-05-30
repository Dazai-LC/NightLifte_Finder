package com.example.nightlife_finder.activities;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
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

    private static final double DEFAULT_LAT = 20.8712;
    private static final double DEFAULT_LON = 105.8643;

    private MapView osmMap;
    private FusedLocationProviderClient fusedLocationClient;

    private TextView placeEmoji, placeName, placeType, placeDistance, placeOpenTime;
    private TextView chipAll, chipFood, chipCafe, chipTea, chipStore, chipSupermarket;
    private TextView btnDirection;

    private double userLat = DEFAULT_LAT;
    private double userLon = DEFAULT_LON;

    private String currentFilter = "all";
    private boolean focusUserAfterRender = false;
    private boolean shouldZoomSelectedAfterRender = false;
    private boolean isUsingDefaultLocation = true;

    private DemoPlace targetPlaceFromHome = null;
    private DemoPlace selectedPlace = null;

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
        receivePlaceFromHome();

        if (targetPlaceFromHome != null) {
            selectedPlace = targetPlaceFromHome;
            focusUserAfterRender = false;
            shouldZoomSelectedAfterRender = true;

            showPlacesByFilter("all");
            requestLocationPermissionFirst();
        } else {
            focusUserAfterRender = true;
            shouldZoomSelectedAfterRender = false;

            requestLocationPermissionFirst();
        }
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

        btnDirection = findViewById(R.id.btnDirection);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void setupMap() {
        osmMap.setTileSource(TileSourceFactory.MAPNIK);
        osmMap.setMultiTouchControls(true);

        GeoPoint defaultCenter = new GeoPoint(DEFAULT_LAT, DEFAULT_LON);
        osmMap.getController().setZoom(14.5);
        osmMap.getController().setCenter(defaultCenter);
    }

    private void setupButtons() {
        findViewById(R.id.btnMyLocation).setOnClickListener(v -> {
            targetPlaceFromHome = null;
            selectedPlace = null;
            shouldZoomSelectedAfterRender = false;
            focusUserAfterRender = true;
            requestLocationPermissionFirst();
        });

        findViewById(R.id.btnZoomIn).setOnClickListener(v -> {
            double zoom = osmMap.getZoomLevelDouble();

            if (zoom < 20) {
                osmMap.getController().setZoom(zoom + 1);
            }
        });

        findViewById(R.id.btnZoomOut).setOnClickListener(v -> {
            double zoom = osmMap.getZoomLevelDouble();

            if (zoom > 3) {
                osmMap.getController().setZoom(zoom - 1);
            }
        });

        findViewById(R.id.btnReloadPlaces).setOnClickListener(v -> {
            showPlacesByFilter(currentFilter);
            Toast.makeText(this, "Đã tải lại dữ liệu demo", Toast.LENGTH_SHORT).show();
        });

        btnDirection.setOnClickListener(v -> {
            if (selectedPlace == null) {
                Toast.makeText(this, "Bạn hãy chọn một quán trước", Toast.LENGTH_SHORT).show();
                return;
            }

            openGoogleMapDirection(selectedPlace);
        });

        chipAll.setOnClickListener(v -> chooseFilter("all", chipAll));
        chipFood.setOnClickListener(v -> chooseFilter("food", chipFood));
        chipCafe.setOnClickListener(v -> chooseFilter("cafe", chipCafe));
        chipTea.setOnClickListener(v -> chooseFilter("tea", chipTea));
        chipStore.setOnClickListener(v -> chooseFilter("store", chipStore));
        chipSupermarket.setOnClickListener(v -> chooseFilter("supermarket", chipSupermarket));
    }

    private void chooseFilter(String filter, TextView activeChip) {
        targetPlaceFromHome = null;
        selectedPlace = null;
        shouldZoomSelectedAfterRender = false;
        focusUserAfterRender = false;

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

        if (selectedPlace == null) {
            updatePlaceInfo(
                    "⌖",
                    "Đang lấy vị trí hiện tại...",
                    "GPS / Wi-Fi / mạng di động",
                    "Vui lòng chờ vài giây",
                    "Nếu GPS lỗi, app sẽ dùng vị trí mặc định"
            );
        }

        CancellationTokenSource tokenSource = new CancellationTokenSource();

        fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null && isValidVietnamLocation(location)) {
                        userLat = location.getLatitude();
                        userLon = location.getLongitude();

                        isUsingDefaultLocation = false;

                        showPlacesByFilter(currentFilter);

                        if (selectedPlace == null && focusUserAfterRender) {
                            updatePlaceInfo(
                                    "📍",
                                    "Vị trí hiện tại",
                                    "GPS đã xác nhận vị trí của bạn",
                                    "Tọa độ: " + String.format(Locale.US, "%.5f, %.5f", userLat, userLon),
                                    "Đang hiển thị địa điểm gần khu vực của bạn"
                            );
                        }

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

                        showPlacesByFilter(currentFilter);

                        if (selectedPlace == null && focusUserAfterRender) {
                            updatePlaceInfo(
                                    "📍",
                                    "Vị trí hiện tại",
                                    "Đã lấy vị trí gần nhất của bạn",
                                    "Tọa độ: " + String.format(Locale.US, "%.5f, %.5f", userLat, userLon),
                                    "Đang hiển thị địa điểm gần khu vực của bạn"
                            );
                        }

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

        showPlacesByFilter(currentFilter);

        if (selectedPlace == null && focusUserAfterRender) {
            updatePlaceInfo(
                    "📍",
                    "Vị trí mặc định",
                    "60 QL1A, Thường Tín, Hà Nội",
                    "Việt Nam",
                    reason
            );
        }

        Toast.makeText(
                this,
                "Không lấy được GPS, dùng vị trí mặc định",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void zoomToUserLocation() {
        GeoPoint userPoint = new GeoPoint(userLat, userLon);

        osmMap.post(() -> {
            osmMap.getController().setZoom(16.5);
            osmMap.getController().setCenter(userPoint);
            osmMap.invalidate();
        });
    }

    private void receivePlaceFromHome() {
        String name = getIntent().getStringExtra("target_name");

        if (name == null || name.trim().isEmpty()) {
            return;
        }

        String category = getIntent().getStringExtra("target_category");
        String type = getIntent().getStringExtra("target_type");
        String address = getIntent().getStringExtra("target_address");
        String openingHours = getIntent().getStringExtra("target_opening_hours");

        double latitude = getIntent().getDoubleExtra("target_latitude", DEFAULT_LAT);
        double longitude = getIntent().getDoubleExtra("target_longitude", DEFAULT_LON);
        int nightScore = getIntent().getIntExtra("target_night_score", 80);

        if (category == null || category.trim().isEmpty()) {
            category = "food";
        }

        if (type == null || type.trim().isEmpty()) {
            type = "Địa điểm";
        }

        if (address == null || address.trim().isEmpty()) {
            address = "Hà Nội";
        }

        if (openingHours == null || openingHours.trim().isEmpty()) {
            openingHours = "Đang cập nhật";
        }

        targetPlaceFromHome = new DemoPlace(
                name,
                category,
                type,
                address,
                openingHours,
                latitude,
                longitude,
                nightScore
        );

        selectedPlace = targetPlaceFromHome;
        addOrReplacePlace(targetPlaceFromHome);

        currentFilter = "all";

        if (chipAll != null) {
            setActiveChip(chipAll);
        }
    }

    private void addOrReplacePlace(DemoPlace newPlace) {
        for (int i = 0; i < demoPlaces.size(); i++) {
            DemoPlace oldPlace = demoPlaces.get(i);

            if (oldPlace.name.equalsIgnoreCase(newPlace.name)) {
                demoPlaces.set(i, newPlace);
                return;
            }
        }

        demoPlaces.add(newPlace);
    }

    private void createHaNoiDemoPlaces() {
        demoPlaces.clear();

        add("Mì Cay Seoul", "food", "Quán ăn / Mì cay", "Khu trung tâm Thường Tín, Hà Nội", "Mở 17:00 - 02:00", 20.8729, 105.8648, 91);
        add("Bún Đậu", "food", "Quán ăn / Bún đậu", "Khu thị trấn Thường Tín, Hà Nội", "Mở 16:00 - 00:30", 20.8710, 105.8617, 84);
        add("Xôi Gà", "food", "Quán ăn / Xôi gà", "Gần Quốc lộ 1A, Thường Tín, Hà Nội", "Mở 20:00 - 04:00", 20.8694, 105.8649, 93);
        add("Gà Rán", "food", "Quán ăn / Gà rán", "Khu trung tâm Thường Tín, Hà Nội", "Mở 10:00 - 01:00", 20.8708, 105.8668, 85);
        add("Mì Xào Bò Thanh Nghị", "food", "Quán ăn / Mì xào", "Khu phố ga Thường Tín, Hà Nội", "Mở 17:00 - 01:30", 20.8689, 105.8609, 87);
        add("Bánh Tráng Trộn", "food", "Ăn vặt / Bánh tráng", "Khu trung tâm Thường Tín, Hà Nội", "Mở 15:00 - 01:00", 20.8721, 105.8631, 83);
        add("Bún Bò Huế", "food", "Quán ăn / Bún bò", "Khu Hà Hồi, Thường Tín, Hà Nội", "Mở 18:00 - 02:00", 20.8586, 105.8555, 88);
        add("Lola Tea", "tea", "Trà chanh / Đồ uống đêm", "Khu Quất Động, Thường Tín, Hà Nội", "Mở 15:00 - 01:30", 20.8864, 105.8556, 82);
        add("Ốc Sốt Cay", "food", "Quán ăn / Hải sản ốc", "Khu trung tâm Thường Tín, Hà Nội", "Mở 17:00 - 02:30", 20.8735, 105.8657, 90);
        add("Cơm Tấm", "food", "Quán ăn / Cơm tấm", "Gần 60 QL1A, Thường Tín, Hà Nội", "Mở 18:00 - 01:00", 20.8702, 105.8639, 86);

        add("Phở Gà 24H", "food", "Quán ăn / Phở gà", "Khu trung tâm Thường Tín, Hà Nội", "Mở 18:00 - 03:00", 20.8712, 105.8643, 92);
        add("Bánh Mì Chảo Cô Ba", "food", "Quán ăn / Bánh mì chảo", "Khu thị trấn Thường Tín, Hà Nội", "Mở 17:00 - 01:30", 20.8705, 105.8628, 88);
        add("Cơm Rang Dưa bò cô Thanh", "food", "Quán ăn / Cơm rang", "Gần Quốc lộ 1A, Thường Tín, Hà Nội", "Mở 18:00 - 02:00", 20.8724, 105.8661, 86);
        add("Lẩu Bò Neon", "food", "Quán ăn / Lẩu bò", "Khu Vân Tảo, Thường Tín, Hà Nội", "Mở 17:00 - 03:30", 20.8915, 105.8693, 94);
        add("Bạch Tuộc Nướng", "food", "Quán ăn / Hải sản nướng", "Khu trung tâm Thường Tín, Hà Nội", "Mở 18:00 - 01:00", 20.8732, 105.8619, 90);
        add("Burger Velocity", "food", "Quán ăn / Burger", "Gần ga Thường Tín, Hà Nội", "Mở 10:00 - 00:30", 20.8698, 105.8604, 82);
        add("Cháo Sườn Starlight", "food", "Quán ăn / Cháo sườn", "Khu thị trấn Thường Tín, Hà Nội", "Mở 17:00 - 02:00", 20.8718, 105.8625, 89);
        add("Pizza Crimson", "food", "Quán ăn / Pizza", "Khu trung tâm Thường Tín, Hà Nội", "Mở 10:00 - 00:30", 20.8704, 105.8661, 80);
        add("Sushi Tokyo", "food", "Quán ăn / Sushi", "Khu trung tâm Thường Tín, Hà Nội", "Mở 17:00 - 00:30", 20.8724, 105.8628, 84);
        add("Lẩu Đêm Neon", "food", "Quán ăn / Lẩu đêm", "Khu Vân Tảo, Thường Tín, Hà Nội", "Mở 17:00 - 03:30", 20.8915, 105.8693, 95);

        add("Cà Phê Phố Huyện", "cafe", "Cafe / Gặp bạn bè", "Khu thị trấn Thường Tín, Hà Nội", "Mở 07:00 - 00:00", 20.8718, 105.8625, 72);
        add("Cà Phê Hà Hồi Đêm", "cafe", "Cafe / Đồ uống đêm", "Khu Hà Hồi, Thường Tín, Hà Nội", "Mở 08:00 - 00:30", 20.8599, 105.8568, 76);
        add("Coffee Đèn Vàng Tứ Hiệp", "cafe", "Cafe / Chill đêm", "Khu Tứ Hiệp, Thanh Trì, Hà Nội", "Mở 08:00 - 01:00", 20.9546, 105.8480, 82);

        add("Trà Sữa Bông Mây", "tea", "Trà sữa / Đồ uống", "Khu trung tâm Thường Tín, Hà Nội", "Mở 24/7", 20.8701, 105.8604, 100);
        add("Trà Chanh Quất Động", "tea", "Trà chanh / Đồ uống", "Khu Quất Động, Thường Tín, Hà Nội", "Mở 15:00 - 01:00", 20.8864, 105.8556, 82);
        add("Trà Sữa Mây Trắng Văn Điển", "tea", "Trà sữa / Đồ uống", "Khu Văn Điển, Thanh Trì, Hà Nội", "Mở 24/7", 20.9451, 105.8479, 100);

        add("Tiệm Tiện Lợi Quốc Lộ 1A", "store", "Cửa hàng tiện lợi", "60 QL1A, Thường Tín, Hà Nội", "Mở 24/7", 20.8712, 105.8643, 100);
        add("Tạp Hóa Quất Động 24h", "store", "Cửa hàng tiện lợi", "Khu Quất Động, Thường Tín, Hà Nội", "Mở 24/7", 20.8883, 105.8549, 100);
        add("Tiệm Tiện Lợi Văn Điển", "store", "Cửa hàng tiện lợi", "Khu Văn Điển, Thanh Trì, Hà Nội", "Mở 24/7", 20.9469, 105.8431, 100);

        add("Siêu Thị Mini Thường Tín", "supermarket", "Siêu thị mini", "Khu trung tâm Thường Tín, Hà Nội", "Mở 08:00 - 23:00", 20.8690, 105.8637, 64);
        add("Chợ Thực Phẩm Quất Động", "supermarket", "Siêu thị thực phẩm", "Khu Quất Động, Thường Tín, Hà Nội", "Mở 07:00 - 23:30", 20.8869, 105.8515, 68);
        add("Siêu Thị Mini Văn Điển", "supermarket", "Siêu thị mini", "Khu Văn Điển, Thanh Trì, Hà Nội", "Mở 08:00 - 23:00", 20.9440, 105.8455, 64);
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

        if (selectedPlace != null && shouldZoomSelectedAfterRender) {
            zoomToPlace(selectedPlace);
            shouldZoomSelectedAfterRender = false;

            Toast.makeText(
                    this,
                    "Đã mở bản đồ tới: " + selectedPlace.name,
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (focusUserAfterRender) {
            zoomToUserLocation();
            focusUserAfterRender = false;
            return;
        }

        if (selectedPlace != null) {
            updateSelectedPlaceInfo(selectedPlace);
            return;
        }

        updatePlaceInfo(
                "✅",
                "Đang hiển thị " + count + " địa điểm",
                "Bộ lọc: " + getFilterName(filter),
                "Dữ liệu demo có địa chỉ, giờ mở cửa, điểm đi đêm",
                "Bấm marker để xem thông tin và chỉ đường"
        );
    }

    private void addMyLocationMarker() {
        GeoPoint point = new GeoPoint(userLat, userLon);

        Marker marker = new Marker(osmMap);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        if (isUsingDefaultLocation) {
            marker.setTitle("Vị trí mặc định");
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
        marker.setSnippet(place.type + " • " + place.openingHours);

        marker.setOnMarkerClickListener((m, mapView) -> {
            targetPlaceFromHome = null;
            selectedPlace = place;
            shouldZoomSelectedAfterRender = false;
            focusUserAfterRender = false;

            zoomToPlace(place);
            m.showInfoWindow();

            return true;
        });

        osmMap.getOverlays().add(marker);
    }

    private void zoomToPlace(DemoPlace place) {
        selectedPlace = place;

        GeoPoint point = new GeoPoint(place.latitude, place.longitude);

        osmMap.post(() -> {
            osmMap.getController().setZoom(18.5);
            osmMap.getController().setCenter(point);
            osmMap.invalidate();
        });

        osmMap.postDelayed(() -> {
            osmMap.getController().setZoom(18.5);
            osmMap.getController().setCenter(point);
            osmMap.invalidate();
        }, 300);

        updateSelectedPlaceInfo(place);
    }

    private void updateSelectedPlaceInfo(DemoPlace place) {
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

    private void openGoogleMapDirection(DemoPlace place) {
        Uri googleMapUri = Uri.parse(
                "google.navigation:q="
                        + place.latitude
                        + ","
                        + place.longitude
                        + "&mode=d"
        );

        Intent googleMapIntent = new Intent(Intent.ACTION_VIEW, googleMapUri);
        googleMapIntent.setPackage("com.google.android.apps.maps");

        try {
            startActivity(googleMapIntent);
        } catch (ActivityNotFoundException e) {
            Uri webMapUri = Uri.parse(
                    "https://www.google.com/maps/dir/?api=1"
                            + "&destination="
                            + place.latitude
                            + ","
                            + place.longitude
                            + "&travelmode=driving"
            );

            Intent webIntent = new Intent(Intent.ACTION_VIEW, webMapUri);
            startActivity(webIntent);
        }
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