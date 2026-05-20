package com.example.nightlife_finder.constants;

/**
 * FirebaseConstants - Lưu trữ tên Collection và Field chuẩn trên Firestore.
 * NGHIÊM CẤM thay đổi giá trị các hằng số này mà không có sự thống nhất
 * với Core Lead (Cương), vì chúng phải đồng bộ 100% với cấu trúc Firebase.
 * Thuộc phần việc của Cương (Core Lead).
 */
public class FirebaseConstants {

    // -------------------------------------------------------
    // Tên Collection trong Firestore
    // -------------------------------------------------------
    public static final String COLLECTION_USERS  = "users";
    public static final String COLLECTION_PLACES = "places";

    // -------------------------------------------------------
    // Tên Field trong Collection USERS
    // -------------------------------------------------------
    public static final String FIELD_UID          = "uid";
    public static final String FIELD_EMAIL        = "email";
    public static final String FIELD_FAVORITES    = "favorites";

    // -------------------------------------------------------
    // Tên Field trong Collection PLACES
    // -------------------------------------------------------
    public static final String FIELD_NAME         = "name";
    public static final String FIELD_ADDRESS      = "address";
    public static final String FIELD_CATEGORY     = "category";
    public static final String FIELD_OPEN_TIME    = "openTime";
    public static final String FIELD_IMAGE_URL    = "imageUrl";
    public static final String FIELD_LAT          = "lat";
    public static final String FIELD_LNG          = "lng";

    // Private constructor — không cho phép khởi tạo
    private FirebaseConstants() {
    }
}