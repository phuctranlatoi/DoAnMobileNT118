package com.example.doannt118.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_MA_TAI_KHOAN = "maTaiKhoan";
    private static final String KEY_VAI_TRO = "vaiTro";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_HO_TEN = "hoTen";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Lưu thông tin đăng nhập
     */
    public void createLoginSession(String maTaiKhoan, String vaiTro, String email, String hoTen) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_MA_TAI_KHOAN, maTaiKhoan);
        editor.putString(KEY_VAI_TRO, vaiTro);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_HO_TEN, hoTen);
        editor.apply();
    }

    /**
     * Kiểm tra xem user đã đăng nhập chưa
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Lấy mã tài khoản
     */
    public String getMaTaiKhoan() {
        return prefs.getString(KEY_MA_TAI_KHOAN, null);
    }

    /**
     * Lấy vai trò
     */
    public String getVaiTro() {
        return prefs.getString(KEY_VAI_TRO, null);
    }

    /**
     * Lấy email
     */
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    /**
     * Lấy họ tên
     */
    public String getHoTen() {
        return prefs.getString(KEY_HO_TEN, null);
    }

    /**
     * Xóa session khi đăng xuất
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }
}
