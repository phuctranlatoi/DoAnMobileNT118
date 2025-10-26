package com.example.doannt118.model;

import java.util.Date;

public class LichSuHoatDong {
    private String maLichSu;
    private String maTaiKhoan;
    private String tenHoatDong;
    private Date thoiGian;
    private String chiTiet;

    public LichSuHoatDong() {}

    public LichSuHoatDong(String maLichSu, String maTaiKhoan, String tenHoatDong, Date thoiGian, String chiTiet) {
        this.maLichSu = maLichSu;
        this.maTaiKhoan = maTaiKhoan;
        this.tenHoatDong = tenHoatDong;
        this.thoiGian = thoiGian;
        this.chiTiet = chiTiet;
    }

    public String getMaLichSu() { return maLichSu; }
    public void setMaLichSu(String maLichSu) { this.maLichSu = maLichSu; }
    public String getMaTaiKhoan() { return maTaiKhoan; }
    public void setMaTaiKhoan(String maTaiKhoan) { this.maTaiKhoan = maTaiKhoan; }
    public String getTenHoatDong() { return tenHoatDong; }
    public void setTenHoatDong(String tenHoatDong) { this.tenHoatDong = tenHoatDong; }
    public Date getThoiGian() { return thoiGian; }
    public void setThoiGian(Date thoiGian) { this.thoiGian = thoiGian; }
    public String getChiTiet() { return chiTiet; }
    public void setChiTiet(String chiTiet) { this.chiTiet = chiTiet; }
}