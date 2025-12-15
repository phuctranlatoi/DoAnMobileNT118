package com.example.doannt118.model;

import com.google.firebase.Timestamp;

public class DangKyNhanTin {
    private String maDangKy;
    private String maBenhNhan;
    private String maBacSi;
    private String maGoi;
    private double giaThanhToan;
    private Timestamp ngayDangKy;
    private Timestamp ngayHetHan;
    private int soTinNhanDaSuDung;
    private String trangThaiThanhToan; // "Chờ thanh toán", "Đã thanh toán", "Hết hạn"
    private String phuongThucThanhToan; // "Ví điện tử", "Thẻ tín dụng", "Chuyển khoản"
    private String maGiaoDich;
    
    // Constructor mặc định cho Firestore
    public DangKyNhanTin() {}
    
    public DangKyNhanTin(String maDangKy, String maBenhNhan, String maBacSi, 
                         String maGoi, double giaThanhToan) {
        this.maDangKy = maDangKy;
        this.maBenhNhan = maBenhNhan;
        this.maBacSi = maBacSi;
        this.maGoi = maGoi;
        this.giaThanhToan = giaThanhToan;
        this.ngayDangKy = Timestamp.now();
        this.soTinNhanDaSuDung = 0;
        this.trangThaiThanhToan = "Chờ thanh toán";
    }
    
    // Getters và Setters
    public String getMaDangKy() { return maDangKy; }
    public void setMaDangKy(String maDangKy) { this.maDangKy = maDangKy; }
    
    public String getMaBenhNhan() { return maBenhNhan; }
    public void setMaBenhNhan(String maBenhNhan) { this.maBenhNhan = maBenhNhan; }
    
    public String getMaBacSi() { return maBacSi; }
    public void setMaBacSi(String maBacSi) { this.maBacSi = maBacSi; }
    
    public String getMaGoi() { return maGoi; }
    public void setMaGoi(String maGoi) { this.maGoi = maGoi; }
    
    public double getGiaThanhToan() { return giaThanhToan; }
    public void setGiaThanhToan(double giaThanhToan) { this.giaThanhToan = giaThanhToan; }
    
    public Timestamp getNgayDangKy() { return ngayDangKy; }
    public void setNgayDangKy(Timestamp ngayDangKy) { this.ngayDangKy = ngayDangKy; }
    
    public Timestamp getNgayHetHan() { return ngayHetHan; }
    public void setNgayHetHan(Timestamp ngayHetHan) { this.ngayHetHan = ngayHetHan; }
    
    public int getSoTinNhanDaSuDung() { return soTinNhanDaSuDung; }
    public void setSoTinNhanDaSuDung(int soTinNhanDaSuDung) { this.soTinNhanDaSuDung = soTinNhanDaSuDung; }
    
    public String getTrangThaiThanhToan() { return trangThaiThanhToan; }
    public void setTrangThaiThanhToan(String trangThaiThanhToan) { this.trangThaiThanhToan = trangThaiThanhToan; }
    
    public String getPhuongThucThanhToan() { return phuongThucThanhToan; }
    public void setPhuongThucThanhToan(String phuongThucThanhToan) { this.phuongThucThanhToan = phuongThucThanhToan; }
    
    public String getMaGiaoDich() { return maGiaoDich; }
    public void setMaGiaoDich(String maGiaoDich) { this.maGiaoDich = maGiaoDich; }
}