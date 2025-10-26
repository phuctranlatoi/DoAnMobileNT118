package com.example.doannt118.model;

public class Admin {
    private String maAdmin;
    private String maTaiKhoan;
    private String hoTen;
    private String soDienThoai;

    public Admin() {}

    public Admin(String maAdmin, String maTaiKhoan, String hoTen, String soDienThoai) {
        this.maAdmin = maAdmin;
        this.maTaiKhoan = maTaiKhoan;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
    }

    public String getMaAdmin() { return maAdmin; }
    public void setMaAdmin(String maAdmin) { this.maAdmin = maAdmin; }
    public String getMaTaiKhoan() { return maTaiKhoan; }
    public void setMaTaiKhoan(String maTaiKhoan) { this.maTaiKhoan = maTaiKhoan; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    @Override
    public String toString() {
        return "Admin{maAdmin='" + maAdmin + "', hoTen='" + hoTen + "'}";
    }
}