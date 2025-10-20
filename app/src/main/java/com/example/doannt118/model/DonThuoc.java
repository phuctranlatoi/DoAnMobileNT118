package com.example.doannt118.model;
import java.util.Date;

public class DonThuoc {
    private String maDonThuoc;
    private String maBenhAn;
    private Date ngayLap;

    public DonThuoc() {}

    public DonThuoc(String maDonThuoc, String maBenhAn, Date ngayLap) {
        this.maDonThuoc = maDonThuoc;
        this.maBenhAn = maBenhAn;
        this.ngayLap = ngayLap;
    }

    public String getMaDonThuoc() { return maDonThuoc; }
    public void setMaDonThuoc(String maDonThuoc) { this.maDonThuoc = maDonThuoc; }

    public String getMaBenhAn() { return maBenhAn; }
    public void setMaBenhAn(String maBenhAn) { this.maBenhAn = maBenhAn; }

    public Date getNgayLap() { return ngayLap; }
    public void setNgayLap(Date ngayLap) { this.ngayLap = ngayLap; }
}
