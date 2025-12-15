package com.example.doannt118.model;

import com.google.firebase.Timestamp;

public class CuocTroChuyenBenhNhan {
    private String maBacSi;
    private String tenBacSi;
    private String avatarBacSi;
    private String tinNhanCuoi;
    private Timestamp thoiGianCuoi;
    private int soTinNhanChuaDoc;
    private boolean laBenhNhanGuiCuoi; // true nếu tin nhắn cuối là bệnh nhân gửi
    
    // Constructor mặc định cho Firestore
    public CuocTroChuyenBenhNhan() {}
    
    public CuocTroChuyenBenhNhan(String maBacSi, String tenBacSi, String tinNhanCuoi, 
                                 Timestamp thoiGianCuoi, boolean laBenhNhanGuiCuoi) {
        this.maBacSi = maBacSi;
        this.tenBacSi = tenBacSi;
        this.tinNhanCuoi = tinNhanCuoi;
        this.thoiGianCuoi = thoiGianCuoi;
        this.laBenhNhanGuiCuoi = laBenhNhanGuiCuoi;
        this.soTinNhanChuaDoc = 0;
    }
    
    // Getters và Setters
    public String getMaBacSi() { return maBacSi; }
    public void setMaBacSi(String maBacSi) { this.maBacSi = maBacSi; }
    
    public String getTenBacSi() { return tenBacSi; }
    public void setTenBacSi(String tenBacSi) { this.tenBacSi = tenBacSi; }
    
    public String getAvatarBacSi() { return avatarBacSi; }
    public void setAvatarBacSi(String avatarBacSi) { this.avatarBacSi = avatarBacSi; }
    
    public String getTinNhanCuoi() { return tinNhanCuoi; }
    public void setTinNhanCuoi(String tinNhanCuoi) { this.tinNhanCuoi = tinNhanCuoi; }
    
    public Timestamp getThoiGianCuoi() { return thoiGianCuoi; }
    public void setThoiGianCuoi(Timestamp thoiGianCuoi) { this.thoiGianCuoi = thoiGianCuoi; }
    
    public int getSoTinNhanChuaDoc() { return soTinNhanChuaDoc; }
    public void setSoTinNhanChuaDoc(int soTinNhanChuaDoc) { this.soTinNhanChuaDoc = soTinNhanChuaDoc; }
    
    public boolean isLaBenhNhanGuiCuoi() { return laBenhNhanGuiCuoi; }
    public void setLaBenhNhanGuiCuoi(boolean laBenhNhanGuiCuoi) { this.laBenhNhanGuiCuoi = laBenhNhanGuiCuoi; }
}