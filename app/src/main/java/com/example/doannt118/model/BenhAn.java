package com.example.doannt118.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BenhAn {
    private String maBenhAn;
    private String maLichKham;
    private String maBenhNhan;
    private String maBacSi;
    private String chanDoan;
    private String ghiChu;
    private Object ngayKham;  // Changed to Object to handle both String and Timestamp
    private String loaiKham;            // Loại khám (Khám cơ bản, Khám chuyên sâu...)
    private String maDichVuKham;        // Mã dịch vụ khám
    private long phiKham;               // Phí khám (VNĐ)

    public BenhAn() {}

    public BenhAn(String maBenhAn, String maLichKham, String maBenhNhan, String maBacSi, String chanDoan, String ghiChu, Timestamp ngayKham) {
        this.maBenhAn = maBenhAn;
        this.maLichKham = maLichKham;
        this.maBenhNhan = maBenhNhan;
        this.maBacSi = maBacSi;
        this.chanDoan = chanDoan;
        this.ghiChu = ghiChu;
        this.ngayKham = ngayKham;
    }

    public String getMaBenhAn() { return maBenhAn; }
    public void setMaBenhAn(String maBenhAn) { this.maBenhAn = maBenhAn; }

    public String getMaLichKham() { return maLichKham; }
    public void setMaLichKham(String maLichKham) { this.maLichKham = maLichKham; }

    public String getMaBenhNhan() { return maBenhNhan; }
    public void setMaBenhNhan(String maBenhNhan) { this.maBenhNhan = maBenhNhan; }

    public String getMaBacSi() { return maBacSi; }
    public void setMaBacSi(String maBacSi) { this.maBacSi = maBacSi; }

    public String getChanDoan() { return chanDoan; }
    public void setChanDoan(String chanDoan) { this.chanDoan = chanDoan; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public Object getNgayKham() { return ngayKham; }
    
    public void setNgayKham(Object ngayKham) { 
        this.ngayKham = ngayKham; 
    }
    
    @Exclude
    public Timestamp getNgayKhamAsTimestamp() {
        if (ngayKham instanceof Timestamp) {
            return (Timestamp) ngayKham;
        } else if (ngayKham instanceof String) {
            // Try to parse string to Timestamp
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = sdf.parse((String) ngayKham);
                return new Timestamp(date);
            } catch (ParseException e) {
                // Try another format
                try {
                    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date date = sdf2.parse((String) ngayKham);
                    return new Timestamp(date);
                } catch (ParseException e2) {
                    return null;
                }
            }
        }
        return null;
    }

    public String getLoaiKham() { return loaiKham; }
    public void setLoaiKham(String loaiKham) { this.loaiKham = loaiKham; }

    public String getMaDichVuKham() { return maDichVuKham; }
    public void setMaDichVuKham(String maDichVuKham) { this.maDichVuKham = maDichVuKham; }

    public long getPhiKham() { return phiKham; }
    public void setPhiKham(long phiKham) { this.phiKham = phiKham; }
}