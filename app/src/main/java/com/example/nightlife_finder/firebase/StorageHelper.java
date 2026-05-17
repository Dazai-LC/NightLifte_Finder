package com.example.nightlife_finder.firebase;

import android.net.Uri;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class StorageHelper {
    private final FirebaseStorage storage;

    public StorageHelper() {
        this.storage = FirebaseManager.getInstance().getStorage();
    }

    // Lấy thư mục gốc
    public StorageReference getRootReference() {
        return storage.getReference();
    }

    // Ví dụ hàm upload ảnh cơ bản
    public UploadTask uploadImage(String path, String fileName, Uri fileUri) {
        StorageReference ref = storage.getReference().child(path).child(fileName);
        return ref.putFile(fileUri);
    }
}