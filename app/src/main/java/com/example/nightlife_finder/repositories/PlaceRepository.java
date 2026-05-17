package com.example.nightlife_finder.repositories;

import com.example.nightlife_finder.constants.FirebaseConstants;
import com.example.nightlife_finder.firebase.FirebaseManager;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class PlaceRepository {
    private final FirebaseFirestore firestore;

    public PlaceRepository() {
        // Lấy kết nối Firestore từ Singleton Manager
        firestore = FirebaseManager.getInstance().getFirestore();
    }

    /**
     * Lấy toàn bộ danh sách địa điểm (Dùng cho màn hình Home/List)
     */
    public Task<QuerySnapshot> getAllPlaces() {
        return firestore.collection(FirebaseConstants.COLLECTION_PLACES)
                .get();
    }

    /**
     * Lấy chi tiết một địa điểm cụ thể dựa vào ID
     */
    public Task<DocumentSnapshot> getPlaceById(String placeId) {
        return firestore.collection(FirebaseConstants.COLLECTION_PLACES)
                .document(placeId)
                .get();
    }
}