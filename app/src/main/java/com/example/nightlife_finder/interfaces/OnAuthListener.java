package com.example.nightlife_finder.interfaces;

import com.google.firebase.auth.FirebaseUser;

/**
 * OnAuthListener - Callback cho các thao tác xác thực (Login / Register).
 * Thuộc phần việc của Đức (Support Backend).
 * Được dùng bởi AuthHelper và AuthRepository.
 */
public interface OnAuthListener {
    /**
     * Gọi khi thao tác xác thực thành công.
     * @param firebaseUser người dùng Firebase hiện tại (không null).
     */
    void onSuccess(FirebaseUser firebaseUser);

    /**
     * Gọi khi thao tác xác thực thất bại.
     * @param error thông báo lỗi từ Firebase hoặc logic nội bộ.
     */
    void onError(String error);
}
