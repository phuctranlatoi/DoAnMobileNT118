package com.example.doannt118.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.doannt118.model.ChiTietDonThuoc;
import com.example.doannt118.receiver.MedicineReminderReceiver;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MedicineReminderManager {
    
    private static final String TAG = "MedicineReminderManager";
    
    // Giờ nhắc nhở cho từng ca
    // TODO: Đổi lại 7:30 sau khi test xong
    public static final int GIO_SANG = 7;
    public static final int PHUT_SANG = 30;
    
    public static final int GIO_TRUA = 11;
    public static final int PHUT_TRUA = 30;
    
    public static final int GIO_CHIEU = 17;
    public static final int PHUT_CHIEU = 0;
    
    private Context context;
    private AlarmManager alarmManager;
    private FirestoreRepository repository;
    
    public MedicineReminderManager(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.repository = new FirestoreRepository();
    }
    
    /**
     * Đặt lịch nhắc nhở uống thuốc cho bệnh nhân
     * Sẽ kiểm tra đơn thuốc đang dùng và đặt alarm cho các ca có thuốc
     */
    public void setupRemindersForPatient(String maBenhNhan) {
        Log.d(TAG, "Setting up reminders for patient: " + maBenhNhan);
        
        // Load đơn thuốc đang dùng của bệnh nhân
        repository.getByField("DonThuoc", "maBenhNhan", maBenhNhan,
            querySnapshot -> {
                Set<String> caCoThuoc = new HashSet<>();
                int donThuocCount = 0;
                
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String trangThai = doc.getString("trangThai");
                    if (trangThai == null || "DANG_DUNG".equals(trangThai)) {
                        donThuocCount++;
                        String maDonThuoc = doc.getId();
                        // Load chi tiết để biết ca nào có thuốc
                        loadChiTietAndSetAlarm(maBenhNhan, maDonThuoc, caCoThuoc);
                    }
                }
                
                if (donThuocCount == 0) {
                    Log.d(TAG, "No active prescriptions, canceling all reminders");
                    cancelAllReminders(maBenhNhan);
                }
            },
            e -> Log.e(TAG, "Error loading prescriptions: " + e.getMessage())
        );
    }
    
    private void loadChiTietAndSetAlarm(String maBenhNhan, String maDonThuoc, Set<String> caCoThuoc) {
        repository.getByField("ChiTietDonThuoc", "maDonThuoc", maDonThuoc,
            querySnapshot -> {
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Boolean uongSang = doc.getBoolean("uongSang");
                    Boolean uongTrua = doc.getBoolean("uongTrua");
                    Boolean uongChieu = doc.getBoolean("uongChieu");
                    Boolean uongToi = doc.getBoolean("uongToi");
                    
                    if (uongSang != null && uongSang) caCoThuoc.add("SANG");
                    if (uongTrua != null && uongTrua) caCoThuoc.add("TRUA");
                    if (uongChieu != null && uongChieu || uongToi != null && uongToi) caCoThuoc.add("CHIEU");
                }
                
                // Đặt alarm cho các ca có thuốc
                if (caCoThuoc.contains("SANG")) {
                    setDailyAlarm(maBenhNhan, "SANG", "Ca Sáng", GIO_SANG, PHUT_SANG);
                }
                if (caCoThuoc.contains("TRUA")) {
                    setDailyAlarm(maBenhNhan, "TRUA", "Ca Trưa", GIO_TRUA, PHUT_TRUA);
                }
                if (caCoThuoc.contains("CHIEU")) {
                    setDailyAlarm(maBenhNhan, "CHIEU", "Ca Chiều", GIO_CHIEU, PHUT_CHIEU);
                }
                
                Log.d(TAG, "Set alarms for: " + caCoThuoc);
            },
            e -> Log.e(TAG, "Error loading prescription details: " + e.getMessage())
        );
    }

    /**
     * Đặt alarm hàng ngày cho một ca uống thuốc
     */
    public void setDailyAlarm(String maBenhNhan, String caUong, String tenCa, int hour, int minute) {
        Intent intent = new Intent(context, MedicineReminderReceiver.class);
        intent.putExtra(MedicineReminderReceiver.EXTRA_MA_BENH_NHAN, maBenhNhan);
        intent.putExtra(MedicineReminderReceiver.EXTRA_CA_UONG, caUong);
        intent.putExtra(MedicineReminderReceiver.EXTRA_TEN_CA, tenCa);
        
        int requestCode = getRequestCode(maBenhNhan, caUong);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, 
            requestCode, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Tính thời gian alarm tiếp theo
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        // Nếu thời gian đã qua trong ngày hôm nay, đặt cho ngày mai
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        // Đặt alarm lặp lại hàng ngày
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
            );
        } else {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            );
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Log.d(TAG, "Alarm set for " + tenCa + " at " + sdf.format(calendar.getTime()));
    }
    
    /**
     * Hủy alarm cho một ca
     */
    public void cancelAlarm(String maBenhNhan, String caUong) {
        Intent intent = new Intent(context, MedicineReminderReceiver.class);
        int requestCode = getRequestCode(maBenhNhan, caUong);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, 
            requestCode, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        alarmManager.cancel(pendingIntent);
        Log.d(TAG, "Alarm canceled for " + caUong);
    }
    
    /**
     * Hủy tất cả alarm nhắc nhở
     */
    public void cancelAllReminders(String maBenhNhan) {
        cancelAlarm(maBenhNhan, "SANG");
        cancelAlarm(maBenhNhan, "TRUA");
        cancelAlarm(maBenhNhan, "CHIEU");
        Log.d(TAG, "All reminders canceled");
    }
    
    /**
     * Đặt lại alarm sau khi thiết bị khởi động lại
     * Gọi từ BootReceiver
     */
    public static void rescheduleReminders(Context context, String maBenhNhan) {
        MedicineReminderManager manager = new MedicineReminderManager(context);
        manager.setupRemindersForPatient(maBenhNhan);
    }
    
    /**
     * Tạo request code unique cho mỗi alarm
     */
    private int getRequestCode(String maBenhNhan, String caUong) {
        int baseCode = maBenhNhan.hashCode() & 0xFFFF; // 16 bit từ maBenhNhan
        switch (caUong) {
            case "SANG": return baseCode + 1;
            case "TRUA": return baseCode + 2;
            case "CHIEU": return baseCode + 3;
            default: return baseCode;
        }
    }
    
    /**
     * Kiểm tra xem ca đã được xác nhận uống chưa trong ngày hôm nay
     */
    public void checkAndShowReminderIfNeeded(String maBenhNhan, String caUong, String tenCa, 
                                             ReminderCheckCallback callback) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String ngayHomNay = dateFormat.format(new Date());
        String keyXacNhan = "CA_" + caUong + "_" + maBenhNhan + "_" + ngayHomNay;
        
        repository.getCollection("XacNhanUongThuoc").document(keyXacNhan).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Boolean daUong = doc.getBoolean("daUong");
                    if (daUong != null && daUong) {
                        callback.onResult(true); // Đã uống
                        return;
                    }
                }
                callback.onResult(false); // Chưa uống
            })
            .addOnFailureListener(e -> callback.onResult(false));
    }
    
    public interface ReminderCheckCallback {
        void onResult(boolean daUong);
    }
}
