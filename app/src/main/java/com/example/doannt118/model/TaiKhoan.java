package com.example.doannt118.model;

public class TaiKhoan {
    private String maTaiKhoan;
    private String tenDangNhap;
    private String matKhau;
    private String vaiTro;
    private String email;
    private String trangThai;

    public TaiKhoan() {
        // Cần có constructor rỗng để Firestore toObject() hoạt động
    }

    public TaiKhoan(String maTaiKhoan, String tenDangNhap, String matKhau, String vaiTro, String email, String trangThai) {
        this.maTaiKhoan = maTaiKhoan;
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
        this.email = email;
        this.trangThai = trangThai;
    }

    // Getters
    public String getMaTaiKhoan() { return maTaiKhoan; }
    public String getTenDangNhap() { return tenDangNhap; }
    public String getMatKhau() { return matKhau; }
    public String getVaiTro() { return vaiTro; }
    public String getEmail() { return email; }
    public String getTrangThai() { return trangThai; }

    // Setters
    public void setMaTaiKhoan(String maTaiKhoan) { this.maTaiKhoan = maTaiKhoan; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }
    public void setEmail(String email) { this.email = email; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return "TaiKhoan{" +
                "maTaiKhoan='" + maTaiKhoan + '\'' +
                ", tenDangNhap='" + tenDangNhap + '\'' +
                ", vaiTro='" + vaiTro + '\'' +
                ", email='" + email + '\'' +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}