package com.example.nightlife_finder.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.nightlife_finder.R;
import com.example.nightlife_finder.interfaces.OnPlaceLoadedListener;
import com.example.nightlife_finder.models.Place;
import com.example.nightlife_finder.repositories.PlaceRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends BaseActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private boolean is3DMode = true;
    private float currentZoom = 15.5f;

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

    private PlaceRepository placeRepository;
    private final List<Place> allPlaces = new ArrayList<>();
    private final Map<String, Marker> mapMarkers = new HashMap<>();
    private Place selectedPlace;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSystemBars();
        setContentView(R.layout.activity_map);

        placeRepository = new PlaceRepository();

        bindViews();
        setupMapButtons();
        setupCategoryChips();
        setupBottomNavigation();

        // Load the SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.realMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.parseColor("#0F0F12"));
        getWindow().setNavigationBarColor(Color.parseColor("#1A1A1F"));
    }

    private void bindViews() {
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

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // Customise maps UI settings
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);

        // Center initially on Hoan Kiem Lake, Hanoi
        LatLng hanoiLocation = new LatLng(21.0333, 105.8500);
        moveCameraTo(hanoiLocation, currentZoom, is3DMode ? 45f : 0f);

        // Access location permissions
        requestLocationPermission();

        // Retrieve places list from Firestore
        loadPlacesFromFirestore();

        // Handle Marker Click
        googleMap.setOnMarkerClickListener(marker -> {
            Place place = (Place) marker.getTag();
            if (place != null) {
                selectedPlace = place;
                String emoji = getEmojiForCategory(place.getCategory());
                updatePlaceInfo(
                        emoji,
                        place.getName(),
                        place.getOpenTime() + " • 📍 " + place.getAddress(),
                        "AI gợi ý: đường nhanh nhất đến " + place.getName()
                );
            }
            return false;
        });
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (googleMap != null && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                }
            }
        }
    }

    private void loadPlacesFromFirestore() {
        placeRepository.getPlaces(new OnPlaceLoadedListener() {
            @Override
            public void onSuccess(List<Place> places) {
                allPlaces.clear();
                allPlaces.addAll(places);

                if (googleMap != null) {
                    googleMap.clear();
                    mapMarkers.clear();

                    for (Place place : allPlaces) {
                        LatLng pos = new LatLng(place.getLat(), place.getLng());
                        float hue = getMarkerColorForCategory(place.getCategory());

                        Marker marker = googleMap.addMarker(new MarkerOptions()
                                .position(pos)
                                .title(place.getName())
                                .icon(BitmapDescriptorFactory.defaultMarker(hue)));

                        if (marker != null) {
                            marker.setTag(place);
                            mapMarkers.put(place.getId(), marker);
                        }
                    }
                }

                // Initial first loaded spot selection card
                if (!allPlaces.isEmpty()) {
                    Place initial = allPlaces.get(0);
                    selectedPlace = initial;
                    updatePlaceInfo(
                            getEmojiForCategory(initial.getCategory()),
                            initial.getName(),
                            initial.getOpenTime() + " • 0,5 km",
                            "AI gợi ý: đường nhanh nhất khoảng 5 phút"
                    );
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MapActivity.this, "Lỗi tải bản đồ: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupMapButtons() {
        findViewById(R.id.btnMyLocation).setOnClickListener(v -> {
            LatLng hanoiLocation = new LatLng(21.0333, 105.8500);
            currentZoom = 15.5f;
            is3DMode = true;
            txtToggle3D.setText("3D");
            moveCameraTo(hanoiLocation, currentZoom, 45f);

            updatePlaceInfo(
                    "◎",
                    "Vị trí của bạn",
                    "Hồ Hoàn Kiếm • Hà Nội",
                    "Bạn đang ở khu vực trung tâm Hà Nội"
            );
            Toast.makeText(this, "Đã quay về vị trí Hồ Hoàn Kiếm, Hà Nội", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnZoomIn).setOnClickListener(v -> {
            if (googleMap != null && currentZoom < 21f) {
                currentZoom += 1f;
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoom));
            }
        });

        findViewById(R.id.btnZoomOut).setOnClickListener(v -> {
            if (googleMap != null && currentZoom > 2f) {
                currentZoom -= 1f;
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoom));
            }
        });

        findViewById(R.id.btnToggle3D).setOnClickListener(v -> {
            if (googleMap == null) return;
            is3DMode = !is3DMode;

            CameraPosition currentCam = googleMap.getCameraPosition();
            float newTilt = is3DMode ? 45f : 0f;

            txtToggle3D.setText(is3DMode ? "3D" : "2D");
            Toast.makeText(this, is3DMode ? "Đã bật chế độ 3D" : "Đã bật chế độ 2D", Toast.LENGTH_SHORT).show();

            moveCameraTo(currentCam.target, currentCam.zoom, newTilt);
        });
    }

    private void moveCameraTo(LatLng target, float zoom, float tilt) {
        if (googleMap == null) return;

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(target)
                .zoom(zoom)
                .tilt(tilt)
                .bearing(0)
                .build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void setupCategoryChips() {
        chipBunBo.setOnClickListener(v -> filterByCategory("Bún bò", chipBunBo));
        chipNuong.setOnClickListener(v -> filterByCategory("Nướng", chipNuong));
        chipLau.setOnClickListener(v -> filterByCategory("Lẩu", chipLau));
        chipTraSua.setOnClickListener(v -> filterByCategory("Trà sữa", chipTraSua));
        chipPizza.setOnClickListener(v -> filterByCategory("Pizza", chipPizza));
    }

    private void filterByCategory(String category, TextView activeChip) {
        setActiveChip(activeChip);
        Toast.makeText(this, "Đang lọc: " + category, Toast.LENGTH_SHORT).show();

        Place firstMatch = null;
        for (Place place : allPlaces) {
            Marker marker = mapMarkers.get(place.getId());
            if (marker != null) {
                boolean visible = place.getCategory().equalsIgnoreCase(category);
                marker.setVisible(visible);
                if (visible && firstMatch == null) {
                    firstMatch = place;
                }
            }
        }

        if (firstMatch != null) {
            selectedPlace = firstMatch;
            LatLng target = new LatLng(firstMatch.getLat(), firstMatch.getLng());
            moveCameraTo(target, currentZoom, is3DMode ? 45f : 0f);

            updatePlaceInfo(
                    getEmojiForCategory(firstMatch.getCategory()),
                    firstMatch.getName(),
                    firstMatch.getOpenTime() + " • 📍 " + firstMatch.getAddress(),
                    "AI gợi ý: đường nhanh nhất khoảng 5 phút"
            );
        } else {
            Toast.makeText(this, "Không tìm thấy quán nào thuộc nhóm " + category, Toast.LENGTH_SHORT).show();
        }
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

    private float getMarkerColorForCategory(String category) {
        if (category == null) return BitmapDescriptorFactory.HUE_RED;
        switch (category.toLowerCase()) {
            case "bún bò":
            case "phở":
                return BitmapDescriptorFactory.HUE_VIOLET;
            case "nướng":
            case "cơm":
                return BitmapDescriptorFactory.HUE_ORANGE;
            case "lẩu":
                return BitmapDescriptorFactory.HUE_GREEN;
            case "trà sữa":
                return BitmapDescriptorFactory.HUE_YELLOW;
            case "pizza":
                return BitmapDescriptorFactory.HUE_RED;
            default:
                return BitmapDescriptorFactory.HUE_AZURE;
        }
    }

    private String getEmojiForCategory(String category) {
        if (category == null) return "📍";
        switch (category.toLowerCase()) {
            case "bún bò":
            case "phở":
                return "🍜";
            case "nướng":
            case "cơm":
                return "🍢";
            case "lẩu":
                return "🍲";
            case "trà sữa":
                return "🧋";
            case "pizza":
                return "🍕";
            default:
                return "📍";
        }
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