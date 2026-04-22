package com.example.doannt118.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.doannt118.R;
import com.example.doannt118.ui.ThongBaoActivity;
import com.example.doannt118.ui.XacNhanUongThuocActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "medical_notification_channel";
    private static final String CHANNEL_NAME = "Thông báo y tế";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Kiểm tra notification payload
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Notification Title: " + title);
            Log.d(TAG, "Notification Body: " + body);
            
            sendNotification(title, body, remoteMessage.getData());
        }

        // Kiểm tra data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataPayload(remoteMessage.getData());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        
        // Gửi token lên server hoặc lưu vào Firestore
        sendRegistrationToServer(token);
    }

    private void handleDataPayload(Map<String, String> data) {
        String type = data.get("type");
        String title = data.get("title");
        String body = data.get("body");
        
        if (type != null) {
            switch (type) {
                case "NHAC_THUOC":
                    sendMedicationNotification(title, body, data);
                    break;
                case "LICH_HEN":
                    sendAppointmentNotification(title, body, data);
                    break;
                case "THONG_BAO_CHUNG":
                    sendGeneralNotification(title, body, data);
                    break;
                case "TIN_NHAN_BAC_SI":
                    sendMessageNotification(title, body, data);
                    break;
                default:
                    sendNotification(title, body, data);
                    break;
            }
        }
    }

    private void sendMedicationNotification(String title, String body, Map<String, String> data) {
        Intent intent = new Intent(this, XacNhanUongThuocActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("maLichUong", data.get("maLichUong"));
        intent.putExtra("maBenhNhan", data.get("maBenhNhan"));
        
        showNotification(title, body, intent, 1);
    }

    private void sendAppointmentNotification(String title, String body, Map<String, String> data) {
        Intent intent = new Intent(this, ThongBaoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("MA_BENH_NHAN", data.get("maBenhNhan"));
        
        showNotification(title, body, intent, 2);
    }

    private void sendGeneralNotification(String title, String body, Map<String, String> data) {
        Intent intent = new Intent(this, ThongBaoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("MA_BENH_NHAN", data.get("maBenhNhan"));
        
        showNotification(title, body, intent, 3);
    }
    
    private void sendMessageNotification(String title, String body, Map<String, String> data) {
        String userRole = data.get("userRole"); // "BENH_NHAN" hoặc "BAC_SI"
        String maBacSi = data.get("maBacSi");
        String maBenhNhan = data.get("maBenhNhan");
        String tenNguoiGui = data.get("tenNguoiGui");
        
        Intent intent;
        if ("BAC_SI".equals(userRole)) {
            // Thông báo cho bác sĩ - mở danh sách tin nhắn bác sĩ
            intent = new Intent(this, com.example.doannt118.ui.DanhSachTinNhanBacSiActivity.class);
            intent.putExtra("MA_BAC_SI", maBacSi);
            intent.putExtra("TEN_BAC_SI", tenNguoiGui);
        } else {
            // Thông báo cho bệnh nhân - mở chat trực tiếp
            intent = new Intent(this, com.example.doannt118.ui.NhanTinBacSiActivity.class);
            intent.putExtra("MA_BAC_SI", maBacSi);
            intent.putExtra("MA_BENH_NHAN", maBenhNhan);
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        showNotification(title, body, intent, 4);
    }

    private void sendNotification(String title, String body, Map<String, String> data) {
        Intent intent = new Intent(this, ThongBaoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        showNotification(title, body, intent, 0);
    }

    private void showNotification(String title, String body, Intent intent, int notificationId) {
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 
            notificationId,
            intent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body));

        NotificationManager notificationManager = 
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo notification channel cho Android O trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Kênh thông báo cho ứng dụng y tế");
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void sendRegistrationToServer(String token) {
        Log.d(TAG, "Saving FCM token: " + token);
        
        // Lưu token vào SharedPreferences để sử dụng sau
        android.content.SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putString("fcm_token", token).apply();
        
        // Lưu token vào Firestore nếu có thông tin user
        String maTaiKhoan = prefs.getString("MA_TAI_KHOAN", "");
        String userType = prefs.getString("USER_TYPE", "");
        
        if (!maTaiKhoan.isEmpty() && !userType.isEmpty()) {
            updateFCMTokenInFirestore(token, maTaiKhoan, userType);
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
                            Log.d(TAG, "FCM token updated successfully"))
                        .addOnFailureListener(e -> 
                            Log.e(TAG, "Error updating FCM token: " + e.getMessage()));
                } else {
                    Log.w(TAG, "User document not found for: " + maTaiKhoan);
                }
            })
            .addOnFailureListener(e -> 
                Log.e(TAG, "Error finding user document: " + e.getMessage()));
    }
}
