package com.example.nightlife_finder.interfaces;

/**
 * OnStorageListener - Callback cho thao tác Firebase Storage (upload/delete ảnh).
 * Được dùng bởi StorageHelper.
 * Thuộc phần việc của Đức (Support Backend).
 */
public interface OnStorageListener {
    /**
     * Gọi khi thao tác thành công.
     * @param downloadUrl URL download của file (null nếu là thao tác xoá).
     */
    void onSuccess(String downloadUrl);

    /**
     * Gọi khi thao tác thất bại.
     * @param error thông báo lỗi.
     */
    void onError(String error);
}
