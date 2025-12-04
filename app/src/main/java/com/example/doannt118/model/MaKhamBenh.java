package com.example.doannt118.model;

import com.google.firebase.Timestamp;

public class MaKhamBenh {
    private String maMaKham;
    private String maKham; // Mã 6 số
    private String maLichKham;
    private String maBenhNhan;
    private String maBacSi;
    private String tenBenhNhan;
    private Timestamp ngayKham;
    private String trangThai; // CHO_KHAM, DANG_KHAM, HOAN_THANH
    private Timestamp thoiGianTao;
    private Timestamp thoiGianHetHan;

    public MaKhamBenh() {}

    public MaKhamBenh(String maMaKham, String maKham, String maLichKham, String maBenhNhan, 
                      String maBacSi, String tenBenhNhan, Timestamp ngayKham, String trangThai,
                      Timestamp thoiGianTao, Timestamp thoiGianHetHan) {
        this.maMaKham = maMaKham;
        this.maKham = maKham;
        this.maLichKham = maLichKham;
        this.maBenhNhan = maBenhNhan;
        this.maBacSi = maBacSi;
        this.tenBenhNhan = tenBenhNhan;
        this.ngayKham = ngayKham;
        this.trangThai = trangThai;
        this.thoiGianTao = thoiGianTao;
        this.thoiGianHetHan = thoiGianHetHan;
    }

    public String getMaMaKham() { return maMaKham; }
    public void setMaMaKham(String maMaKham) { this.maMaKham = maMaKham; }

    public String getMaKham() { return maKham; }
    public void setMaKham(String maKham) { this.maKham = maKham; }

    public String getMaLichKham() { return maLichKham; }
    public void setMaLichKham(String maLichKham) { this.maLichKham = maLichKham; }

    public String getMaBenhNhan() { return maBenhNhan; }
    public void setMaBenhNhan(String maBenhNhan) { this.maBenhNhan = maBenhNhan; }

    public String getMaBacSi() { return maBacSi; }
    public void setMaBacSi(String maBacSi) { this.maBacSi = maBacSi; }

    public String getTenBenhNhan() { return tenBenhNhan; }
    public void setTenBenhNhan(String tenBenhNhan) { this.tenBenhNhan = tenBenhNhan; }

    public Timestamp getNgayKham() { return ngayKham; }
    public void setNgayKham(Timestamp ngayKham) { this.ngayKham = ngayKham; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public Timestamp getThoiGianTao() { return thoiGianTao; }
    public void setThoiGianTao(Timestamp thoiGianTao) { this.thoiGianTao = thoiGianTao; }

    public Timestamp getThoiGianHetHan() { return thoiGianHetHan; }
    public void setThoiGianHetHan(Timestamp thoiGianHetHan) { this.thoiGianHetHan = thoiGianHetHan; }
}
