package com.example.doannt118.model;
import java.util.Date;
import com.google.firebase.Timestamp;

public class DonThuoc {
    private String maDonThuoc;
    private String maBenhAn;
    private String maBenhNhan;
    private String maBacSi; // Mã bác sĩ kê đơn
    private Date ngayLap;
    private Timestamp ngayKeDon; // Ngày kê đơn (Timestamp cho Firestore)
    private int soNgayUong;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    private String trangThai; // "DANG_DUNG", "DA_HET", "DA_HUY"

    public DonThuoc() {
        this.trangThai = "DANG_DUNG"; // Mặc định đang dùng
    }

    public DonThuoc(String maDonThuoc, String maBenhAn, String maBenhNhan, Date ngayLap, int soNgayUong) {
        this.maDonThuoc = maDonThuoc;
        this.maBenhAn = maBenhAn;
        this.maBenhNhan = maBenhNhan;
        this.ngayLap = ngayLap;
        this.soNgayUong = soNgayUong;
        this.trangThai = "DANG_DUNG";
    }

    public String getMaDonThuoc() { return maDonThuoc; }
    public void setMaDonThuoc(String maDonThuoc) { this.maDonThuoc = maDonThuoc; }

    public String getMaBenhAn() { return maBenhAn; }
    public void setMaBenhAn(String maBenhAn) { this.maBenhAn = maBenhAn; }

    public String getMaBenhNhan() { return maBenhNhan; }
    public void setMaBenhNhan(String maBenhNhan) { this.maBenhNhan = maBenhNhan; }

    public Date getNgayLap() { return ngayLap; }
    public void setNgayLap(Date ngayLap) { this.ngayLap = ngayLap; }

    public int getSoNgayUong() { return soNgayUong; }
    public void setSoNgayUong(int soNgayUong) { this.soNgayUong = soNgayUong; }

    public Date getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(Date ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public Date getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(Date ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    
    public String getMaBacSi() { return maBacSi; }
    public void setMaBacSi(String maBacSi) { this.maBacSi = maBacSi; }
    
    public Timestamp getNgayKeDon() { return ngayKeDon; }
    public void setNgayKeDon(Timestamp ngayKeDon) { this.ngayKeDon = ngayKeDon; }
}
