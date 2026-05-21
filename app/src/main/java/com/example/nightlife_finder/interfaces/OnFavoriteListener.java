package com.example.nightlife_finder.interfaces;

/**
 * OnFavoriteListener - Callback cho thao tác yêu thích (add/remove/get).
 * Thuộc phần việc của Đức (Support Backend).
 * Được dùng bởi FavoriteRepository.
 */
public interface OnFavoriteListener {
    /**
     * Gọi khi thao tác yêu thích thành công.
     */
    void onSuccess();

    /**
     * Gọi khi thao tác yêu thích thất bại.
     * @param error thông báo lỗi.
     */
    void onError(String error);
}
