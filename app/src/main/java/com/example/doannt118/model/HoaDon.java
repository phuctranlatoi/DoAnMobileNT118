package com.example.doannt118.model;
import java.util.Date;

public class HoaDon {
    private String maHoaDon;
    private String maBenhAn;
    private String maBenhNhan;
    private Date ngayLap;
    private double tongTien;
    private long phiKham;               // Phí khám
    private long phiThuoc;              // Phí thuốc
    private long phiDichVu;             // Phí dịch vụ khác (xét nghiệm, chụp chiếu...)
    private String trangThai;           // CHUA_THANH_TOAN, DA_THANH_TOAN
    private Date ngayThanhToan;

    public HoaDon() {}

    public HoaDon(String maHoaDon, String maBenhAn, Date ngayLap, double tongTien) {
        this.maHoaDon = maHoaDon;
        this.maBenhAn = maBenhAn;
        this.ngayLap = ngayLap;
        this.tongTien = tongTien;
    }
    
    public HoaDon(String maHoaDon, String maBenhAn, String maBenhNhan, Date ngayLap, double tongTien) {
        this.maHoaDon = maHoaDon;
        this.maBenhAn = maBenhAn;
        this.maBenhNhan = maBenhNhan;
        this.ngayLap = ngayLap;
        this.tongTien = tongTien;
    }

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }

    public String getMaBenhAn() { return maBenhAn; }
    public void setMaBenhAn(String maBenhAn) { this.maBenhAn = maBenhAn; }
    
    public String getMaBenhNhan() { return maBenhNhan; }
    public void setMaBenhNhan(String maBenhNhan) { this.maBenhNhan = maBenhNhan; }

    public Date getNgayLap() { return ngayLap; }
    public void setNgayLap(Date ngayLap) { this.ngayLap = ngayLap; }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }

    public long getPhiKham() { return phiKham; }
    public void setPhiKham(long phiKham) { this.phiKham = phiKham; }

    public long getPhiThuoc() { return phiThuoc; }
    public void setPhiThuoc(long phiThuoc) { this.phiThuoc = phiThuoc; }

    public long getPhiDichVu() { return phiDichVu; }
    public void setPhiDichVu(long phiDichVu) { this.phiDichVu = phiDichVu; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public Date getNgayThanhToan() { return ngayThanhToan; }
    public void setNgayThanhToan(Date ngayThanhToan) { this.ngayThanhToan = ngayThanhToan; }
    
    // Helper method tính tổng tự động
    public long getTongTienLong() {
        return phiKham + phiThuoc + phiDichVu;
    }
}

