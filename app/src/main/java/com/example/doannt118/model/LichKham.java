package com.example.doannt118.model;
import java.util.Date;

public class LichKham {
    private String maLichKham;
    private String maBenhNhan;
    private String maNhanVien;
    private Date ngayKham;
    private String trangThai;

    public LichKham() {}

    public LichKham(String maLichKham, String maBenhNhan, String maNhanVien, Date ngayKham, String trangThai) {
        this.maLichKham = maLichKham;
        this.maBenhNhan = maBenhNhan;
        this.maNhanVien = maNhanVien;
        this.ngayKham = ngayKham;
        this.trangThai = trangThai;
    }

    public String getMaLichKham() { return maLichKham; }
    public void setMaLichKham(String maLichKham) { this.maLichKham = maLichKham; }

    public String getMaBenhNhan() { return maBenhNhan; }
    public void setMaBenhNhan(String maBenhNhan) { this.maBenhNhan = maBenhNhan; }

    public String getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }

    public Date getNgayKham() { return ngayKham; }
    public void setNgayKham(Date ngayKham) { this.ngayKham = ngayKham; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}

