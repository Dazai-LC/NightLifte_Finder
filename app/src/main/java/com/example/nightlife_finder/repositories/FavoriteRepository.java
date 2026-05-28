package com.example.nightlife_finder.repositories;

import com.example.nightlife_finder.constants.FirebaseConstants;
import com.example.nightlife_finder.firebase.FirebaseManager;
import com.example.nightlife_finder.firebase.FirestoreHelper;
import com.example.nightlife_finder.interfaces.OnFavoriteListener;
import com.example.nightlife_finder.interfaces.OnPlaceLoadedListener;
import com.example.nightlife_finder.models.Place;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * FavoriteRepository - Xử lý logic thêm/xoá/lấy danh sách yêu thích.
 * Thuộc phần việc của Đức (Support Backend).
 *
 * LƯU Ý: Class này CHỈ được thao tác trên field "favorites" trong document User.
 * TUYỆT ĐỐI không tự ý thay đổi cấu trúc Model hay tên Collection.
 */
public class FavoriteRepository {

    private final FirebaseFirestore firestore;

    public FavoriteRepository() {
        this.firestore = FirebaseManager.getInstance().getFirestore();
    }

    // -------------------------------------------------------
    // Thêm một địa điểm vào danh sách yêu thích
    // -------------------------------------------------------
    public void addFavorite(String uid, String placeId, OnFavoriteListener listener) {
        firestore.collection(FirebaseConstants.COLLECTION_USERS)
                .document(uid)
                .update(FirebaseConstants.FIELD_FAVORITES, FieldValue.arrayUnion(placeId))
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // -------------------------------------------------------
    // Xoá một địa điểm khỏi danh sách yêu thích
    // -------------------------------------------------------
    public void removeFavorite(String uid, String placeId, OnFavoriteListener listener) {
        firestore.collection(FirebaseConstants.COLLECTION_USERS)
                .document(uid)
                .update(FirebaseConstants.FIELD_FAVORITES, FieldValue.arrayRemove(placeId))
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // -------------------------------------------------------
    // Lấy toàn bộ danh sách Place yêu thích của user
    // -------------------------------------------------------
    public void getFavorites(String uid, OnPlaceLoadedListener listener) {
        firestore.collection(FirebaseConstants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        listener.onError("Không tìm thấy người dùng.");
                        return;
                    }

                    // Lấy list Place ID từ field "favorites"
                    List<String> favoriteIds = (List<String>) documentSnapshot.get(FirebaseConstants.FIELD_FAVORITES);
                    if (favoriteIds == null || favoriteIds.isEmpty()) {
                        listener.onSuccess(new ArrayList<>());
                        return;
                    }

                    // Lấy thông tin Place theo từng ID
                    fetchPlacesByIds(favoriteIds, listener);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // -------------------------------------------------------
    // Kiểm tra một địa điểm có trong favorites chưa
    // -------------------------------------------------------
    public void isFavorite(String uid, String placeId, IsFavoriteCallback callback) {
        firestore.collection(FirebaseConstants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> favorites = (List<String>) documentSnapshot.get(FirebaseConstants.FIELD_FAVORITES);
                    boolean result = favorites != null && favorites.contains(placeId);
                    callback.onResult(result);
                })
                .addOnFailureListener(e -> callback.onResult(false));
    }

    // -------------------------------------------------------
    // Lấy Place objects theo danh sách ID
    // -------------------------------------------------------
    private void fetchPlacesByIds(List<String> placeIds, OnPlaceLoadedListener listener) {
        List<Place> result = new ArrayList<>();
        final int[] remaining = {placeIds.size()};

        for (String placeId : placeIds) {
            firestore.collection(FirebaseConstants.COLLECTION_PLACES)
                    .document(placeId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            if (!Boolean.FALSE.equals(doc.getBoolean("isActive"))) {
                                Place place = doc.toObject(Place.class);
                                if (place != null) result.add(place);
                            }
                        }
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            listener.onSuccess(result);
                        }
                    })
                    .addOnFailureListener(e -> {
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            listener.onSuccess(result); // trả về những gì lấy được
                        }
                    });
        }
    }

    // -------------------------------------------------------
    // Inner callback interface cho isFavorite()
    // -------------------------------------------------------
    public interface IsFavoriteCallback {
        void onResult(boolean isFavorite);
    }
}
