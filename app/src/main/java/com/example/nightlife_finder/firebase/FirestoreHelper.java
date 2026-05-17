package com.example.nightlife_finder.firebase;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreHelper {
    private final FirebaseFirestore db;

    public FirestoreHelper() {
        this.db = FirebaseManager.getInstance().getFirestore();
    }

    // Lấy tham chiếu đến bảng users
    public CollectionReference getUsersCollection() {
        return db.collection("users");
    }

    // Lấy tham chiếu đến bảng places
    public CollectionReference getPlacesCollection() {
        return db.collection("places");
    }
}