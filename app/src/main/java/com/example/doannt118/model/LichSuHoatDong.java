package com.example.doannt118.model;

import java.util.Date;

public class LichSuHoatDong {
    private String maLichSu;
    private String maTaiKhoan;
    private String hanhDong;
    private Date thoiGian;
    private String chiTiet;

    public LichSuHoatDong() {}

    public LichSuHoatDong(String maLichSu, String maTaiKhoan, String hanhDong, Date thoiGian, String chiTiet) {
        this.maLichSu = maLichSu;
        this.maTaiKhoan = maTaiKhoan;
        this.hanhDong = hanhDong;
        this.thoiGian = thoiGian;
        this.chiTiet = chiTiet;
    }

    // Getters
    public String getMaLichSu() { return maLichSu; }
    public String getMaTaiKhoan() { return maTaiKhoan; }
    public String getHanhDong() { return hanhDong; }
    public Date getThoiGian() { return thoiGian; }
    public String getChiTiet() { return chiTiet; }

    // Setters
    public void setMaLichSu(String maLichSu) { this.maLichSu = maLichSu; }
    public void setMaTaiKhoan(String maTaiKhoan) { this.maTaiKhoan = maTaiKhoan; }
    public void setHanhDong(String hanhDong) { this.hanhDong = hanhDong; }
    public void setThoiGian(Date thoiGian) { this.thoiGian = thoiGian; }
    public void setChiTiet(String chiTiet) { this.chiTiet = chiTiet; }
}