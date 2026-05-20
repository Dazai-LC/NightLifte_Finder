package com.example.nightlife_finder.firebase;

import android.net.Uri;

import com.example.nightlife_finder.interfaces.OnStorageListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * StorageHelper - Upload hình ảnh và lấy URL ảnh từ Firebase Storage.
 * Thuộc phần việc của Cương (Core Lead).
 */
public class StorageHelper {

    private final FirebaseStorage storage;

    public StorageHelper() {
        this.storage = FirebaseManager.getInstance().getStorage();
    }

    // -------------------------------------------------------
    // Upload ảnh đại diện người dùng
    // path: "avatars/{uid}.jpg"
    // -------------------------------------------------------
    public void uploadUserAvatar(String uid, Uri imageUri, OnStorageListener listener) {
        StorageReference ref = storage.getReference()
                .child("avatars")
                .child(uid + ".jpg");
        uploadImage(ref, imageUri, listener);
    }

    // -------------------------------------------------------
    // Upload ảnh địa điểm
    // path: "places/{placeId}.jpg"
    // -------------------------------------------------------
    public void uploadPlaceImage(String placeId, Uri imageUri, OnStorageListener listener) {
        StorageReference ref = storage.getReference()
                .child("places")
                .child(placeId + ".jpg");
        uploadImage(ref, imageUri, listener);
    }

    // -------------------------------------------------------
    // Lấy URL download của ảnh theo đường dẫn đầy đủ
    // Ví dụ: fullPath = "avatars/abc123.jpg"
    // -------------------------------------------------------
    public void getImageUrl(String fullPath, OnStorageListener listener) {
        storage.getReference()
                .child(fullPath)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> listener.onSuccess(uri.toString()))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // -------------------------------------------------------
    // Lấy URL download của ảnh đại diện theo UID
    // -------------------------------------------------------
    public void getUserAvatarUrl(String uid, OnStorageListener listener) {
        getImageUrl("avatars/" + uid + ".jpg", listener);
    }

    // -------------------------------------------------------
    // Lấy URL download của ảnh địa điểm theo placeId
    // -------------------------------------------------------
    public void getPlaceImageUrl(String placeId, OnStorageListener listener) {
        getImageUrl("places/" + placeId + ".jpg", listener);
    }

    // -------------------------------------------------------
    // Xoá ảnh theo đường dẫn đầy đủ (fullPath trên Storage)
    // -------------------------------------------------------
    public void deleteImage(String fullPath, OnStorageListener listener) {
        storage.getReference()
                .child(fullPath)
                .delete()
                .addOnSuccessListener(unused -> listener.onSuccess(null))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // -------------------------------------------------------
    // Hàm upload chung — dùng nội bộ
    // -------------------------------------------------------
    private void uploadImage(StorageReference ref, Uri imageUri, OnStorageListener listener) {
        UploadTask uploadTask = ref.putFile(imageUri);
        uploadTask
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl()
                                .addOnSuccessListener(uri -> listener.onSuccess(uri.toString()))
                                .addOnFailureListener(e -> listener.onError(e.getMessage()))
                )
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }
}
