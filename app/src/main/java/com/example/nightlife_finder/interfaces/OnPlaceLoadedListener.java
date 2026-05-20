package com.example.nightlife_finder.interfaces;

import com.example.nightlife_finder.models.Place;

import java.util.List;

/**
 * OnPlaceLoadedListener - Callback cho thao tác tải dữ liệu địa điểm.
 * Thuộc phần việc của Đức (Support Backend).
 * Được dùng bởi PlaceRepository và FavoriteRepository.
 */
public interface OnPlaceLoadedListener {
    /**
     * Gọi khi tải danh sách địa điểm thành công.
     * @param places danh sách địa điểm trả về (có thể rỗng nhưng không null).
     */
    void onSuccess(List<Place> places);

    /**
     * Gọi khi tải dữ liệu thất bại.
     * @param error thông báo lỗi.
     */
    void onError(String error);
}
