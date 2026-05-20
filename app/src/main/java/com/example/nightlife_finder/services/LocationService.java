package com.example.nightlife_finder.services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

/**
 * LocationService – Tiện ích lấy vị trí hiện tại của người dùng.
 *
 * Cách dùng:
 *   LocationService.getInstance(context).getLastLocation(callback);
 *   LocationService.getInstance(context).startUpdates(callback);
 *   LocationService.getInstance(context).stopUpdates();
 *
 * LƯU Ý: Activity gọi phải có quyền ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION
 * được cấp trước khi gọi các phương thức này.
 */
public class LocationService {

    // -------------------------------------------------------
    // Singleton
    // -------------------------------------------------------
    private static LocationService instance;

    public static synchronized LocationService getInstance(Context context) {
        if (instance == null) {
            instance = new LocationService(context.getApplicationContext());
        }
        return instance;
    }

    // -------------------------------------------------------
    // Fields
    // -------------------------------------------------------
    private final Context context;
    private final FusedLocationProviderClient fusedClient;
    private LocationCallback activeCallback;

    private static final long UPDATE_INTERVAL_MS   = 10_000L; // 10 giây
    private static final long FASTEST_INTERVAL_MS  =  5_000L; // 5 giây

    // -------------------------------------------------------
    // Constructor (private)
    // -------------------------------------------------------
    private LocationService(Context context) {
        this.context = context;
        this.fusedClient = LocationServices.getFusedLocationProviderClient(context);
    }

    // -------------------------------------------------------
    // Callback interface
    // -------------------------------------------------------
    public interface OnLocationReceived {
        void onLocation(double lat, double lng);
        void onError(String message);
    }

    // -------------------------------------------------------
    // Lấy vị trí gần nhất (one-shot)
    // -------------------------------------------------------
    public void getLastLocation(@NonNull OnLocationReceived callback) {
        if (!hasPermission()) {
            callback.onError("Chưa được cấp quyền truy cập vị trí.");
            return;
        }

        fusedClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        callback.onLocation(location.getLatitude(), location.getLongitude());
                    } else {
                        // Last location is null → request a single update
                        requestSingleUpdate(callback);
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // -------------------------------------------------------
    // Yêu cầu cập nhật vị trí liên tục
    // -------------------------------------------------------
    public void startUpdates(@NonNull OnLocationReceived callback) {
        if (!hasPermission()) {
            callback.onError("Chưa được cấp quyền truy cập vị trí.");
            return;
        }

        LocationRequest request = new LocationRequest.Builder(UPDATE_INTERVAL_MS)
                .setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();

        activeCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location location = result.getLastLocation();
                if (location != null) {
                    callback.onLocation(location.getLatitude(), location.getLongitude());
                }
            }
        };

        fusedClient.requestLocationUpdates(request, activeCallback, Looper.getMainLooper());
    }

    // -------------------------------------------------------
    // Dừng cập nhật vị trí
    // -------------------------------------------------------
    public void stopUpdates() {
        if (activeCallback != null) {
            fusedClient.removeLocationUpdates(activeCallback);
            activeCallback = null;
        }
    }

    // -------------------------------------------------------
    // Tính khoảng cách giữa 2 điểm (đơn vị: km)
    // -------------------------------------------------------
    public static double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        return results[0] / 1000.0;
    }

    // -------------------------------------------------------
    // Kiểm tra quyền truy cập vị trí
    // -------------------------------------------------------
    public boolean hasPermission() {
        return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // -------------------------------------------------------
    // Private: request a single location update
    // -------------------------------------------------------
    private void requestSingleUpdate(@NonNull OnLocationReceived callback) {
        if (!hasPermission()) return;

        LocationRequest request = new LocationRequest.Builder(1000)
                .setMaxUpdates(1)
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .build();

        LocationCallback singleCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                fusedClient.removeLocationUpdates(this);
                Location location = result.getLastLocation();
                if (location != null) {
                    callback.onLocation(location.getLatitude(), location.getLongitude());
                } else {
                    callback.onError("Không thể xác định vị trí.");
                }
            }
        };

        fusedClient.requestLocationUpdates(request, singleCallback, Looper.getMainLooper());
    }
}
