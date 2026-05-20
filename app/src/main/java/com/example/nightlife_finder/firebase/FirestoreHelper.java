package com.example.nightlife_finder.firebase;

import com.example.nightlife_finder.constants.FirebaseConstants;
import com.example.nightlife_finder.interfaces.OnPlaceLoadedListener;
import com.example.nightlife_finder.models.Place;
import com.example.nightlife_finder.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

/**
 * FirestoreHelper - Chứa các hàm query/truy vấn Firestore tập trung.
 * Mọi thao tác đọc/ghi dữ liệu Firestore đều đi qua class này.
 * Thuộc phần việc của Cương (Core Lead).
 */
public class FirestoreHelper {

    private final FirebaseFirestore firestore;

    public FirestoreHelper() {
        this.firestore = FirebaseManager.getInstance().getFirestore();
    }

    // -------------------------------------------------------
    // USER - Lưu user mới lên Firestore
    // -------------------------------------------------------
    public Task<Void> saveUser(User user) {
        return firestore
                .collection(FirebaseConstants.COLLECTION_USERS)
                .document(user.getUid())
                .set(user);
    }

    // -------------------------------------------------------
    // USER - Lấy thông tin user theo UID
    // -------------------------------------------------------
    public Task<DocumentSnapshot> getUser(String uid) {
        return firestore
                .collection(FirebaseConstants.COLLECTION_USERS)
                .document(uid)
                .get();
    }

    // -------------------------------------------------------
    // USER - Cập nhật một field cụ thể của user
    // -------------------------------------------------------
    public Task<Void> updateUserField(String uid, String field, Object value) {
        return firestore
                .collection(FirebaseConstants.COLLECTION_USERS)
                .document(uid)
                .update(field, value);
    }

    // -------------------------------------------------------
    // PLACE - Lấy toàn bộ danh sách địa điểm
    // -------------------------------------------------------
    public Task<QuerySnapshot> getAllPlaces() {
        return firestore
                .collection(FirebaseConstants.COLLECTION_PLACES)
                .get();
    }

    // -------------------------------------------------------
    // PLACE - Lấy địa điểm theo ID
    // -------------------------------------------------------
    public Task<DocumentSnapshot> getPlaceById(String placeId) {
        return firestore
                .collection(FirebaseConstants.COLLECTION_PLACES)
                .document(placeId)
                .get();
    }

    // -------------------------------------------------------
    // PLACE - Lọc địa điểm theo danh mục (category)
    // -------------------------------------------------------
    public Task<QuerySnapshot> getPlacesByCategory(String category) {
        return firestore
                .collection(FirebaseConstants.COLLECTION_PLACES)
                .whereEqualTo(FirebaseConstants.FIELD_CATEGORY, category)
                .get();
    }

    // -------------------------------------------------------
    // PLACE - Lấy document reference (dùng để cập nhật/xoá)
    // -------------------------------------------------------
    public DocumentReference getPlaceRef(String placeId) {
        return firestore
                .collection(FirebaseConstants.COLLECTION_PLACES)
                .document(placeId);
    }

    // -------------------------------------------------------
    // FAVORITES - Cập nhật danh sách yêu thích của user
    // -------------------------------------------------------
    public Task<Void> updateFavorites(String uid, List<String> favorites) {
        return firestore
                .collection(FirebaseConstants.COLLECTION_USERS)
                .document(uid)
                .update(FirebaseConstants.FIELD_FAVORITES, favorites);
    }
}
