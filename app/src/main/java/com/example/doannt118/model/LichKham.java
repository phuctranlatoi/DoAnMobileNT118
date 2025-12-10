package com.example.doannt118.model;

import com.google.firebase.Timestamp;

public class LichKham {
    private String maLichKham;
    private String maBenhNhan;
    private String maBacSi;
    private String tenBacSi; // Tên bác sĩ (không lưu trong Firestore, chỉ dùng để hiển thị)
    private String maLichLamViec;
    private Timestamp ngayKham;
    private String gioKham; // Giờ khám cụ thể (ví dụ: "08:00" hoặc "08:00-12:00")
    private String trangThai; // CHO, XAC_NHAN, HOAN_THANH, HUY
    private int soThuTu;
    private String lyDoTuChoi;
    private String lyDoKham;
    private String maKhamBenh; // Mã khám 6 số
    private String loaiKham; // Loại khám (Khám tổng quát, Khám chuyên khoa, etc.)
    private Double chiPhi; // Chi phí khám
    private Timestamp ngayTao; // Ngày tạo lịch
    private String ghiChu; // Ghi chú

    public LichKham() {}

    public LichKham(String maLichKham, String maBenhNhan, String maBacSi, String maLichLamViec,
                    Timestamp ngayKham, String trangThai, int soThuTu) {
        this.maLichKham = maLichKham;
        this.maBenhNhan = maBenhNhan;
        this.maBacSi = maBacSi;
        this.maLichLamViec = maLichLamViec;
        this.ngayKham = ngayKham;
        this.trangThai = trangThai;
        this.soThuTu = soThuTu;
    }

    public String getMaLichKham() { return maLichKham; }
    public void setMaLichKham(String maLichKham) { this.maLichKham = maLichKham; }

    public String getMaBenhNhan() { return maBenhNhan; }
    public void setMaBenhNhan(String maBenhNhan) { this.maBenhNhan = maBenhNhan; }

    public String getMaBacSi() { return maBacSi; }
    public void setMaBacSi(String maBacSi) { this.maBacSi = maBacSi; }

    public String getMaLichLamViec() { return maLichLamViec; }
    public void setMaLichLamViec(String maLichLamViec) { this.maLichLamViec = maLichLamViec; }

    public Timestamp getNgayKham() { return ngayKham; }
    public void setNgayKham(Timestamp ngayKham) { this.ngayKham = ngayKham; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public int getSoThuTu() { return soThuTu; }
    public void setSoThuTu(int soThuTu) { this.soThuTu = soThuTu; }

    public String getLyDoTuChoi() { return lyDoTuChoi; }
    public void setLyDoTuChoi(String lyDoTuChoi) { this.lyDoTuChoi = lyDoTuChoi; }

    public String getLyDoKham() { return lyDoKham; }
    public void setLyDoKham(String lyDoKham) { this.lyDoKham = lyDoKham; }

    public String getMaKhamBenh() { return maKhamBenh; }
    public void setMaKhamBenh(String maKhamBenh) { this.maKhamBenh = maKhamBenh; }
    
    public String getGioKham() { return gioKham; }
    public void setGioKham(String gioKham) { this.gioKham = gioKham; }
    
    public String getTenBacSi() { return tenBacSi; }
    public void setTenBacSi(String tenBacSi) { this.tenBacSi = tenBacSi; }
    
    public String getLoaiKham() { return loaiKham; }
    public void setLoaiKham(String loaiKham) { this.loaiKham = loaiKham; }
    
    public Double getChiPhi() { return chiPhi; }
    public void setChiPhi(Double chiPhi) { this.chiPhi = chiPhi; }
    
    public Timestamp getNgayTao() { return ngayTao; }
    public void setNgayTao(Timestamp ngayTao) { this.ngayTao = ngayTao; }
    
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
