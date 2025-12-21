package com.example.doannt118.model;

import java.util.Date;

public class TimeSlot {
    private String maTimeSlot;
    private String maBacSi;
    private Date ngayKham;
    private String gioStart; // "14:00"
    private String gioEnd;   // "14:30"
    private String khungGio; // "14:00-14:30"
    private boolean isBooked; // true nếu đã có người đặt
    private String maBenhNhanDat; // ID bệnh nhân đã đặt (nếu có)
    private String ghiChu;

    public TimeSlot() {}

    public TimeSlot(String maTimeSlot, String maBacSi, Date ngayKham, String gioStart, String gioEnd, String khungGio) {
        this.maTimeSlot = maTimeSlot;
        this.maBacSi = maBacSi;
        this.ngayKham = ngayKham;
        this.gioStart = gioStart;
        this.gioEnd = gioEnd;
        this.khungGio = khungGio;
        this.isBooked = false;
        this.maBenhNhanDat = null;
        this.ghiChu = "";
    }

    // Getters and Setters
    public String getMaTimeSlot() { return maTimeSlot; }
    public void setMaTimeSlot(String maTimeSlot) { this.maTimeSlot = maTimeSlot; }

    public String getMaBacSi() { return maBacSi; }
    public void setMaBacSi(String maBacSi) { this.maBacSi = maBacSi; }

    public Date getNgayKham() { return ngayKham; }
    public void setNgayKham(Date ngayKham) { this.ngayKham = ngayKham; }

    public String getGioStart() { return gioStart; }
    public void setGioStart(String gioStart) { this.gioStart = gioStart; }

    public String getGioEnd() { return gioEnd; }
    public void setGioEnd(String gioEnd) { this.gioEnd = gioEnd; }

    public String getKhungGio() { return khungGio; }
    public void setKhungGio(String khungGio) { this.khungGio = khungGio; }

    public boolean isBooked() { return isBooked; }
    public void setBooked(boolean booked) { isBooked = booked; }

    public String getMaBenhNhanDat() { return maBenhNhanDat; }
    public void setMaBenhNhanDat(String maBenhNhanDat) { this.maBenhNhanDat = maBenhNhanDat; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    @Override
    public String toString() {
        return "TimeSlot{" +
                "khungGio='" + khungGio + '\'' +
                ", isBooked=" + isBooked +
                '}';
    }
}