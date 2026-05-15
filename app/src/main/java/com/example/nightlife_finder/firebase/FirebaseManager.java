package com.example.nightlife_finder.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseManager {

    private static final FirebaseAuth auth =
            FirebaseAuth.getInstance();

    private static final FirebaseFirestore firestore =
            FirebaseFirestore.getInstance();

    public static FirebaseAuth getAuth() {
        return auth;
    }

    public static FirebaseFirestore getFirestore() {
        return firestore;
    }
}