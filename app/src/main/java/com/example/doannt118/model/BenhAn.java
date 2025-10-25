package com.example.doannt118.model;

import com.google.firebase.Timestamp;

public class BenhAn {
    private String maBenhAn;
    private String maLichKham;
    private String maBenhNhan;
    private String chanDoan;
    private String ghiChu;
    private Timestamp ngayKham;

    public BenhAn() {}

    public BenhAn(String maBenhAn, String maLichKham, String maBenhNhan, String chanDoan, String ghiChu, Timestamp ngayKham) {
        this.maBenhAn = maBenhAn;
        this.maLichKham = maLichKham;
        this.maBenhNhan = maBenhNhan;
        this.chanDoan = chanDoan;
        this.ghiChu = ghiChu;
        this.ngayKham = ngayKham;
    }

    public String getMaBenhAn() { return maBenhAn; }
    public void setMaBenhAn(String maBenhAn) { this.maBenhAn = maBenhAn; }

    public String getMaLichKham() { return maLichKham; }
    public void setMaLichKham(String maLichKham) { this.maLichKham = maLichKham; }

    public String getMaBenhNhan() { return maBenhNhan; }
    public void setMaBenhNhan(String maBenhNhan) { this.maBenhNhan = maBenhNhan; }

    public String getChanDoan() { return chanDoan; }
    public void setChanDoan(String chanDoan) { this.chanDoan = chanDoan; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public Timestamp getNgayKham() { return ngayKham; }
    public void setNgayKham(Timestamp ngayKham) { this.ngayKham = ngayKham; }
}