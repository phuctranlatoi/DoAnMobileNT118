package com.example.doannt118.model;
public class BenhNhan {
    private String maBenhNhan;
    private String maTaiKhoan;
    private String hoTen;
    private String soDienThoai;
    private String diaChi;

    public BenhNhan() {}

    public BenhNhan(String maBenhNhan, String maTaiKhoan, String hoTen, String soDienThoai, String diaChi) {
        this.maBenhNhan = maBenhNhan;
        this.maTaiKhoan = maTaiKhoan;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.diaChi = diaChi;
    }

    public String getMaBenhNhan() { return maBenhNhan; }
    public void setMaBenhNhan(String maBenhNhan) { this.maBenhNhan = maBenhNhan; }

    public String getMaTaiKhoan() { return maTaiKhoan; }
    public void setMaTaiKhoan(String maTaiKhoan) { this.maTaiKhoan = maTaiKhoan; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    @Override
    public String toString() {
        return "BenhNhan{" +
                "maBenhNhan='" + maBenhNhan + '\'' +
                ", hoTen='" + hoTen + '\'' +
                '}';
    }
}
