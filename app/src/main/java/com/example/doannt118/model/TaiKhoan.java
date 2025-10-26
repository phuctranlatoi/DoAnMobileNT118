// Đường dẫn: app/src/main/java/com/example/doannt118/model/TaiKhoan.java
package com.example.doannt118.model;

public class TaiKhoan {
    private String maTaiKhoan;
    private String tenDangNhap;
    private String matKhau;
    private String vaiTro;

    public TaiKhoan() {
        // Cần có constructor rỗng để Firestore toObject() hoạt động
    }

    public TaiKhoan(String maTaiKhoan, String tenDangNhap, String matKhau, String vaiTro) {
        this.maTaiKhoan = maTaiKhoan;
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
    }

    // --- Getters ---
    public String getMaTaiKhoan() { return maTaiKhoan; }
    public String getTenDangNhap() { return tenDangNhap; }
    public String getMatKhau() { return matKhau; }
    public String getVaiTro() { return vaiTro; }

    // --- Setters ---
    // (Cũng cần thiết cho Firestore)
    public void setMaTaiKhoan(String maTaiKhoan) { this.maTaiKhoan = maTaiKhoan; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }

    @Override
    public String toString() {
        return "TaiKhoan{" +
                "maTaiKhoan='" + maTaiKhoan + '\'' +
                ", tenDangNhap='" + tenDangNhap + '\'' +
                ", vaiTro='" + vaiTro + '\'' +
                '}';
    }
}