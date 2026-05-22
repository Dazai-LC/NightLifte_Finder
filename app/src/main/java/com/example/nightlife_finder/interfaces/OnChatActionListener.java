package com.example.nightlife_finder.interfaces;

/**
 * OnChatActionListener – callback cho thao tác tạo conversation hoặc gửi message.
 */
public interface OnChatActionListener {
    /** Gọi khi thao tác thành công. @param id document ID vừa tạo. */
    void onSuccess(String id);
    /** Gọi khi có lỗi. */
    void onError(String error);
}
