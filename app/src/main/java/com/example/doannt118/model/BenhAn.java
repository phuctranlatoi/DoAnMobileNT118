package com.example.doannt118.model;
public class BenhAn {
    private String maBenhAn;
    private String maLichKham;
    private String chanDoan;
    private String ghiChu;

    public BenhAn() {}

    public BenhAn(String maBenhAn, String maLichKham, String chanDoan, String ghiChu) {
        this.maBenhAn = maBenhAn;
        this.maLichKham = maLichKham;
        this.chanDoan = chanDoan;
        this.ghiChu = ghiChu;
    }

    public String getMaBenhAn() { return maBenhAn; }
    public void setMaBenhAn(String maBenhAn) { this.maBenhAn = maBenhAn; }

    public String getMaLichKham() { return maLichKham; }
    public void setMaLichKham(String maLichKham) { this.maLichKham = maLichKham; }

    public String getChanDoan() { return chanDoan; }
    public void setChanDoan(String chanDoan) { this.chanDoan = chanDoan; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
