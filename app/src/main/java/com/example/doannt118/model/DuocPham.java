package com.example.doannt118.model;

public class DuocPham {
    private String maDuocPham;
    private String tenDuocPham;
    private String donViTinh;
    private double giaBan;

    public DuocPham() {}

    public DuocPham(String maDuocPham, String tenDuocPham, String donViTinh, double giaBan, int soLuongTon) {
        this.maDuocPham = maDuocPham;
        this.tenDuocPham = tenDuocPham;
        this.donViTinh = donViTinh;
        this.giaBan = giaBan;
    }

    public String getMaDuocPham() { return maDuocPham; }
    public void setMaDuocPham(String maDuocPham) { this.maDuocPham = maDuocPham; }

    public String getTenDuocPham() { return tenDuocPham; }
    public void setTenDuocPham(String tenDuocPham) { this.tenDuocPham = tenDuocPham; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }

    @Override
    public String toString() {
        return tenDuocPham + " (" + donViTinh + ")";
    }
}

