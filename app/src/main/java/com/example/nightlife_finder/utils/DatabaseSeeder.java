package com.example.nightlife_finder.utils;

import com.example.nightlife_finder.models.Place;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class DatabaseSeeder {

    public static void seedPlacesIfEmpty() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("places").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                WriteBatch batch = db.batch();

                List<Place> places = new ArrayList<>();
                places.add(new Place("Bún bò Đêm Phố Cổ", "18 Hàng Muối, Hoàn Kiếm, Hà Nội", "Bún bò", "Mở đến 02:30", "bunbo", 21.0333, 105.8500));
                places.add(new Place("Đồ nướng Hàng Bạc", "46 Hàng Bạc, Hoàn Kiếm, Hà Nội", "Nướng", "Mở đến 01:30", "bbq", 21.0345, 105.8520));
                places.add(new Place("Lẩu khuya Hoàn Kiếm", "5 Đinh Tiên Hoàng, Hoàn Kiếm, Hà Nội", "Lẩu", "Mở đến 03:00", "lau", 21.0310, 105.8530));
                places.add(new Place("Pizza Midnight Hà Nội", "12 Lý Thái Tổ, Hoàn Kiếm, Hà Nội", "Pizza", "Mở đến 04:00", "pizza", 21.0295, 105.8490));
                places.add(new Place("Trà sữa 24h Phố Cổ", "38 Hàng Đường, Hoàn Kiếm, Hà Nội", "Trà sữa", "Mở cả đêm", "che", 21.0320, 105.8515));

                places.add(new Place("Bạch tuộc Neon", "8 Tạ Hiện, Hoàn Kiếm, Hà Nội", "Nướng", "Mở đến 02:00", "bar", 21.0350, 105.8525));
                places.add(new Place("Quán ăn Starlight", "15 Tràng Thi, Hoàn Kiếm, Hà Nội", "Lẩu", "Mở đến 02:00", "diner", 21.0275, 105.8480));
                places.add(new Place("Pizza lát đỏ thẫm", "22 Hàng Trống, Hoàn Kiếm, Hà Nội", "Pizza", "Mở đến 01:30", "pizza", 21.0305, 105.8485));
                places.add(new Place("Cơm Tấm Đêm Hà Nội", "52 Hàng Bông, Hoàn Kiếm, Hà Nội", "Cơm", "Mở đến 03:00", "bar", 21.0300, 105.8470));
                places.add(new Place("Phở Gà Đêm Hàng Bạc", "86 Hàng Bạc, Hoàn Kiếm, Hà Nội", "Phở", "Mở đến 04:00", "diner", 21.0343, 105.8523));

                for (int i = 0; i < places.size(); i++) {
                    Place p = places.get(i);
                    String docId = "place_" + (i + 1);
                    batch.set(db.collection("places").document(docId), p);
                }

                batch.commit();
            }
        });
    }
}
