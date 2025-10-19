package com.example.doannt118.model;
import java.util.Date;

public class LichLamViec {
    private String maLichLamViec;
    private String maNhanVien;
    private Date ngayLamViec;
    private String caLamViec;

    public LichLamViec() {}

    public LichLamViec(String maLichLamViec, String maNhanVien, Date ngayLamViec, String caLamViec) {
        this.maLichLamViec = maLichLamViec;
        this.maNhanVien = maNhanVien;
        this.ngayLamViec = ngayLamViec;
        this.caLamViec = caLamViec;
    }

    public String getMaLichLamViec() { return maLichLamViec; }
    public void setMaLichLamViec(String maLichLamViec) { this.maLichLamViec = maLichLamViec; }

    public String getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }

    public Date getNgayLamViec() { return ngayLamViec; }
    public void setNgayLamViec(Date ngayLamViec) { this.ngayLamViec = ngayLamViec; }

    public String getCaLamViec() { return caLamViec; }
    public void setCaLamViec(String caLamViec) { this.caLamViec = caLamViec; }
}

