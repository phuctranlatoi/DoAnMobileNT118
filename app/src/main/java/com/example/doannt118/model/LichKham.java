package com.example.doannt118.model;

import com.google.firebase.Timestamp;

public class LichKham {
    private String maLichKham;
    private String maBenhNhan;
    private String maBacSi;
    private String maLichLamViec;
    private Timestamp ngayKham;
    private String trangThai; // CHO, XAC_NHAN, HOAN_THANH, HUY
    private int soThuTu;

    public LichKham() {}

    public LichKham(String maLichKham, String maBenhNhan, String maBacSi, String maLichLamViec,
                    Timestamp ngayKham, String trangThai, int soThuTu) {
        this.maLichKham = maLichKham;
        this.maBenhNhan = maBenhNhan;
        this.maBacSi = maBacSi;
        this.maLichLamViec = maLichLamViec;
        this.ngayKham = ngayKham;
        this.trangThai = trangThai;
        this.soThuTu = soThuTu;
    }

    public String getMaLichKham() { return maLichKham; }
    public void setMaLichKham(String maLichKham) { this.maLichKham = maLichKham; }

    public String getMaBenhNhan() { return maBenhNhan; }
    public void setMaBenhNhan(String maBenhNhan) { this.maBenhNhan = maBenhNhan; }

    public String getMaBacSi() { return maBacSi; }
    public void setMaBacSi(String maBacSi) { this.maBacSi = maBacSi; }

    public String getMaLichLamViec() { return maLichLamViec; }
    public void setMaLichLamViec(String maLichLamViec) { this.maLichLamViec = maLichLamViec; }

    public Timestamp getNgayKham() { return ngayKham; }
    public void setNgayKham(Timestamp ngayKham) { this.ngayKham = ngayKham; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public int getSoThuTu() { return soThuTu; }
    public void setSoThuTu(int soThuTu) { this.soThuTu = soThuTu; }
}
