package com.example.nightlife_finder.firebase;

import com.example.nightlife_finder.interfaces.OnAuthListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * AuthHelper - Xử lý nghiệp vụ xác thực: Login, Register, Logout.
 * Lấy FirebaseAuth thông qua FirebaseManager (Singleton).
 * Thuộc phần việc của Cương (Core Lead).
 */
public class AuthHelper {

    private final FirebaseAuth auth;

    public AuthHelper() {
        this.auth = FirebaseManager.getInstance().getAuth();
    }

    // -------------------------------------------------------
    // Đăng ký tài khoản mới
    // -------------------------------------------------------
    public void register(String email, String password, OnAuthListener listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) {
                        listener.onSuccess(user);
                    } else {
                        listener.onError("Đăng ký thất bại: không lấy được thông tin tài khoản.");
                    }
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // -------------------------------------------------------
    // Đăng nhập
    // -------------------------------------------------------
    public void login(String email, String password, OnAuthListener listener) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) {
                        listener.onSuccess(user);
                    } else {
                        listener.onError("Đăng nhập thất bại: không lấy được thông tin tài khoản.");
                    }
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // -------------------------------------------------------
    // Đăng xuất
    // -------------------------------------------------------
    public void logout() {
        auth.signOut();
    }

    // -------------------------------------------------------
    // Lấy người dùng hiện tại
    // -------------------------------------------------------
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    // -------------------------------------------------------
    // Kiểm tra trạng thái đăng nhập
    // -------------------------------------------------------
    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }
}
