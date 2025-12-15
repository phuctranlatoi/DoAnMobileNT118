package com.example.doannt118.model;

import com.google.firebase.Timestamp;

public class GoiNhanTin {
    private String maGoi;
    private String tenGoi;
    private String moTa;
    private double gia;
    private int soTinNhanToiDa;
    private int thoiHanNgay;
    private boolean coGoiDienThoai;
    private boolean coGoiVideo;
    private Timestamp ngayTao;
    private boolean trangThaiHoatDong;
    
    // Constructor mặc định cho Firestore
    public GoiNhanTin() {}
    
    public GoiNhanTin(String maGoi, String tenGoi, String moTa, double gia, 
                      int soTinNhanToiDa, int thoiHanNgay, boolean coGoiDienThoai, 
                      boolean coGoiVideo) {
        this.maGoi = maGoi;
        this.tenGoi = tenGoi;
        this.moTa = moTa;
        this.gia = gia;
        this.soTinNhanToiDa = soTinNhanToiDa;
        this.thoiHanNgay = thoiHanNgay;
        this.coGoiDienThoai = coGoiDienThoai;
        this.coGoiVideo = coGoiVideo;
        this.ngayTao = Timestamp.now();
        this.trangThaiHoatDong = true;
    }
    
    // Getters và Setters
    public String getMaGoi() { return maGoi; }
    public void setMaGoi(String maGoi) { this.maGoi = maGoi; }
    
    public String getTenGoi() { return tenGoi; }
    public void setTenGoi(String tenGoi) { this.tenGoi = tenGoi; }
    
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    
    public double getGia() { return gia; }
    public void setGia(double gia) { this.gia = gia; }
    
    public int getSoTinNhanToiDa() { return soTinNhanToiDa; }
    public void setSoTinNhanToiDa(int soTinNhanToiDa) { this.soTinNhanToiDa = soTinNhanToiDa; }
    
    public int getThoiHanNgay() { return thoiHanNgay; }
    public void setThoiHanNgay(int thoiHanNgay) { this.thoiHanNgay = thoiHanNgay; }
    
    public boolean isCoGoiDienThoai() { return coGoiDienThoai; }
    public void setCoGoiDienThoai(boolean coGoiDienThoai) { this.coGoiDienThoai = coGoiDienThoai; }
    
    public boolean isCoGoiVideo() { return coGoiVideo; }
    public void setCoGoiVideo(boolean coGoiVideo) { this.coGoiVideo = coGoiVideo; }
    
    public Timestamp getNgayTao() { return ngayTao; }
    public void setNgayTao(Timestamp ngayTao) { this.ngayTao = ngayTao; }
    
    public boolean isTrangThaiHoatDong() { return trangThaiHoatDong; }
    public void setTrangThaiHoatDong(boolean trangThaiHoatDong) { this.trangThaiHoatDong = trangThaiHoatDong; }
}