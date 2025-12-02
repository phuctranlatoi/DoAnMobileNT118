package com.example.doannt118.model;

import java.util.Date;

public class HoaDon {
    private String maHoaDon;
    private String maBenhAn;
    private String maBenhNhan;
    private Date ngayLap;
    private double tongTien;
    private String trangThai; // "CHO_THANH_TOAN", "DA_THANH_TOAN"
    private String phuongThucThanhToan; // "TIEN_MAT", "CHUYEN_KHOAN", "THE"

    public HoaDon() {}

    public HoaDon(String maHoaDon, String maBenhAn, String maBenhNhan, Date ngayLap, double tongTien) {
        this.maHoaDon = maHoaDon;
        this.maBenhAn = maBenhAn;
        this.maBenhNhan = maBenhNhan;
        this.ngayLap = ngayLap;
        this.tongTien = tongTien;
        this.trangThai = "CHO_THANH_TOAN";
    }

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }

    public String getMaBenhAn() { return maBenhAn; }
    public void setMaBenhAn(String maBenhAn) { this.maBenhAn = maBenhAn; }

    public String getMaBenhNhan() { return maBenhNhan; }
    public void setMaBenhNhan(String maBenhNhan) { this.maBenhNhan = maBenhNhan; }

    public Date getNgayLap() { return ngayLap; }
    public void setNgayLap(Date ngayLap) { this.ngayLap = ngayLap; }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getPhuongThucThanhToan() { return phuongThucThanhToan; }
    public void setPhuongThucThanhToan(String phuongThucThanhToan) { 
        this.phuongThucThanhToan = phuongThucThanhToan; 
    }
}
