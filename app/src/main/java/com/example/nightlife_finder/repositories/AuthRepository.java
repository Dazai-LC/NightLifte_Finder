package com.example.nightlife_finder.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthRepository {
    private final FirebaseAuth firebaseAuth;

    public AuthRepository() {
        // Khởi tạo FirebaseAuth
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Đăng ký tài khoản mới bằng Email và Password
     */
    public Task<AuthResult> register(String email, String password) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password);
    }

    /**
     * Đăng nhập bằng Email và Password
     */
    public Task<AuthResult> login(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    /**
     * Đăng xuất tài khoản hiện tại
     */
    public void logout() {
        firebaseAuth.signOut();
    }

    /**
     * Lấy thông tin User hiện tại nếu đã đăng nhập
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
}