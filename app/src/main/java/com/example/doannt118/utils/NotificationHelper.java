package com.example.doannt118.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.doannt118.R;
import com.example.doannt118.model.ThongBao;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String CHANNEL_ID = "medical_notification_channel";
    private Context context;
    private FirestoreRepository repository;

    public NotificationHelper(Context context) {
        this.context = context;
        this.repository = new FirestoreRepository();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Thông báo y tế",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Kênh thông báo cho ứng dụng y tế");
            channel.enableVibration(true);

            NotificationManager notificationManager = 
                context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Gửi thông báo nhắc uống thuốc
     */
    public void guiThongBaoNhacUongThuoc(String maBenhNhan, String caUong, String maLichUong) {
        String tieuDe = "Nhắc nhở uống thuốc";
        String noiDung = "Đã đến giờ uống thuốc ca " + caUong.toLowerCase() + ". Vui lòng xác nhận!";
        
        // Lưu thông báo vào Firestore
        luuThongBao(maBenhNhan, null, tieuDe, noiDung, "NHAC_THUOC");
        
        // Gửi push notification qua FCM
        Map<String, String> data = new HashMap<>();
        data.put("type", "NHAC_THUOC");
        data.put("maBenhNhan", maBenhNhan);
        data.put("maLichUong", maLichUong);
        data.put("caUong", caUong);
        
        guiPushNotification(maBenhNhan, tieuDe, noiDung, data);
    }

    /**
     * Gửi thông báo lịch hẹn
     */
    public void guiThongBaoLichHen(String maBenhNhan, String maBacSi, String noiDung) {
        String tieuDe = "Nhắc nhở lịch hẹn";
        
        // Lưu thông báo vào Firestore
        luuThongBao(maBenhNhan, maBacSi, tieuDe, noiDung, "LICH_HEN");
        
        // Gửi push notification
        Map<String, String> data = new HashMap<>();
        data.put("type", "LICH_HEN");
        data.put("maBenhNhan", maBenhNhan);
        data.put("maBacSi", maBacSi);
        
        guiPushNotification(maBenhNhan, tieuDe, noiDung, data);
    }

    /**
     * Gửi thông báo chung từ bác sĩ
     */
    public void guiThongBaoTuBacSi(String maBenhNhan, String maBacSi, 
                                   String tieuDe, String noiDung) {
        // Lưu thông báo vào Firestore
        luuThongBao(maBenhNhan, maBacSi, tieuDe, noiDung, "THONG_BAO_CHUNG");
        
        // Gửi push notification
        Map<String, String> data = new HashMap<>();
        data.put("type", "THONG_BAO_CHUNG");
        data.put("maBenhNhan", maBenhNhan);
        data.put("maBacSi", maBacSi);
        
        guiPushNotification(maBenhNhan, tieuDe, noiDung, data);
    }

    /**
     * Lưu thông báo vào Firestore
     */
    private void luuThongBao(String maBenhNhan, String maBacSi, 
                            String tieuDe, String noiDung, String loaiThongBao) {
        String maThongBao = "TB_" + UUID.randomUUID().toString();
        ThongBao thongBao = new ThongBao(
            maThongBao,
            maBenhNhan,
            maBacSi,
            tieuDe,
            noiDung,
            loaiThongBao,
            Timestamp.now(),
            false
        );

        repository.addDocument("ThongBao", maThongBao, thongBao,
            aVoid -> Log.d(TAG, "Đã lưu thông báo: " + maThongBao),
            e -> Log.e(TAG, "Lỗi lưu thông báo: " + e.getMessage())
        );
    }

    /**
     * Gửi push notification qua FCM
     */
    private void guiPushNotification(String maBenhNhan, String tieuDe, 
                                    String noiDung, Map<String, String> data) {
        // TODO: Implement gửi notification qua FCM Server
        // Cần có FCM Server Key và gửi request đến FCM API
        Log.d(TAG, "Gửi push notification đến: " + maBenhNhan);
        Log.d(TAG, "Tiêu đề: " + tieuDe);
        Log.d(TAG, "Nội dung: " + noiDung);
    }

    /**
     * Lấy FCM token của thiết bị
     */
    public static void getFCMToken(OnTokenReceivedListener listener) {
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String token = task.getResult();
                    Log.d(TAG, "FCM Token: " + token);
                    listener.onTokenReceived(token);
                } else {
                    Log.e(TAG, "Lỗi lấy FCM token", task.getException());
                    listener.onError(task.getException());
                }
            });
    }

    /**
     * Lưu FCM token vào Firestore
     */
    public void luuFCMToken(String maBenhNhan, String token) {
        Map<String, Object> deviceToken = new HashMap<>();
        deviceToken.put("maBenhNhan", maBenhNhan);
        deviceToken.put("token", token);
        deviceToken.put("thoiGianCapNhat", Timestamp.now());

        repository.addDocument("DeviceTokens", maBenhNhan, deviceToken,
            aVoid -> Log.d(TAG, "Đã lưu FCM token"),
            e -> Log.e(TAG, "Lỗi lưu FCM token: " + e.getMessage())
        );
    }

    public interface OnTokenReceivedListener {
        void onTokenReceived(String token);
        void onError(Exception e);
    }
    
    /**
     * Gửi thông báo cho bác sĩ (static method)
     */
    public static void guiThongBaoChoBacSi(Context context, String maBacSi, 
                                          String tieuDe, String noiDung, 
                                          String loaiThongBao, String maBenhNhan) {
        FirestoreRepository repository = new FirestoreRepository();
        String maThongBao = "TB_" + UUID.randomUUID().toString();
        
        ThongBao thongBao = new ThongBao(
            maThongBao,
            null, // Không có maBenhNhan vì gửi cho bác sĩ
            maBacSi,
            tieuDe,
            noiDung,
            loaiThongBao,
            Timestamp.now(),
            false
        );
        
        repository.addDocument("ThongBao", maThongBao, thongBao,
            aVoid -> Log.d(TAG, "Đã gửi thông báo cho bác sĩ: " + maBacSi),
            e -> Log.e(TAG, "Lỗi gửi thông báo: " + e.getMessage())
        );
    }
    
    /**
     * Gửi thông báo cho bệnh nhân (static method)
     */
    public static void guiThongBaoChoBenhNhan(Context context, String maBenhNhan, 
                                             String tieuDe, String noiDung, 
                                             String loaiThongBao, String maBacSi) {
        FirestoreRepository repository = new FirestoreRepository();
        String maThongBao = "TB_" + UUID.randomUUID().toString();
        
        ThongBao thongBao = new ThongBao(
            maThongBao,
            maBenhNhan,
            maBacSi,
            tieuDe,
            noiDung,
            loaiThongBao,
            Timestamp.now(),
            false
        );
        
        repository.addDocument("ThongBao", maThongBao, thongBao,
            aVoid -> Log.d(TAG, "Đã gửi thông báo cho bệnh nhân: " + maBenhNhan),
            e -> Log.e(TAG, "Lỗi gửi thông báo: " + e.getMessage())
        );
    }
}
