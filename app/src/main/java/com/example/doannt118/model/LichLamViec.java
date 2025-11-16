package com.example.doannt118.model;

import com.google.firebase.Timestamp;

import java.util.Date;

public class LichLamViec {
    private String maLichLamViec;
    private String maBacSi;
    private Date ngayLamViec; // Dùng Date thay vì Timestamp để tương thích
    private String caLamViec; // "08:00-09:00" - tên field trong Firestore
    private String trangThai; // CON_TRONG, DA_DAY
    private int soLuongToiDa; // Mặc định 6

    public LichLamViec() {}

    public LichLamViec(String maLichLamViec, String maBacSi, Date ngayLamViec,
                       String caLamViec, String trangThai, int soLuongToiDa) {
        this.maLichLamViec = maLichLamViec;
        this.maBacSi = maBacSi;
        this.ngayLamViec = ngayLamViec;
        this.caLamViec = caLamViec;
        this.trangThai = trangThai;
        this.soLuongToiDa = soLuongToiDa;
    }

    public String getMaLichLamViec() { return maLichLamViec; }
    public void setMaLichLamViec(String maLichLamViec) { this.maLichLamViec = maLichLamViec; }

    public String getMaBacSi() { return maBacSi; }
    public void setMaBacSi(String maBacSi) { this.maBacSi = maBacSi; }

    public Date getNgayLamViec() { return ngayLamViec; }
    public void setNgayLamViec(Date ngayLamViec) { this.ngayLamViec = ngayLamViec; }

    public String getCaLamViec() { return caLamViec; }
    public void setCaLamViec(String caLamViec) { this.caLamViec = caLamViec; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public int getSoLuongToiDa() { return soLuongToiDa; }
    public void setSoLuongToiDa(int soLuongToiDa) { this.soLuongToiDa = soLuongToiDa; }
}
