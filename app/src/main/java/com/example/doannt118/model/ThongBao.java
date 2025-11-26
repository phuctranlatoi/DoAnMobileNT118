package com.example.doannt118.model;

import com.google.firebase.Timestamp;

public class ThongBao {
    private String maThongBao;
    private String maBenhNhan;
    private String maBacSi;
    private String tieuDe;
    private String noiDung;
    private String loaiThongBao; // LICH_HEN, NHAC_THUOC, THONG_BAO_CHUNG
    private Timestamp thoiGianGui;
    private boolean daDoc;

    public ThongBao() {}

    public ThongBao(String maThongBao, String maBenhNhan, String maBacSi, String tieuDe, 
                    String noiDung, String loaiThongBao, Timestamp thoiGianGui, boolean daDoc) {
        this.maThongBao = maThongBao;
        this.maBenhNhan = maBenhNhan;
        this.maBacSi = maBacSi;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.loaiThongBao = loaiThongBao;
        this.thoiGianGui = thoiGianGui;
        this.daDoc = daDoc;
    }

    public String getMaThongBao() { return maThongBao; }
    public void setMaThongBao(String maThongBao) { this.maThongBao = maThongBao; }

    public String getMaBenhNhan() { return maBenhNhan; }
    public void setMaBenhNhan(String maBenhNhan) { this.maBenhNhan = maBenhNhan; }

    public String getMaBacSi() { return maBacSi; }
    public void setMaBacSi(String maBacSi) { this.maBacSi = maBacSi; }

    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getLoaiThongBao() { return loaiThongBao; }
    public void setLoaiThongBao(String loaiThongBao) { this.loaiThongBao = loaiThongBao; }

    public Timestamp getThoiGianGui() { return thoiGianGui; }
    public void setThoiGianGui(Timestamp thoiGianGui) { this.thoiGianGui = thoiGianGui; }

    public boolean isDaDoc() { return daDoc; }
    public void setDaDoc(boolean daDoc) { this.daDoc = daDoc; }
}
