package com.example.doannt118.model;

public class ChiTietDonThuoc {
    private String maChiTiet;
    private String maDonThuoc;
    private String maDuocPham;
    private String tenThuoc;
    private int soLuong;
    private String lieuDung;
    private boolean uongSang;
    private boolean uongTrua;
    private boolean uongToi;

    public ChiTietDonThuoc() {}

    public ChiTietDonThuoc(String maDonThuoc, String maDuocPham, String tenThuoc, int soLuong, 
                          String lieuDung, boolean uongSang, boolean uongTrua, boolean uongToi) {
        this.maDonThuoc = maDonThuoc;
        this.maDuocPham = maDuocPham;
        this.tenThuoc = tenThuoc;
        this.soLuong = soLuong;
        this.lieuDung = lieuDung;
        this.uongSang = uongSang;
        this.uongTrua = uongTrua;
        this.uongToi = uongToi;
    }

    public String getMaChiTiet() { return maChiTiet; }
    public void setMaChiTiet(String maChiTiet) { this.maChiTiet = maChiTiet; }

    public String getMaDonThuoc() { return maDonThuoc; }
    public void setMaDonThuoc(String maDonThuoc) { this.maDonThuoc = maDonThuoc; }

    public String getMaDuocPham() { return maDuocPham; }
    public void setMaDuocPham(String maDuocPham) { this.maDuocPham = maDuocPham; }

    public String getTenThuoc() { return tenThuoc; }
    public void setTenThuoc(String tenThuoc) { this.tenThuoc = tenThuoc; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public String getLieuDung() { return lieuDung; }
    public void setLieuDung(String lieuDung) { this.lieuDung = lieuDung; }

    public boolean isUongSang() { return uongSang; }
    public void setUongSang(boolean uongSang) { this.uongSang = uongSang; }

    public boolean isUongTrua() { return uongTrua; }
    public void setUongTrua(boolean uongTrua) { this.uongTrua = uongTrua; }

    public boolean isUongToi() { return uongToi; }
    public void setUongToi(boolean uongToi) { this.uongToi = uongToi; }
}

