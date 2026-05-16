package com.example.nightlife_finder.activities;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.nightlife_finder.R;
import com.example.nightlife_finder.utils.AppSettings;
import com.example.nightlife_finder.utils.LocaleHelper;
import com.google.android.material.card.MaterialCardView;

import java.io.File;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppSettings.isDarkMode(this)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        applyGlobalUi();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        applyGlobalUi();
    }

    protected void applyGlobalUi() {
        applySystemBars();
        applyThemeToView(getWindow().getDecorView());
        applyBottomNav();
        applyAvatarIfExists();
    }

    private void applySystemBars() {
        getWindow().setStatusBarColor(getColorCompat(R.color.app_bg));
        getWindow().setNavigationBarColor(getColorCompat(R.color.bottom_nav_bg));
    }

    private void applyThemeToView(View view) {
        if (view == null) return;

        if (view instanceof MaterialCardView) {
            MaterialCardView cardView = (MaterialCardView) view;
            cardView.setCardBackgroundColor(getColorCompat(R.color.surface_bg));
        } else {
            Drawable background = view.getBackground();

            if (background instanceof ColorDrawable) {
                int current = ((ColorDrawable) background).getColor();

                if (isAppBackgroundColor(current)) {
                    view.setBackgroundColor(getColorCompat(R.color.app_bg));
                } else if (isSurfaceColor(current)) {
                    view.setBackgroundColor(getColorCompat(R.color.surface_bg));
                }
            }
        }

        if (view instanceof TextView) {
            applyTextStyle((TextView) view);
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int i = 0; i < group.getChildCount(); i++) {
                applyThemeToView(group.getChildAt(i));
            }
        }
    }

    private void applyTextStyle(TextView textView) {
        CharSequence oldText = textView.getText();
        CharSequence oldHint = textView.getHint();

        if (oldText != null) {
            textView.setText(translateIfNeeded(oldText.toString()));
        }

        if (oldHint != null) {
            textView.setHint(translateIfNeeded(oldHint.toString()));
        }

        int currentColor = textView.getCurrentTextColor();

        if (isWhiteOrBlack(currentColor)) {
            textView.setTextColor(getColorCompat(R.color.text_primary));
        } else if (isGray(currentColor)) {
            textView.setTextColor(getColorCompat(R.color.text_secondary));
        } else if (isPurple(currentColor)) {
            textView.setTextColor(getColorCompat(R.color.primary_color));
        }
    }

    private void applyBottomNav() {
        setupNavItem(R.id.navHome, R.drawable.ic_nav_home, R.string.nav_home, isCurrentPage("MainActivity"));
        setupNavItem(R.id.navMap, R.drawable.ic_nav_map, R.string.nav_map, isCurrentPage("MapActivity"));
        setupNavItem(
                R.id.navChat,
                R.drawable.ic_nav_chat,
                R.string.nav_chat,
                isCurrentPage("ChatActivity")
                        || isCurrentPage("ChatDetailActivity")
                        || isCurrentPage("NewChatActivity")
        );
        setupNavItem(R.id.navFavorite, R.drawable.ic_nav_favorite, R.string.nav_favorite, isCurrentPage("FavoriteActivity"));
        setupNavItem(R.id.navProfile, R.drawable.ic_nav_profile, R.string.nav_profile, isCurrentPage("ProfileActivity"));
    }

    private void setupNavItem(int navId, int iconRes, int textRes, boolean active) {
        View navItem = findViewById(navId);

        if (navItem == null) return;

        int color = active
                ? getColorCompat(R.color.primary_color)
                : getColorCompat(R.color.nav_inactive);

        applyNavStyleRecursive(navItem, iconRes, textRes, color, active);
    }

    private void applyNavStyleRecursive(View view, int iconRes, int textRes, int color, boolean active) {
        if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            imageView.setImageResource(iconRes);
            imageView.setColorFilter(color);
        }

        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setText(getString(textRes));
            textView.setTextColor(color);
            textView.setTextSize(10);

            if (active) {
                textView.setTypeface(null, android.graphics.Typeface.BOLD);
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int i = 0; i < group.getChildCount(); i++) {
                applyNavStyleRecursive(group.getChildAt(i), iconRes, textRes, color, active);
            }
        }
    }

    private boolean isCurrentPage(String activityName) {
        return getClass().getSimpleName().equals(activityName);
    }

    private void applyAvatarIfExists() {
        loadAvatarToImageView(R.id.imgHomeAvatar);
        loadAvatarToImageView(R.id.imgProfileAvatar);
    }

    private void loadAvatarToImageView(int imageViewId) {
        ImageView imageView = findViewById(imageViewId);

        if (imageView == null) return;

        String avatarPath = AppSettings.getAvatarPath(this);

        if (avatarPath != null && !avatarPath.isEmpty()) {
            File file = new File(avatarPath);

            if (file.exists()) {
                imageView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                return;
            }
        }

        imageView.setImageResource(R.drawable.logo);
    }

    private String translateIfNeeded(String text) {
        if (!AppSettings.getLanguage(this).equals("en")) {
            return text;
        }

        String clean = text.trim();

        switch (clean) {
            case "Trang chủ":
                return "Home";
            case "Bản đồ":
                return "Map";
            case "Trò chuyện":
                return "Chat";
            case "Yêu thích":
                return "Favorites";
            case "Hồ sơ":
                return "Profile";

            case "Tìm món ăn đêm...":
                return "Search late-night food...";
            case "Tìm quán ăn đêm ở Hà Nội...":
                return "Search late-night places in Hanoi...";
            case "Tìm cuộc trò chuyện...":
                return "Search conversations...";
            case "Tìm tên quán để nhắn...":
                return "Search places to message...";

            case "🔥 Ưu đãi chớp nhoáng":
            case "Ưu đãi chớp nhoáng":
                return "Flash Deals";
            case "🔥 Nổi bật tuần này":
            case "Nổi bật tuần này":
                return "Hot This Week";
            case "Gần bạn":
                return "Near You";
            case "Xem tất cả":
            case "Xem cả":
                return "View all";

            case "Tin nhắn":
                return "Messages";
            case "Tin nhắn mới":
                return "New message";
            case "Tất cả":
                return "All";
            case "Chưa đọc":
                return "Unread";
            case "Hủy":
                return "Cancel";
            case "Nhắn":
                return "Message";

            case "Địa điểm yêu thích":
                return "Favorite Places";
            case "12 địa điểm đã lưu":
                return "12 saved places";
            case "ĐÃ LƯU GẦN ĐÂY":
                return "RECENTLY SAVED";
            case "Lịch sử":
                return "History";
            case "QUÁN ĐÃ TỪNG ĐI":
                return "VISITED PLACES";
            case "Chỉ đường":
            case "⌖  Chỉ đường":
                return "⌖  Directions";
            case "Hộp thoại":
            case "▱  Hộp thoại":
                return "▱  Chat";

            case "Nguyễn Minh Tuấn":
                return "Nguyen Minh Tuan";
            case "Hà Nội, Việt Nam":
                return "Hanoi, Vietnam";
            case "48 Điểm":
                return "48 Points";
            case "12 Yêu thích":
                return "12 Favorites";
            case "7 Đánh giá":
                return "7 Reviews";
            case "Chỉnh sửa hồ sơ":
                return "Edit profile";
            case "Thông báo":
                return "Notifications";
            case "Voucher":
                return "Voucher";
            case "Hỗ trợ":
                return "Support";
            case "TÀI KHOẢN":
                return "ACCOUNT";
            case "Địa chỉ của tôi":
                return "My address";
            case "Đổi mật khẩu":
                return "Change password";
            case "Liên kết mạng xã hội":
                return "Social links";
            case "CÀI ĐẶT":
                return "SETTINGS";
            case "Chế độ tối":
                return "Dark mode";
            case "Ngôn ngữ":
                return "Language";
            case "Tiếng Việt":
                return "Vietnamese";
            case "Tiếng Anh":
                return "English";
            case "KHÁC":
                return "OTHER";
            case "Trung tâm hỗ trợ":
                return "Help center";
            case "Đánh giá ứng dụng":
                return "Rate app";
            case "Đăng xuất":
                return "Log out";

            default:
                return text;
        }
    }

    private boolean isAppBackgroundColor(int color) {
        return sameColor(color, "#0F0F12")
                || sameColor(color, "#101018")
                || sameColor(color, "#F6F6FB");
    }

    private boolean isSurfaceColor(int color) {
        return sameColor(color, "#1A1A1F")
                || sameColor(color, "#252532")
                || sameColor(color, "#FFFFFF")
                || sameColor(color, "#EFEFF6");
    }

    private boolean isWhiteOrBlack(int color) {
        return sameColor(color, "#FFFFFF")
                || sameColor(color, "#000000")
                || sameColor(color, "#111218");
    }

    private boolean isGray(int color) {
        return sameColor(color, "#8A8A95")
                || sameColor(color, "#9CA0AA")
                || sameColor(color, "#A0A0A8")
                || sameColor(color, "#CDD0DA")
                || sameColor(color, "#6E7280");
    }

    private boolean isPurple(int color) {
        return sameColor(color, "#A020F0")
                || sameColor(color, "#8A2BE2");
    }

    private boolean sameColor(int color, String hex) {
        return color == Color.parseColor(hex);
    }

    protected int getColorCompat(int colorRes) {
        return getResources().getColor(colorRes, getTheme());
    }
}