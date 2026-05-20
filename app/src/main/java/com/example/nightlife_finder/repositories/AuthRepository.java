package com.example.nightlife_finder.repositories;

import com.example.nightlife_finder.constants.FirebaseConstants;
import com.example.nightlife_finder.firebase.AuthHelper;
import com.example.nightlife_finder.firebase.FirestoreHelper;
import com.example.nightlife_finder.interfaces.OnAuthListener;
import com.example.nightlife_finder.models.User;
import com.google.firebase.auth.FirebaseUser;

/**
 * AuthRepository - Đóng gói toàn bộ logic xác thực và quản lý người dùng.
 * Kết hợp AuthHelper (Firebase Auth) và FirestoreHelper (Firestore).
 * Thuộc phần việc của Cương (Core Lead).
 */
public class AuthRepository {

    private final AuthHelper authHelper;
    private final FirestoreHelper firestoreHelper;

    public AuthRepository() {
        this.authHelper = new AuthHelper();
        this.firestoreHelper = new FirestoreHelper();
    }

    // -------------------------------------------------------
    // Đăng ký: tạo tài khoản Auth + lưu User vào Firestore
    // -------------------------------------------------------
    public void register(String email, String password, OnAuthListener listener) {
        authHelper.register(email, password, new OnAuthListener() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                // Tạo User model và lưu vào Firestore
                User newUser = new User(firebaseUser.getUid(), email);
                firestoreHelper.saveUser(newUser)
                        .addOnSuccessListener(unused -> listener.onSuccess(firebaseUser))
                        .addOnFailureListener(e -> listener.onError(e.getMessage()));
            }

            @Override
            public void onError(String error) {
                listener.onError(error);
            }
        });
    }

    // -------------------------------------------------------
    // Đăng nhập
    // -------------------------------------------------------
    public void login(String email, String password, OnAuthListener listener) {
        authHelper.login(email, password, listener);
    }

    // -------------------------------------------------------
    // Đăng xuất
    // -------------------------------------------------------
    public void logout() {
        authHelper.logout();
    }

    // -------------------------------------------------------
    // Lấy người dùng hiện tại (Firebase Auth)
    // -------------------------------------------------------
    public FirebaseUser getCurrentUser() {
        return authHelper.getCurrentUser();
    }

    // -------------------------------------------------------
    // Kiểm tra trạng thái đăng nhập
    // -------------------------------------------------------
    public boolean isLoggedIn() {
        return authHelper.isLoggedIn();
    }
}
