package com.example.doannt118.utils;

import android.util.Log;
import com.example.doannt118.model.TinNhanBacSi;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.HashMap;
import java.util.Map;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static FirestoreRepository repository = new FirestoreRepository();
    
    /**
     * Gửi push notification khi có tin nhắn mới
     */
    public static void sendMessageNotification(TinNhanBacSi tinNhan) {
        if (tinNhan == null) return;
        
        // Xác định người nhận thông báo
        String nguoiNhanId;
        String userRole;
        String title;
        String body;
        
        if (tinNhan.getLoaiTinNhan() == TinNhanBacSi.LoaiTinNhan.BENH_NHAN) {
            // Bệnh nhân gửi → Thông báo cho bác sĩ
            nguoiNhanId = tinNhan.getMaBacSi();
            userRole = "BAC_SI";
            title = "Tin nhắn mới từ bệnh nhân";
            body = tinNhan.getTenNguoiGui() + ": " + tinNhan.getNoiDung();
        } else {
            // Bác sĩ gửi → Thông báo cho bệnh nhân
            nguoiNhanId = tinNhan.getMaBenhNhan();
            userRole = "BENH_NHAN";
            title = "Tin nhắn mới từ bác sĩ";
            body = tinNhan.getTenNguoiGui() + ": " + tinNhan.getNoiDung();
        }
        
        // Lấy FCM token của người nhận
        getFCMToken(nguoiNhanId, userRole, token -> {
            if (token != null && !token.isEmpty()) {
                sendPushNotification(token, title, body, tinNhan, userRole);
            } else {
                Log.d(TAG, "Không tìm thấy FCM token cho user: " + nguoiNhanId);
            }
        });
    }
    
    /**
     * Lấy FCM token từ Firestore
     */
    private static void getFCMToken(String userId, String userRole, TokenCallback callback) {
        String collection = "BAC_SI".equals(userRole) ? "BacSi" : "BenhNhan";
        String field = "BAC_SI".equals(userRole) ? "maBacSi" : "maBenhNhan";
        
        repository.getByField(collection, field, userId,
            querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    String fcmToken = doc.getString("fcmToken");
                    callback.onTokenReceived(fcmToken);
                } else {
                    callback.onTokenReceived(null);
                }
            },
            e -> {
                Log.e(TAG, "Lỗi lấy FCM token: " + e.getMessage());
                callback.onTokenReceived(null);
            }
        );
    }
    
    /**
     * Gửi push notification qua FCM
     */
    private static void sendPushNotification(String fcmToken, String title, String body, 
                                           TinNhanBacSi tinNhan, String userRole) {
        // Tạo data payload
        Map<String, String> data = new HashMap<>();
        data.put("type", "TIN_NHAN_BAC_SI");
        data.put("title", title);
        data.put("body", body);
        data.put("userRole", userRole);
        data.put("maBacSi", tinNhan.getMaBacSi());
        data.put("maBenhNhan", tinNhan.getMaBenhNhan());
        data.put("tenNguoiGui", tinNhan.getTenNguoiGui());
        data.put("noiDung", tinNhan.getNoiDung());
        
        // Tạo notification payload
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("body", body);
        notification.put("sound", "default");
        notification.put("badge", "1");
        
        // Tạo message payload
        Map<String, Object> message = new HashMap<>();
        message.put("to", fcmToken);
        message.put("notification", notification);
        message.put("data", data);
        message.put("priority", "high");
        
        // Gửi qua FCM API (cần implement HTTP request)
        // Hoặc sử dụng Firebase Functions để gửi
        Log.d(TAG, "Gửi push notification: " + title + " - " + body);
        
        // TODO: Implement actual FCM API call
        // Có thể sử dụng Firebase Functions hoặc server backend
    }
    
    /**
     * Interface callback cho FCM token
     */
    /**
     * Gửi thông báo cho bác sĩ (method cũ để tương thích)
     */
    public static void guiThongBaoChoBacSi(android.content.Context context, String maBacSi, 
                                          String tieuDe, String noiDung, String loaiThongBao, String maLienKet) {
        android.util.Log.d(TAG, "Gửi thông báo cho bác sĩ: " + maBacSi + " - " + tieuDe);
        // TODO: Implement notification logic
    }
    
    /**
     * Gửi thông báo cho bệnh nhân (method cũ để tương thích)
     */
    public static void guiThongBaoChoBenhNhan(android.content.Context context, String maBenhNhan,
                                             String tieuDe, String noiDung, String loaiThongBao, String maLienKet) {
        android.util.Log.d(TAG, "Gửi thông báo cho bệnh nhân: " + maBenhNhan + " - " + tieuDe);
        // TODO: Implement notification logic
    }
    
    /**
     * Constructor và method instance (để tương thích với code cũ)
     */
    public NotificationHelper(android.content.Context context) {
        // Constructor for backward compatibility
    }
    
    /**
     * Gửi thông báo từ bác sĩ (method instance)
     */
    public void guiThongBaoTuBacSi(String maBenhNhan, String maBacSi, String tieuDe, String noiDung) {
        android.util.Log.d(TAG, "Gửi thông báo từ bác sĩ đến bệnh nhân: " + maBenhNhan + " - " + tieuDe);
        // TODO: Implement notification logic
    }
    
    /**
     * Interface callback cho FCM token
     */
    public interface TokenCallback {
        void onTokenReceived(String token);
    }
}