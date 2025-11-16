package com.example.doannt118.model;

import java.util.ArrayList;
import java.util.List;

public class BacSi {
    private String maBacSi;
    private String maTaiKhoan;
    private String hoTen;
    private String soDienThoai;
    private String bangCap;
    private String hocVi;
    private List<String> chungChiHanhNghe;
    private String trangThaiXacThuc; // "Chờ xác thực", "Đã xác thực", "Từ chối"
    private String chuyenKhoa; // "Nội thận", "Ngoại tiết niệu", etc.
    private String diaChi;
    private int namKinhNghiem;
    private String gioiThieu;
    private String ngaySinh;

    public BacSi() {
        chungChiHanhNghe = new ArrayList<>();
    }

    public BacSi(String maBacSi, String maTaiKhoan, String hoTen, String soDienThoai, String bangCap, String hocVi, List<String> chungChiHanhNghe, String trangThaiXacThuc) {
        this.maBacSi = maBacSi;
        this.maTaiKhoan = maTaiKhoan;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.bangCap = bangCap;
        this.hocVi = hocVi;
        this.chungChiHanhNghe = chungChiHanhNghe != null ? chungChiHanhNghe : new ArrayList<>();
        this.trangThaiXacThuc = trangThaiXacThuc;
    }

    public String getMaBacSi() { return maBacSi; }
    public void setMaBacSi(String maBacSi) { this.maBacSi = maBacSi; }
    public String getMaTaiKhoan() { return maTaiKhoan; }
    public void setMaTaiKhoan(String maTaiKhoan) { this.maTaiKhoan = maTaiKhoan; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getBangCap() { return bangCap; }
    public void setBangCap(String bangCap) { this.bangCap = bangCap; }
    public String getHocVi() { return hocVi; }
    public void setHocVi(String hocVi) { this.hocVi = hocVi; }
    public List<String> getChungChiHanhNghe() { return chungChiHanhNghe; }
    public void setChungChiHanhNghe(List<String> chungChiHanhNghe) { this.chungChiHanhNghe = chungChiHanhNghe; }
    public String getTrangThaiXacThuc() { return trangThaiXacThuc; }
    public void setTrangThaiXacThuc(String trangThaiXacThuc) { this.trangThaiXacThuc = trangThaiXacThuc; }
    
    public String getChuyenKhoa() { return chuyenKhoa; }
    public void setChuyenKhoa(String chuyenKhoa) { this.chuyenKhoa = chuyenKhoa; }
    
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    
    public int getNamKinhNghiem() { return namKinhNghiem; }
    public void setNamKinhNghiem(int namKinhNghiem) { this.namKinhNghiem = namKinhNghiem; }
    
    public String getGioiThieu() { return gioiThieu; }
    public void setGioiThieu(String gioiThieu) { this.gioiThieu = gioiThieu; }
    
    public String getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(String ngaySinh) { this.ngaySinh = ngaySinh; }

    @Override
    public String toString() {
        return "BacSi{maBacSi='" + maBacSi + "', hoTen='" + hoTen + "'}";
    }
}