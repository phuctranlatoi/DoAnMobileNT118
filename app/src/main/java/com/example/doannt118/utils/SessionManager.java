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
        
        // Lưu thông tin cho FCM service
        SharedPreferences appPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        appPrefs.edit()
            .putString("MA_TAI_KHOAN", maTaiKhoan)
            .putString("USER_TYPE", vaiTro)
            .apply();
        
        // Cập nhật FCM token nếu có
        updateFCMToken(maTaiKhoan, vaiTro);
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
        
        // Xóa thông tin FCM
        SharedPreferences appPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        appPrefs.edit().clear().apply();
    }
    
    /**
     * Cập nhật FCM token khi đăng nhập
     */
    private void updateFCMToken(String maTaiKhoan, String vaiTro) {
        SharedPreferences appPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String fcmToken = appPrefs.getString("fcm_token", "");
        
        if (!fcmToken.isEmpty()) {
            updateFCMTokenInFirestore(fcmToken, maTaiKhoan, vaiTro);
        }
    }
    
    private void updateFCMTokenInFirestore(String token, String maTaiKhoan, String userType) {
        com.google.firebase.firestore.FirebaseFirestore db = 
            com.google.firebase.firestore.FirebaseFirestore.getInstance();
        
        String collection = "BENH_NHAN".equals(userType) ? "BenhNhan" : "BacSi";
        String field = "BENH_NHAN".equals(userType) ? "maBenhNhan" : "maBacSi";
        
        // Tìm document theo mã tài khoản
        db.collection(collection)
            .whereEqualTo(field, maTaiKhoan)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    String documentId = querySnapshot.getDocuments().get(0).getId();
                    
                    // Cập nhật FCM token
                    db.collection(collection)
                        .document(documentId)
                        .update("fcmToken", token)
                        .addOnSuccessListener(aVoid -> 
                            android.util.Log.d("SessionManager", "FCM token updated successfully"))
                        .addOnFailureListener(e -> 
                            android.util.Log.e("SessionManager", "Error updating FCM token: " + e.getMessage()));
                } else {
                    android.util.Log.w("SessionManager", "User document not found for: " + maTaiKhoan);
                }
            })
            .addOnFailureListener(e -> 
                android.util.Log.e("SessionManager", "Error finding user document: " + e.getMessage()));
    }
}
