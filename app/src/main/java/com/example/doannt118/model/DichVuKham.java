package com.example.doannt118.model;

import java.io.Serializable;

public class DichVuKham implements Serializable {
    private static final long serialVersionUID = 1L;
    private String maDichVu;
    private String tenDichVu;
    private String loaiDichVu;      // KHAM_CO_BAN, KHAM_CHUYEN_SAU, XET_NGHIEM, CHUP_CHIEU
    private long giaTien;           // VNĐ
    private String moTa;
    private boolean isActive;       // Còn sử dụng không
    private String donVi;           // "lần", "mẫu", "ca"

    public DichVuKham() {}

    public DichVuKham(String maDichVu, String tenDichVu, String loaiDichVu, long giaTien, String moTa) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.loaiDichVu = loaiDichVu;
        this.giaTien = giaTien;
        this.moTa = moTa;
        this.isActive = true;
        this.donVi = "lần";
    }

    // Getters and Setters
    public String getMaDichVu() { return maDichVu; }
    public void setMaDichVu(String maDichVu) { this.maDichVu = maDichVu; }

    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }

    public String getLoaiDichVu() { return loaiDichVu; }
    public void setLoaiDichVu(String loaiDichVu) { this.loaiDichVu = loaiDichVu; }

    public long getGiaTien() { return giaTien; }
    public void setGiaTien(long giaTien) { this.giaTien = giaTien; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public boolean isActive() { return isActive; }
    public boolean getIsActive() { return isActive; }  // Firestore cần getter này
    public void setActive(boolean active) { isActive = active; }
    public void setIsActive(boolean active) { isActive = active; }  // Firestore cần setter này

    public String getDonVi() { return donVi; }
    public void setDonVi(String donVi) { this.donVi = donVi; }

    // Helper method để format giá
    public String getGiaTienFormatted() {
        return String.format("%,d đ", giaTien);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DichVuKham that = (DichVuKham) obj;
        return maDichVu != null ? maDichVu.equals(that.maDichVu) : that.maDichVu == null;
    }

    @Override
    public int hashCode() {
        return maDichVu != null ? maDichVu.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "DichVuKham{" +
                "maDichVu='" + maDichVu + '\'' +
                ", tenDichVu='" + tenDichVu + '\'' +
                ", loaiDichVu='" + loaiDichVu + '\'' +
                ", giaTien=" + giaTien +
                '}';
    }
}
