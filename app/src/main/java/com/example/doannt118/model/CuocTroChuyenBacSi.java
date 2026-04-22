package com.example.doannt118.model;

import com.google.firebase.Timestamp;

public class CuocTroChuyenBacSi {
    private String maBenhNhan;
    private String tenBenhNhan;
    private String avatarBenhNhan;
    private String tinNhanCuoi;
    private Timestamp thoiGianCuoi;
    private int soTinNhanChuaDoc;
    private boolean laBacSiGuiCuoi; // true nếu tin nhắn cuối là bác sĩ gửi
    private TinNhanBacSi.TrangThaiTinNhan trangThaiTinNhanCuoi; // trạng thái tin nhắn cuối
    
    // Constructor mặc định cho Firestore
    public CuocTroChuyenBacSi() {}
    
    public CuocTroChuyenBacSi(String maBenhNhan, String tenBenhNhan, String tinNhanCuoi, 
                              Timestamp thoiGianCuoi, boolean laBacSiGuiCuoi) {
        this.maBenhNhan = maBenhNhan;
        this.tenBenhNhan = tenBenhNhan;
        this.tinNhanCuoi = tinNhanCuoi;
        this.thoiGianCuoi = thoiGianCuoi;
        this.laBacSiGuiCuoi = laBacSiGuiCuoi;
        this.soTinNhanChuaDoc = 0;
    }
    
    // Getters và Setters
    public String getMaBenhNhan() { return maBenhNhan; }
    public void setMaBenhNhan(String maBenhNhan) { this.maBenhNhan = maBenhNhan; }
    
    public String getTenBenhNhan() { return tenBenhNhan; }
    public void setTenBenhNhan(String tenBenhNhan) { this.tenBenhNhan = tenBenhNhan; }
    
    public String getAvatarBenhNhan() { return avatarBenhNhan; }
    public void setAvatarBenhNhan(String avatarBenhNhan) { this.avatarBenhNhan = avatarBenhNhan; }
    
    public String getTinNhanCuoi() { return tinNhanCuoi; }
    public void setTinNhanCuoi(String tinNhanCuoi) { this.tinNhanCuoi = tinNhanCuoi; }
    
    public Timestamp getThoiGianCuoi() { return thoiGianCuoi; }
    public void setThoiGianCuoi(Timestamp thoiGianCuoi) { this.thoiGianCuoi = thoiGianCuoi; }
    
    public int getSoTinNhanChuaDoc() { return soTinNhanChuaDoc; }
    public void setSoTinNhanChuaDoc(int soTinNhanChuaDoc) { this.soTinNhanChuaDoc = soTinNhanChuaDoc; }
    
    public boolean isLaBacSiGuiCuoi() { return laBacSiGuiCuoi; }
    public void setLaBacSiGuiCuoi(boolean laBacSiGuiCuoi) { this.laBacSiGuiCuoi = laBacSiGuiCuoi; }
    
    public TinNhanBacSi.TrangThaiTinNhan getTrangThaiTinNhanCuoi() { return trangThaiTinNhanCuoi; }
    public void setTrangThaiTinNhanCuoi(TinNhanBacSi.TrangThaiTinNhan trangThaiTinNhanCuoi) { this.trangThaiTinNhanCuoi = trangThaiTinNhanCuoi; }
}