package com.example.doannt118.model;

public class ChiTietDonThuoc {
    private String maDonThuoc;
    private String maDuocPham;
    private int soLuong;
    private String lieuDung;

    public ChiTietDonThuoc() {}

    public ChiTietDonThuoc(String maDonThuoc, String maDuocPham, int soLuong, String lieuDung) {
        this.maDonThuoc = maDonThuoc;
        this.maDuocPham = maDuocPham;
        this.soLuong = soLuong;
        this.lieuDung = lieuDung;
    }

    public String getMaDonThuoc() { return maDonThuoc; }
    public void setMaDonThuoc(String maDonThuoc) { this.maDonThuoc = maDonThuoc; }

    public String getMaDuocPham() { return maDuocPham; }
    public void setMaDuocPham(String maDuocPham) { this.maDuocPham = maDuocPham; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public String getLieuDung() { return lieuDung; }
    public void setLieuDung(String lieuDung) { this.lieuDung = lieuDung; }
}

