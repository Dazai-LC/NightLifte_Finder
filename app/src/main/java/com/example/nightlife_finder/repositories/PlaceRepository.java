package com.example.nightlife_finder.repositories;

import com.example.nightlife_finder.firebase.FirestoreHelper;
import com.example.nightlife_finder.interfaces.OnPlaceLoadedListener;
import com.example.nightlife_finder.models.Place;

import java.util.ArrayList;
import java.util.List;

/**
 * PlaceRepository - Quản lý logic tải & truy vấn địa điểm từ Firestore.
 * Thuộc phần việc của Cương (Core Lead).
 */
public class PlaceRepository {

    private final FirestoreHelper firestoreHelper;

    public PlaceRepository() {
        this.firestoreHelper = new FirestoreHelper();
    }

    // -------------------------------------------------------
    // Tải toàn bộ danh sách địa điểm
    // -------------------------------------------------------
    public void getPlaces(OnPlaceLoadedListener listener) {
        firestoreHelper.getAllPlaces()
                .addOnSuccessListener(querySnapshot -> {
                    List<Place> places = querySnapshot.toObjects(Place.class);
                    listener.onSuccess(places);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // -------------------------------------------------------
    // Lấy địa điểm theo ID
    // -------------------------------------------------------
    public void getPlaceById(String placeId, OnPlaceLoadedListener listener) {
        firestoreHelper.getPlaceById(placeId)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Place place = documentSnapshot.toObject(Place.class);
                        List<Place> result = new ArrayList<>();
                        if (place != null) result.add(place);
                        listener.onSuccess(result);
                    } else {
                        listener.onError("Địa điểm không tồn tại.");
                    }
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // -------------------------------------------------------
    // Lọc địa điểm theo danh mục
    // -------------------------------------------------------
    public void getPlacesByCategory(String category, OnPlaceLoadedListener listener) {
        firestoreHelper.getPlacesByCategory(category)
                .addOnSuccessListener(querySnapshot -> {
                    List<Place> places = querySnapshot.toObjects(Place.class);
                    listener.onSuccess(places);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }
}