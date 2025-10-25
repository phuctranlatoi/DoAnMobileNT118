package com.example.doannt118.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LichLamViec {
    private String maLichLamViec;
    private String maBacSi;
    private Date ngayLamViec;
    private String caLamViec;
    private String trangThai;

    public LichLamViec() {}

    public LichLamViec(String maLichLamViec, String maBacSi, Date ngayLamViec, String caLamViec, String trangThai) {
        this.maLichLamViec = maLichLamViec;
        this.maBacSi = maBacSi;
        this.ngayLamViec = ngayLamViec;
        this.caLamViec = caLamViec;
        this.trangThai = trangThai;
    }

    public String getMaLichLamViec() { return maLichLamViec; }
    public void setMaLichLamViec(String maLichLamViec) { this.maLichLamViec = maLichLamViec; }

    public String getMaBacSi() { return maBacSi; }
    public void setMaBacSi(String maBacSi) { this.maBacSi = maBacSi; }

    public Date getNgayLamViec() { return ngayLamViec; }
    public void setNgayLamViec(Date ngayLamViec) { this.ngayLamViec = ngayLamViec; }

    public String getCaLamViec() { return caLamViec; }
    public void setCaLamViec(String caLamViec) { this.caLamViec = caLamViec; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    // Thêm phương thức toMap để hỗ trợ Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("maLichLamViec", maLichLamViec);
        map.put("maBacSi", maBacSi);
        map.put("ngayLamViec", ngayLamViec);
        map.put("caLamViec", caLamViec);
        map.put("trangThai", trangThai);
        return map;
    }
}