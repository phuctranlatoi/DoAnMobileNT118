package com.example.doannt118.model;

import com.google.firebase.Timestamp;

public class TinNhanBacSi {
    
    public enum LoaiTinNhan {
        BENH_NHAN,
        BAC_SI,
        HE_THONG  // Tin nhắn hệ thống (chào mừng, thông báo)
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
    private String conversationId; // ID duy nhất cho cuộc trò chuyện
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
        this.conversationId = generateConversationId(maBenhNhan, maBacSi);
        this.loaiTinNhan = loaiTinNhan;
        this.tenNguoiGui = tenNguoiGui;
        this.trangThai = TrangThaiTinNhan.DA_GUI;
        this.thoiGianGui = Timestamp.now();
    }
    
    /**
     * Tạo ID duy nhất cho cuộc trò chuyện giữa bệnh nhân và bác sĩ
     * Format: "conversation_{maBenhNhan}_{maBacSi}"
     * Đảm bảo thứ tự nhất quán để tránh duplicate
     */
    public static String generateConversationId(String maBenhNhan, String maBacSi) {
        if (maBenhNhan == null || maBacSi == null) {
            throw new IllegalArgumentException("maBenhNhan và maBacSi không được null");
        }
        return "conversation_" + maBenhNhan + "_" + maBacSi;
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
    
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    
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