package com.example.doannt118.model;

import com.google.firebase.Timestamp;
import java.util.Date;

public class LichUongThuoc {
    private String maLichUong;
    private String maDonThuoc;
    private String maBenhNhan;
    private Date ngayUong;
    private String caUong; // "SANG", "TRUA", "TOI"
    private String trangThai; // "CHO_XAC_NHAN", "DA_UONG", "BO_QUA"
    private Timestamp thoiGianXacNhan;

    public LichUongThuoc() {}

    public LichUongThuoc(String maLichUong, String maDonThuoc, String maBenhNhan, 
                        Date ngayUong, String caUong) {
        this.maLichUong = maLichUong;
        this.maDonThuoc = maDonThuoc;
        this.maBenhNhan = maBenhNhan;
        this.ngayUong = ngayUong;
        this.caUong = caUong;
        this.trangThai = "CHO_XAC_NHAN";
    }

    public String getMaLichUong() { return maLichUong; }
    public void setMaLichUong(String maLichUong) { this.maLichUong = maLichUong; }

    public String getMaDonThuoc() { return maDonThuoc; }
    public void setMaDonThuoc(String maDonThuoc) { this.maDonThuoc = maDonThuoc; }

    public String getMaBenhNhan() { return maBenhNhan; }
    public void setMaBenhNhan(String maBenhNhan) { this.maBenhNhan = maBenhNhan; }

    public Date getNgayUong() { return ngayUong; }
    public void setNgayUong(Date ngayUong) { this.ngayUong = ngayUong; }

    public String getCaUong() { return caUong; }
    public void setCaUong(String caUong) { this.caUong = caUong; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public Timestamp getThoiGianXacNhan() { return thoiGianXacNhan; }
    public void setThoiGianXacNhan(Timestamp thoiGianXacNhan) { this.thoiGianXacNhan = thoiGianXacNhan; }
}
