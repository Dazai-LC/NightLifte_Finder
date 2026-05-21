package com.example.nightlife_finder.constants;

/**
 * AppConstants - Lưu trữ các hằng số chung của ứng dụng.
 * Thuộc phần việc của Cương (Core Lead).
 *
 * NGHIÊM CẤM thay đổi nội dung class này mà không có sự thống nhất
 * với Core Lead (Cương).
 */
public class AppConstants {

    // -------------------------------------------------------
    // SharedPreferences
    // -------------------------------------------------------
    public static final String PREF_NAME          = "nightlife_prefs";
    public static final String PREF_USER_UID      = "user_uid";
    public static final String PREF_USER_EMAIL    = "user_email";

    // -------------------------------------------------------
    // Intent Extra Keys
    // -------------------------------------------------------
    public static final String EXTRA_PLACE_ID     = "extra_place_id";
    public static final String EXTRA_USER_UID     = "extra_user_uid";
    public static final String EXTRA_CATEGORY     = "extra_category";

    // -------------------------------------------------------
    // Category Values (phải đồng bộ với giá trị trên Firestore)
    // -------------------------------------------------------
    public static final String CATEGORY_BAR       = "Bar";
    public static final String CATEGORY_CLUB      = "Club";
    public static final String CATEGORY_CAFE      = "Cafe";
    public static final String CATEGORY_KARAOKE   = "Karaoke";
    public static final String CATEGORY_RESTAURANT = "Restaurant";

    // -------------------------------------------------------
    // Pagination
    // -------------------------------------------------------
    public static final int PAGE_SIZE             = 20;

    // -------------------------------------------------------
    // Request Codes
    // -------------------------------------------------------
    public static final int REQUEST_PICK_IMAGE    = 1001;
    public static final int REQUEST_LOCATION      = 1002;

    // Private constructor — không cho phép khởi tạo
    private AppConstants() {
    }
}
