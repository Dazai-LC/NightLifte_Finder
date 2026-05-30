# 📋 Audit Report – NightLife Finder (`final/demo-v4`)

> Branch: final/demo-v4
> Working tree: clean
> Commit cuối: 16ad1cd - Integrate improved map screen

---

## 1. Cấu trúc project chính

### 1.1 Activities

| File | Vai trò |
|---|---|
| `LoginActivity` | Đăng nhập Firebase Auth |
| `RegisterActivity` | Đăng ký tài khoản |
| `MainActivity` | Màn hình chính, duyệt địa điểm |
| `SearchResultsActivity` | Tìm kiếm + filter địa điểm |
| `PlaceDetailActivity` | Chi tiết địa điểm |
| `FavoriteActivity` | Yêu thích + lịch sử xem |
| `ChatActivity` | Danh sách cuộc hội thoại |
| `NewChatActivity` | Tạo cuộc hội thoại mới |
| `ChatDetailActivity` | Nội dung tin nhắn trong hội thoại |
| `ProfileActivity` | Hồ sơ người dùng |
| `EditProfileActivity` | Sửa hồ sơ, avatar, liên kết MXH |
| `AdminDashboardActivity` | Màn điều hướng admin |
| `AdminPlacesActivity` | Quản lý địa điểm (Firestore) |
| `AdminOpeningHoursActivity` | Quản lý giờ mở cửa (Firestore) |
| `AdminImagesActivity` | Quản lý ảnh địa điểm (Firestore) |
| `AdminReviewsActivity` | Quản lý đánh giá (UI demo) |
| `MapActivity` | Bản đồ OSM + GPS thật, hiển thị marker demo |
| `AvatarCropActivity` | Crop ảnh đại diện |
| `BaseActivity` | Base class chung |

### 1.2 Models

| File | Mô tả |
|---|---|
| `Place.java` | id, name, address, category, openTime, imageUrl, lat, lng, isActive |
| `User.java` | uid, email, favorites[], displayName, phone, location, bio, facebookUrl, instagramUrl, zaloContact |
| `Conversation.java` | id, title, createdBy, lastMessage, shopName, address, openTime, closeTime, isRead |
| `ChatMessage.java` | id, conversationId, senderId, senderEmail, text, createdAt |
| `NightPlace.java` | Model phụ (dùng nội bộ) |

### 1.3 Repositories

| File | Nhiệm vụ |
|---|---|
| `AuthRepository` | Gọi `AuthHelper` + `FirestoreHelper` để đăng ký/đăng nhập |
| `ChatRepository` | CRUD conversations + messages subcollection |
| `FavoriteRepository` | Thêm/xóa/kiểm tra favorites trong `users/{uid}.favorites[]` |
| `PlaceRepository` | Load places từ Firestore |
| `NightPlaceDatabase` | Room DB cục bộ (không dùng trong flow chính) |

### 1.4 Firebase / Helpers

| File | Nhiệm vụ |
|---|---|
| `FirebaseManager` | Singleton: trả về `FirebaseFirestore`, `FirebaseAuth`, `FirebaseStorage` |
| `AuthHelper` | Bọc `FirebaseAuth.createUserWithEmailAndPassword`, `signInWithEmailAndPassword` |
| `FirestoreHelper` | `saveUser()`, `getUser()`, `updateUser()` |
| `StorageHelper` | Upload ảnh lên Firebase Storage |

### 1.5 Utils

| File | Nhiệm vụ |
|---|---|
| `AppSettings` | SharedPreferences: name, location, phone, bio, darkMode, language, avatarPath |
| `ValidationUtils` | Kiểm tra email, password, confirm password |
| `DatabaseSeeder` | Seed 10 place mẫu vào Firestore nếu collection rỗng |
| `LocaleHelper` | Đổi ngôn ngữ app |

### 1.6 Adapters

| File | Nhiệm vụ |
|---|---|
| `PlaceAdapter` | RecyclerView hiển thị danh sách `Place`, callback click → `placeId` |
| `BaseAdapter` | Base class chung |

---

## 2. Phân tích từng Activity chính

### 🔐 LoginActivity
- **Chức năng:** Đăng nhập Firebase Auth bằng email + password.
- **Layout:** `activity_login.xml`
- **Dữ liệu:** Từ form EditText → `AuthRepository.login()`
- **Firebase:** ✅ Firebase Auth thật
- **Trạng thái:** ✅ Chạy thật hoàn toàn
- **Ghi chú:** Validate qua `ValidationUtils`. Sau login lưu username vào `AppSettings`.

### 📝 RegisterActivity
- **Chức năng:** Tạo tài khoản mới, tạo document `users/{uid}` trong Firestore.
- **Layout:** `activity_register.xml`
- **Dữ liệu:** Email + password + confirm → `AuthRepository.register()`
- **Firebase:** ✅ Firebase Auth + Firestore (tạo user doc)
- **Trạng thái:** ✅ Chạy thật

### 🏠 MainActivity
- **Chức năng:** Màn hình Home, có section RecyclerView load `places` thật từ Firestore.
- **Layout:** `activity_main.xml`
- **Dữ liệu:** Load `places` từ Firestore trong `onResume()` để khi quay lại từ Admin/Detail thì cập nhật lại.
- **Firebase:** ✅ Firestore (places), ✅ Auth (kiểm tra session)
- **Trạng thái:** ✅ Chạy thật (danh sách nơi nổi bật) / 🎭 Demo (Flash Deals / Hot / Near You card nếu còn hardcode).
- **Ghi chú:** Lọc `isActive != false`. Click item mở `PlaceDetailActivity` bằng documentId thật. Countdown timer Flash Deal có thể vẫn là demo/fixed.

### 🔍 SearchResultsActivity
- **Chức năng:** Tìm kiếm địa điểm theo text + filter chips.
- **Layout:** `activity_search_results.xml`
- **Dữ liệu:** Load toàn bộ `places` từ Firestore 1 lần → lọc local theo `name`, `category`, `address`, `openTime`.
- **Firebase:** ✅ Firestore
- **Trạng thái:** ✅ Chạy thật
- **Ghi chú:** Bỏ qua `isActive=false`. Filter chip lọc theo category. Click kết quả mở `PlaceDetailActivity`.

### 📍 PlaceDetailActivity
- **Chức năng:** Hiển thị chi tiết địa điểm, toggle yêu thích, nút chỉ đường Google Maps.
- **Layout:** `activity_place_detail.xml`
- **Dữ liệu:** Load `places/{placeId}` từ Firestore. Hiển thị name, address, category, openTime, imageUrl/key.
- **Firebase:** ✅ Firestore
- **Trạng thái:** ✅ Chạy thật
- **Ghi chú:** Favorite/unfavorite thật qua `users/{uid}.favorites`. Direction mở Google Maps Intent. Nếu `isActive=false`, hiển thị cảnh báo tạm khóa và hạn chế tương tác yêu thích. Lưu lịch sử xem vào SharedPreferences `nightlife_history`.

### ❤️ FavoriteActivity
- **Chức năng:** Hiển thị Yêu thích và Lịch sử xem.
- **Layout:** `activity_favorite.xml`
- **Dữ liệu:** Load danh sách yêu thích từ `users/{uid}.favorites` và fetch từng place từ `places`. Lịch sử xem dùng SharedPreferences + Firestore.
- **Firebase:** ✅ Firestore
- **Trạng thái:** ✅ Chạy thật
- **Ghi chú:** Lọc `isActive=false`, không tự xóa id khỏi favorites. Subtitle hiển thị số lượng thật. Bỏ yêu thích thật bằng Firestore. Chỉ đường thật bằng Google Maps Intent. (Nếu có nút chat prefill thì đó là tính năng mở rộng).

### 💬 ChatActivity
- **Chức năng:** Danh sách cuộc hội thoại, filter chip All/Unread, tìm kiếm.
- **Layout:** `activity_chat.xml`
- **Dữ liệu:** Có cả dynamic Firestore conversations và một số item hardcode demo (nếu còn tồn tại).
- **Firebase:** ✅ Firestore
- **Trạng thái:** ✅ Chạy thật (hội thoại động) / 🎭 Demo (những item XML hardcode).
- **Ghi chú:** `isRead` dùng để lọc chưa đọc. Một số quick action nếu chưa làm thật thì ghi là tính năng mở rộng.

### ✏️ NewChatActivity
- **Chức năng:** Tạo cuộc hội thoại mới.
- **Layout:** `activity_new_chat.xml`
- **Dữ liệu:** Form + `ChatRepository.createConversation()`
- **Firebase:** ✅ Firestore
- **Trạng thái:** ✅ Chạy thật

### 📩 ChatDetailActivity
- **Chức năng:** Nhắn tin trong một cuộc hội thoại.
- **Layout:** `activity_chat_detail.xml`
- **Dữ liệu:** Load metadata conversation và messages thật nếu mở bằng `CONVERSATION_ID`. `conversations/{id}/messages` là subcollection tin nhắn.
- **Firebase:** ✅ Firestore
- **Trạng thái:** ✅ Chạy thật

### 👤 ProfileActivity & EditProfileActivity
- **Chức năng:** Hiển thị/chỉnh sửa hồ sơ, thống kê, thao tác tài khoản.
- **Layout:** `activity_profile.xml`, `activity_edit_profile.xml`
- **Dữ liệu:** Email lấy từ FirebaseAuth. Thông tin lấy từ Firestore `users/{uid}`.
- **Firebase:** ✅ Firestore + ✅ Auth
- **Trạng thái:** ✅ Chạy thật
- **Ghi chú:** EditProfile lưu `displayName`, `phone`, `location`, `bio`, `facebookUrl`, `instagramUrl`, `zaloContact`. Reset password dùng `FirebaseAuth.sendPasswordResetEmail`. Social links mở browser/dialer nếu có dữ liệu. Stats: favorites từ `users/{uid}.favorites.size()`, conversations query theo `createdBy`, history từ SharedPreferences.

### 🛠️ AdminDashboardActivity
- **Chức năng:** Điều hướng module admin.
- **Layout:** `activity_admin_dashboard.xml`
- **Trạng thái:** ✅ Chạy thật (UI điều hướng)

### 🗂️ AdminPlacesActivity
- **Chức năng:** Quản lý địa điểm (Thêm/Sửa/Tạm khóa).
- **Layout:** `activity_admin_places.xml`
- **Firebase:** ✅ Firestore
- **Trạng thái:** ✅ Chạy thật
- **Ghi chú:** Load `places` thật. Tạm khóa/mở khóa bằng `isActive`. Filter Đang hiển thị / Tạm khóa.

### 🕐 AdminOpeningHoursActivity
- **Chức năng:** Quản lý giờ mở cửa của các địa điểm.
- **Layout:** `activity_admin_opening_hours.xml`
- **Firebase:** ✅ Firestore
- **Trạng thái:** ✅ Chạy thật
- **Ghi chú:** Load `places` thật. Cập nhật `openTime` vào Firestore.

### 🖼️ AdminImagesActivity
- **Chức năng:** Xem và cập nhật ảnh địa điểm.
- **Layout:** `activity_admin_images.xml`
- **Firebase:** ✅ Firestore
- **Trạng thái:** ✅ Chạy thật
- **Ghi chú:** Load `places` thật. Cập nhật field `imageUrl` bằng `SetOptions.merge()` hoặc update tương đương. Hỗ trợ key local hoặc URL (nếu có). Không upload ảnh Firebase Storage thật nếu chưa có.

### ⭐ AdminReviewsActivity
- **Chức năng:** Quản lý đánh giá.
- **Layout:** `activity_admin_reviews.xml`
- **Firebase:** ❌ Không kết nối
- **Trạng thái:** 🎭 Demo / UI mock (chưa kết nối Firestore thật).

### 🗺️ MapActivity
- **Chức năng:** Bản đồ định vị, xem địa điểm xung quanh.
- **Layout:** `activity_map.xml`
- **Firebase:** ❌ (Marker lấy từ dữ liệu Demo tĩnh)
- **Trạng thái:** ✅ GPS/Bản đồ thật / 🎭 Marker Demo
- **Ghi chú cập nhật:** 
  - Bản đồ mở được, view OSMdroid.
  - Lấy vị trí hiện tại thật bằng GPS / `FusedLocationProviderClient`. Có fallback location nếu lỗi hoặc chưa cấp quyền.
  - Hiển thị nhiều địa điểm/marker như quán ăn, cà phê, mì cay... (Dữ liệu marker trong Map hiện là dữ liệu demo/tĩnh của module Map, chưa đồng bộ hoàn toàn với collection `places`).
  - Bấm marker hiển thị thông tin: tên địa điểm, loại hình kinh doanh, giờ mở cửa/đóng cửa.
  - Có thể zoom tới địa điểm được chọn.
  - Chỉ đường mở Google Maps thật bằng Intent.
  - Back không crash.

---

## 3. Schema Firestore thực tế

```txt
users/{uid}
  uid
  email
  displayName
  phone
  location
  bio
  avatarUrl
  facebookUrl
  instagramUrl
  zaloContact
  favorites[]

places/{placeId}
  name
  address
  category
  openTime
  imageUrl
  lat
  lng
  description nếu có
  isActive

conversations/{conversationId}
  id
  title
  createdBy
  createdByEmail
  lastMessage
  createdAt
  lastMessageAt
  shopName
  shopAvatarText
  shopCategory
  address
  openTime
  closeTime
  isRead

conversations/{conversationId}/messages/{messageId}
  id
  conversationId
  senderId
  senderEmail
  text
  createdAt
```

---

## 4. Phân loại chức năng

### ✅ Đã chạy thật

| Chức năng | Module |
|---|---|
| Login/Register Firebase Auth | AuthRepository, LoginActivity, RegisterActivity |
| Reset password | ProfileActivity |
| Home RecyclerView Firestore | MainActivity |
| Search Firestore | SearchResultsActivity |
| PlaceDetail Firestore | PlaceDetailActivity |
| Favorite add/remove/load | PlaceDetailActivity, FavoriteActivity |
| History SharedPrefs + Firestore | FavoriteActivity |
| Chat conversations/messages | ChatActivity, NewChatActivity, ChatDetailActivity |
| Profile Firestore/social links | ProfileActivity, EditProfileActivity |
| Admin Places | AdminPlacesActivity |
| Admin Opening Hours | AdminOpeningHoursActivity |
| Admin Images imageUrl | AdminImagesActivity |
| GPS/Map direction ở mức module Map | MapActivity, PlaceDetailActivity |

### 🎭 UI Demo / Mock

| Chức năng | Module / Ghi chú |
|---|---|
| Flash Deals / Hot / Near You card | MainActivity (nếu còn hardcode) |
| Countdown timer | MainActivity (nếu còn fixed) |
| Một số item chat hardcode XML | ChatActivity (nếu còn) |
| Admin Reviews | AdminReviewsActivity (UI tĩnh) |
| Map marker | MapActivity (vẫn dùng DemoPlace/list tĩnh thay vì Firestore) |
| Notification/Voucher/Support | ProfileActivity (chỉ AlertDialog demo) |

### 🚀 Hướng phát triển

- **Đồng bộ marker Map với Firestore `places`**
- **Admin Reviews thật**
- **Upload ảnh thật lên Firebase Storage**
- **Push notification cho chat**
- **Realtime listener nâng cao**
- **Xóa bỏ hoàn toàn hardcode ở Home/Chat**
- **Phân quyền admin thật nếu chưa có**

---

## 5. Chia 5 người học vấn đáp

### 👤 Người 1 – UI Home & Search
- **Module phụ trách:** UI Trang chủ và Tìm kiếm.
- **File cần học:** `MainActivity.java`, `SearchResultsActivity.java`, `PlaceAdapter.java`
- **Câu hỏi vấn đáp:** Home load dữ liệu bằng cách nào? Tại sao load trong onResume thay vì onCreate?
- **Trả lời ngắn:** Load `places` từ Firestore trong `onResume` để luôn cập nhật khi quay lại từ PlaceDetail hoặc Admin. Home có section RecyclerView thật, nhưng vài card Flash Deals vẫn là Demo hardcode. Lọc `isActive != false`. Search load data 1 lần rồi lọc local.

### 👤 Người 2 – UI Favorite, Chat, Profile
- **Module phụ trách:** Yêu thích, Lịch sử, UI Chat, UI Hồ sơ người dùng.
- **File cần học:** `FavoriteActivity.java`, `ChatActivity.java`, `ProfileActivity.java`
- **Câu hỏi vấn đáp:** Lịch sử xem lưu ở đâu? Filter chip "Chưa đọc" hoạt động ra sao?
- **Trả lời ngắn:** Lịch sử lưu ID trong SharedPreferences, sau đó kết hợp Firestore để lấy tên quán. Filter chip "Chưa đọc" ẩn các hội thoại có `isRead == true` (và ẩn cả item hardcode nếu có). Stats Profile đếm size `favorites` array và truy vấn conversations theo `createdBy`.

### 👤 Người 3 – Lý Ngọc Cương – Backend/Database chính với Firebase
- **Module phụ trách:** Firebase Auth, Firestore users, Firestore places, favorites, profile data, admin data (places, openTime, imageUrl), DatabaseSeeder, FirebaseConstants, FirebaseManager / AuthHelper / FirestoreHelper.
- **File cần học:** `FirebaseManager.java`, `AuthRepository.java`, `AuthHelper.java`, `FirestoreHelper.java`, `FirebaseConstants.java`, `User.java`, `Place.java`, `FavoriteRepository.java`, `DatabaseSeeder.java`, `AdminPlacesActivity.java`, `AdminOpeningHoursActivity.java`, `AdminImagesActivity.java`, `ProfileActivity.java`, `EditProfileActivity.java`
- **Câu hỏi vấn đáp:**
  1. *FirebaseAuth khác Firestore users thế nào?* -> Auth quản lý đăng nhập/mật khẩu, Firestore lưu data mở rộng (tên, sđt, favorite).
  2. *Khi đăng ký tài khoản thì app tạo dữ liệu gì?* -> Tạo Auth user, đồng thời tạo document trong `users` với ID = uid và mảng `favorites` rỗng.
  3. *Collection `places` lưu những field nào?* -> name, address, category, openTime, imageUrl, lat, lng, isActive.
  4. *`isActive` dùng để làm gì?* -> Ẩn/hiện nơi chốn trên UI người dùng (Home, Search, Favorite).
  5. *Vì sao tạm khóa bằng `isActive` thay vì xóa document?* -> Tránh lỗi crash nếu user khác đã lưu ID quán vào Lịch sử hoặc Yêu thích, giữ nguyên dữ liệu tham chiếu.
  6. *Favorite lưu ở đâu?* -> Trong mảng string `favorites` của bảng `users`.
  7. *Admin cập nhật openTime/imageUrl thế nào?* -> Dùng update field, cụ thể AdminImages dùng `SetOptions.merge()` hoặc logic tương tự.
  8. *DatabaseSeeder làm gì?* -> Push 10 place tĩnh vào Firestore nếu bảng rỗng.
  9. *Admin Images có upload ảnh thật chưa?* -> Chỉ lưu string key/url, chưa upload Firebase Storage thực tế.
  10. *Phần nào còn demo?* -> AdminReviews, Map markers, Flash Deals...

### 👤 Người 4 – Backend Chat & Favorites
- **Module phụ trách:** ChatRepository, Conversation model, ChatMessage model, messages subcollection, cờ isRead.
- **File cần học:** `ChatRepository.java`, `Conversation.java`, `ChatMessage.java`, `NewChatActivity.java`, `ChatDetailActivity.java`
- **Câu hỏi vấn đáp:** Schema Firestore cho Chat là gì? Gửi tin nhắn mới cập nhật những gì?
- **Trả lời ngắn:** `conversations/{id}` lưu thông tin tổng, và `conversations/{id}/messages` lưu từng tin. Khi gửi, add doc vào subcollection, đồng thời cập nhật `lastMessage` và `lastMessageAt` ở doc ngoài. (Favorite dùng arrayUnion/arrayRemove).

### 👤 Người 5 – Map & GPS
- **Module phụ trách:** MapActivity, tính năng định vị, bản đồ.
- **File cần học:** `MapActivity.java`, `activity_map.xml`
- **Câu hỏi vấn đáp:** Bản đồ dùng SDK nào? Dữ liệu quán trên bản đồ lấy từ đâu?
- **Trả lời ngắn:** Dùng `osmdroid` (không phải Google Maps SDK). Lấy GPS thật bằng `FusedLocationProviderClient`, nếu lỗi sẽ fallback về vị trí cứng. Marker hiện tại lấy từ dữ liệu list tĩnh (`DemoPlace`) chưa đồng bộ `places` từ Firestore. Có click marker xem thông tin, zoom và chỉ đường qua Intent ra Google Maps.
