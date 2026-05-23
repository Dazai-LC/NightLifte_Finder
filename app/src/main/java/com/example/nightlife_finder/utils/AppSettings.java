package com.example.nightlife_finder.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSettings {

    private static final String PREF_NAME = "night_life_settings";

    private static final String KEY_DARK_MODE       = "dark_mode";
    private static final String KEY_LANGUAGE        = "language";
    private static final String KEY_AVATAR_PATH     = "avatar_path";
    private static final String KEY_TWO_FACTOR      = "two_factor_enabled";

    private static final String KEY_PROFILE_NAME     = "profile_name";
    private static final String KEY_PROFILE_GENDER   = "profile_gender";
    private static final String KEY_PROFILE_BIRTHDAY = "profile_birthday";
    private static final String KEY_PROFILE_PHONE    = "profile_phone";
    private static final String KEY_PROFILE_EMAIL    = "profile_email";
    private static final String KEY_PROFILE_LOCATION = "profile_location";
    private static final String KEY_PROFILE_BIO      = "profile_bio";
    private static final String KEY_PROFILE_ADDRESS  = "profile_address";

    // -------------------------------------------------------
    // Dark mode
    // -------------------------------------------------------
    public static boolean isDarkMode(Context context) {
        return getPrefs(context).getBoolean(KEY_DARK_MODE, true);
    }

    public static void setDarkMode(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    // -------------------------------------------------------
    // Language
    // -------------------------------------------------------
    public static String getLanguage(Context context) {
        return getPrefs(context).getString(KEY_LANGUAGE, "vi");
    }

    public static void setLanguage(Context context, String languageCode) {
        getPrefs(context).edit().putString(KEY_LANGUAGE, languageCode).apply();
    }

    // -------------------------------------------------------
    // Avatar path (local file)
    // -------------------------------------------------------
    public static String getAvatarPath(Context context) {
        return getPrefs(context).getString(KEY_AVATAR_PATH, "");
    }

    public static void setAvatarPath(Context context, String path) {
        getPrefs(context).edit().putString(KEY_AVATAR_PATH, path).apply();
    }

    // -------------------------------------------------------
    // Two-factor / 2-step verification
    // -------------------------------------------------------
    public static boolean isTwoFactorEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_TWO_FACTOR, false);
    }

    public static void setTwoFactorEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_TWO_FACTOR, enabled).apply();
    }

    // -------------------------------------------------------
    // Profile fields
    // -------------------------------------------------------
    public static String getProfileName(Context context) {
        return getPrefs(context).getString(KEY_PROFILE_NAME, "");
    }

    public static void setProfileName(Context context, String value) {
        getPrefs(context).edit().putString(KEY_PROFILE_NAME, value).apply();
    }

    public static String getProfileGender(Context context) {
        return getPrefs(context).getString(KEY_PROFILE_GENDER, "Nam");
    }

    public static void setProfileGender(Context context, String value) {
        getPrefs(context).edit().putString(KEY_PROFILE_GENDER, value).apply();
    }

    public static String getProfileBirthday(Context context) {
        return getPrefs(context).getString(KEY_PROFILE_BIRTHDAY, "");
    }

    public static void setProfileBirthday(Context context, String value) {
        getPrefs(context).edit().putString(KEY_PROFILE_BIRTHDAY, value).apply();
    }

    public static String getProfilePhone(Context context) {
        return getPrefs(context).getString(KEY_PROFILE_PHONE, "");
    }

    public static void setProfilePhone(Context context, String value) {
        getPrefs(context).edit().putString(KEY_PROFILE_PHONE, value).apply();
    }

    public static String getProfileEmail(Context context) {
        return getPrefs(context).getString(KEY_PROFILE_EMAIL, "");
    }

    public static void setProfileEmail(Context context, String value) {
        getPrefs(context).edit().putString(KEY_PROFILE_EMAIL, value).apply();
    }

    public static String getProfileLocation(Context context) {
        return getPrefs(context).getString(KEY_PROFILE_LOCATION, "");
    }

    public static void setProfileLocation(Context context, String value) {
        getPrefs(context).edit().putString(KEY_PROFILE_LOCATION, value).apply();
    }

    public static String getProfileBio(Context context) {
        return getPrefs(context).getString(KEY_PROFILE_BIO, "");
    }

    public static void setProfileBio(Context context, String value) {
        getPrefs(context).edit().putString(KEY_PROFILE_BIO, value).apply();
    }

    /** Địa chỉ cụ thể của người dùng (khác với vị trí / thành phố). */
    public static String getProfileAddress(Context context) {
        return getPrefs(context).getString(KEY_PROFILE_ADDRESS, "");
    }

    public static void setProfileAddress(Context context, String value) {
        getPrefs(context).edit().putString(KEY_PROFILE_ADDRESS, value).apply();
    }

    // -------------------------------------------------------
    // Internal helper
    // -------------------------------------------------------
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}