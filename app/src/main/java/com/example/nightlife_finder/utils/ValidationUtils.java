package com.example.nightlife_finder.utils;

import android.util.Patterns;

/**
 * ValidationUtils - Xác thực dữ liệu đầu vào: email, password, nhập liệu trống...
 * Thuộc phần việc của Đức (Support Backend).
 *
 * LƯU Ý: Class này CHỈ chứa logic validation thuần túy.
 * KHÔNG import hay phụ thuộc vào các file Model, Firebase, Repository.
 */
public class ValidationUtils {

    // -------------------------------------------------------
    // Kiểm tra chuỗi rỗng / null
    // -------------------------------------------------------
    public static boolean isEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }

    // -------------------------------------------------------
    // Kiểm tra định dạng email hợp lệ
    // -------------------------------------------------------
    public static boolean isValidEmail(String email) {
        return !isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // -------------------------------------------------------
    // Kiểm tra mật khẩu đủ mạnh (tối thiểu 6 ký tự)
    // -------------------------------------------------------
    public static boolean isValidPassword(String password) {
        return !isEmpty(password) && password.length() >= 6;
    }

    // -------------------------------------------------------
    // Kiểm tra hai mật khẩu có khớp nhau không
    // -------------------------------------------------------
    public static boolean isPasswordMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }

    // -------------------------------------------------------
    // Kiểm tra số điện thoại Việt Nam cơ bản (10 chữ số, bắt đầu 0)
    // -------------------------------------------------------
    public static boolean isValidPhoneNumber(String phone) {
        return !isEmpty(phone) && phone.matches("^0[0-9]{9}$");
    }

    // -------------------------------------------------------
    // Lấy thông báo lỗi email
    // -------------------------------------------------------
    public static String getEmailError(String email) {
        if (isEmpty(email)) return "Email không được để trống.";
        if (!isValidEmail(email)) return "Định dạng email không hợp lệ.";
        return null;
    }

    // -------------------------------------------------------
    // Lấy thông báo lỗi password
    // -------------------------------------------------------
    public static String getPasswordError(String password) {
        if (isEmpty(password)) return "Mật khẩu không được để trống.";
        if (!isValidPassword(password)) return "Mật khẩu phải có ít nhất 6 ký tự.";
        return null;
    }

    // -------------------------------------------------------
    // Lấy thông báo lỗi confirm password
    // -------------------------------------------------------
    public static String getConfirmPasswordError(String password, String confirmPassword) {
        if (isEmpty(confirmPassword)) return "Vui lòng xác nhận mật khẩu.";
        if (!isPasswordMatch(password, confirmPassword)) return "Mật khẩu xác nhận không khớp.";
        return null;
    }

    // Private constructor — không cho phép khởi tạo
    private ValidationUtils() {
    }
}