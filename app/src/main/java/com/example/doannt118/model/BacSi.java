package com.example.doannt118.model;
public class BacSi {
    private String maBacSi;
    private String maTaiKhoan;
    private String hoTen;
    private String soDienThoai;

    public BacSi() {}

    public BacSi(String maBacSi, String maTaiKhoan, String hoTen, String soDienThoai) {
        this.maBacSi = maBacSi;
        this.maTaiKhoan = maTaiKhoan;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
    }

    public String getMaNhanVien() { return maBacSi; }
    public void setMaNhanVien(String maNhanVien) { this.maBacSi = maNhanVien; }

    public String getMaTaiKhoan() { return maTaiKhoan; }
    public void setMaTaiKhoan(String maTaiKhoan) { this.maTaiKhoan = maTaiKhoan; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    @Override
    public String toString() {
        return "NhanVien{" +
                "maNhanVien='" + maBacSi + '\'' +
                ", hoTen='" + hoTen + '\'' +
                '}';
    }
}
