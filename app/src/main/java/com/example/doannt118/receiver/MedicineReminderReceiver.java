package com.example.doannt118.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.doannt118.R;
import com.example.doannt118.model.ThongBao;
import com.example.doannt118.ui.XacNhanUongThuocActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class MedicineReminderReceiver extends BroadcastReceiver {
    
    private static final String TAG = "MedicineReminderReceiver";
    public static final String CHANNEL_ID = "medicine_reminder_channel";
    public static final String CHANNEL_NAME = "Nhắc nhở uống thuốc";
    
    public static final String EXTRA_MA_BENH_NHAN = "maBenhNhan";
    public static final String EXTRA_CA_UONG = "caUong";
    public static final String EXTRA_TEN_CA = "tenCa";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received medicine reminder alarm");
        
        String maBenhNhan = intent.getStringExtra(EXTRA_MA_BENH_NHAN);
        String caUong = intent.getStringExtra(EXTRA_CA_UONG);
        String tenCa = intent.getStringExtra(EXTRA_TEN_CA);
        
        if (maBenhNhan == null || caUong == null) {
            Log.e(TAG, "Missing required extras");
            return;
        }
        
        // Hiển thị notification hệ thống
        showNotification(context, maBenhNhan, caUong, tenCa);
        
        // Lưu thông báo vào Firestore để hiển thị trong app
        saveThongBaoToFirestore(maBenhNhan, caUong, tenCa);
        
        // Đặt lại alarm cho ngày mai (vì Android M+ không hỗ trợ setRepeating với exact alarm)
        rescheduleAlarmForTomorrow(context, maBenhNhan, caUong, tenCa);
    }
    
    /**
     * Lưu thông báo nhắc uống thuốc vào Firestore
     */
    private void saveThongBaoToFirestore(String maBenhNhan, String caUong, String tenCa) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String ngayHomNay = dateFormat.format(new Date());
        
        // Tạo ID unique cho thông báo
        String maThongBao = "NHAC_THUOC_" + caUong + "_" + maBenhNhan + "_" + 
            new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        
        String tieuDe = "Đã đến giờ uống thuốc!";
        String noiDung = "Nhấn để xác nhận uống thuốc " + tenCa.toLowerCase() + " ngày " + ngayHomNay;
        
        ThongBao thongBao = new ThongBao();
        thongBao.setMaThongBao(maThongBao);
        thongBao.setMaBenhNhan(maBenhNhan);
        thongBao.setMaBacSi(null); // Thông báo hệ thống, không từ bác sĩ
        thongBao.setTieuDe(tieuDe);
        thongBao.setNoiDung(noiDung);
        thongBao.setLoaiThongBao("NHAC_THUOC");
        thongBao.setThoiGianGui(Timestamp.now());
        thongBao.setDaDoc(false);
        
        db.collection("ThongBao").document(maThongBao)
            .set(thongBao)
            .addOnSuccessListener(aVoid -> Log.d(TAG, "Saved ThongBao to Firestore: " + maThongBao))
            .addOnFailureListener(e -> Log.e(TAG, "Error saving ThongBao: " + e.getMessage()));
    }
    
    private void rescheduleAlarmForTomorrow(Context context, String maBenhNhan, String caUong, String tenCa) {
        com.example.doannt118.utils.MedicineReminderManager manager = 
            new com.example.doannt118.utils.MedicineReminderManager(context);
        
        int hour, minute;
        switch (caUong) {
            case "SANG":
                hour = com.example.doannt118.utils.MedicineReminderManager.GIO_SANG;
                minute = com.example.doannt118.utils.MedicineReminderManager.PHUT_SANG;
                break;
            case "TRUA":
                hour = com.example.doannt118.utils.MedicineReminderManager.GIO_TRUA;
                minute = com.example.doannt118.utils.MedicineReminderManager.PHUT_TRUA;
                break;
            case "CHIEU":
                hour = com.example.doannt118.utils.MedicineReminderManager.GIO_CHIEU;
                minute = com.example.doannt118.utils.MedicineReminderManager.PHUT_CHIEU;
                break;
            default:
                return;
        }
        
        manager.setDailyAlarm(maBenhNhan, caUong, tenCa, hour, minute);
        Log.d(TAG, "Rescheduled alarm for tomorrow: " + tenCa);
    }
    
    private void showNotification(Context context, String maBenhNhan, String caUong, String tenCa) {
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        // Tạo notification channel cho Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo nhắc nhở uống thuốc theo ca");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            notificationManager.createNotificationChannel(channel);
        }
        
        // Tạo intent mở XacNhanUongThuocActivity khi bấm vào notification
        Intent openIntent = new Intent(context, XacNhanUongThuocActivity.class);
        openIntent.putExtra(EXTRA_MA_BENH_NHAN, maBenhNhan);
        openIntent.putExtra(EXTRA_CA_UONG, caUong);
        openIntent.putExtra(EXTRA_TEN_CA, tenCa);
        openIntent.putExtra("fromNotification", true);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        int requestCode = getNotificationId(caUong);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            requestCode, 
            openIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Tạo notification
        String title = "Đã đến giờ uống thuốc!";
        String content = "Nhấn để xác nhận uống thuốc " + tenCa.toLowerCase();
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(new long[]{0, 500, 200, 500})
            .setDefaults(NotificationCompat.DEFAULT_SOUND);
        
        notificationManager.notify(requestCode, builder.build());
        Log.d(TAG, "Notification shown for " + tenCa);
    }
    
    private int getNotificationId(String caUong) {
        switch (caUong) {
            case "SANG": return 1001;
            case "TRUA": return 1002;
            case "CHIEU": return 1003;
            default: return 1000;
        }
    }
}
