package com.example.doannt118.model;

import com.google.firebase.Timestamp;

import java.util.Date;

public class LichLamViec {
    private String maLichLamViec;
    private String maBacSi;
    private Date ngayLamViec;
    private String caLamViec; // "08:00-12:00"
    private int soLuongToiDa; // Số lượng bệnh nhân tối đa (mặc định 6)
    private String loaiHinh; // ONLINE, OFFLINE (tại phòng khám)
    private String ghiChu; // Ghi chú cho lịch làm việc

    public LichLamViec() {}

    public LichLamViec(String maLichLamViec, String maBacSi, Date ngayLamViec,
                       String caLamViec, int soLuongToiDa) {
        this.maLichLamViec = maLichLamViec;
        this.maBacSi = maBacSi;
        this.ngayLamViec = ngayLamViec;
        this.caLamViec = caLamViec;
        this.soLuongToiDa = soLuongToiDa;
        this.loaiHinh = "OFFLINE";
    }
    
    public LichLamViec(String maLichLamViec, String maBacSi, Date ngayLamViec,
                       String caLamViec, int soLuongToiDa, String loaiHinh) {
        this.maLichLamViec = maLichLamViec;
        this.maBacSi = maBacSi;
        this.ngayLamViec = ngayLamViec;
        this.caLamViec = caLamViec;
        this.soLuongToiDa = soLuongToiDa;
        this.loaiHinh = loaiHinh;
    }

    public String getMaLichLamViec() { return maLichLamViec; }
    public void setMaLichLamViec(String maLichLamViec) { this.maLichLamViec = maLichLamViec; }

    public String getMaBacSi() { return maBacSi; }
    public void setMaBacSi(String maBacSi) { this.maBacSi = maBacSi; }

    public Date getNgayLamViec() { return ngayLamViec; }
    public void setNgayLamViec(Date ngayLamViec) { this.ngayLamViec = ngayLamViec; }

    public String getCaLamViec() { return caLamViec; }
    public void setCaLamViec(String caLamViec) { this.caLamViec = caLamViec; }



    public int getSoLuongToiDa() { return soLuongToiDa; }
    public void setSoLuongToiDa(int soLuongToiDa) { this.soLuongToiDa = soLuongToiDa; }
    
    public String getLoaiHinh() { return loaiHinh; }
    public void setLoaiHinh(String loaiHinh) { this.loaiHinh = loaiHinh; }
    
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
