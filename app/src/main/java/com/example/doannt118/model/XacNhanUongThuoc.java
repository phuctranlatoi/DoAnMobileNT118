package com.example.doannt118.model;

import com.google.firebase.Timestamp;

public class XacNhanUongThuoc {
    private String maXacNhan;
    private String maLichUong;
    private String maChiTietDonThuoc;
    private String maBenhNhan;
    private boolean daUong;
    private Timestamp thoiGianXacNhan;
    private String ghiChu;

    public XacNhanUongThuoc() {}

    public XacNhanUongThuoc(String maXacNhan, String maLichUong, String maChiTietDonThuoc, 
                           String maBenhNhan, boolean daUong) {
        this.maXacNhan = maXacNhan;
        this.maLichUong = maLichUong;
        this.maChiTietDonThuoc = maChiTietDonThuoc;
        this.maBenhNhan = maBenhNhan;
        this.daUong = daUong;
        this.thoiGianXacNhan = Timestamp.now();
    }

    public String getMaXacNhan() { return maXacNhan; }
    public void setMaXacNhan(String maXacNhan) { this.maXacNhan = maXacNhan; }

    public String getMaLichUong() { return maLichUong; }
    public void setMaLichUong(String maLichUong) { this.maLichUong = maLichUong; }

    public String getMaChiTietDonThuoc() { return maChiTietDonThuoc; }
    public void setMaChiTietDonThuoc(String maChiTietDonThuoc) { this.maChiTietDonThuoc = maChiTietDonThuoc; }

    public String getMaBenhNhan() { return maBenhNhan; }
    public void setMaBenhNhan(String maBenhNhan) { this.maBenhNhan = maBenhNhan; }

    public boolean isDaUong() { return daUong; }
    public void setDaUong(boolean daUong) { this.daUong = daUong; }

    public Timestamp getThoiGianXacNhan() { return thoiGianXacNhan; }
    public void setThoiGianXacNhan(Timestamp thoiGianXacNhan) { this.thoiGianXacNhan = thoiGianXacNhan; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
