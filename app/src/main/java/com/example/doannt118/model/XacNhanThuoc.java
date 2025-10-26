package com.example.doannt118.model;
import java.util.Date;

public class XacNhanThuoc {
    private String maXacNhan;
    private String maDonThuoc;
    private String maNhanVien;
    private Date ngayXacNhan;

    public XacNhanThuoc() {}

    public XacNhanThuoc(String maXacNhan, String maDonThuoc, String maNhanVien, Date ngayXacNhan) {
        this.maXacNhan = maXacNhan;
        this.maDonThuoc = maDonThuoc;
        this.maNhanVien = maNhanVien;
        this.ngayXacNhan = ngayXacNhan;
    }

    public String getMaXacNhan() { return maXacNhan; }
    public void setMaXacNhan(String maXacNhan) { this.maXacNhan = maXacNhan; }

    public String getMaDonThuoc() { return maDonThuoc; }
    public void setMaDonThuoc(String maDonThuoc) { this.maDonThuoc = maDonThuoc; }

    public String getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }

    public Date getNgayXacNhan() { return ngayXacNhan; }
    public void setNgayXacNhan(Date ngayXacNhan) { this.ngayXacNhan = ngayXacNhan; }
}

