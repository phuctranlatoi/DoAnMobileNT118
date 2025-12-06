package com.example.doannt118.model;

public class ChiTietDonThuoc {
    private String maChiTiet;
    private String maDonThuoc;
    private String maDuocPham;
    private String tenThuoc;
    private int soLuong; // Tổng số lượng thuốc
    private String lieuDung; // Mô tả chung
    
    // Thông tin chi tiết
    private int soNgayUong; // Số ngày uống (ví dụ: 7, 14, 30)
    private int soLanMoiNgay; // Số lần uống mỗi ngày (ví dụ: 2, 3)
    private int soVienMoiLan; // Số viên/lần (ví dụ: 1, 2)
    
    // Ca uống trong ngày
    private boolean uongSang;
    private boolean uongTrua;
    private boolean uongChieu;
    private boolean uongToi;
    
    // Ghi chú thêm
    private String cachDung; // "Uống trước ăn", "Uống sau ăn", "Uống khi đói"

    public ChiTietDonThuoc() {}

    public ChiTietDonThuoc(String maDonThuoc, String maDuocPham, String tenThuoc, int soLuong, 
                          String lieuDung, int soNgayUong, int soLanMoiNgay, int soVienMoiLan,
                          boolean uongSang, boolean uongTrua, boolean uongChieu, boolean uongToi) {
        this.maDonThuoc = maDonThuoc;
        this.maDuocPham = maDuocPham;
        this.tenThuoc = tenThuoc;
        this.soLuong = soLuong;
        this.lieuDung = lieuDung;
        this.soNgayUong = soNgayUong;
        this.soLanMoiNgay = soLanMoiNgay;
        this.soVienMoiLan = soVienMoiLan;
        this.uongSang = uongSang;
        this.uongTrua = uongTrua;
        this.uongChieu = uongChieu;
        this.uongToi = uongToi;
    }

    // Getters and Setters
    public String getMaChiTiet() { return maChiTiet; }
    public void setMaChiTiet(String maChiTiet) { this.maChiTiet = maChiTiet; }

    public String getMaDonThuoc() { return maDonThuoc; }
    public void setMaDonThuoc(String maDonThuoc) { this.maDonThuoc = maDonThuoc; }

    public String getMaDuocPham() { return maDuocPham; }
    public void setMaDuocPham(String maDuocPham) { this.maDuocPham = maDuocPham; }

    public String getTenThuoc() { return tenThuoc; }
    public void setTenThuoc(String tenThuoc) { this.tenThuoc = tenThuoc; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public String getLieuDung() { return lieuDung; }
    public void setLieuDung(String lieuDung) { this.lieuDung = lieuDung; }

    public int getSoNgayUong() { return soNgayUong; }
    public void setSoNgayUong(int soNgayUong) { this.soNgayUong = soNgayUong; }

    public int getSoLanMoiNgay() { return soLanMoiNgay; }
    public void setSoLanMoiNgay(int soLanMoiNgay) { this.soLanMoiNgay = soLanMoiNgay; }

    public int getSoVienMoiLan() { return soVienMoiLan; }
    public void setSoVienMoiLan(int soVienMoiLan) { this.soVienMoiLan = soVienMoiLan; }

    public boolean isUongSang() { return uongSang; }
    public void setUongSang(boolean uongSang) { this.uongSang = uongSang; }

    public boolean isUongTrua() { return uongTrua; }
    public void setUongTrua(boolean uongTrua) { this.uongTrua = uongTrua; }

    public boolean isUongChieu() { return uongChieu; }
    public void setUongChieu(boolean uongChieu) { this.uongChieu = uongChieu; }

    public boolean isUongToi() { return uongToi; }
    public void setUongToi(boolean uongToi) { this.uongToi = uongToi; }

    public String getCachDung() { return cachDung; }
    public void setCachDung(String cachDung) { this.cachDung = cachDung; }
    
    /**
     * Tạo mô tả liều dùng đầy đủ
     * Ví dụ: "Uống 7 ngày, ngày 3 lần (sáng, trưa, chiều), mỗi lần 2 viên"
     */
    public String getLieuDungDayDu() {
        // Ưu tiên dùng lieuDung cơ bản nếu có (cho dữ liệu cũ)
        if (lieuDung != null && !lieuDung.isEmpty() && !lieuDung.equals("null")) {
            // Nếu có thông tin ca uống, thêm vào
            StringBuilder caUong = new StringBuilder();
            if (uongSang) caUong.append("sáng");
            if (uongTrua) {
                if (caUong.length() > 0) caUong.append(", ");
                caUong.append("trưa");
            }
            if (uongChieu) {
                if (caUong.length() > 0) caUong.append(", ");
                caUong.append("chiều");
            }
            
            if (caUong.length() > 0) {
                return lieuDung + " • Ca: " + caUong.toString();
            }
            return lieuDung;
        }
        
        // Nếu không có lieuDung, tạo từ thông tin chi tiết
        StringBuilder sb = new StringBuilder();
        
        // Số ngày
        if (soNgayUong > 0) {
            sb.append("Uống ").append(soNgayUong).append(" ngày");
        }
        
        // Số lần/ngày
        if (soLanMoiNgay > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("ngày ").append(soLanMoiNgay).append(" lần");
        }
        
        // Ca uống (chỉ sáng, trưa, chiều)
        StringBuilder caUong = new StringBuilder();
        if (uongSang) caUong.append("sáng");
        if (uongTrua) {
            if (caUong.length() > 0) caUong.append(", ");
            caUong.append("trưa");
        }
        if (uongChieu) {
            if (caUong.length() > 0) caUong.append(", ");
            caUong.append("chiều");
        }
        
        if (caUong.length() > 0) {
            sb.append(" (").append(caUong).append(")");
        }
        
        // Số viên/lần
        if (soVienMoiLan > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("mỗi lần ").append(soVienMoiLan).append(" viên");
        }
        
        // Nếu vẫn rỗng, hiển thị thông tin tối thiểu
        if (sb.length() == 0) {
            // Thử hiển thị ít nhất số lượng nếu có
            if (soLuong > 0) {
                sb.append("Tổng số lượng: ").append(soLuong).append(" viên");
            } else {
                sb.append("Theo chỉ định của bác sĩ");
            }
        }
        
        return sb.toString();
    }
}

