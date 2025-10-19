package com.example.doannt118.model;
public class ChiTietHoaDon {
    private String maHoaDon;
    private String maDuocPham;
    private int soLuong;
    private double donGia;

    public ChiTietHoaDon() {}

    public ChiTietHoaDon(String maHoaDon, String maDuocPham, int soLuong, double donGia) {
        this.maHoaDon = maHoaDon;
        this.maDuocPham = maDuocPham;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }

    public String getMaDuocPham() { return maDuocPham; }
    public void setMaDuocPham(String maDuocPham) { this.maDuocPham = maDuocPham; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }
}

