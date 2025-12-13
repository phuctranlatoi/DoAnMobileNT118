package com.example.doannt118.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CaUongThuocManager {
    
    // Định nghĩa khung giờ cho các ca
    public static final int CA_SANG_START = 6;   // 6:00
    public static final int CA_SANG_END = 11;    // 11:00
    public static final int CA_TRUA_START = 11;  // 11:00
    public static final int CA_TRUA_END = 14;    // 14:00
    public static final int CA_CHIEU_START = 14; // 14:00
    public static final int CA_CHIEU_END = 18;   // 18:00
    public static final int CA_TOI_START = 18;   // 18:00
    public static final int CA_TOI_END = 22;     // 22:00
    
    /**
     * Xác định ca hiện tại dựa trên thời gian
     */
    public static String getCaHienTai() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        
        if (hour >= CA_SANG_START && hour < CA_SANG_END) {
            return "SANG";
        } else if (hour >= CA_TRUA_START && hour < CA_TRUA_END) {
            return "TRUA";
        } else if (hour >= CA_CHIEU_START && hour < CA_CHIEU_END) {
            return "CHIEU";
        } else if (hour >= CA_TOI_START && hour < CA_TOI_END) {
            return "TOI";
        } else {
            // Ngoài giờ - xác định ca gần nhất
            if (hour < CA_SANG_START) {
                return "SANG"; // Sáng sớm
            } else {
                return "TOI"; // Tối muộn
            }
        }
    }
    
    /**
     * Kiểm tra xem có đang trong khung giờ của ca nào không
     */
    public static boolean isDangTrongKhungGioCa(String ca) {
        String caHienTai = getCaHienTai();
        return ca.equals(caHienTai);
    }
    
    /**
     * Lấy tên hiển thị của ca (không có khung giờ)
     */
    public static String getTenCa(String ca) {
        switch (ca.toUpperCase()) {
            case "SANG":
                return "Ca Sáng";
            case "TRUA":
                return "Ca Trưa";
            case "CHIEU":
                return "Ca Chiều";
            case "TOI":
                return "Ca Tối";
            default:
                return "Ca không xác định";
        }
    }
    
    /**
     * Lấy tên hiển thị của ca với khung giờ (cho thông báo)
     */
    public static String getTenCaVoiKhungGio(String ca) {
        switch (ca.toUpperCase()) {
            case "SANG":
                return "Ca Sáng (6:00 - 11:00)";
            case "TRUA":
                return "Ca Trưa (11:00 - 14:00)";
            case "CHIEU":
                return "Ca Chiều (14:00 - 18:00)";
            case "TOI":
                return "Ca Tối (18:00 - 22:00)";
            default:
                return "Ca không xác định";
        }
    }
    
    /**
     * Kiểm tra xem ca đã qua chưa (để xác định có thể bỏ qua không)
     */
    public static boolean isCaDaQua(String ca, Date ngay) {
        Calendar calNgay = Calendar.getInstance();
        calNgay.setTime(ngay);
        
        Calendar calHienTai = Calendar.getInstance();
        
        // Nếu không phải ngày hôm nay thì đã qua
        if (calNgay.get(Calendar.DAY_OF_YEAR) != calHienTai.get(Calendar.DAY_OF_YEAR) ||
            calNgay.get(Calendar.YEAR) != calHienTai.get(Calendar.YEAR)) {
            return calNgay.before(calHienTai);
        }
        
        // Nếu là ngày hôm nay, kiểm tra giờ
        int gioHienTai = calHienTai.get(Calendar.HOUR_OF_DAY);
        
        switch (ca.toUpperCase()) {
            case "SANG":
                return gioHienTai >= CA_SANG_END;
            case "TRUA":
                return gioHienTai >= CA_TRUA_END;
            case "CHIEU":
                return gioHienTai >= CA_CHIEU_END;
            case "TOI":
                return gioHienTai >= CA_TOI_END;
            default:
                return false;
        }
    }
    
    /**
     * Lấy thông báo nhắc nhở cho ca
     */
    public static String getThongBaoNhacNho(String ca) {
        switch (ca.toUpperCase()) {
            case "SANG":
                return "Đã đến giờ uống thuốc ca sáng (6:00 - 11:00)";
            case "TRUA":
                return "Đã đến giờ uống thuốc ca trưa (11:00 - 14:00)";
            case "CHIEU":
                return "Đã đến giờ uống thuốc ca chiều (14:00 - 18:00)";
            case "TOI":
                return "Đã đến giờ uống thuốc ca tối (18:00 - 22:00)";
            default:
                return "Đã đến giờ uống thuốc";
        }
    }
}