package com.example.nightlife_finder.repositories;

import com.example.nightlife_finder.models.Place;

import java.util.ArrayList;
import java.util.List;

public class PlaceRepository {

    public static List<Place> getAllPlaces() {
        List<Place> places = new ArrayList<>();

        places.add(new Place(
                "bach_tuoc_neon",
                "Quán Nướng Vân Tảo",
                "food",
                "Quán ăn / Đồ nướng",
                "Khu Vân Tảo, Thường Tín, Hà Nội",
                "Mở 17:00 - 01:00",
                "bach_tuoc,nuong,hai_san,bbq",
                20.8915,
                105.8693,
                80,
                4.9f
        ));

        places.add(new Place(
                "bach_tuoc_ql1a",
                "Nướng Đêm Quốc Lộ 1A",
                "food",
                "Quán ăn / Đồ nướng",
                "Gần 60 QL1A, Thường Tín, Hà Nội",
                "Mở 18:00 - 02:00",
                "bach_tuoc,nuong,hai_san,bbq",
                20.8712,
                105.8643,
                88,
                4.8f
        ));

        places.add(new Place(
                "lau_ha_hoi",
                "Lẩu Khuya Hà Hồi",
                "food",
                "Quán ăn / Lẩu",
                "Khu Hà Hồi, Thường Tín, Hà Nội",
                "Mở 16:00 - 02:00",
                "lau,hai_san,bo,ga",
                20.8586,
                105.8555,
                85,
                4.6f
        ));

        places.add(new Place(
                "pho_ql1a",
                "Phở Đêm Quốc Lộ 1A",
                "food",
                "Quán ăn / Phở",
                "Khu thị trấn Thường Tín, Hà Nội",
                "Mở 18:00 - 02:00",
                "pho,pho_bo,pho_ga",
                20.8696,
                105.8618,
                86,
                4.7f
        ));

        places.add(new Place(
                "bun_bo_quat_dong",
                "Bún Bò Chị Hương Quất Động",
                "food",
                "Quán ăn / Bún bò",
                "Khu Quất Động, Thường Tín, Hà Nội",
                "Mở 17:30 - 01:00",
                "bun_bo,bun,bo",
                20.8876,
                105.8537,
                80,
                4.5f
        ));

        places.add(new Place(
                "com_rang_anh_tu",
                "Cơm Rang Anh Tú",
                "food",
                "Quán ăn / Cơm rang",
                "Khu trung tâm Thường Tín, Hà Nội",
                "Mở 16:00 - 01:30",
                "com_rang,com,ga,bo",
                20.8709,
                105.8650,
                84,
                4.6f
        ));

        places.add(new Place(
                "cafe_pho_huyen",
                "Cà Phê Phố Huyện",
                "cafe",
                "Cafe / Gặp bạn bè",
                "Khu thị trấn Thường Tín, Hà Nội",
                "Mở 07:00 - 00:00",
                "cafe,ca_phe,do_uong",
                20.8718,
                105.8625,
                72,
                4.5f
        ));

        places.add(new Place(
                "tra_sua_bong_may",
                "Trà Sữa Bông Mây",
                "tea",
                "Trà sữa / Đồ uống",
                "Khu trung tâm Thường Tín, Hà Nội",
                "Mở 24/7",
                "tra_sua,do_uong,tra_chanh",
                20.8701,
                105.8604,
                100,
                4.8f
        ));

        places.add(new Place(
                "tien_loi_ql1a",
                "Tiệm Tiện Lợi Quốc Lộ 1A",
                "store",
                "Cửa hàng tiện lợi",
                "60 QL1A, Thường Tín, Hà Nội",
                "Mở 24/7",
                "cua_hang,tien_loi,do_an_nhanh,nuoc_uong",
                20.8712,
                105.8643,
                100,
                4.7f
        ));

        places.add(new Place(
                "sieu_thi_thuong_tin",
                "Siêu Thị Mini Thường Tín",
                "supermarket",
                "Siêu thị mini",
                "Khu trung tâm Thường Tín, Hà Nội",
                "Mở 08:00 - 23:00",
                "sieu_thi,thuc_pham,do_an,do_uong",
                20.8690,
                105.8637,
                64,
                4.2f
        ));

        return places;
    }

    public static List<Place> getPlacesByDish(String dishKey) {
        List<Place> result = new ArrayList<>();

        for (Place place : getAllPlaces()) {
            if (place.dishTags.contains(dishKey)) {
                result.add(place);
            }
        }

        return result;
    }

    public static List<Place> getPlacesByCategory(String category) {
        List<Place> result = new ArrayList<>();

        for (Place place : getAllPlaces()) {
            if (place.category.equals(category)) {
                result.add(place);
            }
        }

        return result;
    }

    public static Place getPlaceById(String id) {
        for (Place place : getAllPlaces()) {
            if (place.id.equals(id)) {
                return place;
            }
        }

        return null;
    }
}