package com.example.doannt118.model;

import com.google.firebase.Timestamp;

public class TinNhanBacSi {
    
    public enum LoaiTinNhan {
        BENH_NHAN,
        BAC_SI
    }
    
    public enum TrangThaiTinNhan {
        DA_GUI,
        DA_NHAN,
        DA_XEM
    }
    
    private String id;
    private String noiDung;
    private String maBenhNhan;
    private String maBacSi;
    private LoaiTinNhan loaiTinNhan;
    private TrangThaiTinNhan trangThai;
    private Timestamp thoiGianGui;
    private String tenNguoiGui;
    private String avatarNguoiGui;
    
    // Constructor mặc định cho Firestore
    public TinNhanBacSi() {}
    
    public TinNhanBacSi(String noiDung, String maBenhNhan, String maBacSi, 
                        LoaiTinNhan loaiTinNhan, String tenNguoiGui) {
        this.noiDung = noiDung;
        this.maBenhNhan = maBenhNhan;
        this.maBacSi = maBacSi;
        this.loaiTinNhan = loaiTinNhan;
        this.tenNguoiGui = tenNguoiGui;
        this.trangThai = TrangThaiTinNhan.DA_GUI;
        this.thoiGianGui = Timestamp.now();
    }
    
    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }
    
    public String getMaBenhNhan() { return maBenhNhan; }
    public void setMaBenhNhan(String maBenhNhan) { this.maBenhNhan = maBenhNhan; }
    
    public String getMaBacSi() { return maBacSi; }
    public void setMaBacSi(String maBacSi) { this.maBacSi = maBacSi; }
    
    public LoaiTinNhan getLoaiTinNhan() { return loaiTinNhan; }
    public void setLoaiTinNhan(LoaiTinNhan loaiTinNhan) { this.loaiTinNhan = loaiTinNhan; }
    
    public TrangThaiTinNhan getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThaiTinNhan trangThai) { this.trangThai = trangThai; }
    
    public Timestamp getThoiGianGui() { return thoiGianGui; }
    public void setThoiGianGui(Timestamp thoiGianGui) { this.thoiGianGui = thoiGianGui; }
    
    public String getTenNguoiGui() { return tenNguoiGui; }
    public void setTenNguoiGui(String tenNguoiGui) { this.tenNguoiGui = tenNguoiGui; }
    
    public String getAvatarNguoiGui() { return avatarNguoiGui; }
    public void setAvatarNguoiGui(String avatarNguoiGui) { this.avatarNguoiGui = avatarNguoiGui; }
}