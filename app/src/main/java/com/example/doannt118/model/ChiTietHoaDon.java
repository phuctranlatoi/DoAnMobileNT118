package com.example.doannt118.model;

public class ChiTietHoaDon {
    private String maChiTiet;
    private String maHoaDon;
    private String tenDichVu;
    private int soLuong;
    private double donGia;
    private double thanhTien;

    public ChiTietHoaDon() {}

    public ChiTietHoaDon(String maChiTiet, String maHoaDon, String tenDichVu, 
                        int soLuong, double donGia) {
        this.maChiTiet = maChiTiet;
        this.maHoaDon = maHoaDon;
        this.tenDichVu = tenDichVu;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = soLuong * donGia;
    }

    public String getMaChiTiet() { return maChiTiet; }
    public void setMaChiTiet(String maChiTiet) { this.maChiTiet = maChiTiet; }

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }

    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { 
        this.soLuong = soLuong;
        this.thanhTien = soLuong * donGia;
    }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { 
        this.donGia = donGia;
        this.thanhTien = soLuong * donGia;
    }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }
}
