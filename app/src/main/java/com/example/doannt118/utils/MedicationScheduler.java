package com.example.doannt118.utils;

import android.util.Log;
import com.example.doannt118.model.DonThuoc;
import com.example.doannt118.model.LichUongThuoc;
import com.example.doannt118.repository.FirestoreRepository;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class MedicationScheduler {
    private static final String TAG = "MedicationScheduler";
    private FirestoreRepository repository;

    public MedicationScheduler() {
        this.repository = new FirestoreRepository();
    }

    /**
     * Tạo lịch uống thuốc cho đơn thuốc
     */
    public void taoLichUongThuoc(DonThuoc donThuoc, OnScheduleCreatedListener listener) {
        if (donThuoc == null || donThuoc.getSoNgayUong() <= 0) {
            listener.onFailure(new Exception("Thông tin đơn thuốc không hợp lệ"));
            return;
        }

        Date ngayBatDau = donThuoc.getNgayBatDau() != null ? 
            donThuoc.getNgayBatDau() : new Date();
        
        int soNgayUong = donThuoc.getSoNgayUong();
        String maDonThuoc = donThuoc.getMaDonThuoc();
        String maBenhNhan = donThuoc.getMaBenhNhan();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ngayBatDau);

        int successCount = 0;
        int totalSchedules = soNgayUong * 3; // 3 ca mỗi ngày

        for (int i = 0; i < soNgayUong; i++) {
            Date ngayUong = calendar.getTime();
            
            // Tạo lịch cho 3 ca: sáng, trưa, tối
            taoLichTheoCa(maDonThuoc, maBenhNhan, ngayUong, "SANG");
            taoLichTheoCa(maDonThuoc, maBenhNhan, ngayUong, "TRUA");
            taoLichTheoCa(maDonThuoc, maBenhNhan, ngayUong, "TOI");
            
            // Chuyển sang ngày tiếp theo
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Log.d(TAG, "Đã tạo lịch uống thuốc cho " + soNgayUong + " ngày");
        listener.onSuccess();
    }

    private void taoLichTheoCa(String maDonThuoc, String maBenhNhan, Date ngayUong, String caUong) {
        String maLichUong = "LUT_" + UUID.randomUUID().toString();
        LichUongThuoc lichUong = new LichUongThuoc(
            maLichUong, maDonThuoc, maBenhNhan, ngayUong, caUong
        );

        repository.addDocument("LichUongThuoc", maLichUong, lichUong,
            aVoid -> Log.d(TAG, "Đã tạo lịch: " + maLichUong + " - " + caUong),
            e -> Log.e(TAG, "Lỗi tạo lịch: " + e.getMessage())
        );
    }

    /**
     * Lấy giờ nhắc nhở theo ca
     */
    public static int[] getGioNhacNho(String caUong) {
        switch (caUong) {
            case "SANG":
                return new int[]{6, 0}; // 6:00 AM
            case "TRUA":
                return new int[]{12, 0}; // 12:00 PM
            case "TOI":
                return new int[]{18, 0}; // 6:00 PM
            default:
                return new int[]{8, 0};
        }
    }

    public interface OnScheduleCreatedListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}
